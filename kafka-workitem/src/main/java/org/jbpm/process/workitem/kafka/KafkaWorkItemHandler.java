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
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Wid(widfile = "Kafka-workitem.wid", name = "KafkaPublishMessages",
     displayName = "KafkaWorkItemDefinitions",
     defaultHandler = "mvel: new org.jbpm.process.workitem.kafka.KafkaWorkItemHandler()",
     documentation = "${artifactId}/index.html",
     parameters = {
                   @WidParameter(name = "BootstrapServers", required = true),
                   @WidParameter(name = "ClientId", required = true),
                   @WidParameter(name = "KeySerializerClass", required = true),
                   @WidParameter(name = "ValueSerializerClass", required = true),
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
                               keywords = "kafka",
                               action = @WidAction(title = "Publish message to a kafka topic")
     ))

public class KafkaWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaWorkItemHandler.class);

    private Producer<Long, String> producer;

    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

        Map<String, Object> results = new HashMap<String, Object>();

        String bootstrapServers = (String) workItem.getParameter("BootstrapServers");
        String client_id = (String) workItem.getParameter("ClientId");
        String keySerializerClass = (String) workItem.getParameter("KeySerializerClass");
        String valueSerializerClass = (String) workItem.getParameter("ValueSerializerClass");
        String topic = (String) workItem.getParameter("Topic");
        String key = (String) workItem.getParameter("Key");
        String value = (String) workItem.getParameter("Value");


        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.CLIENT_ID_CONFIG, client_id);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializerClass);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializerClass);

        if (producer == null) {
            producer = createProducer(config);
        }
        try {

            producer.send(new ProducerRecord(topic, key, value));
            results.put("Result", "success");
            manager.completeWorkItem(workItem.getId(), results);
        } catch (Exception e) {
            LOG.error("Kafka error", e);
            producer.flush();
            producer.close();
            results.put("Result", "failure");
            handleException(e);
        }

    }

    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

    }

    private static Producer<Long, String> createProducer(Properties config) {
        return new KafkaProducer<Long, String>(config);
    }

    public Producer<Long, String> getProducer() {
        return producer;
    }

    public void setProducer(Producer<Long, String> producer) {
        this.producer = producer;
    }

}
