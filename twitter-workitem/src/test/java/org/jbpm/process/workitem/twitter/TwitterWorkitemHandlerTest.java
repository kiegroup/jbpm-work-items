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
package org.jbpm.process.workitem.twitter;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.bpmn2.handler.WorkItemHandlerRuntimeException;
import org.jbpm.document.service.impl.DocumentImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.jbpm.test.AbstractBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"jdk.internal.reflect.*"})
public class TwitterWorkitemHandlerTest extends AbstractBaseTest {

    @Mock
    Twitter twitter;

    @Mock
    TwitterAuth auth;

    @Mock
    Status status;

    @Mock
    DirectMessage directMessage;

    @Before
    public void setUp() {
        try {
            when(auth.getTwitterService(anyString(),
                                        anyString(),
                                        anyString(),
                                        anyString(),
                                        any(boolean.class))).thenReturn(twitter);
            when(twitter.updateStatus(any(StatusUpdate.class))).thenReturn(status);
            when(twitter.sendDirectMessage(anyString(),
                                           anyString())).thenReturn(directMessage);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testUpdateStatus() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("StatusUpdate",
                              "testUpdateStatus");

        UpdateStatusWorkitemHandler handler = new UpdateStatusWorkitemHandler("testConsumerKey",
                                                                              "testConsumerSecret",
                                                                              "testAccessKey",
                                                                              "testAccessSecret");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        StatusUpdate handlerStatusUpdate = handler.getStatusUpdate();
        assertNotNull(handlerStatusUpdate);
        assertEquals("testUpdateStatus",
                     handlerStatusUpdate.getStatus());
    }

    @Test(expected = WorkItemHandlerRuntimeException.class)
    public void testUpdateStatusInvalidParams() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        UpdateStatusWorkitemHandler handler = new UpdateStatusWorkitemHandler("testConsumerKey",
                                                                              "testConsumerSecret",
                                                                              "testAccessKey",
                                                                              "testAccessSecret");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(0,
                     manager.getResults().size());
    }

    @Test
    public void testUpdateStatusWithMedia() throws Exception {
        DocumentImpl testMediaDoc = new DocumentImpl();
        testMediaDoc.setContent(new String("Test media to send").getBytes());
        testMediaDoc.setName("testMediaToSend.txt");

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("StatusUpdate",
                              "testUpdateStatus");
        workItem.setParameter("Media",
                              testMediaDoc);

        UpdateStatusWorkitemHandler handler = new UpdateStatusWorkitemHandler("testConsumerKey",
                                                                              "testConsumerSecret",
                                                                              "testAccessKey",
                                                                              "testAccessSecret");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        StatusUpdate handlerStatusUpdate = handler.getStatusUpdate();
        assertNotNull(handlerStatusUpdate);
        assertEquals("testUpdateStatus",
                     handlerStatusUpdate.getStatus());
    }

    @Test
    public void testSendDirectMessage() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Message",
                              "hello there");
        workItem.setParameter("ScreenName",
                              "testScreenName");

        SendDirectMessageWorkitemHandler handler = new SendDirectMessageWorkitemHandler("testConsumerKey",
                                                                                        "testConsumerSecret",
                                                                                        "testAccessKey",
                                                                                        "testAccessSecret");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }

    @Test(expected = WorkItemHandlerRuntimeException.class)
    public void testSendDirectMessageInvalidParams() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        SendDirectMessageWorkitemHandler handler = new SendDirectMessageWorkitemHandler("testConsumerKey",
                                                                                        "testConsumerSecret",
                                                                                        "testAccessKey",
                                                                                        "testAccessSecret");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(0,
                     manager.getResults().size());
    }
}
