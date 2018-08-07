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
package org.jbpm.process.workitem.mavenembedder;

import java.io.File;
import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.bpmn2.handler.WorkItemHandlerRuntimeException;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.jbpm.test.AbstractBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RuntimeManagerRegistry.class)
public class MavenEmbedderWorkitemHandlerTest extends AbstractBaseTest {

    @Before
    public void setup() throws Exception {
        RuntimeManager runtimeManager = PowerMockito.mock(RuntimeManager.class);
        RuntimeEngine engine = PowerMockito.mock(RuntimeEngine.class);
        KieSession session = PowerMockito.mock(KieSession.class);
        WorkItemManager workitemManager = PowerMockito.mock(WorkItemManager.class);
        RuntimeManagerRegistry runtimeManagerRegistry = PowerMockito.mock(RuntimeManagerRegistry.class);

        PowerMockito.mockStatic(RuntimeManagerRegistry.class);

        when(RuntimeManagerRegistry.get()).thenReturn(runtimeManagerRegistry);
        when(runtimeManagerRegistry.getManager(anyString())).thenReturn(runtimeManager);
        when(runtimeManager.getRuntimeEngine(any(Context.class))).thenReturn(engine);
        when(engine.getKieSession()).thenReturn(session);
        when(session.getWorkItemManager()).thenReturn(workitemManager);
        doNothing().when(workitemManager).completeWorkItem(anyLong(),
                                                           any(Map.class));
        doNothing().when(runtimeManager).disposeRuntimeEngine(any(RuntimeEngine.class));
    }

    @Test
    public void testCleanInstallSimpleProjectSync() {
        TestWorkItemManager manager = new TestWorkItemManager();
        File simpleTestProjectDir = new File("src/test/resources/simple");

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Goals",
                              "clean install");
        workItem.setParameter("WorkDirectory",
                              simpleTestProjectDir.getAbsolutePath());
        workItem.setParameter("ProjectRoot",
                              simpleTestProjectDir.getAbsolutePath());

        MavenEmbedderWorkItemHandler handler = new MavenEmbedderWorkItemHandler();

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("MavenResults") instanceof Map);

        Map<String, String> mavenResults = (Map<String, String>) manager.getResults().get(workItem.getId()).get("MavenResults");
        assertNotNull(mavenResults);
        assertEquals("",
                     mavenResults.get("stderr"));

        // make sure the build ran - check the build dirs
        assertTrue(new File("src/test/resources/simple/target").exists());
        assertTrue(new File("src/test/resources/simple/target/classes").exists());

        // execute workitem handler with "clean" goal to clean the test project and check
        workItem.setParameter("Goals",
                              "clean");

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        // make sure the sample maven project was cleaned
        assertFalse(new File("src/test/resources/simple/target").exists());
        assertFalse(new File("src/test/resources/simple/target/classes").exists());
    }

    @Test
    public void testCleanInstallSimpleProjectAsync() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        File simpleTestProjectDir = new File("src/test/resources/simple");

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Goals",
                              "clean install");
        workItem.setParameter("WorkDirectory",
                              simpleTestProjectDir.getAbsolutePath());
        workItem.setParameter("ProjectRoot",
                              simpleTestProjectDir.getAbsolutePath());
        workItem.setParameter("Mode",
                              "async");

        MavenEmbedderWorkItemHandler handler = new MavenEmbedderWorkItemHandler();

        handler.executeWorkItem(workItem,
                                manager);

        Thread.sleep(10000); // wait for async build to complete

        // make sure the build ran - check the build dirs
        assertTrue(new File("src/test/resources/simple/target").exists());
        assertTrue(new File("src/test/resources/simple/target/classes").exists());

        // execute workitem handler with "clean" goal to clean the test project and check
        workItem.setParameter("Goals",
                              "clean");

        handler.executeWorkItem(workItem,
                                manager);

        Thread.sleep(10000); // wait for async build to complete

        // make sure the sample maven project was cleaned
        assertFalse(new File("src/test/resources/simple/target").exists());
        assertFalse(new File("src/test/resources/simple/target/classes").exists());        
    }

    @Test
    public void testCleanInstallSimpleProjecSynctWithCLOptions() {
        TestWorkItemManager manager = new TestWorkItemManager();
        File simpleTestProjectDir = new File("src/test/resources/simple");

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Goals",
                              "clean install");
        workItem.setParameter("CLOptions",
                              "-DskipTests -X");
        workItem.setParameter("WorkDirectory",
                              simpleTestProjectDir.getAbsolutePath());
        workItem.setParameter("ProjectRoot",
                              simpleTestProjectDir.getAbsolutePath());

        MavenEmbedderWorkItemHandler handler = new MavenEmbedderWorkItemHandler();

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("MavenResults") instanceof Map);

        Map<String, String> mavenResults = (Map<String, String>) manager.getResults().get(workItem.getId()).get("MavenResults");
        assertNotNull(mavenResults);
        assertEquals("",
                     mavenResults.get("stderr"));
        // with -X debug option we should get a stdout message
        assertTrue(mavenResults.get("stdout").startsWith("Apache Maven"));

        // make sure the build ran - check the build dirs
        assertTrue(new File("src/test/resources/simple/target").exists());
        assertTrue(new File("src/test/resources/simple/target/classes").exists());

        // execute workitem handler with "clean" command to clean the test project and check
        workItem.setParameter("Goals",
                              "clean");

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        // make sure the sample maven project was cleaned
        assertFalse(new File("src/test/resources/simple/target").exists());
        assertFalse(new File("src/test/resources/simple/target/classes").exists());
    }

    @Test
    public void testCleanInstallSimpleProjecAsynctWithCLOptions() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        File simpleTestProjectDir = new File("src/test/resources/simple");

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Goals",
                              "clean install");
        workItem.setParameter("CLOptions",
                              "-DskipTests -X");
        workItem.setParameter("WorkDirectory",
                              simpleTestProjectDir.getAbsolutePath());
        workItem.setParameter("ProjectRoot",
                              simpleTestProjectDir.getAbsolutePath());
        workItem.setParameter("Mode",
                              "async");

        MavenEmbedderWorkItemHandler handler = new MavenEmbedderWorkItemHandler();

        handler.executeWorkItem(workItem,
                                manager);

        Thread.sleep(10000); // wait for async build to complete

        // make sure the build ran - check the build dirs
        assertTrue(new File("src/test/resources/simple/target").exists());
        assertTrue(new File("src/test/resources/simple/target/classes").exists());

        // execute workitem handler with "clean" command to clean the test project and check
        workItem.setParameter("Goals",
                              "clean");

        handler.executeWorkItem(workItem,
                                manager);

        Thread.sleep(10000); // wait for async build to complete

        // make sure the sample maven project was cleaned
        assertFalse(new File("src/test/resources/simple/target").exists());
        assertFalse(new File("src/test/resources/simple/target/classes").exists());
    }

    @Test(expected = WorkItemHandlerRuntimeException.class)
    public void testCleanInstallSimpleProjectWithInvalidParams() {
        TestWorkItemManager manager = new TestWorkItemManager();

        WorkItemImpl workItem = new WorkItemImpl();

        MavenEmbedderWorkItemHandler handler = new MavenEmbedderWorkItemHandler();

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(0,
                     manager.getResults().size());
    }
}
