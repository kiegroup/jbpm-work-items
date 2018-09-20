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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jbpm.contrib.demoservice.Service;
import org.jbpm.contrib.mockserver.JBPMServer;
import org.jbpm.contrib.mockserver.WorkItems;
import org.jbpm.process.workitem.WorkDefinitionImpl;
import org.jbpm.process.workitem.WorkItemRepository;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class RestServiceWorkitemIntegrationTest {

    private static int PORT = 8080;
    private static String DEFAULT_HOST = "localhost";
    private final Logger logger = LoggerFactory.getLogger(RestServiceWorkitemIntegrationTest.class);

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @BeforeClass
    public static void setUp() throws Exception {
        bootUpServices();
    }

    @AfterClass
    public static void cleanUp() {
        JBPMServer.cleanUp();
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
        //        servletHolder.setInitParameter("jersey.config.server.provider.packages", Service.class.getPackage().getName() + ",org.glassfish.jersey.moxy");
        //        servletHolder.setInitParameter("jersey.config.server.tracing", "ALL");

        ServletContextHandler jbpmMock = new ServletContextHandler(contexts, "/kie-server/services/rest", ServletContextHandler.SESSIONS);
        ServletHolder jbpmMockServlet = jbpmMock.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jbpmMockServlet.setInitOrder(0);
        jbpmMockServlet.setInitParameter("jersey.config.server.provider.classnames", WorkItems.class.getCanonicalName());
        server.start();
    }

    @Test
    public void testCallbackWithVariablePassing() throws InterruptedException {
        JBPMServer jbpmServer = JBPMServer.getInstance();
        KieSession kieSession = jbpmServer.getRuntimeEngine().getKieSession();

        final Map<String, String> processVariables = new HashMap<>();
        final Semaphore waitForProcessVariables = new Semaphore(-4);//5 variables set

        ProcessEventListener processEventListener = new DefaultProcessEventListener() {
            public void afterVariableChanged(ProcessVariableChangedEvent event) {
                logger.info("Variable changed {} = {}.", event.getVariableId(), event.getNewValue());
                processVariables.put(event.getVariableId(), event.getNewValue().toString());
                waitForProcessVariables.release();
            }
        };
        kieSession.addEventListener(processEventListener);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("containerId", "mock");
        ProcessInstance processInstance = (ProcessInstance)kieSession.startProcess("service-orchestration", parameters);

        waitForProcessVariables.acquire();

        kieSession.removeEventListener(processEventListener);

        processVariables.forEach((k,v) -> logger.info("Process variable {} : {}", k, v));
        Assert.assertEquals(5, processVariables.size());
        Assert.assertEquals("http://localhost:8080/demo-service/service/cancel/0", processVariables.get("serviceA-cancelUrl"));
        Assert.assertEquals("{person={name=Matej}}", processVariables.get("resultA"));
    }

    @Test
    public void testCancel() throws InterruptedException {
        JBPMServer jbpmServer = JBPMServer.getInstance();
        KieSession kieSession = jbpmServer.getRuntimeEngine().getKieSession();

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
        ProcessInstance processInstance = (ProcessInstance) kieSession.startProcess("service-orchestration", parameters);

        //wait for nodeA active
        nodeAActive.acquire();

        final String pid = Long.toString(processInstance.getId());
        executor.execute(() -> {
            logger.info("Signaling cancel for pid: {}.", pid);
            kieSession.signalEvent("cancel", pid);
        });
        nodeACompleted.tryAcquire(8, TimeUnit.SECONDS);
        kieSession.removeEventListener(processEventListener);
        logger.info("Cancelled A result: {}", resultA.get());
        Assert.assertEquals("{canceled=true}", resultA.get());
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
