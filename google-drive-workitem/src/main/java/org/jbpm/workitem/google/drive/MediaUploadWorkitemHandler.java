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

import java.io.FileOutputStream;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.apache.commons.io.FilenameUtils;
import org.jbpm.document.Document;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "GoogleUploadToDriveDefinitions.wid", name = "GoogleUploadToDrive",
        displayName = "GoogleUploadToDrive",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.drive.MediaUploadWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "DocToUpload", required = true),
                @WidParameter(name = "DocMimeType", required = true),
                @WidParameter(name = "UploadPath", required = true)
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "google,drive,media,upload",
                action = @WidAction(title = "Upload media to Google Drive")
        ))
public class MediaUploadWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String appName;
    private String clientSecret;
    private GoogleDriveAuth auth = new GoogleDriveAuth();

    private static final Logger logger = LoggerFactory.getLogger(MediaUploadWorkitemHandler.class);

    public MediaUploadWorkitemHandler(String appName,
                                      String clentSecret) {
        this.appName = appName;
        this.clientSecret = clentSecret;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        Document docToUpload = (Document) workItem.getParameter("DocToUpload");
        String docMimeType = (String) workItem.getParameter("DocMimeType");
        String uploadPath = (String) workItem.getParameter("UploadPath");

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            Drive drive = auth.getDriveService(appName,
                                               clientSecret);
            File fileMetadata = new File();
            fileMetadata.setTitle(docToUpload.getName());
            fileMetadata.setAlternateLink(docToUpload.getLink());
            if (docToUpload.getLastModified() != null) {
                fileMetadata.setModifiedDate(new DateTime(docToUpload.getLastModified()));
            }

            java.io.File tempDocFile = java.io.File.createTempFile(FilenameUtils.getBaseName(docToUpload.getName()),
                                                                   "." + FilenameUtils.getExtension(docToUpload.getName()));
            FileOutputStream fos = new FileOutputStream(tempDocFile);
            fos.write(docToUpload.getContent());
            fos.close();

            FileContent mediaContent = new FileContent(docMimeType,
                                                       tempDocFile);

            Drive.Files.Insert insert = drive.files().insert(fileMetadata,
                                                             mediaContent);
            MediaHttpUploader uploader = insert.getMediaHttpUploader();
            uploader.setDirectUploadEnabled(true);
            uploader.setProgressListener(new MediaUploadProgressListener());
            insert.execute();

            workItemManager.completeWorkItem(workItem.getId(),
                                             null);
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
