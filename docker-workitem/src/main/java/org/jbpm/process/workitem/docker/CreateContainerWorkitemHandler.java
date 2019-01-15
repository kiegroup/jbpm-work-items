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

import java.util.HashMap;
import java.util.Map;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.PortBinding;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "DockerCreateContainerDefinitions.wid", name = "DockerCreateContainer",
        displayName = "DockerCreateContainer",
        defaultHandler = "mvel: new org.jbpm.process.workitem.docker.CreateContainerWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "DockerCreateContainer.png",
        parameters = {
                @WidParameter(name = "ContainerName", required = true),
                @WidParameter(name = "ContainerImageName", required = true),
                @WidParameter(name = "ContainerCommand"),
                @WidParameter(name = "ContainerHostName"),
                @WidParameter(name = "ContainerEnv"),
                @WidParameter(name = "ContainerPortBindings"),
                @WidParameter(name = "ContainerBinds"),
        },
        results = {
                @WidResult(name = "ContainerId")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "Docker,create,container",
                action = @WidAction(title = "Create Docker container")
        ))
public class CreateContainerWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateContainerWorkitemHandler.class);
    private static final String RESULTS_DOCUMENT = "ContainerId";

    private DockerClient dockerClient;

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        Map<String, Object> results = new HashMap<String, Object>();

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String containerName = (String) workItem.getParameter("ContainerName");
            String containerImageName = (String) workItem.getParameter("ContainerImageName");
            String containerCommand = (String) workItem.getParameter("ContainerCommand");
            String containerHostName = (String) workItem.getParameter("ContainerHostName");
            String containerEnv = (String) workItem.getParameter("ContainerEnv");
            String containerPortBindings = (String) workItem.getParameter("ContainerPortBindings");
            String containerBinds = (String) workItem.getParameter("ContainerBinds");

            if (dockerClient == null) {
                DockerClientConnector connector = new DockerClientConnector();
                dockerClient = connector.getDockerClient();
            }

            CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd(containerImageName).withName(containerName);

            if (containerCommand != null) {
                createContainerCmd = createContainerCmd.withCmd(containerCommand);
            }

            if (containerHostName != null) {
                createContainerCmd = createContainerCmd.withHostName(containerHostName);
            }

            if (containerEnv != null) {
                createContainerCmd = createContainerCmd.withEnv(containerEnv);
            }

            if (containerPortBindings != null) {
                createContainerCmd = createContainerCmd.withPortBindings(PortBinding.parse(containerPortBindings));
            }

            if (containerBinds != null) {
                createContainerCmd = createContainerCmd.withBinds(Bind.parse(containerBinds));
            }

            CreateContainerResponse container = createContainerCmd.exec();

            if (container != null && container.getId() != null) {
                results.put(RESULTS_DOCUMENT,
                            container.getId());
            }

            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
        } catch (Exception e) {
            logger.error("Unable to create container: " + e.getMessage());
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setDockerClient(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }
}
