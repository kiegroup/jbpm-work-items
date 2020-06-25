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

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.apache.commons.io.IOUtils;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.bpmn2.handler.WorkItemHandlerRuntimeException;
import org.jbpm.document.Document;
import org.jbpm.document.service.impl.DocumentImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.jbpm.test.AbstractBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"jdk.internal.reflect.*"})
@PrepareForTest({MediaHttpUploader.class, MediaHttpDownloader.class, Drive.Files.Insert.class, Drive.Files.Get.class})
public class GoogleDriveWorkitemHandlerTest extends AbstractBaseTest {

    @Mock
    GoogleDriveAuth auth;

    @Mock
    Drive gdriveService;

    @Mock
    Drive.Files gdriveFiles;

    private Document testDoc;

    @Before
    public void setUp() {
        try {
            InputStream testInputStream =
                    IOUtils.toInputStream("test doc content",
                                          "UTF-8");

            MediaHttpUploader mediaHttpUploader = PowerMockito.mock(MediaHttpUploader.class);
            MediaHttpDownloader mediaHttpDownloader = PowerMockito.mock(MediaHttpDownloader.class);

            Drive.Files.Insert gdriveFilesInsert = PowerMockito.mock(Drive.Files.Insert.class);
            Drive.Files.Get gdriveFilesGet = PowerMockito.mock(Drive.Files.Get.class);

            when(auth.getDriveService(anyString(),
                                      anyString())).thenReturn(gdriveService);
            when(gdriveService.files()).thenReturn(gdriveFiles);
            when(gdriveFiles.insert(any(File.class),
                                    any(FileContent.class))).thenReturn(gdriveFilesInsert);
            when(gdriveFiles.get(anyString())).thenReturn(gdriveFilesGet);

            when(gdriveFilesInsert.getMediaHttpUploader()).thenReturn(mediaHttpUploader);
            when(gdriveFilesInsert.execute()).thenReturn(new File());
            when(gdriveFilesGet.getMediaHttpDownloader()).thenReturn(mediaHttpDownloader);
            when(gdriveFilesGet.executeMediaAsInputStream()).thenReturn(testInputStream);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testUpload() throws Exception {
        DocumentImpl testUploadDoc = new DocumentImpl();
        testUploadDoc.setContent(new String("Test file to upload").getBytes());
        testUploadDoc.setName("testFileToUpload.txt");

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("DocToUpload",
                              testUploadDoc);
        workItem.setParameter("DocMimeType",
                              "text/plain");
        workItem.setParameter("UploadPath",
                              "/some/upload/path");

        MediaUploadWorkitemHandler handler = new MediaUploadWorkitemHandler("myAppName",
                                                                            "{}");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }

    @Test(expected = WorkItemHandlerRuntimeException.class)
    public void testUploadInvalidParams() throws Exception {
        DocumentImpl testUploadDoc = new DocumentImpl();
        testUploadDoc.setContent(new String("Test file to upload").getBytes());
        testUploadDoc.setName("testFileToUpload.txt");

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        MediaUploadWorkitemHandler handler = new MediaUploadWorkitemHandler("myAppName",
                                                                            "{}");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(0,
                     manager.getResults().size());
    }

    @Test
    public void testDownload() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("DocumentPath",
                              "/some/download/path/testdoc.txt");

        MediaDownloadWorkitemHandler handler = new MediaDownloadWorkitemHandler("myAppName",
                                                                                "{}");

        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("Document") instanceof Document);

        Document downloadedDoc = (Document) manager.getResults().get(workItem.getId()).get("Document");
        assertNotNull(downloadedDoc);
        assertEquals("testdoc.txt",
                     downloadedDoc.getName());
        assertEquals("/some/download/path/testdoc.txt",
                     downloadedDoc.getIdentifier());
        assertEquals("test doc content",
                     new String(downloadedDoc.getContent()));
    }
}
