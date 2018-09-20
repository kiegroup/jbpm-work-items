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
import org.jboss.util.StringPropertyReplacer;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessContext;
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

import static org.jbpm.contrib.restservice.Utils.FORCE_CANCEL_VARIABLE;
import static org.jbpm.contrib.restservice.Utils.TIMEOUT_PROCESS_NAME;
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
//@Wid(widfile="RestServiceDefinitions.wid", name="RestServiceDefinitions",
//        displayName="RestServiceDefinitions", icon="",
//        defaultHandler="mvel: new org.jbpm.contrib.RestServiceWorkItemHandler()",
//        documentation = "",
////        parameters={
////            @WidParameter(name="SampleParam", required = true),
////            @WidParameter(name="ResultParamName", required = true)
////        },
//        results={
//            @WidResult(name="as defined by ResultParamName")
//        },
//        mavenDepends={
//            @WidMavenDepends(group="org.jbpm.contrib", artifact="rest-service-workitem", version="7.9.0.Final")
//        })
//TODO q: the fastest way to redeploy WorkItemHandler
public class RestServiceWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

    public static final String TASK_NAME = "taskName";

    private static final String CANCEL_URL_JSON_POINTER_VARIABLE = "cancelUrlJsonPointer";

    private static Logger logger = LoggerFactory.getLogger(RestServiceWorkItemHandler.class);

    private ProcessContext kcontext;

    private RuntimeManager runtimeManager;

    public RestServiceWorkItemHandler(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
        System.out.println(">>> Constructing with runtimeManager ...");
    }

    public RestServiceWorkItemHandler() {
        System.out.println(">>> Constructing with no parameters...");
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager manager) {
        try {
            //            RequiredParameterValidator.validate(this.getClass(), workItem); //TODO enable

            long taskId = workItem.getId();
            String taskName = Utils.getStringParameter(workItem, TASK_NAME);
            logger.info("Started task {} with id {}.", taskName, taskId);

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

            WorkflowProcessInstance processInstance = Utils.getProcessInstance(runtimeManager, workItem.getProcessInstanceId());
            //TODO q: get without parameters
            String containerId = (String) processInstance.getVariable("containerId");

            boolean cancelRequested = Utils.getBooleanVariable(processInstance, "cancelRequested");

            //should this service run
            if (!cancelRequested || alwaysRun || checkRunAfter(mustRunAfter, workItem.getProcessInstanceId())) { //TODO
                ProcessEventListener onProcessEvent = new DefaultProcessEventListener() {
                    @Override
                    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
                        Utils.getKsession(runtimeManager, workItem.getProcessInstanceId()).removeEventListener(this);
                    }

                    @Override
                    public void afterNodeLeft(ProcessNodeLeftEvent event) {
//TODO
//                        long timeoutProcessId = (long) processInstance.getVariable(getTimeoutPIIDVariableName(taskName));
//                        Utils.getKsession(runtimeManager, timeoutProcessId).abortProcessInstance(timeoutProcessId);
                    }
                };
                KieSession ksession = Utils.getKsession(runtimeManager, workItem.getProcessInstanceId());
                ksession.addEventListener(onProcessEvent);

                if (taskTimeout > 0) {
//                    long timeoutProcessId = startTaskTimeoutProcess(ksession, taskId, taskTimeout);
//                    processInstance.setVariable(getTimeoutPIIDVariableName(taskName), timeoutProcessId);

//                    startTaskTimeoutProcess(ksession, taskId, taskTimeout);
                }

                handleTask(manager, workItem, requestUrl, requestMethod, requestBody, taskName, containerId, cancelUrlJsonPointer); //TODO
            } else {
                logger.info("Skipping task execution ...");
                manager.completeWorkItem(taskId, null);
            }
        } catch(Throwable cause) {
            handleException(cause);
        }
    }

    public static void startTaskTimeoutProcess(KieSession kieSession, long taskId, long timeout) {
        Map<String, Object> data = new HashMap<>();
        data.put("timeoutMillis", timeout);
        data.put(Utils.TIMEOUT_TASK_ID_VARIABLE, taskId);
        data.put(FORCE_CANCEL_VARIABLE, false);
//        ProcessInstance processInstance = kieSession.startProcess(TIMEOUT_PROCESS_NAME, data);
        kieSession.signalEvent(TIMEOUT_PROCESS_NAME, data);
//        return processInstance.getParentProcessInstanceId();
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

    private void handleTask(
            WorkItemManager manager,
            WorkItem workItem,
            String requestUrl,
            String requestMethod,
            String requestBody,
            String taskName,
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

        //TODO might require wrapping in transaction https://developer.jboss.org/thread/199180
        processInstance.setVariable(getCancelUrlVariableName(taskName), cancelUrl);
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
        completeWorkItem(manager, workItem, taskName, "{\"cancelled\":\"true\"}"); //TODO result
    }

}


