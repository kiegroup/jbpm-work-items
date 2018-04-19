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
package org.jbpm.process.workitem.jira;

import java.util.Arrays;
import java.util.Collection;

import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;

import org.apache.commons.lang3.StringUtils;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "JiraResolveIssue.wid", name = "JiraResolveIssue",
        displayName = "JiraResolveIssue",
        defaultHandler = "mvel: new org.jbpm.process.workitem.jira.ResolveIssueWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "IssueKey"),
                @WidParameter(name = "Resolution"),
                @WidParameter(name = "ResolutionComment"),
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}"),
                @WidMavenDepends(group = "com.atlassian.jira", artifact = "jira-rest-java-client", version = "1.0")
        })
public class ResolveIssueWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String userName;
    private String password;
    private String repoURI;

    private JiraAuth auth;

    private static final Logger logger = LoggerFactory.getLogger(ResolveIssueWorkitemHandler.class);

    public ResolveIssueWorkitemHandler(String userName,
                                       String password,
                                       String repoURI) {
        this.userName = userName;
        this.password = password;
        this.repoURI = repoURI;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        try {

            String issueKey = (String) workItem.getParameter("IssueKey");
            String resolution = (String) workItem.getParameter("Resolution");
            String resolutionComment = (String) workItem.getParameter("ResolutionComment");

            if (StringUtils.isNotEmpty(issueKey) && StringUtils.isNotEmpty(resolution)) {

                if (auth == null) {
                    auth = new JiraAuth(userName,
                                        password,
                                        repoURI);
                }

                NullProgressMonitor progressMonitor = new NullProgressMonitor();
                Issue issue = auth.getIssueRestClient().getIssue(issueKey,
                                                                 progressMonitor);

                if (issue != null) {
                    Iterable<Transition> transitions = auth.getIssueRestClient().getTransitions(issue.getTransitionsUri(),
                                                                                                progressMonitor);

                    Transition resolveIssueTransition = getTransitionByName(transitions,
                                                                            "Resolve Issue");
                    Collection<FieldInput> fieldInputs = Arrays.asList(new FieldInput("resolution",
                                                                                      resolution));
                    TransitionInput transitionInput = new TransitionInput(resolveIssueTransition.getId(),
                                                                          fieldInputs,
                                                                          Comment.valueOf(resolutionComment));
                    auth.getIssueRestClient().transition(issue.getTransitionsUri(),
                                                         transitionInput,
                                                         progressMonitor);

                    workItemManager.completeWorkItem(workItem.getId(),
                                                     null);
                } else {
                    logger.error("Could not find issue with key: " + issueKey);
                    throw new IllegalArgumentException("Could not find issue with key: " + issueKey);
                }
            } else {
                logger.error("Missing issue key or resolution.");
                throw new IllegalArgumentException("Missing issue key or resolution.");
            }
        } catch (Exception e) {
            logger.error("Error executing workitem: " + e.getMessage());
            handleException(e);
        }
    }

    private static Transition getTransitionByName(Iterable<Transition> transitions,
                                                  String transitionName) {
        for (Transition transition : transitions) {
            if (transition.getName().equals(transitionName)) {
                return transition;
            }
        }
        return null;
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setAuth(JiraAuth auth) {
        this.auth = auth;
    }
}

