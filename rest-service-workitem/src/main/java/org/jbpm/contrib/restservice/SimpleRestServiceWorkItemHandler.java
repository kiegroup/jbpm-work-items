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

import static org.mvel2.templates.TemplateCompiler.compileTemplate;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.contrib.restservice.util.Mapper;
import org.jbpm.contrib.restservice.util.ProcessUtils;
import org.jbpm.contrib.restservice.util.Strings;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateRuntime;
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

    private static final Logger logger = LoggerFactory.getLogger(SimpleRestServiceWorkItemHandler.class);

    private final RuntimeManager runtimeManager;

    public SimpleRestServiceWorkItemHandler(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
        logger.info(">>> Constructing with runtimeManager ...");
    }

    public SimpleRestServiceWorkItemHandler() {
        logger.info(">>> Constructing without runtimeManager ...");
        runtimeManager = null;
    }

    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        try {
            //TODO enable
            //RequiredParameterValidator.validate(this.getClass(), workItem);

            long processInstanceId = workItem.getProcessInstanceId();
            WorkflowProcessInstance processInstance = ProcessUtils.getProcessInstance(runtimeManager,
                    processInstanceId);

            String cancelUrlJsonPointer = ProcessUtils.getStringParameter(workItem, Constant.CANCEL_URL_JSON_POINTER_VARIABLE);
            String requestUrl = ProcessUtils.getStringParameter(workItem,"requestUrl");
            String requestMethod = ProcessUtils.getStringParameter(workItem,"requestMethod");
            String requestBody = ProcessUtils.getStringParameter(workItem,"requestBody");
            String requestHeaders = ProcessUtils.getStringParameter(workItem,"Headers");

            //TODO get without parameters
            //kcontext.getKieRuntime().getEnvironment().get("deploymentId"));

            WorkflowProcessInstance mainInstance = ProcessUtils.getProcessInstance(
                    runtimeManager,
                    processInstance.getParentProcessInstanceId());
            String containerId = (String) mainInstance.getVariable("containerId");

            //should this service run
            logger.debug("Should run ProcessInstance.id: {}.", processInstance.getId());
            
            try {
                boolean invoked = invokeRemoteService(
                        processInstance,
                        manager,
                        workItem.getId(),
                        requestUrl,
                        requestMethod,
                        requestBody,
                        containerId,
                        cancelUrlJsonPointer,
                        requestHeaders);
                if (!invoked) {
                    logger.warn("Invalid remote service response. ProcessInstanceId {}.", processInstanceId);
                    //TODO refine signal
                    processInstance.signalEvent(Constant.CANCEL_SIGNAL_TYPE, processInstance.getId());
                }
            } catch (Exception e) {
                String message = MessageFormat.format("Failed to invoke remote service. ProcessInstanceId {0}.", processInstanceId);
                logger.warn(message, e);
                //TODO refine signal
                processInstance.signalEvent(Constant.CANCEL_SIGNAL_TYPE, processInstance.getId());
            }
        } catch(Throwable cause) {
            //TODO: removal of AbstractLogOrThrowWorkItemHandler: handleException(cause);
            //for now throw RuntimeException rather than just swallow it
            throw new RuntimeException(cause);
        }
    }

    private boolean invokeRemoteService(
            WorkflowProcessInstance processInstance,
            WorkItemManager manager,
            long workItemId,
            String requestUrl,
            String httpMethod,
            String requestTemplate,
            String containerId,
            String cancelUrlJsonPointer,
            String requestHeaders) throws IOException {

        Map<String, Object> systemVariables = Collections.singletonMap(
                "callbackUrl",
                getKieHost() + "/kie-server/services/rest/server/containers/" + containerId +
                        "/processes/instances/" + processInstance.getId() + "/signal/RESTResponded");

        logger.info("requestTemplate: {}", requestTemplate);

        CompiledTemplate compiled = compileTemplate(requestTemplate);
        String requestBodyEvaluated = (String) TemplateRuntime
                .execute(compiled, null, new SystemVariableResolver(processInstance, systemVariables));

        Map<String, String> requestHeadersMap = Strings.toMap(requestHeaders);
        HttpResponse httpResponse = httpRequest(
                requestUrl,
                requestBodyEvaluated,
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
            completeWorkItem(manager, workItemId, Collections.emptyMap(), "");
            return false;
        }

        JsonNode root;
        Map<String, Object> serviceInvocationResponse;
        try {
            root = Mapper.getInstance().readTree(httpResponse.getEntity().getContent());
            serviceInvocationResponse = Mapper.getInstance().convertValue(root, new TypeReference<Map<String, Object>>(){});
        } catch (Exception e) {
            logger.warn("Cannot parse service invocation response.", e);
            //TODO: this should be improved by throwing a BPM error event instead of just completing the workitem.
            //processInstance.signalEvent(Constant.CANCEL_SIGNAL_TYPE, processInstance.getId());
            return false;
        }

        try {
            JsonNode cancelUrlNode = root.at(cancelUrlJsonPointer);
            String cancelUrl = "";
            if (!cancelUrlNode.isMissingNode()) {
                cancelUrl = cancelUrlNode.asText();
            }
            completeWorkItem(manager, workItemId, serviceInvocationResponse, cancelUrl);
        } catch (Exception e) {
            logger.warn("Cannot read cancel url from service invocation response.", e);
            //TODO: this should be improved by throwing a BPM error event instead of just completing the workitem.
            //processInstance.signalEvent(Constant.CANCEL_SIGNAL_TYPE, processInstance.getId());
            return false;
        }
        return true;
    }

    private HttpResponse httpRequest(
            String url,
            String jsonContent,
            String httpMethod,
            Map<String,String> requestHeaders,
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

        RequestBuilder requestBuilder = RequestBuilder.create(httpMethod).setUri(url);

        requestBuilder.addHeader("Content-Type","application/json");
        if (requestHeaders != null) {
            requestHeaders.forEach((k,v) -> requestBuilder.addHeader(k,v));
        }

        StringEntity entity = new StringEntity(jsonContent, ContentType.APPLICATION_JSON);

        requestBuilder.setEntity(entity);

        logger.info("Invoking remote endpoint {} {} Headers: {} Body: {}.", httpMethod, url, requestHeaders, jsonContent);

        return httpClient.execute(requestBuilder.build());
    }


    private String getKieHost() {
        String host = System.getProperty(Constant.KIE_HOST_SYSTEM_PROPERTY);
        if (host != null) {
            host = "http://" + host;
        }
        if (host == null) {
            host = System.getenv("HOSTNAME_HTTPS"); //TODO configurable
            if (host != null) {
                host = "https://" + host;
            }
        }
        return host;
    }

    /**
     * Complete WorkItem and store the result.
     * Long running operations are not completed using this handler but via REST api call.
     * This method it used to complete the task in case of internal timeout/cancel or skipped execution.
     */
    private void completeWorkItem(
            WorkItemManager manager,
            long workItemId,
            Map<String, Object> serviceInvocationResult,
            String cancelUrl) {
        Map<String, Object> results = new HashMap<>();
        results.put("result", serviceInvocationResult);
        results.put("cancelUrl", cancelUrl);
        manager.completeWorkItem(workItemId, results);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        completeWorkItem(manager, workItem.getId(), Collections.singletonMap("aborted", "true"), "");
    }
}
