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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.service.IssueService;
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

@Wid(widfile = "GithubFetchIssues.wid", name = "GithubFetchIssues",
        displayName = "GithubFetchIssues",
        defaultHandler = "mvel: new org.jbpm.process.workitem.github.FetchIssuesWorkitemHandler(\"userName\", \"password\")",
        documentation = "${artifactId}/index.html",
        module = "${artifactId}", version = "${version}",
        parameters = {
                @WidParameter(name = "User", required = true),
                @WidParameter(name = "RepoName", required = true),
                @WidParameter(name = "IssuesState")
        },
        results = {
                @WidResult(name = "IssuesList", runtimeType = "java.util.List")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "github,repo,repository,fetch,issues",
                action = @WidAction(title = "Fetch issues for a project from GitHub"),
                authinfo = @WidAuth(required = true, params = {"userName", "password"},
                        paramsdescription = {"Github username", "Github password"},
                        referencesite = "https://github.com/")
        ))
public class FetchIssuesWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String userName;
    private String password;
    private GithubAuth auth = new GithubAuth();

    private static final Logger logger = LoggerFactory.getLogger(FetchIssuesWorkitemHandler.class);
    private static final String RESULTS_VALUE = "IssuesList";

    public FetchIssuesWorkitemHandler(String userName,
                                      String password) {
        this.userName = userName;
        this.password = password;
    }

    @Override
    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        try {
            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            Map<String, Object> results = new HashMap<String, Object>();

            String user = (String) workItem.getParameter("User");
            String repoName = (String) workItem.getParameter("RepoName");
            String issuesState = (String) workItem.getParameter("IssuesState");

            IssueService issueService = auth.getIssueService(this.userName,
                                                             this.password);

            // default to open
            if (issuesState == null || (!issuesState.equalsIgnoreCase("open") || !issuesState.equalsIgnoreCase("closed"))) {
                issuesState = IssueService.STATE_OPEN;
            }

            List<Issue> issues = issueService.getIssues(user,
                                                        repoName,
                                                        Collections.singletonMap(IssueService.FILTER_STATE,
                                                                                 issuesState.toLowerCase()));

            // no issues is acceptable
            if (issues != null) {
                results.put(RESULTS_VALUE,
                            issues);
            } else {
                throw new IllegalArgumentException("Could not retrieve valid issues");
            }

            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setAuth(GithubAuth auth) {
        this.auth = auth;
    }
}