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

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.bpmn2.handler.WorkItemHandlerRuntimeException;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kie.api.executor.Executor;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KafkaWorkItemHandlerTest {

    private static final String TOPIC_FIELD = "Topic";
    private static final String KEY_FIELD = "Key";
    private static final String VALUE_FIELD = "Value";
    
    private static final String TOPIC = "myTopic";
    private static final String KEY = "1";
    private static final String VALUE = "Sample";

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    private TestWorkItemManager manager;
    private WorkItemImpl workItem;
    private KafkaWorkItemHandler handler;
    private MockProducer<String, String> mockProducerString;
    private MockProducer<Integer, Integer> mockProducerInteger;

    @Before
    public void init() {
        manager = new TestWorkItemManager();
        initWorkItem();
        buildKafkaWIH(true);
    }

    @After
    public void cleanup() {
        if (mockProducerString != null)
            mockProducerString.close();
        if (mockProducerInteger != null)
            mockProducerInteger.close();
    }
    
    @Test
    public void testSendMessage() throws Exception {
        assertResultSuccessAfterExecuteWorkItem();
    }

    @Test
    public void testSendMessageIntegerSerializer() throws Exception {
        buildKafkaWIHInteger(true);
        workItem.setParameter(KEY_FIELD, 2);
        workItem.setParameter(VALUE_FIELD, 2);

        assertResultSuccessAfterExecuteWorkItem();
    }

    @Test
    public void testMissingRequiredParams() throws Exception {
        WorkItemImpl emptyWorkItem = new WorkItemImpl();

        assertExceptionAfterExecuteWorkItem(emptyWorkItem);
    }

    @Test
    public void testNullParam() throws Exception {
        workItem.setParameter(VALUE_FIELD, null);

        assertExceptionAfterExecuteWorkItem(workItem);
    }
    
    @Test
    public void testWrongTypeKey() throws Exception {
        buildKafkaWIHInteger(true);
        
        assertExceptionAfterExecuteWorkItem(workItem);
    }
    
    @Test
    public void testWrongTypeValue() throws Exception {
        buildKafkaWIHInteger(true);

        assertExceptionAfterExecuteWorkItem(workItem);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTimeoutException() throws Exception{
        Producer<String, String> mockProducerKafka = mock(KafkaProducer.class);
        Future<RecordMetadata> future = mock(Future.class);

        when(mockProducerKafka.send(any())).thenReturn(future);
        when(future.get()).thenThrow(new TimeoutException("timeout"));

        handler = new KafkaWorkItemHandler(new Properties(), mockProducerKafka);

        assertExceptionAfterExecuteWorkItem(workItem);
    }
    
    @Test
    public void testExceptionHandlingDuringSend() throws Exception{
        buildKafkaWIH(false);

        sendErrorLater(100);
        
        assertExceptionAfterExecuteWorkItem(workItem);
    }
    
    @Test
    public void testExceptionHandlingWhenClosed() throws Exception{
        buildKafkaWIH(true);

        mockProducerString.close();

        assertExceptionAfterExecuteWorkItem(workItem);
    }

    @Test
    public void testExceptionHandlingWhenFenced() throws Exception{
        buildKafkaWIH(true);

        mockProducerString.initTransactions();
        mockProducerString.fenceProducer();

        assertExceptionAfterExecuteWorkItem(workItem);
    }

    private void initWorkItem() {
        workItem = new WorkItemImpl();
        workItem.setParameter(TOPIC_FIELD, TOPIC);
        workItem.setParameter(KEY_FIELD, KEY);
        workItem.setParameter(VALUE_FIELD, VALUE);
    }

    private void buildKafkaWIH(boolean autocomplete) {
        mockProducerString = new MockProducer<>(autocomplete, 
                                                new StringSerializer(), 
                                                new StringSerializer());
        handler = new KafkaWorkItemHandler(new Properties(), mockProducerString);
    }
    
    private void buildKafkaWIHInteger(boolean autocomplete) {
        //MockProducer only invokes serialize if partition was defined -needed for cast exceptions during incorrect type tests
        PartitionInfo partitionInfo = new PartitionInfo(TOPIC, 0, null, null, null);
        Cluster cluster = new Cluster(null, emptyList(), asList(partitionInfo),
                                      emptySet(), emptySet());
        mockProducerInteger = new MockProducer<>(cluster, 
                                                 autocomplete, 
                                                 new DefaultPartitioner(), 
                                                 new IntegerSerializer(), 
                                                 new IntegerSerializer());
        
        handler = new KafkaWorkItemHandler(new Properties(), mockProducerInteger);
    }
    
    private void assertResultSuccessAfterExecuteWorkItem() {
        handler.executeWorkItem(workItem, manager);

        assertWorkItemResults(1);
        assertEquals("success", manager.getResults().entrySet().stream()
                                 .map(Map.Entry::getValue)
                                 .findFirst()
                                 .get()
                                 .get("Result"));
    }

    private void assertExceptionAfterExecuteWorkItem(WorkItemImpl workItem) {
        exception.expect(WorkItemHandlerRuntimeException.class);
        handler.executeWorkItem(workItem, manager);
        assertWorkItemResults(0);
    }
    
    private void assertWorkItemResults(int expectedResultItems) {
        assertNotNull(manager.getResults());
        assertEquals(expectedResultItems, manager.getResults().size());
    }
    
    private void sendErrorLater(int time) {
        new Thread(() -> {
            CountDownLatch lock = new CountDownLatch(1);
            try {
                lock.await(time, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
            }
            mockProducerString.errorNext(new RuntimeException("Error during send"));
        }).start();
    }
}
