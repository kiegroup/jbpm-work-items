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

import static org.jbpm.contrib.restservice.util.Helper.getKsession;
import static org.jbpm.contrib.restservice.util.Helper.getParameterNameCancelUrl;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.contrib.restservice.util.Helper;
import org.jbpm.contrib.restservice.util.StringPropertyReplacer;
import org.jbpm.contrib.restservice.util.Strings;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessContext;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

@Wid(widfile="SimpleRestService.wid", 
        name="SimpleRestService",
        displayName="SimpleRestService",
        defaultHandler="mvel: new org.jbpm.contrib.restservice.SimpleRestServiceWorkItemHandler(runtimeManager)",
        category="rest-service-workitem",
        documentation = "",
        parameters={
            @WidParameter(name="requestUrl", required = true),
            @WidParameter(name="requestMethod", required = true),
            @WidParameter(name="requestBody", required = true),
            @WidParameter(name="cancelUrlJsonPointer", required = false)
        },
        results={
            @WidResult(name="result")
        },
        mavenDepends={
            @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}"),
 //           @WidMavenDepends(group="org.slf4j", artifact="slf4j-api") 
        },
        serviceInfo = @WidService(category = "REST service", description = "",
            keywords = "rest",
            action = @WidAction(title = "Simplified execute of an REST service ver. ${version}")
        )
)
public class SimpleRestServiceWorkItemHandler implements WorkItemHandler {

    public static final String TASK_NAME = "taskName";

    private static final String CANCEL_URL_JSON_POINTER_VARIABLE = "cancelUrlJsonPointer";

    private static final Logger logger = LoggerFactory.getLogger(SimpleRestServiceWorkItemHandler.class);

    private ProcessContext kcontext;

    private final RuntimeManager runtimeManager;

    public SimpleRestServiceWorkItemHandler(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
        logger.info(">>> Constructing with runtimeManager ...");
    }

    public SimpleRestServiceWorkItemHandler() {
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
            String requestHeaders = Helper.getStringParameter(workItem,"Headers");

            //TODO use ksession.getEnvironment().get() ?
            String callbackKieRestBase = Helper.getStringParameter(workItem,"callbackKieRestBaseUrl");

            //TODO get without parameters
            String containerId = (String) processInstance.getVariable("containerId");

            //should this service run
            logger.debug("Should run ProcessInstance.id: {}, nodeName: {}.", processInstance.getId(), nodeName);
            
            final KieSession ksession = Helper.getKsession(runtimeManager, workItem.getProcessInstanceId());

            long processInstanceId = processInstance.getId();
            
            try {
                boolean invoked = handleTask(manager,
                        workItem,
                        requestUrl,
                        requestMethod,
                        requestBody,
                        nodeName,
                        containerId,
                        cancelUrlJsonPointer,
                        requestHeaders,
                        callbackKieRestBase);
                if (!invoked) {
                    logger.warn("Invalid remote service response. ProcessInstanceId {}.", processInstanceId);
                    Helper.cancelAll(processInstance);
                }
                
            } catch (Exception e) {
                String message = MessageFormat.format("Failed to invoke remote service. ProcessInstanceId {0}.", processInstanceId);
                logger.warn(message, e);
                Helper.cancelAll(processInstance);
            }
        } catch(Throwable cause) {
            //TODO: removal of AbstractLogOrThrowWorkItemHandler: handleException(cause);
            //for now throw RuntimeException rather than just swallow it
            throw new RuntimeException(cause);
        }
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
            String httpMethod,
            String requestBody,
            String nodeInstanceName,
            String containerId,
            String cancelUrlJsonPointer,
            String requestHeaders,
            String callbackKieRestBase) throws IOException {

        WorkflowProcessInstance processInstance = Helper.getProcessInstance(runtimeManager, workItem.getProcessInstanceId());
        
        Properties properties = new Properties();
        String host = "localhost:8080"; //TODO
        properties.put("handler.callback.url",
                Strings.addEndingSlash(callbackKieRestBase) + "server/containers/" + containerId +
                "/processes/instances/"+processInstance.getId()+"/signal/RESTResponded");
        
        String requestBodyReplaced = StringPropertyReplacer.replaceProperties(
                requestBody, properties);

        Map<String, String> requestHeadersMap = Strings.toMap(requestHeaders);
        HttpResponse httpResponse = Helper.httpRequest(
                requestUrl,
                requestBodyReplaced,
                httpMethod,
                requestHeadersMap,
                5000, //TODO configurable
                5000,
                5000);

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        logger.info("Remote endpoint returned status: {}.", statusCode);

        if (statusCode < 200 || statusCode >= 300 ) {
            logger.debug("Remote service responded with status: {}", statusCode);

            //TODO: this should be improved by throwing a BPM error event instead of just completing the workitem.
            completeWorkItem(manager, workItem, nodeInstanceName, "");
            return false;
        }

        String cancelUrl = "";
        JsonNode root = null;
        try {
            root = Helper.objectMapper.readTree(httpResponse.getEntity().getContent());
            JsonNode cancelUrlNode = root.at(cancelUrlJsonPointer);
            if (!cancelUrlNode.isMissingNode()) {
                cancelUrl = cancelUrlNode.asText();
            }
        } catch (Exception e) {
            logger.warn("Cannot read cancel url.", e);
        }
        processInstance.setVariable(getParameterNameCancelUrl(nodeInstanceName), cancelUrl);
        processInstance.setVariable("result",root==null ? "" : root.asText());
        
        //TODO: The above parsing and cancelUrl setting is redundant to the below
        // but we need to decide whether we process the JSON content in Java code or in the process diagram itself
        completeWorkItem(manager, workItem, nodeInstanceName, root==null ? "" : root.asText());
        
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
