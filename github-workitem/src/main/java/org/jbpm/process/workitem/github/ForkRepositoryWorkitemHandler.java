/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.RepositoryService;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "GithubForkRepository.wid", name = "GithubForkRepository",
        displayName = "GithubForkRepository",
        defaultHandler = "mvel: new org.jbpm.process.workitem.github.ForkRepositoryWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "RepoOwner"),
                @WidParameter(name = "RepoName"),
                @WidParameter(name = "Organization")
        },
        results = {
                @WidResult(name = "ForkedRepoInfo")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}"),
                @WidMavenDepends(group = "org.eclipse.mylyn.github", artifact = "org.eclipse.egit.github.core", version = "2.1.5")
        })
public class ForkRepositoryWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String userName;
    private String password;
    private GithubAuth auth = new GithubAuth();

    private static final Logger logger = LoggerFactory.getLogger(ForkRepositoryWorkitemHandler.class);
    private static final String RESULTS_VALUE = "ForkedRepoInfo";

    public ForkRepositoryWorkitemHandler(String userName,
                                         String password) {
        this.userName = userName;
        this.password = password;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        try {

            Map<String, Object> results = new HashMap<String, Object>();

            String repoOwner = (String) workItem.getParameter("RepoOwner");
            String repoName = (String) workItem.getParameter("RepoName");
            String organization = (String) workItem.getParameter("Organization");

            Repository forkedRepository;

            if (StringUtils.isNotEmpty(repoOwner) && StringUtils.isNotEmpty(repoName)) {

                RepositoryService repoService = auth.getRespositoryService(this.userName,
                                                                           this.password);

                RepositoryId toBeForkedRepo = new RepositoryId(repoOwner,
                                                               repoName);
                if (StringUtils.isNotEmpty(organization)) {
                    forkedRepository = repoService.forkRepository(toBeForkedRepo,
                                                                  organization);
                } else {
                    forkedRepository = repoService.forkRepository(toBeForkedRepo);
                }

                results.put(RESULTS_VALUE,
                            new RepositoryInfo(forkedRepository));

                workItemManager.completeWorkItem(workItem.getId(),
                                                 results);
            } else {
                logger.error("Missing repository info to fork.");
                throw new IllegalArgumentException("Missing repository info to fork.");
            }
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
