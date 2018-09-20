package org.jbpm.contrib.restservice;

import org.apache.http.HttpResponse;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.jbpm.contrib.restservice.Utils.getCancelUrlVariableName;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class CancelTaskOperation {

    private static Logger logger = LoggerFactory.getLogger(CancelTaskOperation.class);

    private RuntimeManager runtimeManager;

    public CancelTaskOperation(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
    }

    void cancelTask(NodeInstance task, WorkflowProcessInstance processInstance) {

        //TODO might require wrapping in transaction https://developer.jboss.org/thread/199180
//        long timeoutProcessInstanceId = (long) processInstance.getVariable(getTimeoutPIIDVariableName(taskName));
//        WorkflowProcessInstance timeoutProcessInstance = getProcessInstance(runtimeManager, timeoutProcessInstanceId);
//
//        boolean forceCancel = (boolean) timeoutProcessInstance.getVariable(FORCE_CANCEL_VARIABLE);
        boolean forceCancel = false;
        boolean willCancel = false;

        String nodeName = task.getNodeName();
        if (!forceCancel) {
            String cancelUrl = (String) processInstance.getVariable(getCancelUrlVariableName(nodeName));
            logger.info("Invoking remote cancellation. pid: {}, taskName: {}, cancelUrl: {}.", processInstance.getId(), nodeName, cancelUrl);
            willCancel = cancelRemote(cancelUrl);
        }

        if (willCancel) {
            //set cancel timeout
//            Map<String, Object> taskInputVariables = activeTask.getTaskData().getTaskInputVariables();
//            long cancelTimeout = (long) taskInputVariables.get(CANCEL_TIMEOUT_VARIABLE);
//            timeoutProcessInstance.setVariable(FORCE_CANCEL_VARIABLE, true);
//            kieSession.execute(new UpdateTimerCommand(timeoutProcessInstanceId, TIMEOUT_TIMER_TASK_NAME, cancelTimeout));
        } else {
            logger.info("Remote endpoint did not accept cancel request. Cancelling internally pid: {}, taskId: {}, taskName: {}.", processInstance.getId(), task.getId(), nodeName);
            Map<String, Object> data = new HashMap<>();
            Map<String, Object> result = new HashMap<>();
            data.put("remote-cancel-failed", true);
            result.put("content", data);
            RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(EmptyContext.get());
            runtimeEngine.getKieSession().getWorkItemManager().completeWorkItem(task.getId(), result);
        }
    }

    private boolean cancelRemote(String cancelUrl) {
        String loginToken = ""; //TODO

        HttpResponse httpResponse;
        try {
            httpResponse = Utils.httpRequest(
                    cancelUrl,
                    "",
                    loginToken,
                    5000,
                    5000,
                    5000);
        } catch (IOException e) {
            logger.warn("Cannot cancel remote service.", e);
            return false;
        }
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        return statusCode >=200 && statusCode < 300;
    }
}
