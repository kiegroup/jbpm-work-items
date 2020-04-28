package org.jbpm.contrib.restservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.drools.core.util.MVELSafeHelper;
import org.jbpm.contrib.restservice.Constant;
import org.jbpm.workflow.instance.impl.ProcessInstanceResolverFactory;
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
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Helper {

    private static Logger logger = LoggerFactory.getLogger(Helper.class);
    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static String getParameterNameCancelUrl(String nodeName) {
        return nodeName + "-cancelUrl";
    }

    public static String getParameterNameTimeoutProcessInstanceId(String nodeName) {
        return nodeName + "-timeoutProcessInstanceId";
    }

    public static String getParameterNameSuccessCondition(String nodeName) {
        return nodeName + "-successCondition";
    }

    public static String getParameterNameSuccessCompletions(String nodeName) {
        return nodeName + "-successCompletion";
    }

    public static WorkflowProcessInstance getProcessInstance(RuntimeManager runtimeManager, long processInstanceId) {
        return (WorkflowProcessInstance)getKsession(runtimeManager, processInstanceId).getProcessInstance(processInstanceId);
    }

    public static KieSession getKsession(RuntimeManager runtimeManager, Long processInstanceId) {
        if (runtimeManager != null) {
            RuntimeEngine engine = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
            return engine.getKieSession();
        }
        return null; //TODO
    }

    public static HttpResponse httpRequest(
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

        logger.info("Invoking remote endpoint {} with data: {}.", url, jsonContent);

        return httpClient.execute(requestBuilder.build());
    }

    public static long getLongParameter(WorkItem workItem, String parameterName) {
        Object parameter = workItem.getParameter(parameterName);
        if (parameter != null) {
            return Long.parseLong((String) parameter);
        } else {
            return -1L;
        }
    }

    public static boolean getBooleanParameter(WorkItem workItem, String parameterName) {
        Object parameter = workItem.getParameter(parameterName);
        if (parameter != null) {
            return (boolean) parameter;
        } else {
            return false;
        }
    }

    public static String getStringParameter(WorkItem workItem, String parameterName) {
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

    public static void startTaskTimeoutProcess(
            KieSession kieSession,
            long mainProcessInstanceId,
            long nodeInstanceId,
            String nodeName,
            long timeoutSeconds,
            boolean forceCancel) {

        WorkflowProcessInstance mainProcessInstance = (WorkflowProcessInstance) kieSession.getProcessInstance(mainProcessInstanceId);
        if (timeoutSeconds > 0) {
            abortPossiblyRunningTimeoutProcess(
                    kieSession,
                    nodeName,
                    mainProcessInstance);

            Map<String, Object> data = new HashMap<>();
            logger.info("Staring timeout process for node instance id: {} in process instance id: {}. Force cancel: {}.", nodeInstanceId, mainProcessInstanceId, forceCancel);
            data.put("timeout", "PT" + timeoutSeconds + "S"); //ISO8601 date format for duration
            data.put(Constant.TIMEOUT_NODE_INSTANCE_ID_VARIABLE, nodeInstanceId);
            data.put(Constant.FORCE_CANCEL_VARIABLE, forceCancel);
            data.put(Constant.MAIN_PROCESS_INSTANCE_ID_VARIABLE, mainProcessInstanceId);
            ProcessInstance timeoutProcessInstance = kieSession.startProcess(Constant.TIMEOUT_PROCESS_NAME, data);
            long timeoutProcessInstanceId = timeoutProcessInstance.getId();
            logger.debug("Started timeout process instance id: {} for nodeInstanceId: {} in process instance id: {}. Force cancel: {}.",
                    timeoutProcessInstanceId, nodeInstanceId, mainProcessInstanceId, forceCancel);
            mainProcessInstance.setVariable(Helper.getParameterNameTimeoutProcessInstanceId(nodeName), timeoutProcessInstanceId);
        } else {
            mainProcessInstance.setVariable(Helper.getParameterNameTimeoutProcessInstanceId(nodeName), -1L);
        }
    }

    public static long startCancelAllProcess(
            KieSession kieSession,
            long mainProcessInstanceId) {
        Map<String, Object> data = new HashMap<>();
        logger.info("Staring cancel all process for processInstanceId: {}.", mainProcessInstanceId);
        data.put("cancel-all", mainProcessInstanceId);
        ProcessInstance cancelProcessInstance = kieSession.startProcess("cancel-all-handler-process", data);
        long cancelProcessInstanceId = cancelProcessInstance.getId();
        logger.debug("Started cancel all process instance.id: {} for  mainProcessInstanceId: {}.",
                cancelProcessInstanceId, mainProcessInstanceId);
        return cancelProcessInstanceId;
    }

    public static boolean isEmpty(String string) {
        if (string == null) {
            return true;
        } else {
            return "".equals(string);
        }
    }

    public static void cancelAll(WorkflowProcessInstance processInstance) {
        //immediately mark as canceled as next node may check for flag before the cancel process sets it.
        processInstance.setVariable("cancelRequested", true);
        processInstance.signalEvent(Constant.CANCEL_SIGNAL_TYPE, processInstance.getId());
//        Utils.startCancelAllProcess(getKsession(runtimeManager,processInstance.getId()), processInstance.getId());
    }

    public static boolean evaluateSuccessCondition(WorkflowProcessInstance processInstance, String successCondition) {
        logger.debug("Evaluating successCondition: {}, for processInstance.id: {}.", successCondition, processInstance.getId());
        try {
            return MVELSafeHelper.getEvaluator().eval(
                    successCondition,
                    new ProcessInstanceResolverFactory(processInstance), Boolean.class);
        } catch (Exception e) {
            String msg = MessageFormat.format("Cannot evaluate success condition {0}.", successCondition);
            logger.debug(msg, e);
            return false;
        }
    }

    private static void abortPossiblyRunningTimeoutProcess(
            KieSession ksession,
            String nodeName,
            WorkflowProcessInstance mainProcessInstance) {
        Object timeoutProcessInstanceIdObj = mainProcessInstance.getVariable(
                getParameterNameTimeoutProcessInstanceId(nodeName));
        if (timeoutProcessInstanceIdObj != null) {
            long timeoutProcessInstanceId = (long) timeoutProcessInstanceIdObj;
            logger.debug("Aborting Timeout Process instance id {}.", timeoutProcessInstanceId);
            ksession.abortProcessInstance(timeoutProcessInstanceId);
        } else {
            logger.debug("Didn't find an existing timeout process instance for node instance name {} in main process instance id {}.",
                    nodeName, mainProcessInstance.getId());
        }
    }
}
