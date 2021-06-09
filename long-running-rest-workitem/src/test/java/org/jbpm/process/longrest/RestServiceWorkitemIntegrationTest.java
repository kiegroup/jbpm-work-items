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
package org.jbpm.process.longrest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.Cookie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.Undertow;
import io.undertow.servlet.api.DeploymentInfo;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.kie.services.impl.DeployedUnitImpl;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.ProcessServiceImpl;
import org.jbpm.kie.services.impl.RuntimeDataServiceImpl;
import org.jbpm.kie.services.impl.bpmn2.BPMN2DataServiceImpl;
import org.jbpm.kie.services.impl.query.QueryServiceImpl;
import org.jbpm.process.longrest.bpm.TestFunctions;
import org.jbpm.process.longrest.demoservices.CookieListener;
import org.jbpm.process.longrest.demoservices.EventType;
import org.jbpm.process.longrest.demoservices.Service;
import org.jbpm.process.longrest.demoservices.ServiceListener;
import org.jbpm.process.longrest.demoservices.dto.PreBuildRequest;
import org.jbpm.process.longrest.demoservices.dto.Request;
import org.jbpm.process.longrest.demoservices.dto.Scm;
import org.jbpm.process.longrest.mockserver.WorkItems;
import org.jbpm.process.longrest.util.Maps;
import org.jbpm.runtime.manager.impl.RuntimeManagerFactoryImpl;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.service.ServiceRegistry;
import org.jbpm.services.task.HumanTaskServiceFactory;
import org.jbpm.services.task.audit.TaskAuditServiceFactory;
import org.jbpm.shared.services.impl.TransactionalCommandService;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutorService;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestServiceWorkitemIntegrationTest extends JbpmJUnitBaseTestCase {

    private final Logger logger = LoggerFactory.getLogger(RestServiceWorkitemIntegrationTest.class);

    private static int PORT = 8080;
    private UndertowJaxrsServer server;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final ActiveTasks activeProcesses = new ActiveTasks();
    private final ServiceListener serviceListener = new ServiceListener();
    private final CookieListener cookieListener = new CookieListener();
    private Long heartbeatMonitorTaskId;

    public RestServiceWorkitemIntegrationTest() {
        super(true, true);
    }

    @Before
    public void preTestSetup() throws Exception {
        System.setProperty(Constant.HOSTNAME_HTTP, "localhost:8080");

        // Configure jBPM server with all the test processes, workitems and event listeners.
        setupPoolingDataSource();

        Map<String, ResourceType> resources = new HashMap<>();
        resources.put("execute-rest.bpmn", ResourceType.BPMN2);
        resources.put("test-process.bpmn", ResourceType.BPMN2);

        manager = createRuntimeManager(Strategy.PROCESS_INSTANCE, resources);
        customProcessListeners.add(new RestServiceProcessEventListener(activeProcesses));
        customHandlers.put("LongRunningRestService", new LongRunningRestServiceWorkItemHandler(manager));

        EntityManagerFactory emf = getEmf();
        buildJbpmServices(emf);

        ExecutorService executorService = (ExecutorService) ServiceRegistry.get().service(ServiceRegistry.EXECUTOR_SERVICE);
        CommandContext commandContext = new CommandContext();
        commandContext.setData(Constant.HEARTBEAT_VALIDATION_VARIABLE, "PT1S");
        heartbeatMonitorTaskId = executorService.scheduleRequest(HeartbeatMonitorCommand.class.getName(), commandContext);

        bootUpServices();
    }

    @After
    public void postTestTeardown() throws Exception {
        ExecutorService executorService = (ExecutorService) ServiceRegistry.get().service(ServiceRegistry.EXECUTOR_SERVICE);
        executorService.cancelRequest(heartbeatMonitorTaskId);
        executorService.destroy();
        logger.info("Stopping http server ...");
        server.stop();
    }

    private void bootUpServices() throws Exception {
        server = new UndertowJaxrsServer();

        ResteasyDeployment deployment = new ResteasyDeployment();
        deployment.setApplicationClass(JaxRsActivator.class.getName());

        DeploymentInfo deploymentInfo = server.undertowDeployment(deployment, "/");
        deploymentInfo.setClassLoader(this.getClass().getClassLoader());
        deploymentInfo.setDeploymentName("TestServices");
        deploymentInfo.setContextPath("/");

        deploymentInfo.addServletContextAttribute(Service.SERVICE_LISTENER_KEY, serviceListener);
        deploymentInfo.addServletContextAttribute(Service.COOKIE_LISTENER_KEY, cookieListener);
        deploymentInfo.addServletContextAttribute(WorkItems.RUNTIME_MANAGER_KEY, manager);

        server.deploy(deploymentInfo);
        Undertow.Builder builder = Undertow.builder().addHttpListener(PORT, "localhost");
        server.start(builder);
    }

    @Test(timeout = 15000)
    public void shouldInvokeRemoteServiceAndReceiveCallback() throws Exception {
        BlockingQueue<ProcessVariableChangedEvent> variableChangedQueue = new ArrayBlockingQueue(1000);

        ProcessEventListener processEventListener = getProcessEventListener(
                variableChangedQueue,
                "preBuildResult",
                "buildResult",
                "completionResult"
        );
        customProcessListeners.add(processEventListener);

        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession kieSession = runtimeEngine.getKieSession();

        Semaphore callbackCompleted = new Semaphore(0);
        ServiceListener.Subscription subscription = serviceListener.subscribe(
                EventType.CALLBACK_COMPLETED,
                (v) -> callbackCompleted.release());

        //when
        Map<String, Object> labels = new HashMap<>();
        labels.put("A", 1);
        labels.put("lines", "two\nlines");
        labels.put("quote", "String \"literal\".");

        ProcessInstance processInstance = kieSession.startProcess(
                "testProcess",
                Collections.singletonMap("input", getProcessParameters(1, 30, 1, 30, labels)));

        manager.disposeRuntimeEngine(runtimeEngine);

        //then
        //ignore variable initialization
        variableChangedQueue.take(); //preBuildResult
        variableChangedQueue.take(); //buildResult

        Map<String, Object> preBuildCallbackResult = (Map<String, Object>) variableChangedQueue.take().getNewValue();
        logger.info("preBuildCallbackResult: " + preBuildCallbackResult);
        Map<String, Object> preBuildResponse = Maps.getStringObjectMap(preBuildCallbackResult, "response");
        Assert.assertEquals("new-scm-tag", Maps.getStringObjectMap(preBuildResponse, "scm").get("revision"));
        Map<String, Object> initialResponse = Maps.getStringObjectMap(preBuildCallbackResult, "initialResponse");
        Assert.assertTrue(initialResponse.get("cancelUrl").toString().startsWith("http://localhost:8080/demo-service/cancel/"));

        Map<String, Object> buildCallbackResult = (Map<String, Object>) variableChangedQueue.take().getNewValue();
        logger.info("buildCallbackResult: " + buildCallbackResult);
        Map<String, Object> buildResponse = Maps.getStringObjectMap(preBuildCallbackResult, "response");
        Assert.assertEquals("SUCCESS", buildResponse.get("status"));

        Map<String, Object> completionResult = (Map<String, Object>) variableChangedQueue.take().getNewValue();
        logger.info("completionResult: " + completionResult);
        Assert.assertEquals("SUCCESS", completionResult.get("status"));
        Map<String, Object> responseLabels = (Map<String, Object>) ((Map<String, Object>) completionResult.get("response")).get("labels");
        Assert.assertEquals(labels.get("lines"), responseLabels.get("lines"));
        Assert.assertEquals(labels.get("quote"), responseLabels.get("quote"));

        logger.info("Waiting for callback to complete...");
        callbackCompleted.acquire(2);
        logger.info("Callback completed.");

        customProcessListeners.remove(processEventListener);
        serviceListener.unsubscribe(subscription);
    }

    @Test(timeout = 20000)
    public void shouldCatchException() throws Exception {
        BlockingQueue<ProcessVariableChangedEvent> variableChangedQueue = new ArrayBlockingQueue(1000);

        ProcessEventListener processEventListener = getProcessEventListener(variableChangedQueue, "preBuildResult");

        customProcessListeners.add(processEventListener);

        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession kieSession = runtimeEngine.getKieSession();

        //when
        Map<String, Object> processParameters = getProcessParameters(2, 10, 10, 10, Collections.emptyMap());
        processParameters.put("preBuildServiceUrl", "http://host-not-found:8080/");

        ProcessInstance processInstance = kieSession.startProcess(
                "testProcess",
                Collections.singletonMap("input", processParameters));

        manager.disposeRuntimeEngine(runtimeEngine);

        //then
        //ignore variable initialization
        variableChangedQueue.take(); //preBuildResult

        Map<String, Object> preBuildCallbackResult = (Map<String, Object>) variableChangedQueue.take().getNewValue();
        logger.info("preBuildCallbackResult: " + preBuildCallbackResult);
        RemoteInvocationException exception = (RemoteInvocationException) preBuildCallbackResult.get("error");
        logger.info("Expected exception: {}.", exception.getMessage());
        Assert.assertNotNull(exception);

        customProcessListeners.remove(processEventListener);
    }

    @Test(timeout = 20000)
    public void shouldRetryFailedRequest() throws Exception {
        BlockingQueue<ProcessVariableChangedEvent> variableChangedQueue = new ArrayBlockingQueue(1000);

        ProcessEventListener processEventListener = getProcessEventListener(variableChangedQueue, "preBuildResult", "retryAttempt", "completionResult");

        customProcessListeners.add(processEventListener);

        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession kieSession = runtimeEngine.getKieSession();

        //when
        Map<String, Object> processParameters = getProcessParameters(2, 10, 10, 10, Collections.emptyMap());
        processParameters.put("preBuildServiceUrl", "http://host-not-found:8080/");
        processParameters.put("retryDelay", 250);
        processParameters.put("maxRetries", 3);

        ProcessInstance processInstance = kieSession.startProcess(
                "testProcess",
                Collections.singletonMap("input", processParameters));

        manager.disposeRuntimeEngine(runtimeEngine);

        //then
        //ignore variable initialization
        variableChangedQueue.take(); //preBuildResult
        variableChangedQueue.take(); //retryAttempt
        variableChangedQueue.take(); //retryAttempt
        variableChangedQueue.take(); //retryAttempt

        Integer retryAttempt = (Integer) variableChangedQueue.take().getNewValue();//retryAttempt
        Assert.assertEquals("3", retryAttempt.toString());

        Map<String, Object> preBuildCallbackResult = (Map<String, Object>) variableChangedQueue.take().getNewValue();
        logger.info("preBuildCallbackResult: " + preBuildCallbackResult);
        RemoteInvocationException exception = (RemoteInvocationException) preBuildCallbackResult.get("error");
        logger.info("Expected exception: {}.", exception.getMessage());
        Assert.assertNotNull(exception);

        //wait for completionResult
        variableChangedQueue.take(); //preBuildResult
        variableChangedQueue.take(); //preBuildResult
        variableChangedQueue.take(); //retryAttempt
        ProcessVariableChangedEvent event = variableChangedQueue.take();
        Object newValue = event.getNewValue();//completionResult

        customProcessListeners.remove(processEventListener);
    }

    /**
     * Invoke cancel while first service is running. Cancel completes successfully.
     */
    @Test(timeout = 15000)
    public void testTimeoutServiceDoesNotRespondCancelSuccess() throws InterruptedException {
        BlockingQueue<ProcessVariableChangedEvent> variableChangedQueue = new ArrayBlockingQueue(1000);
        ProcessEventListener processEventListener = getProcessEventListener(variableChangedQueue, "restResponse", "preBuildResult");
        customProcessListeners.add(processEventListener);

        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession kieSession = runtimeEngine.getKieSession();

        Semaphore callbackCompleted = new Semaphore(0);
        AtomicInteger buildRequested = new AtomicInteger();
        ServiceListener.Subscription subscription = serviceListener.subscribe(
                EventType.CALLBACK_COMPLETED,
                (v) -> callbackCompleted.release());
        ServiceListener.Subscription buildSubscription = serviceListener.subscribe(
                EventType.BUILD_REQUESTED,
                (v) -> buildRequested.incrementAndGet());

        //when
        ProcessInstance processInstance = kieSession.startProcess(
                "testProcess",
                Collections.singletonMap("input", getProcessParameters(10, 30, 1, 30, Collections.emptyMap())));
        manager.disposeRuntimeEngine(runtimeEngine);

        //ignore variable initialization
        variableChangedQueue.take(); //preBuildResult

        //then wait for first service to start
        variableChangedQueue.take().getNewValue();
        RuntimeEngine runtimeEngineCancel = getRuntimeEngine(processInstance.getId());
        logger.info("Signalling cancel ...");
        runtimeEngineCancel.getKieSession().signalEvent(Constant.CANCEL_SIGNAL_TYPE, null);
        manager.disposeRuntimeEngine(runtimeEngineCancel);

        Map<String, Object> preBuildCallbackResult = (Map<String, Object>) variableChangedQueue.take().getNewValue();
        logger.info("preBuildCallbackResult: " + preBuildCallbackResult);
        Map<String, Object> preBuildResponse = Maps.getStringObjectMap(preBuildCallbackResult, "response");
        Assert.assertEquals("true", preBuildResponse.get("cancelled"));

        logger.info("Waiting for all processes to complete...");
        activeProcesses.waitAllCompleted();
        logger.info("All processes completed.");

        logger.info("Waiting for callback to complete...");
        callbackCompleted.acquire();
        logger.info("Callback completed.");

        //make sure buildResult is not set (build service did not run)
        Assert.assertEquals(0, buildRequested.get());

        customProcessListeners.remove(processEventListener);
        serviceListener.unsubscribe(subscription);
    }

    /**
     * The test will execute a process with just one task that is set with 2s timeout while the REST service invoked in it is set with 10sec callback time.
     * After 2 seconds the timeout process will kick in by finishing the REST workitem and setting the information that it has failed.
     */
    @Test(timeout = 15000)
    public void serviceTimesOutInternalCancelSucceeds() throws InterruptedException {
        BlockingQueue<ProcessVariableChangedEvent> variableChangedQueue = new ArrayBlockingQueue(1000);
        ProcessEventListener processEventListener = getProcessEventListener(variableChangedQueue,
                                                                            "preBuildResult",
                                                                            "completionResult");
        customProcessListeners.add(processEventListener);

        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession kieSession = runtimeEngine.getKieSession();

        Semaphore callbackCompleted = new Semaphore(0);
        ServiceListener.Subscription subscription = serviceListener.subscribe(
                EventType.CALLBACK_COMPLETED,
                (v) -> callbackCompleted.release());

        Map<String, Cookie> cookies = new HashMap<>();
        cookieListener.addConsumer(h -> cookies.putAll(h));

        //when
        ProcessInstance processInstance = kieSession.startProcess(
                "testProcess",
                Collections.singletonMap("input", getProcessParameters(10, 2, 1, 30, Collections.emptyMap())));
        manager.disposeRuntimeEngine(runtimeEngine);
        //ignore variable initialization
        variableChangedQueue.take(); //preBuildResult

        //then wait for first service to start
        variableChangedQueue.take().getNewValue();

        Map<String, Object> preBuildResult = (Map<String, Object>) variableChangedQueue.take().getNewValue();
        logger.info("preBuildResult: " + preBuildResult);
        Map<String, Object> preBuildResponse = Maps.getStringObjectMap(preBuildResult, "response");
        Assert.assertEquals("true", preBuildResponse.get("cancelled"));
        Assert.assertEquals("TIMED_OUT", preBuildResult.get("status"));

        //make sure final task (send result) has been called
        //use getChangedVariable because of there are multiple events of preBuildResult in case of cancel
        Map<String, Object> completionResult = getChangedVariable(variableChangedQueue, "completionResult");
        logger.info("completionResult: " + completionResult);
        Assert.assertEquals("SUCCESS", completionResult.get("status"));

        activeProcesses.waitAllCompleted();
        callbackCompleted.acquire();

        //make sure cookie header has been used in the cancel request
        Assert.assertFalse(cookies.isEmpty());
        Assert.assertEquals(Service.PRE_BUILD_COOKIE_VALUE, cookies.get(Service.PRE_BUILD_COOKIE_NAME).getValue());

        customProcessListeners.remove(processEventListener);
        serviceListener.unsubscribe(subscription);
    }

    /**
     * Exceptions are expected in the log as callback is executed after the cancel completed.
     */
    @Test(timeout = 15000)
    public void serviceTimesOutInternalCancelTimesOut() throws InterruptedException {
        BlockingQueue<ProcessVariableChangedEvent> variableChangedQueue = new ArrayBlockingQueue(1000);
        ProcessEventListener processEventListener = getProcessEventListener(variableChangedQueue, "preBuildResult");
        customProcessListeners.add(processEventListener);

        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession kieSession = runtimeEngine.getKieSession();

        Semaphore callbackCompleted = new Semaphore(0);
        ServiceListener.Subscription subscription = serviceListener.subscribe(
                EventType.CALLBACK_COMPLETED,
                (v) -> callbackCompleted.release());

        //when
        ProcessInstance processInstance = kieSession.startProcess(
                "testProcess",
                Collections.singletonMap("input", getProcessParameters(10, 2, 10, 2, Collections.emptyMap())));
        manager.disposeRuntimeEngine(runtimeEngine);
        //ignore variable initialization
        variableChangedQueue.take();

        //then wait for first service to start
        variableChangedQueue.take().getNewValue(); //preBuildResult

        Map<String, Object> preBuildResult = (Map<String, Object>) variableChangedQueue.take().getNewValue();
        logger.info("preBuildResult: " + preBuildResult);
        Map<String, Object> preBuildResponse = Maps.getStringObjectMap(preBuildResult, "response");
        Assert.assertEquals("TIMED_OUT", preBuildResult.get("status"));

        activeProcesses.waitAllCompleted();
        callbackCompleted.acquire();

        customProcessListeners.remove(processEventListener);
        serviceListener.unsubscribe(subscription);
    }

    @Test(timeout = 15000)
    public void shouldStartAndCompleteExecuteRestProcess() throws InterruptedException {
        // Semaphore for process completed event
        final Semaphore processFinished = new Semaphore(0);
        BlockingQueue<ProcessVariableChangedEvent> variableChangedQueue = new ArrayBlockingQueue(1000);

        ProcessEventListener processEventListener = new DefaultProcessEventListener() {
            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
                logger.info("Event ID: {}, event node ID: {}, event node name: {}", event.getNodeInstance().getId(), event.getNodeInstance().getNodeId(), event.getNodeInstance().getNodeName());
            }

            public void afterProcessCompleted(ProcessCompletedEvent event) {
                logger.info("Process completed, unblocking test.");
                processFinished.release();
            }

            public void afterVariableChanged(ProcessVariableChangedEvent event) {
                String variableId = event.getVariableId();
                logger.info("Process: {}, variable: {}, changed to: {}.",
                            event.getProcessInstance().getProcessName(),
                            variableId,
                            event.getNewValue());

                String[] enqueueEvents = new String[]{
                        "result"
                };
                if (Arrays.asList(enqueueEvents).contains(variableId)) {
                    variableChangedQueue.add(event);
                }
            }
        };
        customProcessListeners.add(processEventListener);
        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession kieSession = runtimeEngine.getKieSession();

        ProcessInstance processInstance = (ProcessInstance) kieSession.startProcess("org.jbpm.process.longrest.executerest", getExecuteRestParameters());
        manager.disposeRuntimeEngine(runtimeEngine);

        boolean completed = processFinished.tryAcquire(15, TimeUnit.SECONDS);
        if (!completed) {
            Assert.fail("Failed to complete the process.");
        }

        Map<String, Object> result = (Map<String, Object>) variableChangedQueue.take().getNewValue();
        logger.info("result: " + result);
        Assert.assertEquals("new-scm-tag", Maps.getStringObjectMap(Maps.getStringObjectMap(result, "response"), "scm").get("revision"));

        customProcessListeners.remove(processEventListener);
    }

    @Test(timeout = 15000)
    public void shouldFailWhenThereIsNoHeartBeat() {
        BlockingQueue<ProcessVariableChangedEvent> variableChangedQueue = new ArrayBlockingQueue(1000);
        ProcessEventListener processEventListener = getProcessEventListener(variableChangedQueue, "preBuildResult");
        customProcessListeners.add(processEventListener);

        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession kieSession = runtimeEngine.getKieSession();

        Semaphore callbackCompleted = new Semaphore(0);
        ServiceListener.Subscription subscription = serviceListener.subscribe(
                EventType.CALLBACK_COMPLETED,
                (v) -> callbackCompleted.release());

        TestFunctions.addHeartBeatToRequest = true;
        try {
            //when
            ProcessInstance processInstance = kieSession.startProcess(
                    "testProcess",
                    Collections.singletonMap("input", getProcessParameters(10, 10, 10, 2, 2, "PT2S", Collections.emptyMap())));
            manager.disposeRuntimeEngine(runtimeEngine);
            //ignore variable initialization
            variableChangedQueue.take();

            Map<String, Object> preBuildResult = (Map<String, Object>) variableChangedQueue.take().getNewValue();
            logger.info("preBuildResult: " + preBuildResult);
            Assert.assertEquals("DIED", preBuildResult.get("status"));

            activeProcesses.waitAllCompleted();
            callbackCompleted.acquire();

            customProcessListeners.remove(processEventListener);
            serviceListener.unsubscribe(subscription);
        } catch (Throwable throwable){
            Assert.fail(throwable.getMessage());
        } finally {
            TestFunctions.addHeartBeatToRequest = false;
        }
    }

    private Map<String, Object> getExecuteRestParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("requestMethod", "POST");
        parameters.put("requestHeaders", null);
        parameters.put("requestUrl", "http://localhost:8080/demo-service/prebuild");
        parameters.put("requestTemplate", getPreBuildRequestBody());
        parameters.put("taskTimeout", "10");
        parameters.put("cancel", false);
        parameters.put("cancelTimeout", null);
        parameters.put("cancelUrlJsonPointer", null);
        parameters.put("cancelUrlTemplate", null);
        parameters.put("cancelUrlTemplate", null);
        parameters.put("cancelMethod", null);
        parameters.put("cancelHeaders", null);
        parameters.put("successEvalTemplate", null);

        return parameters;
    }

    private String getPreBuildRequestBody() {
        PreBuildRequest request = new PreBuildRequest();
        Scm scm = new Scm();
        scm.setUrl("https://github.com/kiegroup/jbpm-work-items.git");
        request.setScm(scm);
        Request callback = new Request();
        callback.setMethod("POST");
        callback.setUrl("@{system.callbackUrl}");
        request.setCallback(callback);
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            Assert.fail("Cannot serialize preBuildRequest: " + e.getMessage());
            return null;
        }
    }

    private Map<String, Object> getProcessParameters(
            int preBuildCallbackDelay,
            int preBuildTimeout,
            int cancelDelay,
            int preBuildCancelTimeout,
            Map<String, Object> labels) {
        return getProcessParameters(preBuildCallbackDelay, preBuildTimeout, cancelDelay, preBuildCancelTimeout, 10, "", labels);
    }

    private Map<String, Object> getProcessParameters(
            int preBuildCallbackDelay,
            int preBuildTimeout,
            int cancelDelay,
            int preBuildCancelTimeout,
            int cancelHeartBeatAfter,
            String heartbeatTimeout,
            Map<String, Object> labels) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("serviceBaseUrl", "http://localhost:8080/demo-service");
        parameters.put("preBuildServiceUrl", "http://localhost:8080/demo-service/prebuild?"
                + "callbackDelay=" + preBuildCallbackDelay
                + "&cancelDelay=" + cancelDelay
                + "&cancelHeartBeatAfter=" + cancelHeartBeatAfter);
        parameters.put("preBuildTimeout", preBuildTimeout);
        parameters.put("preBuildCancelTimeout", preBuildCancelTimeout);
        parameters.put("retryDelay", 0);
        parameters.put("maxRetries", 0);
        parameters.put("heartbeatTimeout", heartbeatTimeout);
        Map<String, Object> buildConfiguration = new HashMap<>();
        buildConfiguration.put("id", "1");
        buildConfiguration.put("scmRepoURL", "https://github.com/kiegroup/jbpm-work-items.git");
        buildConfiguration.put("scmRevision", "master");
        buildConfiguration.put("preBuildSyncEnabled", "true");
        buildConfiguration.put("buildScript", "true");
        buildConfiguration.put("labels", labels);
        parameters.put("buildConfiguration", buildConfiguration);
        return parameters;
    }

    private KieSession getKieSession(long processInstanceId) {
        RuntimeEngine runtimeEngine = getRuntimeEngine(processInstanceId);
        return runtimeEngine.getKieSession();
    }

    private RuntimeEngine getRuntimeEngine(long processInstanceId) {
        ProcessInstanceIdContext processInstanceContext = ProcessInstanceIdContext.get(processInstanceId);
        return manager.getRuntimeEngine(processInstanceContext);
    }

    private ProcessEventListener getProcessEventListener(BlockingQueue<ProcessVariableChangedEvent> variableChangedQueue, String... enqueueEvents) {
        return new DefaultProcessEventListener() {
            public void afterVariableChanged(ProcessVariableChangedEvent event) {
                String variableId = event.getVariableId();
                logger.info("Process: {}, variable: {}, changed to: {}.",
                            event.getProcessInstance().getProcessName(),
                            variableId,
                            event.getNewValue());
                if (Arrays.asList(enqueueEvents).contains(variableId)) {
                    variableChangedQueue.add(event);
                }
            }
        };
    }

    /**
     * Get variable identified by variableName from the queue.
     * All queue entries before this variable will be removed from the queue.
     */
    private Map<String, Object> getChangedVariable(
            BlockingQueue<ProcessVariableChangedEvent> variableChangedQueue, String variableName)
            throws InterruptedException {
        Map<String, Object> completionResult;
        while (true) {
            ProcessVariableChangedEvent event = variableChangedQueue.take();
            if (event.getVariableId().equals(variableName)) {
                return (Map<String, Object>) event.getNewValue();
            }
        }
    }

    /**
     * see https://github.com/kiegroup/jbpm/blob/master/jbpm-services/jbpm-kie-services/src/main/java/org/jbpm/kie/services/impl/utils/DefaultKieServiceConfigurator.java
     */
    private void buildJbpmServices(EntityManagerFactory emf) {
        TaskService taskService = HumanTaskServiceFactory.newTaskServiceConfigurator().entityManagerFactory(emf).getTaskService();

        // build definition service
        DefinitionService bpmn2Service = new BPMN2DataServiceImpl();

        System.setProperty("org.kie.executor.jms", "false");
        ExecutorService executorService = ExecutorServiceFactory.newExecutorService(emf);
        executorService.setInterval(1);
        executorService.init();
        ServiceRegistry.get().register(ServiceRegistry.EXECUTOR_SERVICE, executorService);

        QueryService queryService = new QueryServiceImpl();
//        ((QueryServiceImpl) queryService).setIdentityProvider(identityProvider);
        ((QueryServiceImpl) queryService).setUserGroupCallback(userGroupCallback);
        ((QueryServiceImpl) queryService).setCommandService(new TransactionalCommandService(emf));
        ((QueryServiceImpl) queryService).init();

        DeploymentService deploymentService = new KModuleDeploymentService();
        ((KModuleDeploymentService) deploymentService).setBpmn2Service(bpmn2Service);
        ((KModuleDeploymentService) deploymentService).setEmf(emf);
//        ((KModuleDeploymentService) deploymentService).setIdentityProvider(identityProvider);
        ((KModuleDeploymentService) deploymentService).setManagerFactory(new RuntimeManagerFactoryImpl());

        DeployedUnitImpl deployedUnit = new DeployedUnitImpl(null);
        deployedUnit.setRuntimeManager(manager);

        ((KModuleDeploymentService) deploymentService).getDeploymentsMap().put("default-per-pinstance", deployedUnit);
//        ((KModuleDeploymentService) deploymentService).setFormManagerService(formManagerService);

        // build runtime data service
        RuntimeDataService runtimeDataService = new RuntimeDataServiceImpl();
        ((RuntimeDataServiceImpl) runtimeDataService).setCommandService(new TransactionalCommandService(emf));
//        ((RuntimeDataServiceImpl) runtimeDataService).setIdentityProvider(identityProvider);
        ((RuntimeDataServiceImpl) runtimeDataService).setTaskService(taskService);
        ((RuntimeDataServiceImpl) runtimeDataService).setTaskAuditService(TaskAuditServiceFactory.newTaskAuditServiceConfigurator().setTaskService(taskService).getTaskAuditService());
        ((KModuleDeploymentService) deploymentService).setRuntimeDataService(runtimeDataService);
        ServiceRegistry.get().register(ServiceRegistry.RUNTIME_DATA_SERVICE, runtimeDataService);

        // set runtime data service as listener on deployment service
        ((KModuleDeploymentService) deploymentService).addListener(((RuntimeDataServiceImpl) runtimeDataService));
        ((KModuleDeploymentService) deploymentService).addListener(((BPMN2DataServiceImpl) bpmn2Service));
        ((KModuleDeploymentService) deploymentService).addListener(((QueryServiceImpl) queryService));

        // build process service
        ProcessService processService = new ProcessServiceImpl();
        ((ProcessServiceImpl) processService).setDataService(runtimeDataService);
        ServiceRegistry.get().register(ServiceRegistry.PROCESS_SERVICE, processService);
        ((ProcessServiceImpl) processService).setDeploymentService(deploymentService);
    }

}
