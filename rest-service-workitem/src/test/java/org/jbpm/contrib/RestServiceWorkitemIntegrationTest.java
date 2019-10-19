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
package org.jbpm.contrib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jbpm.bpmn2.xml.XmlBPMNProcessDumper;
import org.jbpm.contrib.demoservices.Service;
import org.jbpm.contrib.mockserver.ServiceFlowMustRunTestProcess;
import org.jbpm.contrib.mockserver.ServiceFlowTestProcess;
import org.jbpm.contrib.mockserver.WorkItems;
import org.jbpm.contrib.restservice.CancelAllActiveTasksWorkitemHandler;
import org.jbpm.contrib.restservice.Constant;
import org.jbpm.contrib.restservice.RestServiceProcessEventListener;
import org.jbpm.contrib.restservice.RestServiceWorkItemHandler;
import org.jbpm.contrib.restservice.TaskTimeoutWorkitemHandler;
import org.jbpm.process.instance.event.DefaultSignalManagerFactory;
import org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory;
import org.jbpm.process.workitem.WorkDefinitionImpl;
import org.jbpm.process.workitem.WorkItemRepository;
import org.jbpm.runtime.manager.impl.DefaultRegisterableItemsFactory;
import org.jbpm.runtime.manager.impl.SimpleRegisterableItemsFactory;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.jbpm.workflow.core.WorkflowProcess;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author Ryszard Kozmik
 */
public class RestServiceWorkitemIntegrationTest extends JbpmJUnitBaseTestCase {

    private static int PORT = 8080;
    private static String DEFAULT_HOST = "localhost";
    private final Logger logger = LoggerFactory.getLogger(RestServiceWorkitemIntegrationTest.class);

    ExecutorService executor = Executors.newSingleThreadExecutor();
    
    protected static WorkItemManager currentWorkItemManager;

    @BeforeClass
    public static void mainSetUp() throws Exception {
        bootUpServices();
    }

    @Before
    public void preTestSetup() {
        
        // Configure jBPM server with all the test processes, workitems and event listeners.
        setupPoolingDataSource();
        
        WorkflowProcess[] binaryProcesses = { new ServiceFlowTestProcess(ServiceFlowTestProcess.Mode.PASS).getProcess(),
                new ServiceFlowTestProcess(ServiceFlowTestProcess.Mode.FAIL).getProcess(),
                new ServiceFlowMustRunTestProcess(ServiceFlowMustRunTestProcess.Mode.MUST_RUN).getProcess() };
        
        RuntimeManager manager = createRuntimeManager( binaryProcesses,
                "timeout-handler-process.bpmn",
                "cancel-all-handler-process.bpmn",
                "service-orchestration-test.bpmn",
                "timeout-test.bpmn2",
                "timeout-test-cancel-timeouts.bpmn",
                "invalid-service-url-test.bpmn");
        RuntimeEngine runtimeEngine = getRuntimeEngine();
        
        KieSession kieSession = runtimeEngine.getKieSession();
        kieSession.addEventListener(new RestServiceProcessEventListener());

        WorkItemManager workItemManager = kieSession.getWorkItemManager();
        workItemManager.registerWorkItemHandler("RestServiceWorkItemHandler", new RestServiceWorkItemHandler(manager));
        workItemManager.registerWorkItemHandler("CancelAllActiveTasksWorkitemHandler", new CancelAllActiveTasksWorkitemHandler(manager));
        workItemManager.registerWorkItemHandler("TaskTimeoutWorkitemHandler", new TaskTimeoutWorkitemHandler(manager));
        
        currentWorkItemManager = kieSession.getWorkItemManager();
    }
    
    @After
    public void postTestTeardown() {
        currentWorkItemManager=null;
    }
    
    public static void completeWorkItem( int workItemId, Map<String, Object> result ) {
        currentWorkItemManager.completeWorkItem(workItemId, result);
    }
    
    private static void bootUpServices() throws Exception {
        ContextHandlerCollection contexts = new ContextHandlerCollection();

        final Server server = new Server(PORT);
        server.setHandler(contexts);

        ServletContextHandler demoService = new ServletContextHandler(contexts, "/demo-service", ServletContextHandler.SESSIONS);

        ServletHolder servletHolder = demoService.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        servletHolder.setInitOrder(0);
        // Tells the Jersey Servlet which REST service/class to load.
        servletHolder.setInitParameter("jersey.config.server.provider.classnames", Service.class.getCanonicalName());

        ServletContextHandler jbpmMock = new ServletContextHandler(contexts, "/kie-server/services/rest", ServletContextHandler.SESSIONS);
        ServletHolder jbpmMockServlet = jbpmMock.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jbpmMockServlet.setInitOrder(0);
        jbpmMockServlet.setInitParameter("jersey.config.server.provider.classnames", WorkItems.class.getCanonicalName());
        server.start();
    }

    protected RuntimeManager createRuntimeManager( WorkflowProcess[] binaryProcesses, String... processes ) {
        
        Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
        for (String p : processes) {
            resources.put(p, ResourceType.BPMN2);
        }
        
        return createRuntimeManager(Strategy.SINGLETON, resources, binaryProcesses, null);
    }
    
    protected RuntimeManager createRuntimeManager(Strategy strategy, Map<String, ResourceType> resources, 
            WorkflowProcess[] binaryProcesses, String identifier) {
        if (manager != null) {
            throw new IllegalStateException("There is already one RuntimeManager active");
        }

        RuntimeEnvironmentBuilder builder = null;
        if (!setupDataSource){
            builder = RuntimeEnvironmentBuilder.Factory.get()
                    .newEmptyBuilder()
            .addConfiguration("drools.processSignalManagerFactory", DefaultSignalManagerFactory.class.getName())
            .addConfiguration("drools.processInstanceManagerFactory", DefaultProcessInstanceManagerFactory.class.getName())            
            .registerableItemsFactory(new SimpleRegisterableItemsFactory() {

                @Override
                public Map<String, WorkItemHandler> getWorkItemHandlers(RuntimeEngine runtime) {
                    Map<String, WorkItemHandler> handlers = new HashMap<String, WorkItemHandler>();
                    handlers.putAll(super.getWorkItemHandlers(runtime));
                    handlers.putAll(customHandlers);
                    return handlers;
                }

                @Override
                public List<ProcessEventListener> getProcessEventListeners(RuntimeEngine runtime) {
                    List<ProcessEventListener> listeners = super.getProcessEventListeners(runtime);
                    listeners.addAll(customProcessListeners);
                    return listeners;
                }

                @Override
                public List<AgendaEventListener> getAgendaEventListeners( RuntimeEngine runtime) {
                    List<AgendaEventListener> listeners = super.getAgendaEventListeners(runtime);
                    listeners.addAll(customAgendaListeners);
                    return listeners;
                }

                @Override
                public List<TaskLifeCycleEventListener> getTaskListeners() {
                    List<TaskLifeCycleEventListener> listeners = super.getTaskListeners();
                    listeners.addAll(customTaskListeners);
                    return listeners;
                }

            });

        } else if (sessionPersistence) {
            builder = RuntimeEnvironmentBuilder.Factory.get()
                    .newDefaultBuilder()
            .entityManagerFactory(getEmf())
            .registerableItemsFactory(new DefaultRegisterableItemsFactory() {

                @Override
                public Map<String, WorkItemHandler> getWorkItemHandlers(RuntimeEngine runtime) {
                    Map<String, WorkItemHandler> handlers = new HashMap<String, WorkItemHandler>();
                    handlers.putAll(super.getWorkItemHandlers(runtime));
                    handlers.putAll(customHandlers);
                    return handlers;
                }

                @Override
                public List<ProcessEventListener> getProcessEventListeners(RuntimeEngine runtime) {
                    List<ProcessEventListener> listeners = super.getProcessEventListeners(runtime);
                    listeners.addAll(customProcessListeners);
                    return listeners;
                }

                @Override
                public List<AgendaEventListener> getAgendaEventListeners( RuntimeEngine runtime) {
                    List<AgendaEventListener> listeners = super.getAgendaEventListeners(runtime);
                    listeners.addAll(customAgendaListeners);
                    return listeners;
                }

                @Override
                public List<TaskLifeCycleEventListener> getTaskListeners() {
                    List<TaskLifeCycleEventListener> listeners = super.getTaskListeners();
                    listeners.addAll(customTaskListeners);
                    return listeners;
                }

            });
        } else {
            builder = RuntimeEnvironmentBuilder.Factory.get()
                    .newDefaultInMemoryBuilder()
                    .entityManagerFactory(getEmf())
                    .registerableItemsFactory(new DefaultRegisterableItemsFactory() {

                @Override
                public Map<String, WorkItemHandler> getWorkItemHandlers(RuntimeEngine runtime) {
                    Map<String, WorkItemHandler> handlers = new HashMap<String, WorkItemHandler>();
                    handlers.putAll(super.getWorkItemHandlers(runtime));
                    handlers.putAll(customHandlers);
                    return handlers;
                }

                @Override
                public List<ProcessEventListener> getProcessEventListeners(RuntimeEngine runtime) {
                    List<ProcessEventListener> listeners = super.getProcessEventListeners(runtime);
                    listeners.addAll(customProcessListeners);
                    return listeners;
                }

                @Override
                public List<AgendaEventListener> getAgendaEventListeners( RuntimeEngine runtime) {
                    List<AgendaEventListener> listeners = super.getAgendaEventListeners(runtime);
                    listeners.addAll(customAgendaListeners);
                    return listeners;
                }

                @Override
                public List<TaskLifeCycleEventListener> getTaskListeners() {
                    List<TaskLifeCycleEventListener> listeners = super.getTaskListeners();
                    listeners.addAll(customTaskListeners);
                    return listeners;
                }

            });
        }
        builder.userGroupCallback(userGroupCallback);
        
        for (Entry<String, Object> envEntry : customEnvironmentEntries.entrySet()) {
            builder.addEnvironmentEntry(envEntry.getKey(), envEntry.getValue());
        }

        for (Map.Entry<String, ResourceType> entry : resources.entrySet()) {
            builder.addAsset(ResourceFactory.newClassPathResource(entry.getKey()), entry.getValue());
        }
        if( binaryProcesses!=null ) {
            for( WorkflowProcess wp : binaryProcesses ) {
                builder.addAsset(ResourceFactory.newByteArrayResource(XmlBPMNProcessDumper.INSTANCE.dump(wp).getBytes()), ResourceType.BPMN2);
            }
        }

        return createRuntimeManager(strategy, resources, builder.get(), identifier);
    }

    @Test
    public void testCallbackWithVariablePassing() throws InterruptedException {
        KieSession kieSession = getRuntimeEngine().getKieSession();

        Semaphore nodeACompleted = new Semaphore(0);
        final Map<String, String> processVariables = new HashMap<>();

        ProcessEventListener processEventListener = new DefaultProcessEventListener() {
            public void afterVariableChanged(ProcessVariableChangedEvent event) {
                logger.info("Variable changed {}: oldVal: {}, newVal: {}.", event.getVariableId(), event.getOldValue(), event.getNewValue());
                processVariables.put(event.getVariableId(), event.getNewValue().toString());
                if (event.getVariableId().equals("resultA")) {
                    nodeACompleted.release();
                }
            }
        };
        kieSession.addEventListener(processEventListener);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("containerId", "mock");
        ProcessInstance processInstance = (ProcessInstance)kieSession.startProcess("service-orchestration-test", parameters);

        nodeACompleted.tryAcquire(30, TimeUnit.SECONDS);

        kieSession.removeEventListener(processEventListener);

        processVariables.forEach((k,v) -> logger.info("Process variable {} : {}", k, v));
        String expectedCancelUrl = "http://localhost:8080/demo-service/service/cancel/";
        String cancelUrl = processVariables.get("serviceA-cancelUrl");
        Assert.assertTrue("Cancel url is expected to start with: " + expectedCancelUrl + ". Actual value: " + cancelUrl,
                cancelUrl.startsWith(expectedCancelUrl));
        Assert.assertEquals("{person={name=Matej}}", processVariables.get("resultA"));
    }

    @Test
    public void testCancel() throws InterruptedException {
        KieSession kieSession = getRuntimeEngine().getKieSession();

        final Semaphore nodeAActive = new Semaphore(0);
        final Semaphore nodeACompleted = new Semaphore(0);
        final AtomicReference<String> resultA = new AtomicReference<>();

        ProcessEventListener processEventListener = new DefaultProcessEventListener() {
            public void afterVariableChanged(ProcessVariableChangedEvent event) {
                logger.info("Variable changed {} = {}.", event.getVariableId(), event.getNewValue());
                if (event.getVariableId().equals("serviceA-cancelUrl")) {
                    nodeAActive.release();
                }
                if (event.getVariableId().equals("resultA")) {
                    resultA.set(event.getNewValue().toString());
                    nodeACompleted.release();
                }
            }
        };
        kieSession.addEventListener(processEventListener);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("containerId", "mock");
        ProcessInstance processInstance = (ProcessInstance) kieSession.startProcess("service-orchestration-test", parameters);

        //wait for nodeA active
        nodeAActive.acquire();

        final String pid = Long.toString(processInstance.getId());
        executor.execute(() -> {
            logger.info("Signaling cancel for pid: {}.", pid);
            kieSession.signalEvent(Constant.CANCEL_SIGNAL_TYPE, pid);
        });
        nodeACompleted.tryAcquire(8, TimeUnit.SECONDS);
        kieSession.removeEventListener(processEventListener);
        logger.info("Cancelled A result: {}", resultA.get());
        Assert.assertEquals("{canceled=true}", resultA.get());
    }

    @Test
    public void testTimeoutServiceDoesNotRespondCancelSucess() throws InterruptedException {
        KieSession kieSession = getRuntimeEngine().getKieSession();

        final Semaphore nodeACompleted = new Semaphore(0);
        final AtomicReference<String> resultA = new AtomicReference<>();

        ProcessEventListener processEventListener = new DefaultProcessEventListener() {
            public void afterVariableChanged(ProcessVariableChangedEvent event) {
                if (event.getVariableId().equals("resultA")) {
                    resultA.set(event.getNewValue().toString());
                    nodeACompleted.release();
                }
            }
        };
        kieSession.addEventListener(processEventListener);

        Map<String, Object> parameters = new HashMap<String, Object>();

        parameters.put("containerId", "mock");
        ProcessInstance processInstance = (ProcessInstance) kieSession.startProcess("timeout-test", parameters);

        boolean completed = nodeACompleted.tryAcquire(10, TimeUnit.SECONDS);
        if (!completed) {
            Assert.fail("Failed to complete the process.");
        }
        kieSession.removeEventListener(processEventListener);
        Assert.assertEquals("{canceled=true}", resultA.get());
    }

    /**
     * The test will execute a process with just one task that is set with 2s timeout while the REST service invoked in it is set with 10sec callback time.
     * After 2 seconds the timeout process will kick in by finishing the REST workitem and setting the information that it has failed.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testTimeoutServiceDoesNotRespondCancelTimeouts() throws InterruptedException {

        final Semaphore nodeACompleted = new Semaphore(0);
        final AtomicReference<String> resultA = new AtomicReference<>();

        ProcessEventListener processEventListener = new DefaultProcessEventListener() {
            public void afterVariableChanged(ProcessVariableChangedEvent event) {
                if (event.getVariableId().equals("resultA")) {
                    resultA.set(event.getNewValue().toString());
                    nodeACompleted.release();
                }
            }
        };
        
        KieSession kieSession = getRuntimeEngine().getKieSession();
        kieSession.addEventListener(processEventListener);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("containerId", "mock");
        ProcessInstance processInstance = (ProcessInstance) kieSession.startProcess("timeout-test-cancel-timeouts", parameters);

        boolean completed = nodeACompleted.tryAcquire(15, TimeUnit.SECONDS);
        if (!completed) {
            Assert.fail("Failed to complete the process.");
        }
        kieSession.removeEventListener(processEventListener);
        Assert.assertEquals("{\"remote-cancel-failed\":true}", resultA.get());
        
        assertProcessInstanceCompleted(processInstance.getId());
        disposeRuntimeManager();
    }

    @Test
    public void testInvalidServiceUrl() throws InterruptedException {
        KieSession kieSession = getRuntimeEngine().getKieSession();

        final Semaphore nodeACompleted = new Semaphore(0);
        final AtomicReference<String> resultA = new AtomicReference<>();

        ProcessEventListener processEventListener = new DefaultProcessEventListener() {
            public void afterVariableChanged(ProcessVariableChangedEvent event) {
                if (event.getVariableId().equals("resultA")) {
                    resultA.set(event.getNewValue().toString());
                    nodeACompleted.release();
                }
            }
        };
        kieSession.addEventListener(processEventListener);

        Map<String, Object> parameters = new HashMap<String, Object>();

        parameters.put("containerId", "mock");
        ProcessInstance processInstance = (ProcessInstance) kieSession.startProcess("invalid-service-url-test", parameters);

        boolean completed = nodeACompleted.tryAcquire(15, TimeUnit.SECONDS);
        if (!completed) {
            Assert.fail("Failed to complete the process.");
        }
        kieSession.removeEventListener(processEventListener);
        Assert.assertEquals("{\"remote-cancel-failed\":true}", resultA.get());
    }

    @Test
    public void processFlowTest() throws InterruptedException {
        KieSession kieSession = getRuntimeEngine().getKieSession();

        final Semaphore nodeACompleted = new Semaphore(0);
        final AtomicReference<Boolean> serviceASuccessCompletion = new AtomicReference<>();
        final Semaphore nodeBCompleted = new Semaphore(0);
        final AtomicReference<String> resultB = new AtomicReference<>();

        ProcessEventListener processEventListener = new DefaultProcessEventListener() {
            public void afterVariableChanged(ProcessVariableChangedEvent event) {
                logger.info("Process variable: {}, changed to: {}.", event.getVariableId(), event.getNewValue());
                if (event.getVariableId().equals("serviceA-successCompletion")) {
                    serviceASuccessCompletion.set((Boolean) event.getNewValue());
                    nodeACompleted.release();
                }
                if (event.getVariableId().equals("resultB")) {
                    resultB.set(event.getNewValue().toString());
                }
                if (event.getVariableId().equals("serviceB-successCompletion")) {
                    nodeBCompleted.release();
                }
            }
        };
        kieSession.addEventListener(processEventListener);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("containerId", "mock");
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) kieSession.startProcess("org.jbpm.ServiceFlowTest", parameters);

        boolean completed = nodeACompleted.tryAcquire(5, TimeUnit.SECONDS);
        if (!completed) {
            Assert.fail("Failed to complete the process.");
        }
        Assert.assertTrue(serviceASuccessCompletion.get());

        nodeBCompleted.tryAcquire(5, TimeUnit.SECONDS);

        Assert.assertEquals("{fullName=Matej Lazar}", resultB.get());

        kieSession.removeEventListener(processEventListener);
    }

    @Test
    public void processFlowCancelOnFailureTest() throws InterruptedException {
        KieSession kieSession = getRuntimeEngine().getKieSession();

        final Semaphore nodeACompleted = new Semaphore(0);
        final AtomicReference<Boolean> serviceASuccessCompletion = new AtomicReference<>();
        final Semaphore nodeBCompleted = new Semaphore(0);
        final AtomicReference<String> resultB = new AtomicReference<>();

        ProcessEventListener processEventListener = new DefaultProcessEventListener() {
            public void afterVariableChanged(ProcessVariableChangedEvent event) {
                logger.info("Process variable: {}, changed to: {}.", event.getVariableId(), event.getNewValue());
                if (event.getVariableId().equals("serviceA-successCompletion")) {
                    serviceASuccessCompletion.set((Boolean) event.getNewValue());
                    nodeACompleted.release();
                }
                if (event.getVariableId().equals("resultB")) {
                    resultB.set(event.getNewValue().toString());
                }
                if (event.getVariableId().equals("serviceB-successCompletion")) {
                    nodeBCompleted.release();
                }
            }
        };
        kieSession.addEventListener(processEventListener);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("containerId", "mock");
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) kieSession.startProcess("org.jbpm.ServiceFlowFailingServiceTest", parameters);

        boolean completed = nodeACompleted.tryAcquire(5, TimeUnit.SECONDS);
        if (!completed) {
            Assert.fail("Failed to complete the process.");
        }
        Assert.assertFalse(serviceASuccessCompletion.get());

        nodeBCompleted.tryAcquire(5, TimeUnit.SECONDS);

        Assert.assertEquals(null, resultB.get());

        kieSession.removeEventListener(processEventListener);
    }

    @Test
    public void processFlowMustRunAfterTest() throws InterruptedException {
        KieSession kieSession = getRuntimeEngine().getKieSession();

        final Semaphore nodeACompleted = new Semaphore(0);
        final AtomicReference<Boolean> serviceASuccessCompletion = new AtomicReference<>();
        final Semaphore nodeBCompleted = new Semaphore(0);
        final AtomicReference<String> resultTimeout = new AtomicReference<>();
        final AtomicReference<String> resultB = new AtomicReference<>();

        ProcessEventListener processEventListener = new DefaultProcessEventListener() {
            public void afterVariableChanged(ProcessVariableChangedEvent event) {
                logger.info("Process variable: {}, changed to: {}.", event.getVariableId(), event.getNewValue());
                if (event.getVariableId().equals("serviceA-successCompletion")) {
                    serviceASuccessCompletion.set((Boolean) event.getNewValue());
                    nodeACompleted.release();
                }
                if (event.getVariableId().equals("resultTimeout")) {
                    resultTimeout.set((String) event.getNewValue());
                }
                if (event.getVariableId().equals("resultB")) {
                    resultB.set(event.getNewValue().toString());
                }
                if (event.getVariableId().equals("serviceB-successCompletion")) {
                    nodeBCompleted.release();
                }
            }
        };
        kieSession.addEventListener(processEventListener);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("containerId", "mock");
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) kieSession.startProcess("org.jbpm.ServiceFlowMustRunAfterFailingServiceTest", parameters);

        boolean completed = nodeACompleted.tryAcquire(5, TimeUnit.SECONDS);
        if (!completed) {
            Assert.fail("Failed to complete the process.");
        }
        Assert.assertTrue(serviceASuccessCompletion.get());

        nodeBCompleted.tryAcquire(5, TimeUnit.SECONDS);

        Assert.assertEquals("{fullName=Matej Lazar}", resultB.get());
        Assert.assertEquals("{\"remote-cancel-failed\":true}", resultTimeout.get());

        kieSession.removeEventListener(processEventListener);
    }

    @Test @Ignore
    public void testWorkitemValidity() {

        String repoPath = "file://" + System.getProperty("builddir") +
                "/" + System.getProperty("artifactId") + "-" +
                System.getProperty("version") + "/";

        Map<String, WorkDefinitionImpl> repoResults = new WorkItemRepository().getWorkDefinitions(repoPath,
                                                                                                  null,
                                                                                                  System.getProperty("artifactId"));
        assertNotNull(repoResults);
        assertEquals(1,
                     repoResults.size());
    }
}
