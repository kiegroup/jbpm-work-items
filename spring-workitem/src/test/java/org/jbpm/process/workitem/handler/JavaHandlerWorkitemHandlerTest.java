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

package org.jbpm.process.workitem.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.drools.compiler.compiler.ProcessBuilderFactory;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.core.runtime.process.ProcessRuntimeFactory;
import org.jbpm.process.builder.ProcessBuilderFactoryServiceImpl;
import org.jbpm.process.instance.ProcessRuntimeFactoryServiceImpl;
import org.jbpm.process.workitem.config.CustomConfig;
import org.jbpm.process.workitem.handler.JavaHandlerWorkitemHandlerTest.TestApplicationConfiguration;
import org.jbpm.test.AbstractBaseTest;
import org.jbpm.workflow.instance.WorkflowRuntimeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApplicationConfiguration.class})
@Configuration
public class JavaHandlerWorkitemHandlerTest extends AbstractBaseTest {

    @Autowired
    ApplicationContext applicationContext;

    @Test
    public void contextLoadsCorrectly() {
        assertThat(applicationContext, is(notNullValue()));
        CustomConfig bean = applicationContext.getBean(CustomConfig.class);
        assertThat(bean.getName(), is("John Doe"));
    }

    @Test(expected = WorkflowRuntimeException.class)
    public void testHandler() throws Exception {
        KieBase kbase = readKnowledgeBase();
        KieSession ksession = createSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Handler",
                                                              new JavaHandlerWorkItemHandler(ksession));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employeeId",
                   "12345-ABC");
        // process will fail due to NPE in org.jbpm.process.workitem.handler.RecordHandler.execute
        ksession.startProcess("com.sample.bpmn.java",
                              params);
    }

    private static KieBase readKnowledgeBase() throws Exception {
        ProcessBuilderFactory.setProcessBuilderFactoryService(new ProcessBuilderFactoryServiceImpl());
        ProcessRuntimeFactory.setProcessRuntimeFactoryService(new ProcessRuntimeFactoryServiceImpl());
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("JavaHandler.bpmn"),
                     ResourceType.BPMN2);
        return kbuilder.newKieBase();
    }

    private static KieSession createSession(KieBase kbase) {
        Properties properties = new Properties();
        properties.put("drools.processInstanceManagerFactory",
                       "org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory");
        properties.put("drools.processSignalManagerFactory",
                       "org.jbpm.process.instance.event.DefaultSignalManagerFactory");
        KieSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration(properties);
        return kbase.newKieSession(config,
                                   KieServices.get().newEnvironment());
    }

    @Configuration
    @ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = CommandLineRunner.class))
    @EnableAutoConfiguration
    public class TestApplicationConfiguration {
    }
}
