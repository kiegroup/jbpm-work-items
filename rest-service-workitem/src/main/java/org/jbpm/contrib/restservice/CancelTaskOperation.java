package org.jbpm.contrib.restservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.HttpResponse;
import org.drools.core.process.instance.WorkItem;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.jbpm.contrib.restservice.Utils.CANCEL_TIMEOUT_VARIABLE;
import static org.jbpm.contrib.restservice.Utils.getLongParameter;
import static org.jbpm.contrib.restservice.Utils.getParameterNameCancelUrl;
import static org.jbpm.contrib.restservice.Utils.startTaskTimeoutProcess;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class CancelTaskOperation {

    private static Logger logger = LoggerFactory.getLogger(CancelTaskOperation.class);

    private RuntimeManager runtimeManager;

    public CancelTaskOperation(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
    }

    void cancelTask(NodeInstance nodeInstance, WorkflowProcessInstance mainProcessInstance, boolean forceCancel) {
        if (forceCancel) {
            cancelNow(nodeInstance);
        } else {
            String nodeName = nodeInstance.getNodeName();
            String cancelUrl = (String) mainProcessInstance.getVariable(getParameterNameCancelUrl(nodeName));
            long mainProcessInstanceId = mainProcessInstance.getId();
            logger.info("Invoking remote cancellation. pid: {}, taskName: {}, cancelUrl: {}.",
                    mainProcessInstanceId, nodeName, cancelUrl);
            boolean willCancel = false;
            if (cancelUrl != null && !cancelUrl.equals("")) {
                willCancel = remoteCancelRequest(cancelUrl);
            }
            if (willCancel) {
                KieSession ksession = Utils.getKsession(runtimeManager, mainProcessInstanceId);
                startCancelTimeout(ksession, nodeInstance, mainProcessInstanceId);
            } else {
                logger.info("Remote endpoint did not accept cancel request. Cancelling internally pid: {}, nodeInstance.id: {}, taskName: {}.",
                        mainProcessInstanceId, nodeInstance.getId(), nodeName);
                cancelNow(nodeInstance);
            }
        }
    }

    private void startCancelTimeout(KieSession kieSession, NodeInstance nodeInstance, long mainProcessInstanceId) {
        logger.debug("Starting cancel timeout process for nodeInstance id {} in Main Process instance id: {}.", nodeInstance.getId(), mainProcessInstanceId);
        WorkItem workItem = getWorkItem(nodeInstance);
        long cancelTimeout = getLongParameter(workItem, CANCEL_TIMEOUT_VARIABLE);
        startTaskTimeoutProcess(kieSession, mainProcessInstanceId, nodeInstance.getId(), nodeInstance.getNodeName(), cancelTimeout, true);
    }

    private void cancelNow(NodeInstance nodeInstance) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        data.put(Utils.REMOTE_CANCEL_FAILED, true);
        try {
            result.put("content", Utils.objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            logger.warn("Cannot serialize data.", e);
            result.put("content", "cannot serialize data");
        }
        KieSession ksession = Utils.getKsession(runtimeManager, nodeInstance.getProcessInstance().getId());
        ksession.getWorkItemManager().completeWorkItem(getWorkItem(nodeInstance).getId(), result);
    }

    private boolean remoteCancelRequest(String cancelUrl) {
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

    private WorkItem getWorkItem(NodeInstance nodeInstance) {
        WorkItemNodeInstance workItemNodeInstance = (WorkItemNodeInstance)nodeInstance; //TODO check casting
        return workItemNodeInstance.getWorkItem();
    }
}
