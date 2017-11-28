/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.workitem.google.mail;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.document.service.impl.DocumentImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.jbpm.test.AbstractBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class GoogleMailWorkitemHandlerTest extends AbstractBaseTest {

    @Mock
    GoogleMailAuth auth;

    @Mock
    Gmail gmailService;

    @Mock
    Gmail.Users gmailUsers;

    @Mock
    Gmail.Users.Messages gmailUserMessages;

    @Mock
    Gmail.Users.Messages.Send gmailUserMessagesSend;

    @Before
    public void setUp() {
        try {
            when(auth.getGmailService(anyString(),
                                      anyString())).thenReturn(gmailService);
            when(gmailService.users()).thenReturn(gmailUsers);
            when(gmailUsers.messages()).thenReturn(gmailUserMessages);
            when(gmailUserMessages.send(anyString(),
                                        anyObject())).thenReturn(gmailUserMessagesSend);
            when(gmailUserMessagesSend.execute()).thenReturn(new Message());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSendEmailWithAttachment() throws Exception {

        DocumentImpl attachmentDoc = new DocumentImpl();
        attachmentDoc.setContent(new String("Attachment sources").getBytes());
        attachmentDoc.setName("attachmentFileName.txt");

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("To",
                              "someone@gmail.com");
        workItem.setParameter("From",
                              "me@gmail.com");
        workItem.setParameter("Subject",
                              "Hello!");
        workItem.setParameter("BodyText",
                              "Hello from me!");
        workItem.setParameter("Attachment",
                              attachmentDoc);

        SendMailWorkitemHandler handler = new SendMailWorkitemHandler("myAppName", "{}");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }
}
