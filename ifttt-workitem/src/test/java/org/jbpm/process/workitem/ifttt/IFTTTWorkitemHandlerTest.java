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
package org.jbpm.process.workitem.ifttt;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.bpmn2.handler.WorkItemHandlerRuntimeException;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IFTTTWorkitemHandlerTest {

    @Mock
    Client client;

    @Mock
    WebTarget webTarget;

    @Mock
    Builder builder;

    @Before
    public void setUp() {

        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);
        when(builder.post(any(Entity.class),any(Class.class))).thenReturn(new String("testResponse"));
    }

    @Test
    public void testSendTriggerRequest() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Trigger",
                              "testTrigger");
        workItem.setParameter("Value1",
                              "testValue1");
        workItem.setParameter("Value2",
                              "testValue2");
        workItem.setParameter("Value3",
                              "testValue3");

        IFTTTWorkitemHandler handler = new IFTTTWorkitemHandler("testKey");
        handler.setClient(client);
        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        String testValueJSONString = "{\"value1\":\"testValue1\",\"value2\":\"testValue2\",\"value3\":\"testValue3\"}";
        assertEquals(testValueJSONString,
                     handler.getRequestBody());
    }

    @Test(expected = WorkItemHandlerRuntimeException.class)
    public void testSendTriggerRequestInvalidParams() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();

        WorkItemImpl workItem = new WorkItemImpl();

        IFTTTWorkitemHandler handler = new IFTTTWorkitemHandler("testKey");
        handler.setClient(client);
        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(0,
                     manager.getResults().size());
    }
}
