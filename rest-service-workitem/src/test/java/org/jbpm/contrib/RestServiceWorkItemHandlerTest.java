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
package org.jbpm.contrib;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.contrib.restservice.RestServiceWorkItemHandler;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.jbpm.test.AbstractBaseTest;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

public class RestServiceWorkItemHandlerTest extends AbstractBaseTest {

    @Test @Ignore
    public void testHandler() throws Exception {
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("SampleParam", "testParamValue");
        workItem.setParameter("SampleParamTwo", "testParamValue");

        TestWorkItemManager manager = new TestWorkItemManager();

        org.jbpm.contrib.restservice.RestServiceWorkItemHandler handler = new RestServiceWorkItemHandler();
        handler.setLogThrownException(true);
        handler.executeWorkItem(workItem, manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }
}
