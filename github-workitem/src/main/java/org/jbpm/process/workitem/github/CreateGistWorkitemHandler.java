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

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.service.GistService;
import org.jbpm.document.Document;
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

@Wid(widfile = "GithubCreateGist.wid", name = "GithubCreateGist",
        displayName = "GithubCreateGist",
        defaultHandler = "mvel: new org.jbpm.process.workitem.github.CreateGistWorkitemHandler(\"userName\", \"password\")",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "Content", required = true),
                @WidParameter(name = "Description"),
                @WidParameter(name = "IsPublic"),
        },
        results = {
                @WidResult(name = "GistURL")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "gist,github,create",
                action = @WidAction(title = "Create a new Gist"),
                authinfo = @WidAuth(required = true, params = {"userName", "password"},
                        paramsdescription = {"Github username", "Github password"},
                        referencesite = "https://github.com/")
        ))
public class CreateGistWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String userName;
    private String password;
    private GithubAuth auth = new GithubAuth();

    private static final Logger logger = LoggerFactory.getLogger(CreateGistWorkitemHandler.class);
    private static final String RESULTS_VALUE = "GistURL";

    public CreateGistWorkitemHandler(String userName,
                                     String password) {
        this.userName = userName;
        this.password = password;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            Document content = (Document) workItem.getParameter("Content");
            String description = (String) workItem.getParameter("Description");
            String isPublicStr = (String) workItem.getParameter("IsPublic");

            Map<String, Object> results = new HashMap<String, Object>();

            GistService gistService = auth.getGistService(this.userName,
                                                          this.password);

            Gist gist = new Gist();
            gist.setPublic(Boolean.parseBoolean(isPublicStr));
            gist.setDescription(description);

            GistFile file = new GistFile();
            file.setContent(new String(content.getContent(),
                                       StandardCharsets.UTF_8));
            file.setFilename(content.getName());

            gist.setFiles(Collections.singletonMap(file.getFilename(),
                                                   file));
            gist = gistService.createGist(gist);

            results.put(RESULTS_VALUE,
                        gist.getHtmlUrl());

            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setAuth(GithubAuth auth) {
        this.auth = auth;
    }
}
