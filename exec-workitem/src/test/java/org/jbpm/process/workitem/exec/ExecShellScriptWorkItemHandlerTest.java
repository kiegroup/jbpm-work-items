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

package org.jbpm.process.workitem.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.junit.Test;

public class ExecShellScriptWorkItemHandlerTest {

    @Test
    public void testExecShellScriptCommand() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ShellScriptLocation",
        		"src/test/resources/TestScript.sh");
        ExecShellScriptWorkItemHandler handler = new ExecShellScriptWorkItemHandler();
        handler.setLogThrownException(true);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        List<String> result = (List<String>) results.get(ExecWorkItemHandler.RESULT);

        assertNotNull(result);
        assertTrue(result.contains("Test Script Started") || result.contains("Test Script Ended"));
    }
    
    @Test
    public void testExecShellScriptCommandWithTimeout() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ShellScriptLocation",
        		"src/test/resources/TestScript.sh");
        workItem.setParameter("Timeout",
        		"0.1");
        ExecShellScriptWorkItemHandler handler = new ExecShellScriptWorkItemHandler();
        handler.setLogThrownException(true);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(0,
                     manager.getResults().size());

    }
    
    @Test
    public void testExecShellScriptCommandWithAddEnvironmentVariables() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ShellScriptLocation",
        		"src/test/resources/TestScript.sh");
        Map<String, String> envVariableList = new HashMap<>();
        envVariableList.put("testVariable","tested");
        workItem.setParameter("AddEnvironmentVariable",
        		envVariableList);
        ExecShellScriptWorkItemHandler handler = new ExecShellScriptWorkItemHandler();
        handler.setLogThrownException(true);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        List<String> result = (List<String>) results.get(ExecWorkItemHandler.RESULT);

        assertNotNull(result);
        assertTrue(result.contains("tested"));
    }
    
    @Test
    public void testExecShellScriptCommandWithRemoveEnvironmentVariables() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ShellScriptLocation",
        		"src/test/resources/TestScript.sh");
        
        Map<String, String> envVariableList = new HashMap<>();
        envVariableList.put("newVariable","variableRemoved");
        workItem.setParameter("AddEnvironmentVariable",
        		envVariableList);
        List<String> removeEnvList = new ArrayList<>();
        removeEnvList.add("newVariable");
        workItem.setParameter("RemoveEnvironmentVariable",
        		removeEnvList);
        ExecShellScriptWorkItemHandler handler = new ExecShellScriptWorkItemHandler();
        handler.setLogThrownException(true);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        List<String> result = (List<String>) results.get(ExecWorkItemHandler.RESULT);

        assertNotNull(result);
        assertTrue(!result.contains("variableRemoved"));
    }

   
    @Test
    public void testExecShellScriptCommandInvalidParam() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        ExecShellScriptWorkItemHandler handler = new ExecShellScriptWorkItemHandler();
        handler.setLogThrownException(true);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(0,
                     manager.getResults().size());
    }
   
}

