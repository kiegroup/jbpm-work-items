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
package org.jbpm.process.workitem.google.sheets;

import java.util.ArrayList;
import java.util.List;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
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
public class GoogleSheetsWorkitemHandlerTest extends AbstractBaseTest {

    @Mock
    GoogleSheetsAuth auth;

    @Mock
    Sheets sheetsClient;

    @Mock
    Sheets.Spreadsheets spreadsheets;

    @Mock
    Sheets.Spreadsheets.Values spreasheetsValues;

    @Mock
    Sheets.Spreadsheets.Values.Get spreasheetsValuesGet;

    @Before
    public void setUp() {
        try {
            ValueRange valueRange = new ValueRange();
            List<List<Object>> testValues = new ArrayList<>();
            List<Object> testRowValues = new ArrayList<>();
            testRowValues.add("testValueOne");
            testRowValues.add("testValueTwo");
            testValues.add(testRowValues);
            valueRange.setValues(testValues);

            when(auth.getSheetsService(anyString(),
                                       anyString())).thenReturn(sheetsClient);
            when(sheetsClient.spreadsheets()).thenReturn(spreadsheets);
            when(spreadsheets.values()).thenReturn(spreasheetsValues);
            when(spreasheetsValues.get(anyString(),
                                       anyString())).thenReturn(spreasheetsValuesGet);
            when(spreasheetsValuesGet.execute()).thenReturn(valueRange);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testReadSheetValues() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("SheetId",
                              "testSheetId");
        workItem.setParameter("Range",
                              "Class Data!A2:E");

        ReadSheetValuesWorkitemHandler handler = new ReadSheetValuesWorkitemHandler("testAppName",
                                                                                    "{}");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("SheetValues") instanceof List);

        List<List<Object>> returnValues = (List<List<Object>>) (manager.getResults().get(workItem.getId())).get("SheetValues");
        assertNotNull(returnValues);
        assertEquals("testValueOne",
                     returnValues.get(0).get(0));
        assertEquals("testValueTwo",
                     returnValues.get(0).get(1));
    }

    @Test(expected = WorkItemHandlerRuntimeException.class)
    public void testReadSheetValuesInvalidParams() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        ReadSheetValuesWorkitemHandler handler = new ReadSheetValuesWorkitemHandler("testAppName",
                                                                                    "{}");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(0,
                     manager.getResults().size());
    }
}
