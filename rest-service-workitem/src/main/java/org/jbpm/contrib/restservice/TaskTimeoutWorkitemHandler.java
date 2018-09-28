package org.jbpm.contrib.restservice;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import static org.jbpm.contrib.restservice.Utils.FORCE_CANCEL_VARIABLE;
import static org.jbpm.contrib.restservice.Utils.MAIN_PROCESS_INSTANCE_ID_VARIABLE;
import static org.jbpm.contrib.restservice.Utils.TIMEOUT_NODE_INSTANCE_ID_VARIABLE;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class TaskTimeoutWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static Logger logger = LoggerFactory.getLogger(TaskTimeoutWorkitemHandler.class);

    private RuntimeManager runtimeManager;

    public TaskTimeoutWorkitemHandler() {
    }

    public TaskTimeoutWorkitemHandler(RuntimeManager runtimeManager) {
        logger.info("Constructing with runtimeManager ...");
        this.runtimeManager = runtimeManager;
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        logger.debug("Executing timeout handler ...");
        WorkflowProcessInstance timeoutProcessInstance = Utils.getProcessInstance(runtimeManager, workItem.getProcessInstanceId());
        if (timeoutProcessInstance == null) {
            logger.debug("Cannot find Timeout Process instance for main process instance id {}", workItem.getProcessInstanceId());
        }
        Object _nodeInstanceId = timeoutProcessInstance.getVariable(TIMEOUT_NODE_INSTANCE_ID_VARIABLE);
        if (_nodeInstanceId == null) {
            logger.warn("Missing node instance id in Timeout Process Instance id {}.",workItem.getProcessInstanceId() );
        }
        long nodeInstanceId = (long) _nodeInstanceId;

        long mainProcessInstanceId = (long) timeoutProcessInstance.getVariable(MAIN_PROCESS_INSTANCE_ID_VARIABLE);
        WorkflowProcessInstance mainProcessInstance = Utils.getProcessInstance(runtimeManager, mainProcessInstanceId);
        if (mainProcessInstance == null) {
            logger.warn("Cannot find main process instance with id {} from Timeout Process instance id {}.", mainProcessInstanceId, timeoutProcessInstance.getId());
        }
        boolean forceCancel = (boolean) timeoutProcessInstance.getVariable(FORCE_CANCEL_VARIABLE);
        logger.debug("Running timeout procedure for node instance id: {} in process instance id {}. Force cancel: {}. Timeout Process Instance id: {}.",
                nodeInstanceId, mainProcessInstanceId, forceCancel, timeoutProcessInstance.getId());
        NodeInstance taskToCancel = mainProcessInstance.getNodeInstance(nodeInstanceId);
        logger.info("Running timeout procedure for node name: {} in process instance id {}. Force cancel: {}.", taskToCancel.getNodeName(), mainProcessInstanceId, forceCancel);

        CancelTaskOperation cancelTaskOperation = new CancelTaskOperation(runtimeManager);

        cancelTaskOperation.cancelTask(taskToCancel, mainProcessInstance, forceCancel);
        manager.completeWorkItem(workItem.getId(), Collections.EMPTY_MAP);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

    }

}
