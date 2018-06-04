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
package org.jbpm.process.workitem.google.translate;

import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.Translation;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.bpmn2.handler.WorkItemHandlerRuntimeException;
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
public class GoogleTranslateWorkitemHandlerTest extends AbstractBaseTest {

    @Mock
    GoogleTranslateAuth auth;

    @Mock
    Translate translationService;

    @Mock
    Detection detection;

    @Mock
    Translation translation;

    @Before
    public void setUp() {
        try {
            when(auth.getTranslationService(anyString())).thenReturn(translationService);
            when(translationService.detect(anyString())).thenReturn(detection);
            when(detection.getLanguage()).thenReturn("Serbian");
            when(translationService.translate(anyString(),
                                              any(TranslateOption.class),
                                              any(TranslateOption.class))).thenReturn(translation);
            when(translation.getTranslatedText()).thenReturn("Dobar dan");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testDetectLanguage() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ToDetectText",
                              "Dobar dan");

        DetectLanguageWorkitemHandler handler = new DetectLanguageWorkitemHandler("testApiKey");
        handler.setTranslationAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        String detectedLanguage = (String) (manager.getResults().get(workItem.getId())).get("DetectedLanguage");
        assertNotNull(detectedLanguage);
        assertEquals("Serbian",
                     detectedLanguage);
    }

    @Test(expected = WorkItemHandlerRuntimeException.class)
    public void testDetectLanguageInvalidParams() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        DetectLanguageWorkitemHandler handler = new DetectLanguageWorkitemHandler("testApiKey");
        handler.setTranslationAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(0,
                     manager.getResults().size());
    }

    @Test
    public void testTranslateText() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ToTranslate",
                              "Guten Tag");
        workItem.setParameter("SourceLang",
                              "de");
        workItem.setParameter("TargetLang",
                              "sr");

        TranslateWorkitemHandler handler = new TranslateWorkitemHandler("testApiKey");
        handler.setTranslationAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        String translation = (String) (manager.getResults().get(workItem.getId())).get("Translation");
        assertNotNull(translation);
        assertEquals("Dobar dan",
                     translation);
    }

    @Test(expected = WorkItemHandlerRuntimeException.class)
    public void testTranslateTextInvalidParams() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        TranslateWorkitemHandler handler = new TranslateWorkitemHandler("testApiKey");
        handler.setTranslationAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(0,
                     manager.getResults().size());
    }
}
