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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.MergeStatus;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.GistService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.jbpm.bpmn2.handler.WorkItemHandlerRuntimeException;
import org.jbpm.document.service.impl.DocumentImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.jbpm.test.AbstractBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GithubWorkitemHandlerTest extends AbstractBaseTest {

    @Mock
    GithubAuth auth;

    @Mock
    RepositoryService repositoryService;

    @Mock
    GistService gistService;

    @Mock
    PullRequestService pullRequestService;

    @Mock
    IssueService issueService;

    @Mock
    Gist gist;

    @Mock
    Repository forkedRepository;

    @Mock
    PullRequest pullRequest;

    @Mock
    MergeStatus mergeStatus;

    @Before
    public void setUp() {
        try {
            when(auth.getGistService(anyString(),
                                     anyString())).thenReturn(gistService);
            when(auth.getRespositoryService(anyString(),
                                            anyString())).thenReturn(repositoryService);
            when(auth.getPullRequestService(anyString(),
                                            anyString())).thenReturn(pullRequestService);
            when(auth.getIssueService(anyString(),
                                      anyString())).thenReturn(issueService);

            // gist service
            when(gistService.createGist(any(Gist.class))).thenReturn(gist);
            when(gist.getHtmlUrl()).thenReturn("testGistURL");

            // repository service
            when(repositoryService.forkRepository(any(RepositoryId.class),
                                                  anyString())).thenReturn(forkedRepository);

            List<Repository> testRepoList = new ArrayList<>();
            when(repositoryService.getRepositories(anyString())).thenReturn(testRepoList);

            // pull request service
            when(pullRequestService.getPullRequest(any(RepositoryId.class),
                                                   anyInt())).thenReturn(pullRequest);
            when(pullRequest.isMergeable()).thenReturn(true);
            when(pullRequestService.merge(any(RepositoryId.class),
                                          anyInt(),
                                          anyString())).thenReturn(mergeStatus);
            when(mergeStatus.isMerged()).thenReturn(true);

            // issue service
            List<Issue> issueList = new ArrayList<>();
            Issue issue1 = new Issue();
            issue1.setId(1);
            issue1.setBody("issue1");
            Issue issue2 = new Issue();
            issue2.setId(2);
            issue2.setBody("issue2");
            issueList.add(issue1);
            issueList.add(issue2);
            when(issueService.getIssues(anyString(),
                                        anyString(),
                                        any(Map.class))).thenReturn(issueList);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCreateGist() throws Exception {

        DocumentImpl testGistDoc = new org.jbpm.document.service.impl.DocumentImpl();
        testGistDoc.setContent(new String("Test gist file content").getBytes());
        testGistDoc.setName("testGistFile.txt");

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Content",
                              testGistDoc);
        workItem.setParameter("Description",
                              "test gist");
        workItem.setParameter("IsPublic",
                              "true");

        CreateGistWorkitemHandler handler = new CreateGistWorkitemHandler("testusername",
                                                                          "testpassword");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("GistURL") instanceof String);

        String createdGistURL = (String) manager.getResults().get(workItem.getId()).get("GistURL");
        assertNotNull(createdGistURL);
        assertEquals("testGistURL",
                     createdGistURL);
    }

    @Test(expected = WorkItemHandlerRuntimeException.class)
    public void testCreateGistInvalidParams() throws Exception {

        DocumentImpl testGistDoc = new org.jbpm.document.service.impl.DocumentImpl();
        testGistDoc.setContent(new String("Test gist file content").getBytes());
        testGistDoc.setName("testGistFile.txt");

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        CreateGistWorkitemHandler handler = new CreateGistWorkitemHandler("testusername",
                                                                          "testpassword");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(0,
                     manager.getResults().size());
    }

    @Test
    public void testForkRepository() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("RepoOwner",
                              "testRepoOwner");
        workItem.setParameter("RepoName",
                              "testRepoName");
        workItem.setParameter("Organization",
                              "testOrganization");

        ForkRepositoryWorkitemHandler handler = new ForkRepositoryWorkitemHandler("testusername",
                                                                                  "testpassword");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("ForkedRepoInfo") instanceof RepositoryInfo);
    }

    @Test
    public void testListRepositories() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("User",
                              "testUser");

        ListRepositoriesWorkitemHandler handler = new ListRepositoriesWorkitemHandler("testusername",
                                                                                      "testpassword");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("RepoListInfo") instanceof List);
    }

    @Test
    public void testMergePullRequest() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("RepoOwner",
                              "testRepoOwner");
        workItem.setParameter("RepoName",
                              "testRepoName");
        workItem.setParameter("PullRequestNum",
                              "10");
        workItem.setParameter("CommitMessage",
                              "testCommitMessage");

        MergePullRequestWorkitemHandler handler = new MergePullRequestWorkitemHandler("testusername",
                                                                                      "testpassword");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("IsMerged") instanceof Boolean);

        Boolean isPullRequestMerged = (Boolean) manager.getResults().get(workItem.getId()).get("IsMerged");
        assertNotNull(isPullRequestMerged);
        assertTrue(isPullRequestMerged.booleanValue());
    }

    @Test
    public void testFetchIssues() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("User",
                              "testUser");
        workItem.setParameter("RepoName",
                              "testRepoName");
        workItem.setParameter("IssuesState",
                              "open");

        FetchIssuesWorkitemHandler handler = new FetchIssuesWorkitemHandler("testusername",
                                                                            "testpassword");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("IssuesList") instanceof List);
        List<Issue> issueList = (List<Issue>) manager.getResults().get(workItem.getId()).get("IssuesList");
        assertNotNull(issueList);
        assertEquals(2,
                     issueList.size());
    }
}
