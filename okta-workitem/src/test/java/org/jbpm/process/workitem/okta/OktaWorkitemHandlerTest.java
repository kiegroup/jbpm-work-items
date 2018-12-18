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
package org.jbpm.process.workitem.okta;

import com.okta.sdk.client.Client;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class OktaWorkitemHandlerTest {

    @Mock
    Client oktaClient;

    @Test
    public void testAddUserToGroup() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("UserId",
                              "testUserId");
        workItem.setParameter("GroupId",
                              "testGroupId");

        AddUserToGroupWorkitemHandler handler = new AddUserToGroupWorkitemHandler(oktaClient);
        handler.setLogThrownException(true);
        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
    }

    @Test
    public void testCreateGroup() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("GroupName",
                              "testGroupName");
        workItem.setParameter("GroupDescription",
                              "testGroupDescription");

        CreateGroupWorkitemHandler handler = new CreateGroupWorkitemHandler(oktaClient);
        handler.setLogThrownException(true);
        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
    }

    @Test
    public void testCreateUser() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("UserEmail",
                              "testUserEmail");
        workItem.setParameter("UserFirstName",
                              "testUserFirstName");
        workItem.setParameter("UserLastName",
                              "testUserLastName");

        CreateUserWorkitemHandler handler = new CreateUserWorkitemHandler(oktaClient);
        handler.setLogThrownException(true);
        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
    }

    @Test
    public void testGetApplication() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("AppIds",
                              "testAppId1,testAppId2");

        GetApplicationsWorkitemHandler handler = new GetApplicationsWorkitemHandler(oktaClient);
        handler.setLogThrownException(true);
        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
    }

    @Test
    public void testGetUsers() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("UserIds",
                              "testUserId1,testUserId2");

        GetUsersWorkitemHandler handler = new GetUsersWorkitemHandler(oktaClient);
        handler.setLogThrownException(true);
        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
    }
}
