/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.process.workitem.github;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "GithubListRepositories.wid", name = "GithubListRepositories",
        displayName = "GithubListRepositories",
        defaultHandler = "mvel: new org.jbpm.process.workitem.github.ListRepositoriesWorkitemHandler(\"userName\", \"password\")",
        documentation = "${artifactId}/index.html",
        module = "${artifactId}", version = "${version}",
        parameters = {
                @WidParameter(name = "User", required = true)
        },
        results = {
                @WidResult(name = "RepoListInfo", runtimeType = "java.util.List")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "github,repo,repository,list",
                action = @WidAction(title = "List all GitHub repositories"),
                authinfo = @WidAuth(required = true, params = {"userName", "password"},
                        paramsdescription = {"Github username", "Github password"},
                        referencesite = "https://github.com/")
        ))
public class ListRepositoriesWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String userName;
    private String password;
    private GithubAuth auth = new GithubAuth();

    private static final Logger logger = LoggerFactory.getLogger(ListRepositoriesWorkitemHandler.class);
    private static final String RESULTS_VALUE = "RepoListInfo";

    public ListRepositoriesWorkitemHandler(String userName,
                                           String password) {
        this.userName = userName;
        this.password = password;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            Map<String, Object> results = new HashMap<String, Object>();

            String user = (String) workItem.getParameter("User");

            RepositoryService repoService = auth.getRespositoryService(this.userName,
                                                                       this.password);

            List<Repository> userRepos = repoService.getRepositories(user);
            List<RepositoryInfo> resultRepositoryInformation = new ArrayList<>();

            if (userRepos != null) {
                for (Repository repo : userRepos) {
                    resultRepositoryInformation.add(new RepositoryInfo(repo));
                }
            } else {
                logger.info("No repositories found for " + user);
            }

            results.put(RESULTS_VALUE,
                        resultRepositoryInformation);

            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setAuth(GithubAuth auth) {
        this.auth = auth;
    }
}
