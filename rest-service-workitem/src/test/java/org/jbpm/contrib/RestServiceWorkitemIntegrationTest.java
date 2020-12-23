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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jbpm.contrib.bpm.TestFunctions;
import org.jbpm.contrib.demoservices.EventType;
import org.jbpm.contrib.demoservices.Service;
import org.jbpm.contrib.demoservices.ServiceListener;
import org.jbpm.contrib.demoservices.dto.PreBuildRequest;
import org.jbpm.contrib.demoservices.dto.Request;
import org.jbpm.contrib.demoservices.dto.Scm;
import org.jbpm.contrib.mockserver.WorkItems;
import org.jbpm.contrib.restservice.Constant;
import org.jbpm.contrib.restservice.RemoteInvocationException;
import org.jbpm.contrib.restservice.SimpleRestServiceWorkItemHandler;
import org.jbpm.contrib.restservice.util.Maps;
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
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jbpm.contrib.restservice.Constant.KIE_HOST_SYSTEM_PROPERTY;

/**
 * 
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author Ryszard Kozmik
 */
public class RestServiceWorkitemIntegrationTest extends JbpmJUnitBaseTestCase {

    private final Logger logger = LoggerFactory.getLogger(RestServiceWorkitemIntegrationTest.class);

    private static int PORT = 8080;
    private static String DEFAULT_HOST = "localhost";
    private Server server;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final ActiveTasks activeProcesses = new ActiveTasks();
    private final ServiceListener serviceListener = new ServiceListener();

    public RestServiceWorkitemIntegrationTest() {
        super(true, true);
    }

    @Before
    public void preTestSetup() throws Exception {
        System.setProperty(KIE_HOST_SYSTEM_PROPERTY, "localhost:8080");

        // Configure jBPM server with all the test processes, workitems and event listeners.
        setupPoolingDataSource();

        Map<String, ResourceType> resources = new HashMap<>();
        resources.put("execute-rest.bpmn", ResourceType.BPMN2);
        resources.put("test-process.bpmn", ResourceType.BPMN2);

        manager = createRuntimeManager(Strategy.PROCESS_INSTANCE, resources);
        customProcessListeners.add(new RestServiceProcessEventListener(activeProcesses));
        customHandlers.put("SimpleRestService", new SimpleRestServiceWorkItemHandler(manager));

        bootUpServices();
    }

    @After
    public void postTestTeardown() throws Exception {
        logger.info("Stopping http server ...");
        server.stop();
    }

    private void bootUpServices() throws Exception {
        ContextHandlerCollection contexts = new ContextHandlerCollection();

        server = new Server(PORT);
        server.setHandler(contexts);

        ServletContextHandler demoService = new ServletContextHandler(contexts, "/demo-service", ServletContextHandler.SESSIONS);
        ServletHolder servletHolder = demoService.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        servletHolder.setInitOrder(0);
        // Tells the Jersey Servlet which REST service/class to load.
        servletHolder.setInitParameter("jersey.config.server.provider.classnames", Service.class.getCanonicalName());
        demoService.setAttribute("listener", serviceListener);

        // JBPM server mock
        ServletContextHandler jbpmMock = new ServletContextHandler(contexts, "/services/rest", ServletContextHandler.SESSIONS);
        jbpmMock.setAttribute("runtimeManager", manager);
        ServletHolder jbpmMockServlet = jbpmMock.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jbpmMockServlet.setInitOrder(0);
        jbpmMockServlet.setInitParameter("jersey.config.server.provider.classnames", WorkItems.class.getCanonicalName());
        server.start();
    }

    @Test (timeout=15000)
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
        Map<String,Object> labels = new HashMap<>();
        labels.put("A", 1);
        labels.put("lines", "two\nlines");
        labels.put("quote", "String \"literal\".");

        ProcessInstance processInstance = kieSession.startProcess(
                "testProcess",
                Collections.singletonMap("in_initData", getProcessParameters(1, 30, 1, 30, labels)));

        manager.disposeRuntimeEngine(runtimeEngine);

        //then
        //skip variable initialization
        variableChangedQueue.take(); //preBuildResult
        variableChangedQueue.take(); //buildResult

        Map<String, Object> preBuildCallbackResult  = (Map<String, Object>) variableChangedQueue.take().getNewValue();
        logger.info("preBuildCallbackResult: " + preBuildCallbackResult);
        Map<String, Object> preBuildResponse = Maps.getStringObjectMap(preBuildCallbackResult, "response");
        Assert.assertEquals("new-scm-tag", Maps.getStringObjectMap(preBuildResponse, "scm").get("revision"));
        Map<String, Object> initialResponse = Maps.getStringObjectMap(preBuildCallbackResult, "initialResponse");
        Assert.assertTrue(initialResponse.get("cancelUrl").toString().startsWith("http://localhost:8080/demo-service/service/cancel/"));

        Map<String, Object> buildCallbackResult  = (Map<String, Object>) variableChangedQueue.take().getNewValue();
        logger.info("buildCallbackResult: " + buildCallbackResult);
        Map<String, Object> buildResponse = Maps.getStringObjectMap(preBuildCallbackResult, "response");
        Assert.assertEquals("SUCCESS", buildResponse.get("status"));

        Map<String, Object> completionResult  = (Map<String, Object>) variableChangedQueue.take().getNewValue();
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

    @Test (timeout=20000)
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
                Collections.singletonMap("in_initData", processParameters));

        manager.disposeRuntimeEngine(runtimeEngine);

        //then
        //skip variable initialization
        variableChangedQueue.take(); //preBuildResult

        Map<String, Object> preBuildCallbackResult  = (Map<String, Object>) variableChangedQueue.take().getNewValue();
        logger.info("preBuildCallbackResult: " + preBuildCallbackResult);
        RemoteInvocationException exception = (RemoteInvocationException) preBuildCallbackResult.get("error");
        logger.info("Expected exception: {}.", exception.getMessage());
        Assert.assertNotNull(exception);

        customProcessListeners.remove(processEventListener);
    }

    @Test (timeout=20000)
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
                Collections.singletonMap("in_initData", processParameters));

        manager.disposeRuntimeEngine(runtimeEngine);

        //then
        //skip variable initialization
        variableChangedQueue.take(); //preBuildResult
        variableChangedQueue.take(); //retryAttempt
        variableChangedQueue.take(); //retryAttempt
        variableChangedQueue.take(); //retryAttempt

        Integer retryAttempt = (Integer) variableChangedQueue.take().getNewValue();//retryAttempt
        Assert.assertEquals("3", retryAttempt.toString());

        Map<String, Object> preBuildCallbackResult  = (Map<String, Object>) variableChangedQueue.take().getNewValue();
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
    @Test
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
                Collections.singletonMap("in_initData", getProcessParameters(10, 30, 1, 30, Collections.emptyMap())));
        manager.disposeRuntimeEngine(runtimeEngine);

        //skip variable initialization
        variableChangedQueue.take(); //preBuildResult

        //then wait for first service to start
        variableChangedQueue.take().getNewValue();
        RuntimeEngine runtimeEngineCancel = getRuntimeEngine(processInstance.getId());
        logger.info("Signalling cancel ...");
        runtimeEngineCancel.getKieSession().signalEvent(Constant.CANCEL_SIGNAL_TYPE, null);
        manager.disposeRuntimeEngine(runtimeEngineCancel);

        Map<String, Object> preBuildCallbackResult  = (Map<String, Object>) variableChangedQueue.take().getNewValue();
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
        Assert.assertTrue(buildRequested.get() == 0);

        customProcessListeners.remove(processEventListener);
        serviceListener.unsubscribe(subscription);
    }

    /**
     * The test will execute a process with just one task that is set with 2s timeout while the REST service invoked in it is set with 10sec callback time.
     * After 2 seconds the timeout process will kick in by finishing the REST workitem and setting the information that it has failed.
     */
    @Test(timeout=15000)
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

        //when
        ProcessInstance processInstance = kieSession.startProcess(
                "testProcess",
                Collections.singletonMap("in_initData", getProcessParameters(10, 2, 1, 30, Collections.emptyMap())));
        manager.disposeRuntimeEngine(runtimeEngine);
        //skip variable initialization
        variableChangedQueue.take(); //preBuildResult

        //then wait for first service to start
        variableChangedQueue.take().getNewValue();

        Map<String, Object> preBuildResult  = (Map<String, Object>) variableChangedQueue.take().getNewValue();
        logger.info("preBuildResult: " + preBuildResult);
        Map<String, Object> preBuildResponse = Maps.getStringObjectMap(preBuildResult, "response");
        Assert.assertEquals("true", preBuildResponse.get("cancelled"));
        Assert.assertEquals("TIMED_OUT", preBuildResult.get("status"));

        //make sure final task (send result) has been called
        //use getChangedVariable because of there are multiple events of preBuildResult in case of cancel
        //TODO make sure it's correct that there are multiple events of preBuildResult in case of cancel
        Map<String, Object> completionResult = getChangedVariable(variableChangedQueue, "completionResult");
        logger.info("completionResult: " + completionResult);
        Assert.assertEquals("SUCCESS", completionResult.get("status"));

        activeProcesses.waitAllCompleted();
        callbackCompleted.acquire();

        customProcessListeners.remove(processEventListener);
        serviceListener.unsubscribe(subscription);
    }

    /**
     * Exceptions are expected in the log as callback is executed after the cancel completed.
     */
    @Test(timeout=15000)
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
                Collections.singletonMap("in_initData", getProcessParameters(10, 2, 10, 2, Collections.emptyMap())));
        manager.disposeRuntimeEngine(runtimeEngine);
        //skip variable initialization
        variableChangedQueue.take();

        //then wait for first service to start
        variableChangedQueue.take().getNewValue(); //preBuildResult

        Map<String, Object> preBuildResult  = (Map<String, Object>) variableChangedQueue.take().getNewValue();
        logger.info("preBuildResult: " + preBuildResult);
        Map<String, Object> preBuildResponse = Maps.getStringObjectMap(preBuildResult, "response");
        Assert.assertEquals("TIMED_OUT", preBuildResult.get("status"));

        activeProcesses.waitAllCompleted();
        callbackCompleted.acquire();

        customProcessListeners.remove(processEventListener);
        serviceListener.unsubscribe(subscription);
    }

    @Test(timeout=15000)
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

        ProcessInstance processInstance = (ProcessInstance) kieSession.startProcess("kogito.executerest", getExecuteRestParameters());
        manager.disposeRuntimeEngine(runtimeEngine);

        boolean completed = processFinished.tryAcquire(15, TimeUnit.SECONDS);
        if (!completed) {
            Assert.fail("Failed to complete the process.");
        }

        Map<String, Object> result  = (Map<String, Object>) variableChangedQueue.take().getNewValue();
        logger.info("result: " + result);
        Assert.assertEquals("new-scm-tag", Maps.getStringObjectMap(Maps.getStringObjectMap(result, "response"), "scm").get("revision"));

        customProcessListeners.remove(processEventListener);
    }

    @Test(timeout=15000)
    public void shouldFailWhenThereIsNoHeartBeat() throws InterruptedException {
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
                    Collections.singletonMap("in_initData", getProcessParameters(10, 10, 10, 2, 2, 4, Collections.emptyMap())));
            manager.disposeRuntimeEngine(runtimeEngine);
            //skip variable initialization
            variableChangedQueue.take();

             Map<String, Object> preBuildResult  = (Map<String, Object>) variableChangedQueue.take().getNewValue();
            logger.info("preBuildResult: " + preBuildResult);
            Map<String, Object> preBuildResponse = Maps.getStringObjectMap(preBuildResult, "response");
            Assert.assertEquals("DIED", preBuildResult.get("status"));

            activeProcesses.waitAllCompleted();
            callbackCompleted.acquire();

            customProcessListeners.remove(processEventListener);
            serviceListener.unsubscribe(subscription);
        } finally {
            TestFunctions.addHeartBeatToRequest = false;
        }

    }

    private Map<String, Object> getExecuteRestParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("requestMethod", "POST");
        parameters.put("requestHeaders", null);
        parameters.put("requestUrl", "http://localhost:8080/demo-service/service/prebuild");
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
        return getProcessParameters(preBuildCallbackDelay, preBuildTimeout, cancelDelay, preBuildCancelTimeout, 10, 0, labels);
    }

    private Map<String, Object> getProcessParameters(
            int preBuildCallbackDelay,
            int preBuildTimeout,
            int cancelDelay,
            int preBuildCancelTimeout,
            int cancelHeartBeatAfter,
            int heartbeatTimeout,
            Map<String, Object> labels) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("serviceBaseUrl", "http://localhost:8080/demo-service/service");
        parameters.put("preBuildServiceUrl", "http://localhost:8080/demo-service/service/prebuild?"
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
}
