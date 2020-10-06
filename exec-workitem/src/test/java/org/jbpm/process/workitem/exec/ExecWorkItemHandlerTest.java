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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExecWorkItemHandlerTest {

    @Test
    public void testExecCommand() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Command",
                              "java -version");
        ExecWorkItemHandler handler = new ExecWorkItemHandler();
        handler.setLogThrownException(true);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        String result = (String) results.get(ExecWorkItemHandler.RESULT);

        assertEquals("[java, -version]",
                     handler.getParsedCommandStr());

        assertNotNull(result);
        assertTrue(result.contains("java version") || result.contains("jdk version"));
    }

    @Test
    public void testExecCommandWithArguments() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Command",
                              "java");
        List<String> argumentList = new ArrayList<>();
        argumentList.add("-version");
        workItem.setParameter("Arguments",
                              argumentList);
        ExecWorkItemHandler handler = new ExecWorkItemHandler();
        handler.setLogThrownException(true);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        String result = (String) results.get(ExecWorkItemHandler.RESULT);

        assertEquals("[java, -version]",
                     handler.getParsedCommandStr());

        assertNotNull(result);
        assertTrue(result.contains("java version") || result.contains("jdk version"));
    }
    
	@Test(timeout = 6000)
	public void testExecCommandWithTimeout() throws Exception {

		TestWorkItemManager manager = new TestWorkItemManager();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setParameter("Command",
		                      "ping");
		List<String> argumentList = new ArrayList<>();
		argumentList.add("127.0.0.1");
		workItem.setParameter("Arguments",
							argumentList);
		workItem.setParameter("TimeoutInMillis", 
								"PT5S");
		ExecWorkItemHandler handler = new ExecWorkItemHandler();
		handler.setLogThrownException(true);

		handler.executeWorkItem(workItem,
								manager);
		
		assertNotNull(manager.getResults());
		assertEquals(1,
				manager.getResults().size());
		assertTrue(manager.getResults().containsKey(workItem.getId()));

		Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
		String result = (String) results.get(ExecWorkItemHandler.RESULT);

		assertEquals("[ping, 127.0.0.1]",
				      handler.getParsedCommandStr());

		assertNotNull(result);
		assertTrue(result.contains("A timeout occured"));

	}

    @Test
    public void testExecCommandInvalidParam() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        ExecWorkItemHandler handler = new ExecWorkItemHandler();
        handler.setLogThrownException(true);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(0,
                manager.getResults().size());
    }
}
