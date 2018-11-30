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
package org.jbpm.process.workitem.kafka;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.Producer;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.junit.Test;

import static org.junit.Assert.*;

public class KafkaWorkItemHandlerTest {

    @Test
    public void testSendMessage() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Topic",
                              "myTopic");
        workItem.setParameter("Key",
                              "1");
        workItem.setParameter("Value",
                              "Sample");

        Producer<Long, String> mockProducer = new MockProducer();
        KafkaWorkItemHandler handler = new KafkaWorkItemHandler(mockProducer);
        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
    }
}
