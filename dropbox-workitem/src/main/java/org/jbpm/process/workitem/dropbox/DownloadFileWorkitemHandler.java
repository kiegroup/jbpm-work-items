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
package org.jbpm.process.workitem.dropbox;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.dropbox.core.v2.DbxClientV2;
import org.apache.commons.io.IOUtils;
import org.jbpm.document.Document;
import org.jbpm.document.service.impl.DocumentImpl;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "DropboxDownloadFileDefinitions.wid", name = "DropboxDownloadFile",
        displayName = "DropboxDownloadFile",
        defaultHandler = "mvel: new org.jbpm.process.workitem.dropbox.DownloadFileWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "DocumentPath")
        },
        results = {
                @WidResult(name = "Document")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        })
public class DownloadFileWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(DownloadFileWorkitemHandler.class);
    private static final String RESULTS_DOCUMENT = "Document";

    private DropboxAuth auth;
    private DbxClientV2 client;
    private String clientIdentifier;
    private String accessToken;

    public DownloadFileWorkitemHandler(String clientIdentifier,
                                       String accessToken) {
        this.clientIdentifier = clientIdentifier;
        this.accessToken = accessToken;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        Map<String, Object> results = new HashMap<String, Object>();

        try {

            if (auth == null) {
                auth = new DropboxAuth();
            }

            client = auth.authorize(clientIdentifier,
                                    accessToken);

            String dropboxDocumentPath = (String) workItem.getParameter("DocumentPath");

            InputStream inStream = client.files().downloadBuilder(dropboxDocumentPath)
                    .start().getInputStream();

            Path path = Paths.get(dropboxDocumentPath);

            Document doc = new DocumentImpl();
            doc.setName(path.getFileName().toString());
            doc.setIdentifier(dropboxDocumentPath);
            doc.setLastModified(new Date());
            doc.setContent(IOUtils.toByteArray(inStream));

            results.put(RESULTS_DOCUMENT,
                        doc);

            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
        } catch (Exception e) {
            logger.error("Unable to download file: " + e.getMessage());
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setAuth(DropboxAuth auth) {
        this.auth = auth;
    }
}
