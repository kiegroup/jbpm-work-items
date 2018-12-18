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
package org.jbpm.workitem.google.drive;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.api.services.drive.Drive;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jbpm.document.Document;
import org.jbpm.document.service.impl.DocumentImpl;
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

@Wid(widfile = "GoogleDownloadFromDriveDefinitions.wid", name = "GoogleDownloadFromDrive",
        displayName = "GoogleDownloadFromDrive",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.drive.MediaDownloadWorkitemHandler(\"appName\", \"clentSecret\")",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        parameters = {
                @WidParameter(name = "DocumentPath", required = true)
        },
        results = {
                @WidResult(name = "Document", runtimeType = "org.jbpm.document.Document")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "google,drive,media,download",
                action = @WidAction(title = "Download media from Google Drive"),
                authinfo = @WidAuth(required = true, params = {"appName", "clentSecret"},
                        paramsdescription = {"Google app name", "Google client secret"},
                        referencesite = "https://developers.google.com/drive/api/v3/about-auth")
        ))
public class MediaDownloadWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String appName;
    private String clientSecret;
    private GoogleDriveAuth auth = new GoogleDriveAuth();

    private static final Logger logger = LoggerFactory.getLogger(MediaDownloadWorkitemHandler.class);
    private static final String RESULTS_DOCUMENT = "Document";

    public MediaDownloadWorkitemHandler(String appName,
                                        String clentSecret) {
        this.appName = appName;
        this.clientSecret = clentSecret;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        Map<String, Object> results = new HashMap<String, Object>();

        String documentPath = (String) workItem.getParameter("DocumentPath");

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            Drive drive = auth.getDriveService(appName,
                                               clientSecret);

            Drive.Files.Get request = drive.files().get(documentPath);
            request.getMediaHttpDownloader().setProgressListener(new MediaDownloadProgressListener());
            request.getMediaHttpDownloader().setDirectDownloadEnabled(true);

            InputStream docInputStream = request.executeMediaAsInputStream();

            Document doc = new DocumentImpl();
            String docBaseName = FilenameUtils.getBaseName(documentPath);
            String docExtension = FilenameUtils.getExtension(documentPath);
            doc.setName(docBaseName + "." + docExtension);
            doc.setIdentifier(documentPath);
            doc.setLastModified(new Date());
            doc.setContent(IOUtils.toByteArray(docInputStream));

            results.put(RESULTS_DOCUMENT,
                        doc);

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
    public void setAuth(GoogleDriveAuth auth) {
        this.auth = auth;
    }
}
