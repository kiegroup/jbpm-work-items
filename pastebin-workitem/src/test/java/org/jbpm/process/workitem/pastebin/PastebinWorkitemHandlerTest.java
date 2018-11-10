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
package org.jbpm.process.workitem.pastebin;

import java.net.URL;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.document.service.impl.DocumentImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.jbpm.test.AbstractBaseTest;
import org.jpastebin.pastebin.PastebinLink;
import org.jpastebin.pastebin.PastebinPaste;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PastebinWorkitemHandlerTest extends AbstractBaseTest {

    @Mock
    PastebinPaste pastebin;

    @Mock
    PastebinLink pastebinLink;

    @Before
    public void setUp() throws Exception {
        when(pastebin.paste()).thenReturn(pastebinLink);
        when(pastebinLink.getPaste()).thenReturn(pastebin);
    }

    @Test
    public void testCreateNewPasteDocContent() throws Exception {
        URL testURL = new URL("http://testpasteurl");
        when(pastebinLink.getLink()).thenReturn(testURL);

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Title",
                              "test Title");
        workItem.setParameter("Format",
                              "text");
        workItem.setParameter("Visibility",
                              "0");
        workItem.setParameter("Author",
                              "testAuthor");
        workItem.setParameter("Pastebin",
                              pastebin);

        DocumentImpl testPasteDoc = new DocumentImpl();
        testPasteDoc.setContent(new String("Test paste to send").getBytes());
        testPasteDoc.setName("testPaste.txt");

        workItem.setParameter("Content",
                              testPasteDoc);

        CreatePastebinWorkitemHandler handler = new CreatePastebinWorkitemHandler("testDevKey");

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("PasteURL") instanceof URL);

        URL testPasteURL = (URL) manager.getResults().get(workItem.getId()).get("PasteURL");
        assertNotNull(testPasteURL);
        assertEquals("http://testpasteurl",
                     testPasteURL.toString());
    }

    @Test
    public void testCreateNewPasteStringContent() throws Exception {
        URL testURL = new URL("http://testpasteurl");
        when(pastebinLink.getLink()).thenReturn(testURL);

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Title",
                              "test Title");
        workItem.setParameter("Format",
                              "text");
        workItem.setParameter("Visibility",
                              "0");
        workItem.setParameter("Author",
                              "testAuthor");
        workItem.setParameter("Pastebin",
                              pastebin);

        workItem.setParameter("Content",
                              "Test paste to send");

        CreatePastebinWorkitemHandler handler = new CreatePastebinWorkitemHandler("testDevKey");

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("PasteURL") instanceof URL);

        URL testPasteURL = (URL) manager.getResults().get(workItem.getId()).get("PasteURL");
        assertNotNull(testPasteURL);
        assertEquals("http://testpasteurl",
                     testPasteURL.toString());
    }

    @Test
    public void testGetExistingPasteContent() throws Exception {
        doNothing().when(pastebinLink).fetchContent();
        when(pastebin.getContents()).thenReturn("test paste content");

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("PastebinKey",
                              "testKey");
        workItem.setParameter("PastebinLink",
                              pastebinLink);

        workItem.setParameter("Content",
                              "Test paste to send");

        GetExistingPastebinWorkitemHandler handler = new GetExistingPastebinWorkitemHandler("testDevKey");

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("PasteContent") instanceof String);

        String testPasteContent = (String) manager.getResults().get(workItem.getId()).get("PasteContent");
        assertNotNull(testPasteContent);
        assertEquals("test paste content",
                     testPasteContent);
    }
}
