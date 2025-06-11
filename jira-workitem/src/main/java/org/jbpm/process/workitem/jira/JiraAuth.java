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

import java.net.URI;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.UserRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

public class JiraAuth {

    private JiraRestClient jiraRestClient;

    public JiraAuth(String username,
                    String password,
                    String repoURI) throws Exception {
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        final URI jiraServerUri = new URI(repoURI);

        jiraRestClient = factory.createWithBasicHttpAuthentication(jiraServerUri,
                                                                   username,
                                                                   password);
    }

    public SearchRestClient getSearchRestClient() throws Exception {
        return jiraRestClient.getSearchClient();
    }

    public IssueRestClient getIssueRestClient() throws Exception {
        return jiraRestClient.getIssueClient();
    }

    public ProjectRestClient getProjectRestClient() throws Exception {
        return jiraRestClient.getProjectClient();
    }

    public MetadataRestClient getMetaDataRestClient() throws Exception {
        return jiraRestClient.getMetadataClient();
    }

    public UserRestClient getUserRestClient() throws Exception {
        return jiraRestClient.getUserClient();
    }
}
