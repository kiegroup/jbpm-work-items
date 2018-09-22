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
package org.jbpm.contrib.restservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpResponse;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jboss.util.StringPropertyReplacer;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessContext;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.jbpm.contrib.restservice.Utils.CANCEL_SIGNAL_TYPE;
import static org.jbpm.contrib.restservice.Utils.getCancelUrlVariableName;

/**
 * *PROCESS FLOW*
 * On BPM level all the tasks are executed every time.
 * WIHahdler decides if the remote service is executed or no.
 *
 * *OnCancel*
 * - Process fires a cancel event
 * - currently executing handler catches the event and calls cancel on remote service
 * - remote service might not allow cancel, this is defined by always-run flag
 * - in some cases remote services have to run if previous task completed
 *
 * *TimeOut*
 * - when timeout hits in the task signal cancel event
 * - cancel event needs a reason field (user-requested|time-out)
 *
 * *OnFailure / OnSuccess*
 *
 *
 * *PARAMETERS MAPPING*
 *
 *
 */
/*
@Wid(widfile="RestServiceDefinitions.wid", name="RestServiceDefinitions",
        displayName="RestServiceDefinitions", icon="",
        defaultHandler="mvel: new org.jbpm.contrib.RestServiceWorkItemHandler(runtimeManager)",
        documentation = "",
        parameters={
            @WidParameter(name="requestUrl", required = true),
            @WidParameter(name="requestMethod", required = true),
            @WidParameter(name="requestBody", required = true),
            @WidParameter(name="taskTimeout", required = true),
            @WidParameter(name="cancelUrlJsonPointer", required = true),
            @WidParameter(name="cancelTimeout", required = true)
        },
//        results={
//            @WidResult(name="as defined by ResultParamName")
//        },
        mavenDepends={
            @WidMavenDepends(group="org.jbpm.contrib", artifact="rest-service-workitem", version="7.9.0.Final")
        })
*/
public class RestServiceWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

    public static final String TASK_NAME = "taskName";

    private static final String CANCEL_URL_JSON_POINTER_VARIABLE = "cancelUrlJsonPointer";

    private static Logger logger = LoggerFactory.getLogger(RestServiceWorkItemHandler.class);

    private ProcessContext kcontext;

    private RuntimeManager runtimeManager;

    public RestServiceWorkItemHandler(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
        logger.info(">>> Constructing with runtimeManager ...");
    }

    public RestServiceWorkItemHandler() {
        logger.info(">>> Constructing with no parameters...");
    }

    public void executeWorkItem(WorkItem _workItem,
                                WorkItemManager manager) {
        try {
            //TODO enable
            //RequiredParameterValidator.validate(this.getClass(), workItem);

            WorkItemImpl workItem = (WorkItemImpl) _workItem; //TODO use public api
            WorkflowProcessInstance processInstance = Utils.getProcessInstance(runtimeManager, workItem.getProcessInstanceId());

            long nodeInstanceId = workItem.getNodeInstanceId();
            NodeInstance nodeInstance = processInstance.getNodeInstance(nodeInstanceId);
            String nodeInstanceName = nodeInstance.getNodeName();

            logger.info("Started nodeInstance.name {}, nodeInstance.id {}.", nodeInstanceName, nodeInstanceId);

            String cancelUrlJsonPointer = Utils.getStringParameter(workItem, CANCEL_URL_JSON_POINTER_VARIABLE);

            String requestUrl = Utils.getStringParameter(workItem,"requestUrl");
            String requestMethod = Utils.getStringParameter(workItem,"requestMethod");
            String requestBody = Utils.getStringParameter(workItem,"requestBody");

            long taskTimeout = Utils.getLongParameter(workItem, "taskTimeout");

            boolean alwaysRun = Utils.getBooleanParameter(workItem, "alwaysRun");
            String mustRunAfterStr = Utils.getStringParameter(workItem,"mustRunAfter"); //comma separated list of task names
            List<String> mustRunAfter = Collections.EMPTY_LIST;
            if (mustRunAfterStr != null && !"".equals(mustRunAfterStr)) {
                mustRunAfter = Arrays.asList(mustRunAfterStr.split(","));
            }

            //TODO get without parameters
            String containerId = (String) processInstance.getVariable("containerId");

            boolean cancelRequested = Utils.getBooleanVariable(processInstance, "cancelRequested");

            //should this service run
            if (!cancelRequested || alwaysRun || checkRunAfter(mustRunAfter, workItem.getProcessInstanceId())) { //TODO
                final KieSession ksession = Utils.getKsession(runtimeManager, workItem.getProcessInstanceId());

                final long timoutProcessInstanceId;
                long processInstanceId = processInstance.getId();
                if (taskTimeout > 0) {
                    timoutProcessInstanceId = Utils.startTaskTimeoutProcess(
                            ksession,
                            processInstanceId,
                            nodeInstanceId,
                            taskTimeout,
                            false);
                } else {
                    timoutProcessInstanceId = -1;
                }

                ProcessEventListener onProcessEvent = new DefaultProcessEventListener() {
                    @Override
                    public void afterNodeLeft(ProcessNodeLeftEvent event) {
                        ksession.removeEventListener(this);
                        //stop timeout process
                        long completedNodeInstanceId = event.getNodeInstance().getId();
                        logger.debug("Completed nodeInstanceId {}, registering nodeInstanceId {} belonging to processInstance.id: {}. TimeoutProcessInstance.id: {}.",
                                completedNodeInstanceId, nodeInstanceId, processInstanceId, timoutProcessInstanceId);
                        if (completedNodeInstanceId == nodeInstanceId && timoutProcessInstanceId > -1) {
                            ProcessInstance timeoutProcessInstance = ksession.getProcessInstance(timoutProcessInstanceId);
                            if (timeoutProcessInstance != null) {
                                logger.info("Aborting timeout process instance for nodeInstanceId: {}. TimeoutProcessInstance.id: {}.", nodeInstanceId, timoutProcessInstanceId);
                                ksession.abortProcessInstance(timoutProcessInstanceId);
                            } else {
                                logger.debug("Not Found timeout process instance for nodeInstanceId: {}. TimeoutProcessInstance.id: {}.", nodeInstanceId, timoutProcessInstanceId);
                            }
                        }
                    }
                };
                ksession.addEventListener(onProcessEvent);

                try {
                    boolean invoked = handleTask(manager,
                            workItem,
                            requestUrl,
                            requestMethod,
                            requestBody,
                            nodeInstanceName,
                            containerId,
                            cancelUrlJsonPointer);
                    if (!invoked) {
                        logger.warn("Invalid remote service response.");
                        processInstance.signalEvent(CANCEL_SIGNAL_TYPE, processInstanceId);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to invoke remote service.", e);
                    processInstance.signalEvent(CANCEL_SIGNAL_TYPE, processInstanceId);
                }
            } else {
                logger.info("Skipping task execution ...");
                manager.completeWorkItem(workItem.getId(), null);
            }
        } catch(Throwable cause) {
            handleException(cause);
        }
    }

    private boolean checkRunAfter(List<String> runAfterTasks, long processInstanceId) {
        for (String runAfterTask : runAfterTasks) {
            if (checkTaskCompleted(runAfterTask, processInstanceId)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkTaskCompleted(String taskName, long processInstanceId) {
        WorkflowProcessInstance processInstance = Utils.getProcessInstance(runtimeManager, processInstanceId);
        return processInstance.getVariable("task." + taskName + ".result") != null;
    }

    /**
     * For async tasks there should be a callback field with ${handler.callback.url} variable.
     * The ${handler.callback.url} is replaced with the url to complete this task.
     */
    private Function<String, String> propertyProvider(String containerId, long processInstanceId, long workItemId) {
        WorkflowProcessInstance processInstance = Utils.getProcessInstance(runtimeManager, processInstanceId);
        ProcessVariableResolver processVariableResolver = new ProcessVariableResolver(processInstance);

        return (key) -> {
            if (key.equals("handler.callback.url")) {
                String host = "localhost:8080"; //TODO
                return "http://" + host + "/kie-server/services/rest/server/containers/" + containerId + "/processes/instances/" + processInstanceId + "/workitems/" + workItemId + "/completed";
            } else {
                return processVariableResolver.get(key);
            }
        };
    }

    private boolean handleTask(
            WorkItemManager manager,
            WorkItem workItem,
            String requestUrl,
            String requestMethod,
            String requestBody,
            String nodeInstanceName,
            String containerId,
            String cancelUrlJsonPointer) throws IOException {

        WorkflowProcessInstance processInstance = Utils.getProcessInstance(runtimeManager, workItem.getProcessInstanceId());

        String requestBodyReplaced = StringPropertyReplacer.replaceProperties(
                requestBody,
                propertyProvider(containerId, workItem.getProcessInstanceId(), workItem.getId()));

        String loginToken = ""; //TODO

        HttpResponse httpResponse = Utils.httpRequest(
                requestUrl,
                requestBodyReplaced,
                loginToken,
                5000,
                5000,
                5000);

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        logger.info("Remote endpoint returned status: {}.", statusCode);

        if (statusCode < 200 || statusCode >= 300 ) {
            logger.debug("Remote service responded with status: {}", statusCode);
            return false;
        }

        String cancelUrl = "";
        try {
            JsonNode root = Utils.objectMapper.readTree(httpResponse.getEntity().getContent());
            JsonNode cancelUrlNode = root.at(cancelUrlJsonPointer);
            if (!cancelUrlNode.isMissingNode()) {
                cancelUrl = cancelUrlNode.asText();
            }
        } catch (Exception e) {
            logger.warn("Cannot read cancel url.", e);
        }
        processInstance.setVariable(getCancelUrlVariableName(nodeInstanceName), cancelUrl);
        return true;
    }

    /**
     * Complete WorkItem and store the result.
     * Long running operations are not completed using this handler but via REST api call.
     * This method it used to complete the task in case of internal timeout/cancel or skipped execution.
     */
    private void completeWorkItem(
            WorkItemManager manager,
            WorkItem workItem,
            String taskName,
            String jsonResult) {
        Map<String, Object> results = new HashMap<>();
        results.put(taskName, jsonResult);
        manager.completeWorkItem(workItem.getId(), results);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        String taskName = Utils.getStringParameter(workItem, "taskName");
        completeWorkItem(manager, workItem, taskName, "{\"aborted\":\"true\"}");
    }

}
