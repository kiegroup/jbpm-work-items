/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.jbpm.workitem.springboot.samples.KafkaFixture.KAFKA_PROCESS_ID;
import static org.jbpm.workitem.springboot.samples.KafkaFixture.KEY;
import static org.jbpm.workitem.springboot.samples.KafkaFixture.VALUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@SpringBootTest(classes = {JBPMApplication.class, TestAutoConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
public class KafkaProxySampleTest extends KafkaProxyBase {

    @Test(timeout = 240000)
    public void testKafkaWIHNoConnection() throws Exception {

        countDownLatchEventListener.configureNode(KAFKA_PROCESS_ID, "TaskErrorAfterKafkaMessageSent", 2);
        
        kafkaProxy.setConnectionCut(true);
        
        //Kafka WIH will try during  publish config max.block.ms -60 seconds by default- to get connected to Kafka
        //TimeoutException: Topic mytopic not present in metadata after 60000 ms.
        Long processInstanceId = processService.startProcess(deploymentId,
                                                             KAFKA_PROCESS_ID,
                                                             kafkaFixture.getProcessVariables());

        assertTrue(processInstanceId > 0);

        //Countdown decrements the count of the latch twice: 
        //TaskErrorAfterKafkaMessageSent node and before process ends
        countDownLatchEventListener.getCountDown().await();

        assertEquals("failure", (String)countDownLatchEventListener.getResult());
    }
    
    @Test(timeout = 60000)
    public void testKafkaWIHReconnect() throws Exception {

        countDownLatchEventListener.configure(KAFKA_PROCESS_ID, 1);
        
        kafkaProxy.setConnectionCut(true);
        
        reconnectProxyLater(10);
        
        Long processInstanceId = processService.startProcess(deploymentId,
                                                             KAFKA_PROCESS_ID,
                                                             kafkaFixture.getProcessVariables());

        assertTrue(processInstanceId > 0);

        kafkaFixture.assertConsumerMessages(proxyBootstrap, KEY, VALUE);

        //Countdown decrements the count of the latch before process ends
        countDownLatchEventListener.getCountDown().await();
        
        assertEquals("success", countDownLatchEventListener.getResult());
    }

}
