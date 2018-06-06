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

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "JiraJqlSearch.wid", name = "JiraJqlSearch",
        displayName = "JiraJqlSearch",
        defaultHandler = "mvel: new org.jbpm.process.workitem.jira.JqlSearchWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "SearchQuery", required = true)
        },
        results = {
                @WidResult(name = "SearchResults")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "jira,jql,search,query",
                action = @WidAction(title = "Execute a Jql query")
        ))
public class JqlSearchWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String userName;
    private String password;
    private String repoURI;

    private JiraAuth auth;

    private static final Logger logger = LoggerFactory.getLogger(JqlSearchWorkitemHandler.class);
    private static final String RESULTS_VALUE = "SearchResults";

    public JqlSearchWorkitemHandler(String userName,
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

            String jqlQuery = (String) workItem.getParameter("SearchQuery");

            if (auth == null) {
                auth = new JiraAuth(userName,
                                    password,
                                    repoURI);
            }

            Map<String, Object> results = new HashMap<String, Object>();
            Map<String, String> resultIssues = new HashMap<>();

            NullProgressMonitor progressMonitor = new NullProgressMonitor();
            SearchResult searchResult = auth.getSearchRestClient().searchJql(jqlQuery,
                                                                             progressMonitor);
            Iterable<BasicIssue> foundIssues = searchResult.getIssues();

            for (BasicIssue issue : foundIssues) {
                resultIssues.put(issue.getKey(),
                                 issue.getSelf().toURL().toString());
            }

            results.put(RESULTS_VALUE,
                        resultIssues);

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
