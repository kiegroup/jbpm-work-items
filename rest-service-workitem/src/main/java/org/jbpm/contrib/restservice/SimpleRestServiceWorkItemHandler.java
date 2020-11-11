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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jbpm.contrib.restservice.util.Mapper;
import org.jbpm.contrib.restservice.util.ProcessUtils;
import org.jbpm.contrib.restservice.util.Strings;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mvel2.templates.TemplateCompiler.compileTemplate;

@Wid(widfile="SimpleRestService.wid", 
        name="SimpleRestService",
        displayName="SimpleRestService",
        defaultHandler="mvel: new org.jbpm.contrib.restservice.SimpleRestServiceWorkItemHandler(runtimeManager)",
        category="rest-service-workitem",
        documentation = "",
        parameters={
            @WidParameter(name="url", required = true),
            @WidParameter(name="method", required = true),
            @WidParameter(name="template", required = true),
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
public class SimpleRestServiceWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(SimpleRestServiceWorkItemHandler.class);

    private final RuntimeManager runtimeManager;

    public SimpleRestServiceWorkItemHandler(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
        logger.info(">>> Constructing with runtimeManager ...");
        setLogThrownException(false);
    }

    public SimpleRestServiceWorkItemHandler() {
        logger.info(">>> Constructing without runtimeManager ...");
        runtimeManager = null;
        setLogThrownException(false);
    }

    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        try {
            //TODO enable
            //RequiredParameterValidator.validate(this.getClass(), workItem);

            long processInstanceId = workItem.getProcessInstanceId();
            WorkflowProcessInstance processInstance = ProcessUtils.getProcessInstance(runtimeManager,
                    processInstanceId);

            String cancelUrlJsonPointer = ProcessUtils.getStringParameter(workItem, Constant.CANCEL_URL_JSON_POINTER_VARIABLE);
            String cancelUrlTemplate = ProcessUtils.getStringParameter(workItem, Constant.CANCEL_URL_TEMPLATE_VARIABLE);
            String requestUrl = ProcessUtils.getStringParameter(workItem,"url");
            String requestMethod = ProcessUtils.getStringParameter(workItem,"method");
            String requestTemplate = ProcessUtils.getStringParameter(workItem,"template");
            String requestHeaders = ProcessUtils.getStringParameter(workItem,"headers");

            //TODO get without parameters
            //kcontext.getKieRuntime().getEnvironment().get("deploymentId"));

            String containerId = (String) getProcessVariable("containerId", processInstance);

            //should this service run
            logger.debug("Should run ProcessInstance.id: {}.", processInstance.getId());
            
            try {
                invokeRemoteService(
                        processInstance,
                        manager,
                        workItem.getId(),
                        requestUrl,
                        requestMethod,
                        requestTemplate,
                        containerId,
                        cancelUrlJsonPointer,
                        cancelUrlTemplate,
                        requestHeaders);
            } catch (RemoteInvocationException e) {
                String message = MessageFormat.format("Failed to invoke remote service. ProcessInstanceId {0}.", processInstanceId);
                logger.warn(message, e);
                completeWorkItem(manager, workItem.getId(), e);
            } catch (ResponseProcessingException e) {
                String message = MessageFormat.format("Failed to process response. ProcessInstanceId {0}.", processInstanceId);
                logger.warn(message, e);
                completeWorkItem(manager, workItem.getId(), e);
            }
        } catch(Throwable cause) {
            logger.error("Failed to execute workitem handler due to the following error.", cause);
            completeWorkItem(manager, workItem.getId(), cause);
        }
    }

    /**
     * Get process variable, if it is not find on the given process instance, parent process instances are searched recursively.
     */
    private Object getProcessVariable(
            String variableName, WorkflowProcessInstance processInstance) {
        logger.info("Looking for process variable " + variableName + " in process instance " + processInstance.getProcessId());
        Object processVariable = processInstance.getVariable(variableName);
        if (processVariable == null) {
            long parentProcessInstanceId = processInstance.getParentProcessInstanceId();
            if (parentProcessInstanceId > 0) {
                WorkflowProcessInstance parentProcessInstance = ProcessUtils.getProcessInstance(
                        runtimeManager, parentProcessInstanceId);
                return getProcessVariable(variableName, parentProcessInstance);
            } else {
                return null;
            }
        } else {
            return processVariable;
        }
    }

    private void invokeRemoteService(
            WorkflowProcessInstance processInstance,
            WorkItemManager manager,
            long workItemId,
            String requestUrl,
            String httpMethod,
            String requestTemplate,
            String containerId,
            String cancelUrlJsonPointer,
            String cancelUrlTemplate,
            String requestHeaders) throws RemoteInvocationException, ResponseProcessingException {

        logger.info("requestTemplate: {}", requestTemplate);

        VariableResolverFactory variableResolverFactory = getVariableResolverFactory(processInstance, containerId);

        String requestBodyEvaluated;
        if (requestTemplate != null && !requestTemplate.equals("")) {
            CompiledTemplate compiled = compileTemplate(requestTemplate);
            requestBodyEvaluated = (String) TemplateRuntime.execute(compiled, null, variableResolverFactory);
        } else {
            requestBodyEvaluated = "";
        }

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
            String message = MessageFormat.format("Remote service responded with error status code {0} and reason: {1}. ProcessInstanceId {2}.", statusCode, httpResponse.getStatusLine().getReasonPhrase(), processInstance.getId());
            throw new RemoteInvocationException(message);
        }

        if (statusCode == 204) {
            completeWorkItem(manager, workItemId, statusCode, Collections.emptyMap(), "");
        } else {
            JsonNode root;
            Map<String, Object> serviceInvocationResponse;
            try {
                root = Mapper.getInstance().readTree(httpResponse.getEntity().getContent());
                serviceInvocationResponse = Mapper.getInstance()
                        .convertValue(root, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                String message = MessageFormat.format("Cannot parse service invocation response. ProcessInstanceId {0}.",
                        processInstance.getId());
                throw new ResponseProcessingException(message, e);
            }
            String cancelUrl = "";
            try {
                if (!Strings.isEmpty(cancelUrlTemplate)) {
                    logger.debug("Setting cancel url from template: {}.", cancelUrlTemplate);
                    CompiledTemplate compiled = compileTemplate(cancelUrlTemplate);
                    requestBodyEvaluated = (String) TemplateRuntime
                            .execute(compiled, null, variableResolverFactory);
                } else {
                    logger.debug("Setting cancel url from json pointer: {}.", cancelUrlJsonPointer);
                    JsonNode cancelUrlNode = root.at(cancelUrlJsonPointer);
                    if (!cancelUrlNode.isMissingNode()) {
                        cancelUrl = cancelUrlNode.asText();
                    }
                }
            } catch (Exception e) {
                String message = MessageFormat.format("Cannot read cancel url from service invocation response. ProcessInstanceId {0}.", processInstance.getId());
                throw new ResponseProcessingException(message, e);
            }
            completeWorkItem(manager, workItemId, statusCode, serviceInvocationResponse, cancelUrl);
        }
    }

    private VariableResolverFactory getVariableResolverFactory(
            WorkflowProcessInstance processInstance, String containerId) {
        Map<String, Object> systemVariables = new HashMap<>();
        systemVariables.put(
                "callbackUrl",
                getKieHost() + "/services/rest/server/containers/" + containerId + //TODO configurable base path
                        "/processes/instances/" + processInstance.getId() + "/signal/RESTResponded");
        systemVariables.put("callbackMethod", "POST");
        return getVariableResolverFactoryChain(
                systemVariables,
                processInstance);
    }

    private VariableResolverFactory getVariableResolverFactoryChain(
            Map<String, Object> systemVariables,
            WorkflowProcessInstance processInstance) {
        VariableResolverFactory variableResolverFactory = new MapVariableResolverFactory(
                Collections.singletonMap("system", systemVariables));

        VariableResolverFactory resolver = variableResolverFactory.setNextFactory(
                new ProcessVariableResolverFactory(processInstance));

        WorkflowProcessInstance currentInstance = processInstance;
        //add all parent instances to resolver chain
        for (int i = 0; i < 100; i++) { //circuit-breaker: allow max 100 nested process instances
            long parentProcessInstanceId = currentInstance.getParentProcessInstanceId();
            if (parentProcessInstanceId > 0) {
                WorkflowProcessInstance parentProcessInstance = ProcessUtils.getProcessInstance(
                        runtimeManager, parentProcessInstanceId);
                resolver.setNextFactory(new ProcessVariableResolverFactory(parentProcessInstance));
                currentInstance = parentProcessInstance;
            } else {
                break;
            }
        }
        return variableResolverFactory;
    }

    private HttpResponse httpRequest(
            String url,
            String jsonContent,
            String httpMethod,
            Map<String,String> requestHeaders,
            int readTimeout,
            int connectTimeout,
            int requestTimeout) throws RemoteInvocationException {
        RequestConfig config = RequestConfig.custom()
                .setSocketTimeout(readTimeout)
                .setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(requestTimeout)
                .build();

        HttpClientBuilder clientBuilder = HttpClientBuilder.create()
                .setDefaultRequestConfig(config);

        HttpClient httpClient = clientBuilder.build();

        RequestBuilder requestBuilder = RequestBuilder.create(httpMethod).setUri(url);

        if (requestHeaders != null) {
            requestHeaders.forEach((k,v) -> requestBuilder.addHeader(k,v));
        }

        if (jsonContent != null && !jsonContent.equals("")) {
            requestBuilder.addHeader("Content-Type","application/json");
            StringEntity entity = new StringEntity(jsonContent, ContentType.APPLICATION_JSON);
            requestBuilder.setEntity(entity);
        }

        logger.info("Invoking remote endpoint {} {} Headers: {} Body: {}.", httpMethod, url, requestHeaders, jsonContent);

        HttpResponse httpResponse;
        try {
            httpResponse = httpClient.execute(requestBuilder.build());
        } catch (IOException e) {
            throw new RemoteInvocationException("Unable to invoke remote endpoint.", e);
        }
        return httpResponse;
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
     * Complete WorkItem and store the http service response.
     * Long running operations are not completed using this handler but via REST api call.
     */
    private void completeWorkItem(
            WorkItemManager manager,
            long workItemId,
            int responseCode,
            Map<String, Object> serviceInvocationResult,
            String cancelUrl) {
        completeWorkItem(
                manager,
                workItemId,
                responseCode,
                serviceInvocationResult,
                cancelUrl,
                Optional.empty());
    }

    private void completeWorkItem(WorkItemManager manager, long workItemId, Throwable cause) {
        completeWorkItem(
                manager,
                workItemId,
                -1,
                Collections.emptyMap(),
                "",
                Optional.ofNullable(cause));
    }

    private void completeWorkItem(
            WorkItemManager manager,
            long workItemId,
            int responseCode,
            Map<String, Object> serviceInvocationResult,
            String cancelUrl,
            Optional<Throwable> cause) {
        Map<String, Object> results = new HashMap<>();
        results.put("responseCode", responseCode);
        results.put("result", serviceInvocationResult);
        results.put("cancelUrl", cancelUrl);
        cause.ifPresent(c -> {
            results.put("error", c);
        });
        logger.info("Rest service workitem completion result {}.", results);
        manager.completeWorkItem(workItemId, results);
    }


    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        completeWorkItem(manager, workItem.getId(), new WorkitemAbortedException());
    }
}
