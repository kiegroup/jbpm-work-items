package org.jbpm.contrib.restservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Utils {

    static final String TIMEOUT_NODE_INSTANCE_ID_VARIABLE = "nodeInstanceId"; //TODO update value

    /**
     * When true the task is canceled internally without trying to cancel remote operation.
     */
    static final String FORCE_CANCEL_VARIABLE = "forceCancel";

    public final static String CANCEL_SIGNAL_TYPE = "cancel-all";

    private static Logger logger = LoggerFactory.getLogger(Utils.class);
    static final ObjectMapper objectMapper = new ObjectMapper();

    static final String TIMEOUT_PROCESS_NAME = "timeout-handler-process";
    static final String CANCEL_TIMEOUT_VARIABLE = "cancelTimeout";
    static final String CANCEL_URL_VARIABLE = "cancelUrl";;
    static final String MAIN_PROCESS_INSTANCE_ID_VARIABLE = "mainProcessInstanceId";

    public static String getCancelUrlVariableName(String nodeInstanceName) {
        return nodeInstanceName + "-" + CANCEL_URL_VARIABLE;
    }

    static WorkflowProcessInstance getProcessInstance(RuntimeManager runtimeManager, long processInstanceId) {
        return (WorkflowProcessInstance)getKsession(runtimeManager, processInstanceId).getProcessInstance(processInstanceId);
    }

    static KieSession getKsession(RuntimeManager runtimeManager, Long processInstanceId) {
        if (runtimeManager != null) {
            RuntimeEngine engine = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
            return engine.getKieSession();
        }
        return null; //TODO
    }

    static HttpResponse httpRequest(
            String url,
            String jsonContent,
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

        return httpClient.execute(request);
    }

    static long getLongParameter(WorkItem workItem, String parameterName) {
        Object parameter = workItem.getParameter(parameterName);
        if (parameter != null) {
            return Long.parseLong((String) parameter);
        } else {
            return -1L;
        }
    }

    static boolean getBooleanParameter(WorkItem workItem, String parameterName) {
        Object parameter = workItem.getParameter(parameterName);
        if (parameter != null) {
            return (boolean) parameter;
        } else {
            return false;
        }
    }

    static String getStringParameter(WorkItem workItem, String parameterName) {
        Object parameter = workItem.getParameter(parameterName);
        if (parameter != null) {
            return (String) parameter;
        } else {
            return "";
        }
    }

    public static boolean getBooleanVariable(WorkflowProcessInstance processInstance, String name) {
        Object variable = processInstance.getVariable(name);
        if (variable instanceof Boolean) {
            return (boolean) variable;
        } else {
            return Boolean.parseBoolean((String) variable);
        }
    }

    public static long startTaskTimeoutProcess(
            KieSession kieSession,
            long mainProcessInstanceId,
            long nodeInstanceId,
            long timeoutSeconds,
            boolean forceCancel) {
        Map<String, Object> data = new HashMap<>();
        logger.info("Staring timeout process for nodeInstanceId: {} belonging to processInstanceId: {}. Force cancel: {}.", nodeInstanceId, mainProcessInstanceId, forceCancel);
        data.put("timeout", "PT" + timeoutSeconds + "S"); //ISO8601 date format for duration
        data.put(TIMEOUT_NODE_INSTANCE_ID_VARIABLE, nodeInstanceId);
        data.put(FORCE_CANCEL_VARIABLE, forceCancel);
        data.put(MAIN_PROCESS_INSTANCE_ID_VARIABLE, mainProcessInstanceId);
        ProcessInstance timeoutProcessInstance = kieSession.startProcess(TIMEOUT_PROCESS_NAME, data);
        long timeoutProcessInstanceId = timeoutProcessInstance.getId();
        logger.debug("Started timeout process instance.id: {} for nodeInstanceId: {} belonging to processInstanceId: {}. Force cancel: {}.",
                timeoutProcessInstanceId, nodeInstanceId, mainProcessInstanceId, forceCancel);
        return timeoutProcessInstanceId;
    }
}
