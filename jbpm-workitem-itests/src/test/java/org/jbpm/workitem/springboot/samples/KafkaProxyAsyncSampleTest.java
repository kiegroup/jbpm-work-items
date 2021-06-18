/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.workitem.springboot.samples;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jbpm.services.api.RuntimeDataService;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.query.QueryContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE;

@RunWith(Parameterized.class)
@SpringBootTest(classes = {JBPMApplication.class, TestAutoConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test-async.properties")
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
public class KafkaProxyAsyncSampleTest extends KafkaProxyBase {

    private static final String KEY_ASYNC = "PAM_Producer";
    private static final String VALUE_ASYNC = "That's no moon. It's a space station.";
    private static final Map<String, Object> VARIABLES_MAP = singletonMap("msg", VALUE_ASYNC);
    private static final String HELLO_PROCESS = "kafka-event-emitter.HelloWorld";
    private static final String KAFKA_WIH_PROCESS = "kafka-event-emitter.KafkaEventEmitter";
    protected static final String ASYNC_TEMPLATE_FILE = "src/test/resources/templates/kie-deployment-descriptor.async_template";
    
    @Autowired
    private ExecutorService executorService;
    
    @Autowired
    private RuntimeDataService runtimeDataService;
    
    @BeforeClass
    public static void beforeClass() {
        kafkaFixture.setTemplateFile(ASYNC_TEMPLATE_FILE);
    }
    
    @Before
    public void setup() throws IOException, InterruptedException {
        super.setup();
        executorService.init();
    }
    
    @After
    public void cleanup() {
        super.cleanup();
        executorService.clearAllErrors();
        executorService.clearAllRequests();
        executorService.destroy();
    }
    
    @Test(timeout = 30000)
    public void testAsyncKafkaWIH() throws Exception {
        countDownLatchEventListener.configureNode(KAFKA_WIH_PROCESS, "End", 2);
        
        Long processInstanceId= processService.startProcess(deploymentId, KAFKA_WIH_PROCESS, VARIABLES_MAP);
        assertTrue(processInstanceId > 0);
        
        //End is reached
        countDownLatchEventListener.getCountDown().await();
        
        kafkaFixture.assertConsumerMessages(proxyBootstrap, KEY_ASYNC, VALUE_ASYNC);
        
        assertRequestsAndProcesses(0, 1, 0);
    }

    @Test(timeout = 30000)
    public void testAsyncKafkaWIHConnectedLater() throws Exception {
        countDownLatchEventListener.configureNode(KAFKA_WIH_PROCESS, "End", 2);
        
        CountDownLatch latch = new CountDownLatch(1);
        
        kafkaProxy.setConnectionCut(true);
        Long processInstanceId= processService.startProcess(deploymentId, KAFKA_WIH_PROCESS, VARIABLES_MAP);
        
        assertTrue(processInstanceId > 0);
        
        //Kafka WIH will try during  publish the message but the broker is down during this first 10 seconds 
        latch.await(10, TimeUnit.SECONDS);
        assertEquals(1, runtimeDataService.getProcessInstances(singletonList(STATE_ACTIVE), null, null).size());
        
        //Reconnect after 10 seconds, Kafka Broker available
        kafkaProxy.setConnectionCut(false);
        //End is reached
        countDownLatchEventListener.getCountDown().await();
        
        kafkaFixture.assertConsumerMessages(proxyBootstrap, KEY_ASYNC, VALUE_ASYNC);
        
        assertRequestsAndProcesses(0, 1, 0);
    }
    
    @Test(timeout = 80000)
    public void testAsyncKafkaWIHNoConnection() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        kafkaProxy.setConnectionCut(true);
        //Kafka WIH will try during  publish config max.block.ms -60 seconds by default- to get connected to Kafka
        //TimeoutException: Topic PAM_Events not present in metadata after 60000 ms.
        Long processInstanceId= processService.startProcess(deploymentId, KAFKA_WIH_PROCESS, VARIABLES_MAP);
        
        assertTrue(processInstanceId > 0);
        
        try {
            startOtherProcess();
            
            // After more than 60 seconds, connection has not been reestablished and process keeps on active
            latch.await(65, TimeUnit.SECONDS);
            assertRequestsAndProcesses(1, 0, 1);
            
            startOtherProcess();
        } finally {
            processService.abortProcessInstance(processInstanceId);
        }
    }
    
    private void assertRequestsAndProcesses(int expectedError, int expectedCompleted, int expectedActive) {
        assertEquals(expectedError, executorService.getInErrorRequests(new QueryContext()).size());
        assertEquals(expectedCompleted, executorService.getCompletedRequests(new QueryContext()).size());
        assertEquals(expectedActive, runtimeDataService.getProcessInstances(singletonList(STATE_ACTIVE), null, null).size());
    }
    
    private void startOtherProcess() {
        //It's not hung during Kafka broker down
        Map<String, Object> outcome = processService.computeProcessOutcome(deploymentId, HELLO_PROCESS, singletonMap("name", "Grogu"));
        assertEquals("Hello Grogu", outcome.get("outcome"));
    }
}
