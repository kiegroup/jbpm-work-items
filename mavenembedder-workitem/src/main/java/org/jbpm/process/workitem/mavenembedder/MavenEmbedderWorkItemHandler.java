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

import java.util.Map;

import org.apache.maven.cli.KieMavenCli;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "MavenEmbedderDefinitions.wid", name = "MavenEmbedder",
        displayName = "MavenEmbedder",
        defaultHandler = "mvel: new org.jbpm.process.workitem.mavenembedder.MavenEmbedderWorkItemHandler()",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "MavenEmbedder.png",
        parameters = {
                @WidParameter(name = "Goals", required = true),
                @WidParameter(name = "CLOptions"), // command line options
                @WidParameter(name = "WorkDirectory", required = true),
                @WidParameter(name = "ProjectRoot", required = true),
                @WidParameter(name = "Mode")
        },
        results = {
                @WidResult(name = "MavenResults", runtimeType = "java.util.Map")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "maven,mvn,build,execute,pom,project,intall",
                action = @WidAction(title = "Execute Maven commands")
        ))
public class MavenEmbedderWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(MavenEmbedderWorkItemHandler.class);
    private static final String RESULTS_VALUES = "MavenResults";

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String goals = (String) workItem.getParameter("Goals");
            String cliOptionsParam = (String) workItem.getParameter("CLOptions");
            String commandLineOptions = cliOptionsParam == null ? "-X" : cliOptionsParam;

            String workDir = (String) workItem.getParameter("WorkDirectory");
            String projectRoot = (String) workItem.getParameter("ProjectRoot");
            String modeStr = (String) workItem.getParameter("Mode");
            MavenEmbedderUtils.MavenEmbedderMode mode = MavenEmbedderUtils.MavenEmbedderMode.valueOf(modeStr == null ? "SYNC" : modeStr.toUpperCase());

            logger.debug("About to execute maven {} with options {} with working directory {}",
                         goals,
                         commandLineOptions,
                         workDir);
            switch (mode) {
                case SYNC:

                    Map<String, Object> results = MavenEmbedderUtils.executeMavenGoals(new KieMavenCli(projectRoot),
                                                                                       RESULTS_VALUES,
                                                                                       projectRoot,
                                                                                       commandLineOptions,
                                                                                       goals,
                                                                                       workDir);

                    workItemManager.completeWorkItem(workItem.getId(),
                                                     results);
                    break;
                case ASYNC:
                    long workItemId = workItem.getId();
                    String deploymentId = ((WorkItemImpl) workItem).getDeploymentId() == null ? "" : ((WorkItemImpl) workItem).getDeploymentId();
                    long processInstanceId = workItem.getProcessInstanceId();

                    new Thread(new Runnable() {
                        public void run() {
                            try {

                                Map<String, Object> results = MavenEmbedderUtils.executeMavenGoals(new KieMavenCli(projectRoot),
                                                                                                   RESULTS_VALUES,
                                                                                                   projectRoot,
                                                                                                   commandLineOptions,
                                                                                                   goals,
                                                                                                   workDir);

                                RuntimeManager manager = RuntimeManagerRegistry.get().getManager(deploymentId);
                                if (manager != null) {
                                    RuntimeEngine engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));

                                    engine.getKieSession().getWorkItemManager().completeWorkItem(workItemId,
                                                                                                 results);

                                    manager.disposeRuntimeEngine(engine);
                                } else {
                                    logger.error("Unable to complete workitem: runtime manager not found.");
                                    throw new RuntimeException("Unable to complete workitem: runtime manager not found.");
                                }
                            } catch (Exception e) {
                                logger.error("Unable to execute maven commands asynchronously",
                                             e);
                                throw new RuntimeException("Unable to execute maven commands asynchronously",
                                                           e);
                            }
                        }
                    }).start();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());

            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem workItem,
                              WorkItemManager manager) {
    }
}
