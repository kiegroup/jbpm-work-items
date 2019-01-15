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
import com.github.dockerjava.api.command.InspectContainerResponse;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.jbpm.process.workitem.docker.responses.SerializableInspectContainerResponse;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "DockerInspectContainerDefinitions.wid", name = "DockerInspectContainer",
        displayName = "DockerInspectContainer",
        defaultHandler = "mvel: new org.jbpm.process.workitem.docker.InspectContainerWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "DockerInspectContainer.png",
        parameters = {
                @WidParameter(name = "ContainerId", required = true)
        },
        results = {
                @WidResult(name = "ContainerInfo", runtimeType = "org.jbpm.process.workitem.docker.responses.SerializableInspectContainerResponse")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "Docker,inspect,container",
                action = @WidAction(title = "Inspect Docker container")
        ))
public class InspectContainerWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(InspectContainerWorkitemHandler.class);
    private static final String RESULTS_DOCUMENT = "ContainerInfo";

    private DockerClient dockerClient;

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        Map<String, Object> results = new HashMap<String, Object>();

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String containerId = (String) workItem.getParameter("ContainerId");

            if (dockerClient == null) {
                DockerClientConnector connector = new DockerClientConnector();
                dockerClient = connector.getDockerClient();
            }

            InspectContainerResponse containerResponse
                    = dockerClient.inspectContainerCmd(containerId).exec();

            results.put(RESULTS_DOCUMENT,
                        new SerializableInspectContainerResponse(containerResponse));

            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
        } catch (Exception e) {
            logger.error("Unable to inspect container: " + e.getMessage());
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
