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
import java.util.List;
import java.util.Map;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.model.Container;
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

import static java.util.Collections.singletonList;

@Wid(widfile = "DockerListContainersDefinitions.wid", name = "DockerListContainers",
        displayName = "DockerListContainers",
        defaultHandler = "mvel: new org.jbpm.process.workitem.docker.ListContainersWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "DockerListContainers.png",
        parameters = {
                @WidParameter(name = "StatusFilter")
        },
        results = {
                @WidResult(name = "Containers", runtimeType = "java.util.List")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "Docker,list,containers",
                action = @WidAction(title = "List Docker containers")
        ))
public class ListContainersWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(ListContainersWorkitemHandler.class);
    private static final String RESULTS_DOCUMENT = "Containers";

    private DockerClient dockerClient;

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        Map<String, Object> results = new HashMap<>();

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String statusFilter = (String) workItem.getParameter("StatusFilter");

            if (dockerClient == null) {
                DockerClientConnector connector = new DockerClientConnector();
                dockerClient = connector.getDockerClient();
            }

            ListContainersCmd listContainersCmd = dockerClient.listContainersCmd()
                    .withShowAll(true).withShowSize(true);

            if (statusFilter != null && statusFilter.trim().length() > 0) {
                listContainersCmd = listContainersCmd.withStatusFilter(singletonList(statusFilter));
            }

            List<Container> containers = listContainersCmd.exec();

            results.put(RESULTS_DOCUMENT,
                        containers);

            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
        } catch (Exception e) {
            logger.error("Unable to get list of containers: " + e.getMessage());
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
