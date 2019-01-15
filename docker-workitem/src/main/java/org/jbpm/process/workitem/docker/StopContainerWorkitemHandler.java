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

import com.github.dockerjava.api.DockerClient;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "DockerStopContainerDefinitions.wid", name = "DockerStopContainer",
        displayName = "DockerStopContainer",
        defaultHandler = "mvel: new org.jbpm.process.workitem.docker.StopContainerWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "DockerStopContainer.png",
        parameters = {
                @WidParameter(name = "ContainerId", required = true)
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "Docker,stop,container",
                action = @WidAction(title = "Stop Docker container")
        ))
public class StopContainerWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(StopContainerWorkitemHandler.class);

    private DockerClient dockerClient;

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String containerId = (String) workItem.getParameter("ContainerId");

            if (dockerClient == null) {
                DockerClientConnector connector = new DockerClientConnector();
                dockerClient = connector.getDockerClient();
            }

            dockerClient.stopContainerCmd(containerId).exec();

            workItemManager.completeWorkItem(workItem.getId(),
                                             null);
        } catch (Exception e) {
            logger.error("Unable to stop container: " + e.getMessage());
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

