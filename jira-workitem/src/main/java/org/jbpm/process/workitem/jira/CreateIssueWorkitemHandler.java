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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.IssueType;
import com.atlassian.jira.rest.client.domain.input.IssueInput;
import com.atlassian.jira.rest.client.domain.input.IssueInputBuilder;
import org.apache.commons.lang3.StringUtils;
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

@Wid(widfile = "JiraCreateIssue.wid", name = "JiraCreateIssue",
        displayName = "JiraCreateIssue",
        defaultHandler = "mvel: new org.jbpm.process.workitem.jira.CreateIssueWorkitemHandler(\"userName\", \"password\", \"repoURI\")",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "icon.png",
        parameters = {
                @WidParameter(name = "ProjectKey", required = true),
                @WidParameter(name = "IssueSummary"),
                @WidParameter(name = "IssueDescription"),
                @WidParameter(name = "IssueType"),
                @WidParameter(name = "AssigneeName"),
                @WidParameter(name = "ReporterName"),
                @WidParameter(name = "ComponentName")
        },
        results = {
                @WidResult(name = "CreatedIssueKey")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "jira,issue,create",
                action = @WidAction(title = "Create a new Jira issue"),
                authinfo = @WidAuth(required = true, params = {"userName", "password"},
                        paramsdescription = {"Jira user", "Jira password"},
                        referencesite = "https://www.atlassian.com/software/jira")
        ))
public class CreateIssueWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String userName;
    private String password;
    private String repoURI;

    private JiraAuth auth;

    private static final Logger logger = LoggerFactory.getLogger(CreateIssueWorkitemHandler.class);
    private static final String RESULTS_VALUE = "CreatedIssueKey";

    public CreateIssueWorkitemHandler(String userName,
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

            String projectKey = (String) workItem.getParameter("ProjectKey");
            String issueSummary = (String) workItem.getParameter("IssueSummary");
            String issueDescription = (String) workItem.getParameter("IssueDescription");
            String givenIssueType = (String) workItem.getParameter("IssueType");
            String assigneeName = (String) workItem.getParameter("AssigneeName");
            String reporterName = (String) workItem.getParameter("ReporterName");
            String componentName = (String) workItem.getParameter("ComponentName");

            if (auth == null) {
                auth = new JiraAuth(userName,
                                    password,
                                    repoURI);
            }

            Map<String, Object> results = new HashMap<String, Object>();

            IssueType issueTypeObj = null;
            IssueInputBuilder issueBuilder;

            if (StringUtils.isEmpty(givenIssueType)) {
                givenIssueType = "Bug";
            }

            NullProgressMonitor progressMonitor = new NullProgressMonitor();
            // get the issue type for whats given
            Iterable<IssueType> allIssueTypes = auth.getMetaDataRestClient().getIssueTypes(progressMonitor);
            for (IssueType myIssueType : allIssueTypes) {
                if (myIssueType.getName().equals(givenIssueType)) {
                    issueTypeObj = myIssueType;
                }
            }

            if (issueTypeObj != null) {
                issueBuilder = new IssueInputBuilder(projectKey,
                                                     issueTypeObj.getId(),
                                                     issueSummary);
            } else {
                issueBuilder = new IssueInputBuilder(projectKey,
                                                     1L,
                                                     issueSummary);
            }

            if (StringUtils.isNotEmpty(issueDescription)) {
                issueBuilder.setDescription(issueDescription);
            }

            if (StringUtils.isNotEmpty(assigneeName)) {
                issueBuilder.setAssigneeName(assigneeName);
            }

            if (StringUtils.isNotEmpty(reporterName)) {
                issueBuilder.setReporterName(reporterName);
            }

            if (StringUtils.isNotEmpty(componentName)) {
                issueBuilder.setComponentsNames(Arrays.asList(componentName));
            }

            IssueInput issueInput = issueBuilder.build();
            BasicIssue toCreateIssue = auth.getIssueRestClient().createIssue(issueInput,
                                                                             progressMonitor);

            Issue createdIssue = auth.getIssueRestClient().getIssue(toCreateIssue.getKey(),
                                                                    progressMonitor);

            results.put(RESULTS_VALUE,
                        createdIssue.getKey());

            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
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
