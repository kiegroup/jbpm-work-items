/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.process.workitem.docker;

import java.util.ArrayList;
import java.util.List;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.KillContainerCmd;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.jbpm.process.workitem.docker.responses.SerializableInspectContainerResponse;
import org.jbpm.test.AbstractBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DockerWorkitemHandlerTest extends AbstractBaseTest {

    @Mock
    DockerClient dockerClient;

    @Mock
    CreateContainerCmd createContainerCmd;

    @Mock
    CreateContainerResponse createContainerResponse;

    @Mock
    InspectContainerCmd inspectContainerCmd;

    @Mock
    InspectContainerResponse inspectContainerResponse;

    @Mock
    KillContainerCmd killContainerCmd;

    @Mock
    ListContainersCmd listContainersCmd;

    @Mock
    ListImagesCmd listImagesCmd;

    @Mock
    StartContainerCmd startContainerCmd;

    @Mock
    StopContainerCmd stopContainerCmd;

    @Before
    public void setUp() {
        try {
            List<Container> testContainers = new ArrayList<>();
            List<Image> testImages = new ArrayList<>();

            when(dockerClient.createContainerCmd(anyString())).thenReturn(createContainerCmd);
            when(createContainerCmd.withName(anyString())).thenReturn(createContainerCmd);
            when(createContainerCmd.exec()).thenReturn(createContainerResponse);
            when(createContainerResponse.getId()).thenReturn("1");

            when(dockerClient.inspectContainerCmd(anyString())).thenReturn(inspectContainerCmd);
            when(inspectContainerCmd.exec()).thenReturn(inspectContainerResponse);

            when(dockerClient.killContainerCmd(anyString())).thenReturn(killContainerCmd);

            when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
            when(listContainersCmd.withShowAll(any(Boolean.class))).thenReturn(listContainersCmd);
            when(listContainersCmd.withShowSize(any(Boolean.class))).thenReturn(listContainersCmd);
            when(listContainersCmd.exec()).thenReturn(testContainers);

            when(dockerClient.listImagesCmd()).thenReturn(listImagesCmd);
            when(listImagesCmd.exec()).thenReturn(testImages);

            when(dockerClient.startContainerCmd(anyString())).thenReturn(startContainerCmd);

            when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testDockerCreateContainer() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ContainerName",
                              "testContainerName");
        workItem.setParameter("ContainerImageName",
                              "testContainerImageName");

        CreateContainerWorkitemHandler handler = new CreateContainerWorkitemHandler();
        handler.setDockerClient(dockerClient);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("ContainerId") instanceof String);
    }

    @Test
    public void testDockerInspectContainer() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ContainerId",
                              "testContainerId");

        InspectContainerWorkitemHandler handler = new InspectContainerWorkitemHandler();
        handler.setDockerClient(dockerClient);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("ContainerInfo") instanceof SerializableInspectContainerResponse);
    }

    @Test
    public void testDockerKillContainer() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ContainerId",
                              "testContainerId");

        KillContainerWorkitemHandler handler = new KillContainerWorkitemHandler();
        handler.setDockerClient(dockerClient);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }

    @Test
    public void testDockerListContainers() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        ListContainersWorkitemHandler handler = new ListContainersWorkitemHandler();
        handler.setDockerClient(dockerClient);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("Containers") instanceof List);
    }

    @Test
    public void testDockerListImages() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        ListImagesWorkitemHandler handler = new ListImagesWorkitemHandler();
        handler.setDockerClient(dockerClient);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("Images") instanceof List);
    }

    @Test
    public void testDockerStartContainer() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ContainerId",
                              "testContainerId");

        StartContainerWorkitemHandler handler = new StartContainerWorkitemHandler();
        handler.setDockerClient(dockerClient);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }

    @Test
    public void testDockerStopContainer() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ContainerId",
                              "testContainerId");

        StopContainerWorkitemHandler handler = new StopContainerWorkitemHandler();
        handler.setDockerClient(dockerClient);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }
}
