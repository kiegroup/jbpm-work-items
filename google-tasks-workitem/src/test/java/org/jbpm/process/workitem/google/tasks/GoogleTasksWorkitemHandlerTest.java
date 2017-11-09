/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.process.workitem.google.tasks;

import java.util.List;

import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.jbpm.test.AbstractBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GoogleTasksWorkitemHandlerTest extends AbstractBaseTest {

    @Mock
    GoogleTasksAuth auth;

    @Mock
    Tasks tasksService;

    @Mock
    Tasks.Tasklists taskLists;

    @Mock
    Tasks.Tasklists.List taskListsList;

    @Mock
    Tasks.Tasklists.Insert taskListsInsert;

    @Before
    public void setUp() {
        try {
            List<TaskList> testTaskList = new java.util.ArrayList<>();
            TaskList listOne = new TaskList();
            listOne.setTitle("buy groceries");
            listOne.setKind("home task");
            TaskList listTwo = new TaskList();
            listTwo.setTitle("pickup kid from school");
            listTwo.setKind("home task");
            testTaskList.add(listOne);
            testTaskList.add(listTwo);

            TaskLists taskListsModel = new TaskLists();
            taskListsModel.setItems(testTaskList);

            when(auth.getTasksService(anyString(),
                                      anyString())).thenReturn(tasksService);
            when(tasksService.tasklists()).thenReturn(taskLists);
            when(taskLists.list()).thenReturn(taskListsList);
            when(taskLists.insert(any(TaskList.class))).thenReturn(taskListsInsert);
            when(taskListsList.setMaxResults(anyLong())).thenReturn(taskListsList);
            when(taskListsList.execute()).thenReturn(taskListsModel);
            when(taskListsInsert.execute()).thenReturn(listOne);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetTasks() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("NumOfTasks",
                              "10");

        GetTasksWorkitemHandler handler = new GetTasksWorkitemHandler("testAppName",
                                                                      "{}");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        List<TaskInfo> returnedTasks = (List<TaskInfo>) (manager.getResults().get(workItem.getId())).get("FoundTasks");
        assertNotNull(returnedTasks);
        assertEquals(2,
                     returnedTasks.size());
        assertEquals("buy groceries",
                     returnedTasks.get(0).getTitle());
        assertEquals("pickup kid from school",
                     returnedTasks.get(1).getTitle());
    }

    @Test
    public void testAddTasks() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("TaskName",
                              "buy groceries");
        workItem.setParameter("TaskKind",
                              "home task");

        AddTaskWorkitemHandler handler = new AddTaskWorkitemHandler("testAppName",
                                                                    "{}");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }
}
