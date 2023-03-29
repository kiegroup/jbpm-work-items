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
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.serialization.StringSerializer;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.workitem.springboot.samples.events.listeners.CountDownLatchEventListener;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.utility.DockerImageName;

import io.strimzi.test.container.StrimziKafkaContainer;

import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.jbpm.workitem.springboot.samples.KafkaFixture.KAFKA_RESULT;
import static org.jbpm.workitem.springboot.samples.KafkaFixture.TOXIPROXY_IMAGE;

public abstract class KafkaProxyBase extends KafkaBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProxyBase.class);
    
    protected static final int TOXY_PROXY_PORT = Integer.parseInt(System.getProperty("toxiproxy.port"));
    
    private static final String VERSION = String.join(".", Arrays.copyOfRange(System.getProperty("kafka.container.version", "3.1.0").split("\\."), 0, 3));
    
    @ClassRule
    public static final SpringClassRule scr = new SpringClassRule();
 
    @Rule
    public final SpringMethodRule smr = new SpringMethodRule();

    @Rule
    public Network network = Network.newNetwork();

    @Rule
    public StrimziKafkaContainer kafka = new StrimziKafkaContainer()
                                                .withKafkaVersion(VERSION)
                                                .withNetwork(network);

    @Rule
    public ToxiproxyContainer toxiproxy  = new ToxiproxyContainer(DockerImageName.parse(TOXIPROXY_IMAGE))
                                                 .withNetwork(network);

    @Autowired
    private DeploymentService deploymentService;
    
    @Autowired
    protected ProcessService processService;
    
    @Autowired
    CountDownLatchEventListener countDownLatchEventListener;
    
    protected static KafkaFixture kafkaFixture = new KafkaFixture();
    
    protected String deploymentId;
    
    protected String proxyBootstrap;

    protected ToxiproxyContainer.ContainerProxy kafkaProxy;
    
    @BeforeClass
    public static void generalSetup() {
        kafkaFixture.generalSetup();
    }
    
    @Before
    public void setup() throws IOException, InterruptedException {
        toxiproxy.start();
        kafkaProxy = toxiproxy.getProxy(kafka, TOXY_PROXY_PORT);
        kafka.start();
        kafkaFixture.createTopic(kafka);
        proxyBootstrap = kafkaProxy.getContainerIpAddress()+":"+kafkaProxy.getProxyPort();
        
        System.setProperty(BOOTSTRAP_SERVERS_CONFIG, proxyBootstrap);
        System.setProperty(CLIENT_ID_CONFIG, "test_jbpm");
        System.setProperty(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        System.setProperty(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        deploymentId = kafkaFixture.setup(deploymentService, strategy);
        
        countDownLatchEventListener.setVariable(KAFKA_RESULT);
    }

    @After
    public void cleanup() {
        kafka.stop();
        toxiproxy.stop();
        kafkaFixture.cleanup(deploymentService);
    }

    protected void reconnectProxyLater(int reconnectTime) {
        new Thread(() -> {
            CountDownLatch lock = new CountDownLatch(1);
            try {
                lock.await(reconnectTime, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            kafkaProxy.setConnectionCut(false);
        }).start();
    }
}
