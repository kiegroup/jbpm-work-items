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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.ProgressMonitor;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.StatusCategory;
import com.atlassian.jira.rest.client.api.UserRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.BasicVotes;
import com.atlassian.jira.rest.client.api.domain.BasicWatchers;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.api.domain.Status;

import io.atlassian.util.concurrent.Promise;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.bpmn2.handler.WorkItemHandlerRuntimeException;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
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

            @SuppressWarnings("unchecked")
            Promise<Iterable<IssueType>> promiseIssueTypes = mock(Promise.class);
            when(promiseIssueTypes.claim()).thenReturn(testIssueTypes); // Explicitly return the list
            when(metadataRestClient.getIssueTypes()).thenReturn(promiseIssueTypes);

            // Mock statuses
            List<Status> testStatuses = new ArrayList<>();
            testStatuses.add(new Status(testURI, 1L, "testStatus", "testStatusDesc", testURI, new StatusCategory(testURI, "testCategory", 1L, "testKey", "blue")));
            @SuppressWarnings("unchecked")
            Promise<Iterable<Status>> promiseStatuses = mock(Promise.class);
            when(promiseStatuses.claim()).thenReturn(testStatuses);
            when(metadataRestClient.getStatuses()).thenReturn(promiseStatuses);
            // issuerestclient
            BasicIssue basicIssue = new BasicIssue(testURI, "testIssueKey", 1L);
            @SuppressWarnings("unchecked")
            Promise<BasicIssue> promiseBasicIssue = mock(Promise.class);
            when(promiseBasicIssue.claim()).thenReturn(basicIssue);
            when(issueRestClient.createIssue(any(IssueInput.class))).thenReturn(promiseBasicIssue);

           // Mock StatusCategory
            StatusCategory testStatusCategory = new StatusCategory(testURI, "testCategory", 1L, "testKey", "blue");
            // Mock Status
            Status testStatus = new Status(testURI, 1L, "testStatus", "testStatusDesc", testURI, testStatusCategory);
            // Mock BasicProject
            BasicProject testProject = new BasicProject(testURI, "testProjectKey", 1L, "testProject");
            Issue issue = new Issue(
                    "testSummary",
                    testURI,
                    "testIssueKey",
                    1L,
                    testProject,
                    testIssueType,
                    testStatus,
                    "testDescription",
                    null,
                    null,
                    new ArrayList<>(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    null,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    testURI,
                    null,
                    new BasicVotes(testURI, 0, false),
                    new ArrayList<>(),
                    new BasicWatchers(testURI, false, 1),
                    new ArrayList<>(),
                    null,
                    null,
                    null,
                    new HashSet<>()
            );
            @SuppressWarnings("unchecked")
            Promise<Issue> promiseIssue = mock(Promise.class);
            when(promiseIssue.claim()).thenReturn(issue);
            when(issueRestClient.getIssue(any(String.class))).thenReturn(promiseIssue); // Mock for getIssue(String)
            // Mock addComment to return Promise<Void>
            @SuppressWarnings("unchecked")
            Promise<Void> promiseAddComment = mock(Promise.class);
            when(promiseAddComment.claim()).thenReturn(null);
            when(issueRestClient.addComment(any(URI.class), any(Comment.class))).thenReturn(promiseAddComment);

            Transition testTransition = new Transition("Resolve Issue",
                                                       1,
                                                       null);
            List<Transition> testAllTransitions = new ArrayList<>();
            testAllTransitions.add(testTransition);
            @SuppressWarnings("unchecked")
            Promise<Iterable<Transition>> promiseTransitions = mock(Promise.class);
            when(promiseTransitions.claim()).thenReturn(testAllTransitions);
            when(issueRestClient.getTransitions(any(URI.class))).thenReturn(promiseTransitions);
            // Mock transition to return Promise<Void>
            @SuppressWarnings("unchecked")
            Promise<Void> promiseTransition = mock(Promise.class);
            when(promiseTransition.claim()).thenReturn(null);
            when(issueRestClient.transition(any(URI.class), any(TransitionInput.class))).thenReturn(promiseTransition);

            // searchrestclient
            BasicIssue basicIssueForSearch = new BasicIssue(testURI, "testIssueKey", 1L);
            List<Issue> testSearchResults = new ArrayList<>();
            testSearchResults.add(issue);
            @SuppressWarnings("unchecked")
            Promise<SearchResult> promiseSearchResult = mock(Promise.class);
            @SuppressWarnings("unchecked")
            SearchResult mockSearchResult = mock(SearchResult.class);
            when(promiseSearchResult.claim()).thenReturn(mockSearchResult);
            when(mockSearchResult.getIssues()).thenReturn(testSearchResults);
            when(searchRestClient.searchJql(anyString())).thenReturn(promiseSearchResult);

            // userrestclient
            @SuppressWarnings("unchecked")
            Promise<User> promiseUser = mock(Promise.class);
            when(promiseUser.claim()).thenReturn(user);
            when(userRestClient.getUser(anyString())).thenReturn(promiseUser);
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

    @Test(expected = WorkItemHandlerRuntimeException.class)
    public void testCreateIssueInvalidParams() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        CreateIssueWorkitemHandler handler = new CreateIssueWorkitemHandler("testusername",
                                                                            "testpassword",
                                                                            "testjiraurl");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(0,
                     manager.getResults().size());
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

    @Test(expected = WorkItemHandlerRuntimeException.class)
    public void testJqlSearchInvalidParams() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        JqlSearchWorkitemHandler handler = new JqlSearchWorkitemHandler("testusername",
                                                                        "testpassword",
                                                                        "testjiraurl");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(0,
                     manager.getResults().size());
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

    @Test(expected = WorkItemHandlerRuntimeException.class)
    public void testAddCommentInvalidParams() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        AddCommentOnIssueWorkitemHandler handler = new AddCommentOnIssueWorkitemHandler("testusername",
                                                                                        "testpassword",
                                                                                        "testjiraurl");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(0,
                     manager.getResults().size());
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

    @Test(expected = WorkItemHandlerRuntimeException.class)
    public void testResolveIssueInvalidParams() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        ResolveIssueWorkitemHandler handler = new ResolveIssueWorkitemHandler("testusername",
                                                                              "testpassword",
                                                                              "testjiraurl");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(0,
                     manager.getResults().size());
    }
}
