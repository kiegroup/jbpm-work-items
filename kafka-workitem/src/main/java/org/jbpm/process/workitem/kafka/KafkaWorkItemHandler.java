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

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
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
                referencesite = "https://kafka.apache.org/10/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html")
        ))

public class KafkaWorkItemHandler extends AbstractLogOrThrowWorkItemHandler implements Cacheable {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaWorkItemHandler.class);

    private Producer<String, String> producer;
    private static final String RESULTS_VALUE = "Result";
    
    public KafkaWorkItemHandler(Producer producer) {
        this.producer = producer;
    }

    public KafkaWorkItemHandler(String bootstrapServers,
                                String clientId,
                                String keySerializerClass,
                                String valueSerializerClass,
                                ClassLoader classLoader) {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                   bootstrapServers);
        config.put(ProducerConfig.CLIENT_ID_CONFIG,
                   clientId);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                   keySerializerClass);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                   valueSerializerClass);
        // it is needed to change the classloader to KIEURLClassLoader for dependencies to be resolved and then, set it back
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
          Thread.currentThread().setContextClassLoader(classLoader);
          producer = new KafkaProducer<String, String>(config);
        } finally {
          Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public KafkaWorkItemHandler(String bootstrapServers,
                                String clientId,
                                String keySerializerClass,
                                String valueSerializerClass) {

        this(bootstrapServers, clientId, keySerializerClass, valueSerializerClass, KafkaProducer.class.getClassLoader());
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager manager) {

        try {
            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            Map<String, Object> results = new HashMap<String, Object>();
            String topic = (String) workItem.getParameter("Topic");
            Object key = workItem.getParameter("Key");
            Object value = workItem.getParameter("Value");

            producer.send(new ProducerRecord(topic, 
                                             key, 
                                             value))
                    .get();

            results.put(RESULTS_VALUE, "success");
            manager.completeWorkItem(workItem.getId(), results);
        } catch (Exception exp) {
            LOG.error("Handler error",
                      exp);
            handleException(exp);
        }
    }

    public void abortWorkItem(WorkItem workItem,
                              WorkItemManager manager) {

    }

    @Override
    public void close() {
        if (producer != null) {
            producer.flush();
            producer.close();
        }
    }
}
