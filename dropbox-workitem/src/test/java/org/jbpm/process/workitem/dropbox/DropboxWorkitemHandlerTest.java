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
import java.util.Date;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.DownloadBuilder;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.WriteMode;
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
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DropboxWorkitemHandlerTest extends AbstractBaseTest {

    @Mock
    DropboxAuth auth;

    @Mock
    DbxClientV2 client;

    @Mock
    DbxUserFilesRequests fileRequests;

    @Mock
    UploadBuilder uploadBuilder;

    @Mock
    FileMetadata metaData;

    @Mock
    DownloadBuilder downloadBuilder;

    @Mock
    DbxDownloader<FileMetadata> downloader;

    private Document testDoc;

    @Before
    public void setUp() {
        try {
            testDoc = new DocumentImpl();
            testDoc.setName("testDoc.txt");
            testDoc.setIdentifier("testDoc");
            testDoc.setLastModified(new Date());
            testDoc.setContent(new String("test doc content").getBytes());

            InputStream testInputStream =
                    IOUtils.toInputStream("test doc content",
                                          "UTF-8");

            when(auth.authorize(anyString(),
                                anyString())).thenReturn(client);
            when(client.files()).thenReturn(fileRequests);

            // upload
            when(fileRequests.uploadBuilder(anyString())).thenReturn(uploadBuilder);
            when(uploadBuilder.withMode(any(WriteMode.class))).thenReturn(uploadBuilder);
            when(uploadBuilder.withClientModified(any(Date.class))).thenReturn(uploadBuilder);
            when(uploadBuilder.uploadAndFinish(any(java.io.InputStream.class))).thenReturn(metaData);

            // download
            when(fileRequests.downloadBuilder(anyString())).thenReturn(downloadBuilder);
            when(downloadBuilder.start()).thenReturn(downloader);
            when(downloader.getInputStream()).thenReturn(testInputStream);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testUploadFile() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Path",
                              "/testpath");
        workItem.setParameter("Document",
                              testDoc);

        UploadFileWorkitemHandler handler = new UploadFileWorkitemHandler("testClientID",
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
    public void testUploadFileInvalidParams() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        UploadFileWorkitemHandler handler = new UploadFileWorkitemHandler("testClientID",
                                                                          "{}");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(0,
                     manager.getResults().size());
    }

    @Test
    public void testDownloadFile() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("DocumentPath",
                              "/testpath/testDocName.txt");

        DownloadFileWorkitemHandler handler = new DownloadFileWorkitemHandler("testClientID",
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
        assertEquals("testDocName.txt",
                     downloadedDoc.getName());
        assertEquals("test doc content",
                     new String(downloadedDoc.getContent()));
    }

    @Test(expected = WorkItemHandlerRuntimeException.class)
    public void testDownloadFileInvalidParams() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        DownloadFileWorkitemHandler handler = new DownloadFileWorkitemHandler("testClientID",
                                                                              "{}");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(0,
                     manager.getResults().size());
    }
}
