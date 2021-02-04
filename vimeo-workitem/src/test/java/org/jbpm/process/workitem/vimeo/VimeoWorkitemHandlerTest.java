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
package org.jbpm.process.workitem.vimeo;

import java.io.File;

import com.clickntap.vimeo.Vimeo;
import com.clickntap.vimeo.VimeoResponse;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VimeoWorkitemHandlerTest {

    @Mock
    Vimeo vimeo;

    @Mock
    VimeoResponse vimeoResponse;

    @Before
    public void setUp() {
        try {
            when(vimeo.addVideo(any(File.class),
                                anyBoolean())).thenReturn("testVideEndPoint");

            when(vimeo.updateVideoMetadata(any(),
                                           any(),
                                           any(),
                                           any(),
                                           any(),
                                           any(),
                                           anyBoolean())).thenReturn(vimeoResponse);

            when(vimeo.getVideoInfo(anyString())).thenReturn(vimeoResponse);

            when(vimeo.removeVideo(anyString())).thenReturn(vimeoResponse);

            when(vimeoResponse.getStatusCode()).thenReturn(200);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testUploadVideo() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();

        File tempFile = File.createTempFile("testfile",
                                            ".tmp");
        tempFile.deleteOnExit();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("VideoFile",
                              tempFile);
        workItem.setParameter("Vimeo",
                              vimeo);

        UploadVideoWorkitemHandler handler = new UploadVideoWorkitemHandler("testAccessToken");

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }

    @Test
    public void testUpdateVideoMetadata() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("VideoEndpoint",
                              "testVideoEndpoint");
        workItem.setParameter("Vimeo",
                              vimeo);

        UpdateVideoMetadataWorkitemHandler handler = new UpdateVideoMetadataWorkitemHandler("testAccessToken");

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }

    @Test
    public void testGetVideoInfo() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("VideoEndpoint",
                              "testVideoEndpoint");
        workItem.setParameter("Vimeo",
                              vimeo);

        GetVideoInfoWorkitemHandler handler = new GetVideoInfoWorkitemHandler("testAccessToken");

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }

    @Test
    public void testDeleteVideo() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("VideoEndpoint",
                              "testVideoEndpoint");
        workItem.setParameter("Vimeo",
                              vimeo);

        DeleteVideoWorkitemHandler handler = new DeleteVideoWorkitemHandler("testAccessToken");

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }
}
