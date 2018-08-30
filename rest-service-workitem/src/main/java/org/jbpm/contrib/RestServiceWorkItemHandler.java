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
package org.jbpm.contrib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.util.StringPropertyReplacer;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessContext;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

    private static Logger logger = LoggerFactory.getLogger(RestServiceWorkItemHandler.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    private ProcessContext kcontext;

    private RuntimeManager runtimeManager;

    public RestServiceWorkItemHandler(ProcessContext kcontext) {
        this.kcontext = kcontext;
        System.out.println(">>> Constructing with kcontext ...");
    }

    public RestServiceWorkItemHandler(KieSession ksession) {
        System.out.println(">>> Constructing with kcontext ...");
    }

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
            String workItemName = workItem.getName();
            System.out.println(">>> Invoked name: " + workItemName);

//            RequiredParameterValidator.validate(this.getClass(), workItem);

            String taskName = getStringParameter(workItem, TASK_NAME);
            System.out.println(">>> taskName: " + taskName);

            String requestUrl = getStringParameter(workItem,"requestUrl");
            String requestMethod = getStringParameter(workItem,"requestMethod");
            String requestBody = getStringParameter(workItem,"requestBody");

            boolean alwaysRun = getBooleanParameter(workItem, "alwaysRun");
            String mustRunAfterStr = getStringParameter(workItem,"mustRunAfter"); //comma separated list of task names
            List<String> mustRunAfter = Collections.EMPTY_LIST;
            if (mustRunAfterStr != null && !"".equals(mustRunAfterStr)) {
                mustRunAfter = Arrays.asList(mustRunAfterStr.split(","));
            }

            //TODO q: get without parameters
            String containerId = (String) getProcessInstance(workItem.getProcessInstanceId()).getVariable("containerId");

            //should this service run
            if (true || alwaysRun || checkRunAfter(mustRunAfter, workItem.getProcessInstanceId())) { //TODO
                //TODO q:register signal listeners
//                getProcessInstance(workItem.getProcessInstanceId())
                ProcessEventListener onProcessEvent = new DefaultProcessEventListener();
                kcontext.getKieRuntime().addEventListener(onProcessEvent);

                handleTask(manager, workItem, requestUrl, requestMethod, requestBody, taskName, containerId); //TODO
            } else {
                logger.info("Skipping task execution ...");
                manager.completeWorkItem(workItem.getId(), null);
            }


        } catch(Throwable cause) {
            handleException(cause);
        }
    }

    private String getStringParameter(WorkItem workItem, String parameterName) {
        Object parameter = workItem.getParameter(parameterName);
        if (parameter != null) {
            return (String) parameter;
        } else {
            return "";
        }
    }

    private boolean getBooleanParameter(WorkItem workItem, String parameterName) {
        Object parameter = workItem.getParameter(parameterName);
        if (parameter != null) {
            return (boolean) parameter;
        } else {
            return false;
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
        WorkflowProcessInstance processInstance = getProcessInstance(processInstanceId);
        return processInstance.getVariable("task." + taskName + ".result") != null;
    }

    /**
     * For async tasks there should be a callback field with ${handler.callback.url} variable.
     * The ${handler.callback.url} is replaced with the url to complete this task.
     */
    private Function<String, String> propertyProvider(String containerId, long processInstanceId, long workItemId) {
        WorkflowProcessInstance processInstance = getProcessInstance(processInstanceId);
        ProcessVariableResolver processVariableResolver = new ProcessVariableResolver(processInstance);


        return (key) -> {
            if (key.equals("handler.callback.url")) {
                String host = "localhost:8080";
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
            String containerId) throws IOException {

        WorkflowProcessInstance processInstance = getProcessInstance(workItem.getProcessInstanceId());

        String requestBodyReplaced = StringPropertyReplacer.replaceProperties(
                requestBody,
                propertyProvider(containerId, workItem.getProcessInstanceId(), workItem.getId()));

        String cancelUrlJsonPointer = ""; //TODO
        String loginToken = ""; //TODO

        String cancelUrl = invokeRemote(
                requestUrl,
                requestBodyReplaced,
                cancelUrlJsonPointer,
                loginToken,
                5000,
                5000,
                5000);


        //TODO might require wrapping in transaction https://developer.jboss.org/thread/199180
        processInstance.setVariable(taskName + "-cancel", cancelUrl);
        //TODO q:start a timer to abort work item

    }

    private String invokeRemote(
            String url,
            String jsonContent,
            String cancelUrlJsonPointer,
            String loginToken,
            int readTimeout,
            int connectTimeout,
            int requestTimeout) throws IOException {
        RequestConfig config = RequestConfig.custom()
                .setSocketTimeout(readTimeout)
                .setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(requestTimeout)
                .build();

        HttpClientBuilder clientBuilder = HttpClientBuilder.create()
                .setDefaultRequestConfig(config);

        HttpClient httpClient = clientBuilder.build();

        HttpPost request = new HttpPost(url); //TODO

        request.setHeader("Content-Type","application/json");
        request.setHeader("Authorization", "Bearer " + loginToken);

        StringEntity entity = new StringEntity(jsonContent, ContentType.APPLICATION_JSON);

        request.setEntity(entity);

        logger.info("Invoking remote endpoint {} with data: {}.", url, jsonContent);

        HttpResponse response = httpClient.execute(request);

        try {
            JsonNode root = objectMapper.readTree(response.getEntity().getContent());
            JsonNode cancelUrlNode = root.at(cancelUrlJsonPointer);
            if (!cancelUrlNode.isMissingNode()) {
                String cancelUrl = cancelUrlNode.asText();
                return cancelUrl;
            }
        } catch (Exception e) {
            logger.warn("Cannot read cancel url.");
        }
        return "";
    }

    public void onTimeOut() {
        onCancelRequest();
    }

    /**
     * Try to cancel remote job, if it does not happen in given timeout force cancel internally.
     *
     * http://localhost:8080/kie-server/docs/#/Process%20instances%20::%20BPM/signalProcessInstance
     */
    public void onCancelRequest() {
        WorkItem workItem = null; //TODO
        WorkItemManager workItemManager = null; //TODO

        WorkflowProcessInstance processInstance = getProcessInstance(workItem.getProcessInstanceId());

        //TODO might require wrapping in transaction https://developer.jboss.org/thread/199180
        String taskName = getStringParameter(workItem, TASK_NAME);

        String cancelUrl = (String) processInstance.getVariable(taskName + "-cancel");
        //invokeRemoteCancel(cancelUrl);
        //TODO q: start a timer to abort work item
    }

    private WorkflowProcessInstance getProcessInstance(long processInstanceId) {
        return (WorkflowProcessInstance)getKsession(processInstanceId).getProcessInstance(processInstanceId);
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
        String taskName = getStringParameter(workItem, "taskName");
        completeWorkItem(manager, workItem, taskName, "{\"cancelled\":\"true\"}"); //TODO result
    }

    private KieSession getKsession(Long processInstanceId) {
        if (runtimeManager != null) {
            RuntimeEngine engine = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
            return engine.getKieSession();
        }
        return null;
    }

/*    private NodeInstance getTaskByName(String name) {
        for (NodeInstance nodeInstance : kcontext.getNodeInstance().getNodeInstanceContainer().getNodeInstances()) {
//            nodeInstance.getNode().getName().equals(name)
            if (nodeInstance.getVariable(TASK_NAME).toString().equals(name)) {
                return nodeInstance;
            }
        }

//        TaskService taskService = null;
//        findNodeInstance(1, );
//        List<TaskSummary> tasksByStatusByProcessInstanceId = taskService.getTasksByStatusByProcessInstanceId(1);
//        List<Long> tasksByProcessInstanceId = taskService.getTasksByProcessInstanceId(processInstanceId);
//        for (TaskSummary taskSummary : tasksByStatusByProcessInstanceId) {
//            taskSummary.getId();
//        }

        return null;
    }
*/
}


