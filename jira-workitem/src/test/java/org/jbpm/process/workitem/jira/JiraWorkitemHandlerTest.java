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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.MetadataRestClient;
import com.atlassian.jira.rest.client.ProgressMonitor;
import com.atlassian.jira.rest.client.ProjectRestClient;
import com.atlassian.jira.rest.client.SearchRestClient;
import com.atlassian.jira.rest.client.UserRestClient;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.IssueType;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.User;
import com.atlassian.jira.rest.client.domain.input.IssueInput;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;

import org.drools.core.process.instance.impl.WorkItemImpl;

import org.jbpm.process.workitem.core.TestWorkItemManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JiraWorkitemHandlerTest {

    @Mock
    JiraAuth auth;

    @Mock
    IssueRestClient issueRestClient;

    @Mock
    JiraRestClient jiraRestClient;

    @Mock
    MetadataRestClient metadataRestClient;

    @Mock
    ProjectRestClient projectRestClient;

    @Mock
    SearchRestClient searchRestClient;

    @Mock
    UserRestClient userRestClient;

    @Mock
    SearchResult searchResults;

    @Mock
    User user;

    private URI testURI = URI.create("http://testURI.com/test");

    @Before
    public void setUp() {
        try {
            when(auth.getIssueRestClient()).thenReturn(issueRestClient);
            when(auth.getMetaDataRestClient()).thenReturn(metadataRestClient);
            when(auth.getProjectRestClient()).thenReturn(projectRestClient);
            when(auth.getSearchRestClient()).thenReturn(searchRestClient);
            when(auth.getUserRestClient()).thenReturn(userRestClient);

            // metadataclient
            IssueType testIssueType = new IssueType(testURI,
                                                    1L,
                                                    "testIssueType",
                                                    false,
                                                    "test descriptoin",
                                                    testURI);
            List<IssueType> testIssueTypes = new ArrayList<>();
            testIssueTypes.add(testIssueType);

            when(metadataRestClient.getIssueTypes(any(ProgressMonitor.class))).thenReturn(testIssueTypes);

            // issuerestclient
            BasicIssue basicIssue = new BasicIssue(testURI,
                                                   "testIssueKey");
            when(issueRestClient.createIssue(any(IssueInput.class),
                                             any(ProgressMonitor.class))).thenReturn(basicIssue);

            Issue issue = new Issue("",
                                    null,
                                    "testIssueKey",
                                    null,
                                    null,
                                    null,
                                    "",
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null);
            when(issueRestClient.getIssue(anyString(),
                                          any(ProgressMonitor.class))).thenReturn(issue);
            doNothing().when(issueRestClient).addComment(any(ProgressMonitor.class),
                                                         any(URI.class),
                                                         any(Comment.class));

            Transition testTransition = new Transition("Resolve Issue",
                                                       1,
                                                       null);
            List<Transition> testAllTransitions = new ArrayList<>();
            testAllTransitions.add(testTransition);
            when(issueRestClient.getTransitions(any(URI.class),
                                                any(ProgressMonitor.class))).thenReturn(testAllTransitions);
            doNothing().when(issueRestClient).transition(any(URI.class),
                                                         any(TransitionInput.class),
                                                         any(ProgressMonitor.class));

            // searchrestclient
            List<BasicIssue> testSearchResults = new ArrayList<>();
            testSearchResults.add(basicIssue);
            when(searchRestClient.searchJql(anyString(),
                                            any(ProgressMonitor.class))).thenReturn(searchResults);
            when(searchResults.getIssues()).thenReturn(testSearchResults);

            // userrestclient
            when(userRestClient.getUser(anyString(),
                                        any(ProgressMonitor.class))).thenReturn(user);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCreateIssue() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ProjectKey",
                              "testJiraProject");
        workItem.setParameter("IssueSummary",
                              "test issue summary");
        workItem.setParameter("IssueDescription",
                              "this is test issue description");
        workItem.setParameter("IssueType",
                              "testIssueType");
        workItem.setParameter("AssigneeName",
                              "testAssigneeName");
        workItem.setParameter("ReporterName",
                              "testReporterName");
        workItem.setParameter("ComponentName",
                              "testComponentName");

        CreateIssueWorkitemHandler handler = new CreateIssueWorkitemHandler("testusername",
                                                                            "testpassword",
                                                                            "testjiraurl");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("CreatedIssueKey") instanceof String);

        String cretedJiraKey = (String) manager.getResults().get(workItem.getId()).get("CreatedIssueKey");
        assertNotNull(cretedJiraKey);
        assertEquals("testIssueKey",
                     cretedJiraKey);
    }

    @Test
    public void testJqlSearch() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("SearchQuery",
                              "test jql query");

        JqlSearchWorkitemHandler handler = new JqlSearchWorkitemHandler("testusername",
                                                                        "testpassword",
                                                                        "testjiraurl");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("SearchResults") instanceof Map);

        Map<String, String> searchResults = (Map<String, String>) manager.getResults().get(workItem.getId()).get("SearchResults");
        assertNotNull(searchResults);
        assertEquals(1,
                     searchResults.size());
    }

    @Test
    public void testAddComment() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("IssueKey",
                              "testIssueKey");
        workItem.setParameter("Comment",
                              "testComment");
        workItem.setParameter("Commenter",
                              "testCommenter");
        workItem.setParameter("CommentVisibleTo",
                              "testVisibilityGroup");

        AddCommentOnIssueWorkitemHandler handler = new AddCommentOnIssueWorkitemHandler("testusername",
                                                                                        "testpassword",
                                                                                        "testjiraurl");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }

    @Test
    public void testResolveIssue() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("IssueKey",
                              "testIssueKey");
        workItem.setParameter("Resolution",
                              "testResolved");
        workItem.setParameter("ResolutionComment",
                              "testResolutionComment");

        ResolveIssueWorkitemHandler handler = new ResolveIssueWorkitemHandler("testusername",
                                                                              "testpassword",
                                                                              "testjiraurl");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }
}
