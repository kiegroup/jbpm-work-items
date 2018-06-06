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
package org.jbpm.process.workitem.jira;

import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.User;
import com.atlassian.jira.rest.client.domain.Visibility;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.joda.time.DateTime;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "JiraAddComment.wid", name = "JiraAddComment",
        displayName = "JiraAddComment",
        defaultHandler = "mvel: new org.jbpm.process.workitem.jira.AddCommentOnIssueWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "IssueKey", required = true),
                @WidParameter(name = "Comment", required = true),
                @WidParameter(name = "Commenter"),
                @WidParameter(name = "CommentVisibleTo")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "jira,issue,add,comment",
                action = @WidAction(title = "Add comment to an existing Jira issue")
        ))
public class AddCommentOnIssueWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String userName;
    private String password;
    private String repoURI;

    private JiraAuth auth;

    private static final Logger logger = LoggerFactory.getLogger(AddCommentOnIssueWorkitemHandler.class);

    public AddCommentOnIssueWorkitemHandler(String userName,
                                            String password,
                                            String repoURI) {
        this.userName = userName;
        this.password = password;
        this.repoURI = repoURI;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String issueKey = (String) workItem.getParameter("IssueKey");
            String comment = (String) workItem.getParameter("Comment");
            String commenter = (String) workItem.getParameter("Commenter");
            String commentVisibleTo = (String) workItem.getParameter("CommentVisibleTo");

            if (auth == null) {
                auth = new JiraAuth(userName,
                                    password,
                                    repoURI);
            }

            NullProgressMonitor progressMonitor = new NullProgressMonitor();
            Issue issue = auth.getIssueRestClient().getIssue(issueKey,
                                                             progressMonitor);
            User user = auth.getUserRestClient().getUser(commenter,
                                                         progressMonitor);

            if (issue != null) {
                Comment toAddComment = new Comment(null,
                                                   comment,
                                                   user,
                                                   null,
                                                   new DateTime(),
                                                   null,
                                                   new Visibility(Visibility.Type.GROUP,
                                                                  commentVisibleTo),
                                                   null);
                auth.getIssueRestClient().addComment(progressMonitor,
                                                     issue.getSelf(),
                                                     toAddComment);

                workItemManager.completeWorkItem(workItem.getId(),
                                                 null);
            } else {
                logger.error("Could not find issue with key: " + issueKey);
                throw new IllegalArgumentException("Could not find issue with key: " + issueKey);
            }
        } catch (Exception e) {
            logger.error("Error executing workitem: " + e.getMessage());
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setAuth(JiraAuth auth) {
        this.auth = auth;
    }
}
