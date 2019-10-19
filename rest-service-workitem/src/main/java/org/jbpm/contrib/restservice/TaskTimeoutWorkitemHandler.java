package org.jbpm.contrib.restservice;

import org.jbpm.contrib.restservice.util.Helper;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import static org.jbpm.contrib.restservice.Constant.FORCE_CANCEL_VARIABLE;
import static org.jbpm.contrib.restservice.Constant.MAIN_PROCESS_INSTANCE_ID_VARIABLE;
import static org.jbpm.contrib.restservice.Constant.TIMEOUT_NODE_INSTANCE_ID_VARIABLE;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Wid(widfile="RestTaskTimeout.wid", 
name="RestTaskTimeout",
displayName="RestTaskTimeout",
defaultHandler="mvel: new org.jbpm.contrib.restservice.TaskTimeoutWorkitemHandler(runtimeManager)",
category="rest-service-workitem",
documentation = "",
parameters={
},
results={
},
mavenDepends={
    @WidMavenDepends(group="org.jbpm.contrib", artifact="rest-service-workitem", version="7.23.0.Final"),
    @WidMavenDepends(group="org.slf4j", artifact="slf4j-api")
},
serviceInfo = @WidService(category = "REST Task Timeout service", description = "",
    keywords = "rest",
    action = @WidAction(title = "Execute a timeout on currently running REST service task.")
)
)
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
        WorkflowProcessInstance timeoutProcessInstance = Helper.getProcessInstance(runtimeManager, workItem.getProcessInstanceId());
        if (timeoutProcessInstance == null) {
            logger.debug("Cannot find Timeout Process instance for main process instance id {}", workItem.getProcessInstanceId());
        }
        Object _nodeInstanceId = timeoutProcessInstance.getVariable(TIMEOUT_NODE_INSTANCE_ID_VARIABLE);
        if (_nodeInstanceId == null) {
            logger.warn("Missing node instance id in Timeout Process Instance id {}.",workItem.getProcessInstanceId() );
        }
        long nodeInstanceId = (long) _nodeInstanceId;

        long mainProcessInstanceId = (long) timeoutProcessInstance.getVariable(MAIN_PROCESS_INSTANCE_ID_VARIABLE);
        WorkflowProcessInstance mainProcessInstance = Helper.getProcessInstance(runtimeManager, mainProcessInstanceId);
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
