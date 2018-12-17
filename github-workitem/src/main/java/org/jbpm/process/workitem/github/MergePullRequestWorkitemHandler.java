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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.egit.github.core.MergeStatus;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.PullRequestService;
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

@Wid(widfile = "GithubMergePullRequest.wid", name = "GithubMergePullRequest",
        displayName = "GithubMergePullRequest",
        defaultHandler = "mvel: new org.jbpm.process.workitem.github.MergePullRequestWorkitemHandler(\"userName\", \"password\")",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        parameters = {
                @WidParameter(name = "RepoOwner", required = true),
                @WidParameter(name = "RepoName", required = true),
                @WidParameter(name = "PullRequestNum", required = true),
                @WidParameter(name = "CommitMessage")
        },
        results = {
                @WidResult(name = "IsMerged", runtimeType = "java.lang.Boolean")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "github,repo,repository,merge,pull,request,pullrequest,pr",
                action = @WidAction(title = "Merget a pull request on GitHub"),
                authinfo = @WidAuth(required = true, params = {"userName", "password"},
                        paramsdescription = {"Github username", "Github password"},
                        referencesite = "https://github.com/")
        ))
public class MergePullRequestWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String userName;
    private String password;
    private GithubAuth auth = new GithubAuth();

    private static final Logger logger = LoggerFactory.getLogger(MergePullRequestWorkitemHandler.class);
    private static final String RESULTS_VALUE = "IsMerged";

    public MergePullRequestWorkitemHandler(String userName,
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

            String repoOwner = (String) workItem.getParameter("RepoOwner");
            String repoName = (String) workItem.getParameter("RepoName");
            String pullRequestNum = (String) workItem.getParameter("PullRequestNum");
            String commitMessage = (String) workItem.getParameter("CommitMessage");

            MergeStatus mergeStatus;

            PullRequestService pullRequestService = auth.getPullRequestService(this.userName,
                                                                               this.password);

            RepositoryId repositoryId = new RepositoryId(repoOwner,
                                                         repoName);
            if (pullRequestService.getPullRequest(repositoryId,
                                                  Integer.parseInt(pullRequestNum)).isMergeable()) {
                mergeStatus = pullRequestService.merge(repositoryId,
                                                       Integer.parseInt(pullRequestNum),
                                                       commitMessage);

                if (mergeStatus != null && mergeStatus.isMerged()) {
                    results.put(RESULTS_VALUE,
                                mergeStatus.isMerged());
                } else {
                    throw new IllegalArgumentException("Unable to merget pull request: " + mergeStatus.getMessage());
                }
            } else {
                throw new IllegalArgumentException("Pull request " + pullRequestNum + " is not mergeable");
            }

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
