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
import org.jbpm.contrib.restservice.util.StringPropertyReplacer;
import org.jbpm.contrib.restservice.util.Helper;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessContext;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.jbpm.contrib.restservice.util.Helper.getParameterNameCancelUrl;
import static org.jbpm.contrib.restservice.util.Helper.getStringParameter;

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
            @WidParameter(name="taskTimeout", required = false),
            @WidParameter(name="cancelUrlJsonPointer", required = false),
            @WidParameter(name="cancelTimeout", required = false),
            @WidParameter(name="alwaysRun", required = false),
            @WidParameter(name="mustRunAfter", required = false),
            @WidParameter(name="successCondition", required = false)
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

    private static final Logger logger = LoggerFactory.getLogger(RestServiceWorkItemHandler.class);

    private ProcessContext kcontext;

    private final RuntimeManager runtimeManager;

    public RestServiceWorkItemHandler(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
        logger.info(">>> Constructing with runtimeManager ...");
    }

    public RestServiceWorkItemHandler() {
        logger.info(">>> Constructing with no parameters...");
        runtimeManager = null;
    }

    public void executeWorkItem(WorkItem _workItem,
                                WorkItemManager manager) {
        try {
            //TODO enable
            //RequiredParameterValidator.validate(this.getClass(), workItem);

            WorkItemImpl workItem = (WorkItemImpl) _workItem; //TODO use api
            WorkflowProcessInstance processInstance = Helper.getProcessInstance(runtimeManager, workItem.getProcessInstanceId());

            long nodeInstanceId = workItem.getNodeInstanceId();
            NodeInstance nodeInstance = processInstance.getNodeInstance(nodeInstanceId);
            String nodeName = nodeInstance.getNodeName();

            logger.info("Started nodeInstance.name {}, nodeInstance.id {}.", nodeName, nodeInstanceId);

            String cancelUrlJsonPointer = Helper.getStringParameter(workItem, CANCEL_URL_JSON_POINTER_VARIABLE);

            String requestUrl = Helper.getStringParameter(workItem,"requestUrl");
            String requestMethod = Helper.getStringParameter(workItem,"requestMethod");
            String requestBody = Helper.getStringParameter(workItem,"requestBody");

            long taskTimeout = Helper.getLongParameter(workItem, "taskTimeout");

            boolean alwaysRun = Helper.getBooleanParameter(workItem, "alwaysRun");
            //comma separated list of node names, after which this node will run if they completed successfully
            String mustRunAfterStr = Helper.getStringParameter(workItem,"mustRunAfter");
            String successCondition = getStringParameter(workItem, "successCondition");
            processInstance.setVariable(Helper.getParameterNameSuccessCondition(nodeName), successCondition);
            List<String> mustRunAfter = Collections.EMPTY_LIST;
            if (mustRunAfterStr != null && !"".equals(mustRunAfterStr)) {
                mustRunAfter = Arrays.asList(mustRunAfterStr.split(","));
            }

            //TODO get without parameters
            String containerId = (String) processInstance.getVariable("containerId");

            boolean cancelRequested = Helper.getBooleanVariable(processInstance, "cancelRequested");

            //should this service run
            logger.debug("Should run ProcessInstance.id: {}, nodeName: {}, cancelRequested: {}, alwaysRun: {}, runAfter: {}.", processInstance.getId(), nodeName, cancelRequested, alwaysRun, mustRunAfterStr);
            if (!cancelRequested || alwaysRun || checkRunAfter(mustRunAfter, workItem.getProcessInstanceId())) {
                final KieSession ksession = Helper.getKsession(runtimeManager, workItem.getProcessInstanceId());

                long processInstanceId = processInstance.getId();
                Helper.startTaskTimeoutProcess(
                        ksession,
                        processInstanceId,
                        nodeInstanceId,
                        nodeInstance.getNodeName(),
                        taskTimeout,
                        false);
                try {
                    boolean invoked = handleTask(manager,
                            workItem,
                            requestUrl,
                            requestMethod,
                            requestBody,
                            nodeName,
                            containerId,
                            cancelUrlJsonPointer);
                    if (!invoked) {
                        logger.warn("Invalid remote service response. ProcessInstanceId {}.", processInstanceId);
                        Helper.cancelAll(processInstance);
                    }
                } catch (Exception e) {
                    String message = MessageFormat.format("Failed to invoke remote service. ProcessInstanceId {0}.", processInstanceId);
                    logger.warn(message, e);
                    Helper.cancelAll(processInstance);
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
            if (checkTaskCompletedSuccessfully(runAfterTask, processInstanceId)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkTaskCompletedSuccessfully(String nodeName, long processInstanceId) {
        WorkflowProcessInstance processInstance = Helper.getProcessInstance(runtimeManager, processInstanceId);
        Object completedSuccessfully = processInstance.getVariable(Helper.getParameterNameSuccessCompletions(nodeName));
        if (completedSuccessfully == null) {
            return false;
        } else {
            return (Boolean) completedSuccessfully;
        }
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

        WorkflowProcessInstance processInstance = Helper.getProcessInstance(runtimeManager, workItem.getProcessInstanceId());

        Properties properties = new Properties();
        String host = "localhost:8080"; //TODO
        properties.put("handler.callback.url",
                "http://" + host + "/kie-server/services/rest/server/containers/" + containerId +
                "/processes/instances/" + processInstance.getId() + "/workitems/" + workItem.getId() + "/completed");

        String requestBodyReplaced = StringPropertyReplacer.replaceProperties(
                requestBody, properties);

        String loginToken = ""; //TODO

        HttpResponse httpResponse = Helper.httpRequest(
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
            JsonNode root = Helper.objectMapper.readTree(httpResponse.getEntity().getContent());
            JsonNode cancelUrlNode = root.at(cancelUrlJsonPointer);
            if (!cancelUrlNode.isMissingNode()) {
                cancelUrl = cancelUrlNode.asText();
            }
        } catch (Exception e) {
            logger.warn("Cannot read cancel url.", e);
        }
        processInstance.setVariable(getParameterNameCancelUrl(nodeInstanceName), cancelUrl);
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
        String taskName = Helper.getStringParameter(workItem, "taskName");
        completeWorkItem(manager, workItem, taskName, "{\"aborted\":\"true\"}");
    }

}
