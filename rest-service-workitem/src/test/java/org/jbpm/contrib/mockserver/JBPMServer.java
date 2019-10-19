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
package org.jbpm.contrib.mockserver;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jbpm.process.instance.event.DefaultSignalManagerFactory;
import org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class JBPMServer {

    private static ConcurrentMap<String, JBPMServer> instance = new ConcurrentHashMap<>();

    public static JBPMServer getInstance() {
        return getInstance("default");
    }

    public static JBPMServer getInstance(String key) {
        return instance.computeIfAbsent(key, (k) -> new JBPMServer());
    }

    private RuntimeManager manager;

    public JBPMServer() {
//        manager = getInMemmoryRuntimeManager("service-orchestration.bpmn", "timeout.bpmn");
//        manager = getPersistentRuntimeManager(
//                "timeout-handler-process.bpmn",
//                "cancel-all-handler-process.bpmn",
//                "service-orchestration-test.bpmn",
//                "timeout-test.bpmn",
//                "timeout-test-cancel-timeouts.bpmn",
//                "invalid-service-url-test.bpmn");
        
//        manager = getInMemoryRuntimeManager(
//              "timeout-handler-process.bpmn",
//              "cancel-all-handler-process.bpmn",
//              "service-orchestration-test.bpmn",
//              "timeout-test.bpmn",
//              "timeout-test-cancel-timeouts.bpmn",
//              "invalid-service-url-test.bpmn");
//
//        RuntimeEngine runtime = getRuntimeEngine();
//        KieSession ksession = runtime.getKieSession();
//
//        ksession.addEventListener(new RestServiceProcessEventListener());
//
//        WorkItemManager workItemManager = ksession.getWorkItemManager();
//        workItemManager.registerWorkItemHandler("RestServiceWorkItemHandler", new RestServiceWorkItemHandler(manager));
//        workItemManager.registerWorkItemHandler("CancelAllActiveTasksWorkitemHandler", new CancelAllActiveTasksWorkitemHandler(manager));
//        workItemManager.registerWorkItemHandler("TaskTimeoutWorkitemHandler", new TaskTimeoutWorkitemHandler(manager));
    }

    private static RuntimeManager getInMemoryRuntimeManager(String... processes) {
        RuntimeEnvironmentBuilder runtimeEnvironmentBuilder = RuntimeEnvironmentBuilder.Factory.get().newDefaultBuilder();
        
        for (String processId : processes) {
            runtimeEnvironmentBuilder.addAsset(ResourceFactory.newClassPathResource(processId), ResourceType.BPMN2);
        }
        
        RuntimeEnvironment environment = runtimeEnvironmentBuilder //.newDefaultInMemoryBuilder()
                //.knowledgeBase(KieServices.Factory.get().getKieClasspathContainer().getKieBase("my-knowledge-base"))
                .persistence(false)
                .addConfiguration("drools.processSignalManagerFactory", DefaultSignalManagerFactory.class.getName())
                .addConfiguration("drools.processInstanceManagerFactory", DefaultProcessInstanceManagerFactory.class.getName())
                .get();
        
        return RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);
    }

    public RuntimeManager getManager() {
        return manager;
    }

    public RuntimeEngine getRuntimeEngine() {
        return manager.getRuntimeEngine(EmptyContext.get());
    }

    /**
     * Required to work with taskService.
     */
    private static RuntimeManager getPersistentRuntimeManager(String... processes) {
        // load up the knowledge base
     //   JBPMHelper.startH2Server();
     //   JBPMHelper.setupDataSource();

        RuntimeEnvironmentBuilder runtimeEnvironmentBuilder = RuntimeEnvironmentBuilder.Factory.get().newDefaultBuilder();

        for (String processId : processes) {
            runtimeEnvironmentBuilder.addAsset(ResourceFactory.newClassPathResource(processId), ResourceType.BPMN2);
        }

        runtimeEnvironmentBuilder.addAsset(ResourceFactory.newByteArrayResource(new ServiceFlowTestProcess(ServiceFlowTestProcess.Mode.PASS).getBytes()), ResourceType.BPMN2);
        runtimeEnvironmentBuilder.addAsset(ResourceFactory.newByteArrayResource(new ServiceFlowTestProcess(ServiceFlowTestProcess.Mode.FAIL).getBytes()), ResourceType.BPMN2);
        runtimeEnvironmentBuilder.addAsset(ResourceFactory.newByteArrayResource(new ServiceFlowMustRunTestProcess(ServiceFlowMustRunTestProcess.Mode.MUST_RUN).getBytes()), ResourceType.BPMN2);

        
        RuntimeEnvironment environment = runtimeEnvironmentBuilder.get();
        return RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);
    }

    public static void cleanUp() {
        String home = System.getProperty("user.home");
        new File(home + "/jbpm-db.h2.db").delete();
        new File(home + "/jbpm-db.lock.db").delete();
    }
}
