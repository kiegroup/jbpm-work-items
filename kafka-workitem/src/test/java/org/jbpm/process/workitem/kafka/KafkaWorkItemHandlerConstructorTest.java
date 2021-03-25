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

package org.jbpm.process.workitem.kafka;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Properties;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Test;
import org.kie.internal.runtime.manager.InternalRuntimeManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class KafkaWorkItemHandlerConstructorTest {

    @Test
    public void testNoParamsKafkaWIHConstructor() throws Exception {
        Constructor<KafkaWorkItemHandler> emptyConstructor = KafkaWorkItemHandler.class.getDeclaredConstructor();
        emptyConstructor.setAccessible(true);
        KafkaWorkItemHandler kafkaWIH = emptyConstructor.newInstance();

        assertKafkaConstructorParams(kafkaWIH);
    }
    
    @Test
    public void testClassLoaderKafkaWIHConstructor() throws Exception {
        Constructor<KafkaWorkItemHandler> emptyConstructor = KafkaWorkItemHandler.class.getDeclaredConstructor(ClassLoader.class);
        emptyConstructor.setAccessible(true);
        KafkaWorkItemHandler kafkaWIH = emptyConstructor.newInstance(this.getClass().getClassLoader());

        assertKafkaConstructorParams(kafkaWIH);
    }
    
    @Test
    public void testClassLoaderRuntimeKafkaWIHConstructor() throws Exception {
        Constructor<KafkaWorkItemHandler> emptyConstructor = KafkaWorkItemHandler.class.getDeclaredConstructor(ClassLoader.class, InternalRuntimeManager.class);
        emptyConstructor.setAccessible(true);
        KafkaWorkItemHandler kafkaWIH = emptyConstructor.newInstance(this.getClass().getClassLoader(), null);

        assertKafkaConstructorParams(kafkaWIH);
    }

    @Test
    public void testEmptyParamsKafkaWIHConstructor() throws Exception {
        Constructor<KafkaWorkItemHandler> emptyConstructor = KafkaWorkItemHandler.class.getDeclaredConstructor(String.class, String.class, String.class, String.class);
        emptyConstructor.setAccessible(true);
        KafkaWorkItemHandler kafkaWIH = emptyConstructor.newInstance("", "", "", "");

        assertKafkaConstructorParams(kafkaWIH);
    }
    
    @Test
    public void testNullParamsKafkaWIHConstructor() throws Exception {
        Constructor<KafkaWorkItemHandler> emptyConstructor = KafkaWorkItemHandler.class.getDeclaredConstructor(String.class, String.class, String.class, String.class);
        emptyConstructor.setAccessible(true);
        KafkaWorkItemHandler kafkaWIH = emptyConstructor.newInstance(null, null, null, null);

        assertKafkaConstructorParams(kafkaWIH);
    }

    private void assertKafkaConstructorParams(KafkaWorkItemHandler kafkaWIH) throws NoSuchFieldException, IllegalAccessException {
        Field properties = kafkaWIH.getClass().getDeclaredField("properties");
        properties.setAccessible(true);
        
        String defaultHost = getStaticField(kafkaWIH, "DEFAULT_HOST").toString();
        String defaultKafkaClientId = getStaticField(kafkaWIH, "DEFAULT_KAFKA_CLIENT_ID").toString();
        String defaultSerializer = getStaticField(kafkaWIH, "DEFAULT_SERIALIZER").toString();
        
        assertEquals(defaultHost, ((Properties)properties.get(kafkaWIH)).get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(defaultKafkaClientId, ((Properties)properties.get(kafkaWIH)).get(ProducerConfig.CLIENT_ID_CONFIG));
        assertEquals(defaultSerializer, ((Properties)properties.get(kafkaWIH)).get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(defaultSerializer, ((Properties)properties.get(kafkaWIH)).get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
    }
    
    private Object getStaticField(KafkaWorkItemHandler kafkaWIH, String field) throws NoSuchFieldException, IllegalAccessException {
        Field staticVar = kafkaWIH.getClass().getDeclaredField(field);
        staticVar.setAccessible(true);
        Object staticValue = staticVar.get(null);
        assertNotNull(staticValue);
        return staticValue;
    }
    
}
