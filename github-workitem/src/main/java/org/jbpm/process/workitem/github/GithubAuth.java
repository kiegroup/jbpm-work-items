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

import java.io.IOException;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;

public class GithubAuth {

    private GitHubClient getGitHubClient(String username,
                                         String password) {
        GitHubClient client = new GitHubClient();
        client.setCredentials(username,
                              password);
        return client;
    }

    public GistService getGistService(String username,
                                      String password) throws IOException {

        return new GistService(getGitHubClient(username,
                                               password));
    }

    public RepositoryService getRespositoryService(String username,
                                                   String password) throws IOException {
        return new RepositoryService(getGitHubClient(username,
                                                     password));
    }

    public PullRequestService getPullRequestService(String username,
                                                    String password) throws IOException {
        return new PullRequestService(getGitHubClient(username,
                                                      password));
    }

    public IssueService getIssueService(String username,
                                        String password) throws IOException {
        return new IssueService(getGitHubClient(username,
                                                password));
    }
}