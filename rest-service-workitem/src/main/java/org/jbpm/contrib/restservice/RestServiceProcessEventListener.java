package org.jbpm.contrib.restservice;

import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jbpm.contrib.restservice.Utils.cancelAll;
import static org.jbpm.contrib.restservice.Utils.evaluateSuccessCondition;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RestServiceProcessEventListener extends DefaultProcessEventListener {

    private final Logger logger = LoggerFactory.getLogger(RestServiceProcessEventListener.class);

    public RestServiceProcessEventListener() {

    }

    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) event.getProcessInstance();

        if (event.getNodeInstance() instanceof WorkItemNodeInstance) {
            WorkItemNodeInstance nodeInstance = (WorkItemNodeInstance) event.getNodeInstance();
            //TODO do no use hardcoded name
            if (!"RestServiceWorkItemHandler".equals(nodeInstance.getWorkItem().getName())) {
                //run only when exiting RestServiceWorkItemHandler
                return;
            }
        } else {
            //run only when exiting RestServiceWorkItemHandler
            return;
        }
        KieRuntime kieRuntime = event.getKieRuntime();
        String nodeName = event.getNodeInstance().getNodeName();
        long nodeInstanceId = ((WorkItemNodeInstance) event.getNodeInstance()).getWorkItemId();
        logger.debug("Leaving node {} in process instance {}.", nodeName, processInstance.getId());

        //evaluate service completion status
        String successCondition = (String) processInstance.getVariable(Utils.getParameterNameSuccessCondition(nodeName));
        if (Utils.isEmpty(successCondition)) {
            //TODO check if it has been cancelled internally. Do we care if there is no successCondition ?
            processInstance.setVariable(Utils.getParameterNameSuccessCompletions(nodeName), true);
        } else {
            boolean completedSuccessfully = evaluateSuccessCondition(processInstance, successCondition);
            processInstance.setVariable(Utils.getParameterNameSuccessCompletions(nodeName), completedSuccessfully);
            if (!completedSuccessfully) {
                logger.info("Service completed with error, cancelling other operations. ProcessInstanceId {}.",
                        processInstance.getId());
                cancelAll(processInstance);
            }
        }

        //stop timeout process
        //        for (ProcessInstance _timeoutProcessInstance : kieRuntime.getProcessInstances()) {
        //            WorkflowProcessInstance timeoutProcessInstance = (WorkflowProcessInstance) _timeoutProcessInstance;
        //            Object _otherNodeInstanceId = timeoutProcessInstance.getVariable(TIMEOUT_NODE_INSTANCE_ID_VARIABLE);
        //            if (_otherNodeInstanceId != null) { //is a timeout process instance
        //                long otherNodeInstanceId = (long) _otherNodeInstanceId;
        //                if (otherNodeInstanceId == nodeInstanceId) { //is a timeout process instance for this node
        //                    //about all timeout processes for this
        //                    logger.debug("Aborting timeout process instance id {}.", processInstance.getId());
        //                    kieRuntime.abortProcessInstance(timeoutProcessInstance.getId());
        //                }
        //            }
        //        }

        Object timeoutProcessInstanceIdObj = processInstance.getVariable(
                Utils.getParameterNameTimeoutProcessInstanceId(nodeName));
        if (timeoutProcessInstanceIdObj != null) {
            long timeoutProcessInstanceId = (long) timeoutProcessInstanceIdObj;
            long completedNodeInstanceId = event.getNodeInstance().getId();
            logger.debug("Completed nodeInstanceId {} in processInstance.id: {}. TimeoutProcessInstance.id: {}.",
                    completedNodeInstanceId, processInstance.getId(), timeoutProcessInstanceId);
            if (timeoutProcessInstanceId > -1) {
                ProcessInstance timeoutProcessInstance = kieRuntime.getProcessInstance(timeoutProcessInstanceId);
                if (timeoutProcessInstance != null) {
                    logger.info(
                            "Aborting timeout process instance for node instance id: {}. Timeout Process instance id: {}.",
                            completedNodeInstanceId,
                            timeoutProcessInstanceId);
                    kieRuntime.abortProcessInstance(timeoutProcessInstanceId);
                } else {
                    logger.debug(
                            "Not Found timeout process instance for node instance id: {}. Timeout Process instance id: {}.",
                            completedNodeInstanceId,
                            timeoutProcessInstanceId);
                }
            }
        }
    }

    public void beforeProcessCompleted(ProcessCompletedEvent event) {
        logger.info("Process instance id {} completed.", event.getProcessInstance().getId());
    }
}