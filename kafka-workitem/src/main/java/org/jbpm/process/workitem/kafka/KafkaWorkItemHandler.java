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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.executor.impl.wih.AsyncWorkItemHandlerCmdCallback;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.executor.Command;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "KafkaWorkItem.wid", name = "KafkaPublishMessages",
        displayName = "KafkaPublishMessages",
        defaultHandler = "mvel: new org.jbpm.process.workitem.kafka.KafkaWorkItemHandler(\"bootstrapServers\", \"clientId\", \"keySerializerClass\", \"valueSerializerClass\")",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "KafkaPublishMessages.png",
        parameters = {
                @WidParameter(name = "Topic", required = true),
                @WidParameter(name = "Key", required = true),
                @WidParameter(name = "Value", required = true)
        },
        results = {
                @WidResult(name = "Result")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "kafka,publish,message,topic",
                action = @WidAction(title = "Publish message to a kafka topic"),
                authinfo = @WidAuth(required = true, params = {"bootstrapServers", "clientId", "keySerializerClass", "valueSerializerClass"},
                paramsdescription = {"Bootstrap Servers", "Client ID", "Key Serializer class", "Value Serializer class"},
                referencesite = "https://red.ht/kafka-wih-params")
        ))

public class KafkaWorkItemHandler extends AbstractLogOrThrowWorkItemHandler implements Cacheable {
    private static String DEFAULT_HOST = "localhost:9092";
    private static String DEFAULT_KAFKA_CLIENT_ID = "jBPM-Kafka-PublishMessage";
    private static String DEFAULT_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";

    private static String PROPERTY_PREFIX = "org.jbpm.process.workitem.kafka.";
    private static String GLOBAL_RECONNECT_BACKOFF_MAX_MS = PROPERTY_PREFIX + CommonClientConfigs.RECONNECT_BACKOFF_MAX_MS_CONFIG; //reconnect.backoff.max.ms
    private static String GLOBAL_RECONNECT_BACKOFF_MS = PROPERTY_PREFIX + CommonClientConfigs.RECONNECT_BACKOFF_MS_CONFIG; //reconnect.backoff.ms
    private static String GLOBAL_REQUEST_TIMEOUT_MS = PROPERTY_PREFIX + CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG; // request.timeout.ms 
    private static String GLOBAL_RETRIES = PROPERTY_PREFIX + CommonClientConfigs.RETRIES_CONFIG; // retries
    private static String GLOBAL_RETRY_BACKOFF_MS = PROPERTY_PREFIX + CommonClientConfigs.RETRY_BACKOFF_MS_CONFIG; // retry.backoff.ms
    private static String GLOBAL_ENABLE_IDEMPOTENCE = PROPERTY_PREFIX + ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG; // enable.idempotence


    private static final Logger LOG = LoggerFactory.getLogger(KafkaWorkItemHandler.class);
    private static Map<Properties, Producer> producers = new ConcurrentHashMap<Properties, Producer>();

    private ExecutorService executorService;
    private Properties properties;

    private static final String RESULTS_VALUE = "Result";

    
    public KafkaWorkItemHandler(Properties properties, Producer producer) {
        this.properties = properties;
        producers.put(properties, producer);
    }

    public KafkaWorkItemHandler( ) {
        this(DEFAULT_HOST, 
             DEFAULT_KAFKA_CLIENT_ID, 
             DEFAULT_SERIALIZER, 
             DEFAULT_SERIALIZER);
    }

    public KafkaWorkItemHandler(ClassLoader classLoader) {
        this(DEFAULT_HOST, 
             DEFAULT_KAFKA_CLIENT_ID, 
             DEFAULT_SERIALIZER, 
             DEFAULT_SERIALIZER,
             classLoader);
    }

    public KafkaWorkItemHandler(ClassLoader classLoader, InternalRuntimeManager runtimeManager) {
        this(DEFAULT_HOST, 
             DEFAULT_KAFKA_CLIENT_ID, 
             DEFAULT_SERIALIZER, 
             DEFAULT_SERIALIZER,
             classLoader,
             runtimeManager);
    }

    public KafkaWorkItemHandler(String bootstrapServers,
                                String clientId,
                                String keySerializerClass,
                                String valueSerializerClass) {

        this(bootstrapServers, clientId, keySerializerClass, valueSerializerClass, KafkaProducer.class.getClassLoader());
    }

    public KafkaWorkItemHandler(String bootstrapServers,
                                String clientId,
                                String keySerializerClass,
                                String valueSerializerClass,
                                ClassLoader classLoader) {
        this(bootstrapServers, clientId, keySerializerClass, valueSerializerClass, classLoader, null);
    }
    public KafkaWorkItemHandler(String bootstrapServers,
                                String clientId,
                                String keySerializerClass,
                                String valueSerializerClass,
                                ClassLoader classLoader,
                                InternalRuntimeManager runtimeManager) {


        this.properties = new Properties();
        this.properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, !isEmpty(bootstrapServers) ? bootstrapServers : DEFAULT_HOST);
        this.properties.put(ProducerConfig.CLIENT_ID_CONFIG, !isEmpty(clientId) ? clientId : DEFAULT_KAFKA_CLIENT_ID);
        this.properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, !isEmpty(keySerializerClass) ? keySerializerClass : DEFAULT_SERIALIZER);
        this.properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,  !isEmpty(valueSerializerClass) ? valueSerializerClass : DEFAULT_SERIALIZER);

        // global variables
        String reconnectBackoffMaxMs = System.getProperty(GLOBAL_RECONNECT_BACKOFF_MAX_MS);
        if(reconnectBackoffMaxMs != null) {
            this.properties.put(CommonClientConfigs.RECONNECT_BACKOFF_MAX_MS_CONFIG, reconnectBackoffMaxMs);
        }
        String reconectBackOffMs = System.getProperty(GLOBAL_RECONNECT_BACKOFF_MS);
        if(reconectBackOffMs != null) {
            this.properties.put(CommonClientConfigs.RECONNECT_BACKOFF_MS_CONFIG, reconectBackOffMs);
        }
        String requestTimeoutMs = System.getProperty(GLOBAL_REQUEST_TIMEOUT_MS);
        if(requestTimeoutMs != null) {
            this.properties.put(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);
        }
        String retries = System.getProperty(GLOBAL_RETRIES);
        if(retries != null) {
            this.properties.put(CommonClientConfigs.RETRIES_CONFIG, retries);
        }
        String retryBackOffMs = System.getProperty(GLOBAL_RETRY_BACKOFF_MS);
        if(retryBackOffMs != null) {
            this.properties.put(CommonClientConfigs.RETRY_BACKOFF_MS_CONFIG, retryBackOffMs);
        }
        String enableIdempotence = System.getProperty(GLOBAL_ENABLE_IDEMPOTENCE);
        if(enableIdempotence != null) {
            this.properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);
        }

        if(runtimeManager != null) {
            this.executorService = (ExecutorService) runtimeManager.getEnvironment().getEnvironment().get("ExecutorService");
            LOG.info("Kafka WorkItem Handler Producer created with async {}", properties);
        } else {
            LOG.info("Kafka WorkItem Handler Producer created with sync for {}", properties);
        }

        // it is needed to change the classloader to KIEURLClassLoader for dependencies to be resolved and then, set it back

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
          Thread.currentThread().setContextClassLoader(classLoader);
          producers.computeIfAbsent(properties, (config) -> new KafkaProducer(config));
        } finally {
          Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public boolean isEmpty(String val) {
        return val == null || val.isEmpty();
    }

    public static class KafkaWorkItemHandlerProducerCommand implements Command {
        @Override
        public ExecutionResults execute(CommandContext ctx) throws Exception {
            String topic = (String) ctx.getData().get("topic");
            Object key = ctx.getData().get("key");
            Object value = ctx.getData().get("value");
            Properties properties = (Properties) ctx.getData().get("producerProperties");
            LOG.debug("Kafka WorkItem Handler {} about to send to topic {} key {} and value {}", properties, topic, key, value);
            producers.get(properties).send(new ProducerRecord(topic, key, value)).get();
            LOG.debug("Kafka WorkItem Handler {} sent to topic {} key {} and value {}", properties, topic, key, value);
            ExecutionResults results = new ExecutionResults();
            results.setData(RESULTS_VALUE, "success");
            return results;
        }
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager manager) {

        try {
            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String topic = (String) workItem.getParameter("Topic");
            Object key = workItem.getParameter("Key");
            Object value = workItem.getParameter("Value");

            // check whether is async or not
            if(this.executorService == null || !this.executorService.isActive()) {
                producers.get(this.properties).send(new ProducerRecord(topic, 
                                                 key, 
                                                 value))
                        .get();
                Map<String, Object> results = new HashMap<String, Object>();
                results.put(RESULTS_VALUE, "success");
                manager.completeWorkItem(workItem.getId(), results);
            } else {
                CommandContext ctxCMD = new CommandContext();
                ctxCMD.setData("workItem", workItem);
                ctxCMD.setData("processInstanceId", getProcessInstanceId(workItem));
                ctxCMD.setData("deploymentId", ((WorkItemImpl)workItem).getDeploymentId());
                ctxCMD.setData("callbacks", AsyncWorkItemHandlerCmdCallback.class.getName());
                ctxCMD.setData("topic", topic);
                ctxCMD.setData("key", key);
                ctxCMD.setData("value", value);
                ctxCMD.setData("producerProperties", this.properties);

                Long requestId = executorService.scheduleRequest(KafkaWorkItemHandlerProducerCommand.class.getName(), ctxCMD);
                LOG.debug("Request Kafka producer successfully with id {}", requestId);

            }

        } catch (Exception exp) {
            LOG.error("Handler error", exp);
            handleException(exp);
        }
    }

    public void abortWorkItem(WorkItem workItem,
                              WorkItemManager manager) {

    }

    @Override
    public void close() {
        if (producers != null && producers.containsKey(this.properties)) {
            Producer producer = producers.remove(this.properties);
            producer.flush();
            producer.close();
        }
    }

    protected long getProcessInstanceId(WorkItem workItem) {
        return ((WorkItemImpl) workItem).getProcessInstanceId();
    }
}
