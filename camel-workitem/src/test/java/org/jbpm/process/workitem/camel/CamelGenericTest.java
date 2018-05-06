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
package org.jbpm.process.workitem.camel;

import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.impl.DefaultWorkItemManager;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.test.AbstractBaseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.process.WorkItemManager;

public class CamelGenericTest extends AbstractBaseTest {

    private static boolean called;

    public void testMethod() {
        called = true;
    }

    @Before
    public void setup() {
        called = false;
    }

    @Test
    public void testClass() {
        GenericCamelWorkitemHandler handler = new GenericCamelWorkitemHandler("class",
                                                                              "FQCN");

        final WorkItem workItem = new WorkItemImpl();
        workItem.setParameter("FQCN",
                              getClass().getCanonicalName());
        workItem.setParameter("method",
                              "testMethod");

        WorkItemManager manager = new DefaultWorkItemManager(null);
        handler.executeWorkItem(workItem,
                                manager);

        Assert.assertTrue(called);
    }
}
