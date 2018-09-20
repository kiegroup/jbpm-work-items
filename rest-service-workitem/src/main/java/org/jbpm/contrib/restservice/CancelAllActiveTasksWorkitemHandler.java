package org.jbpm.contrib.restservice;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class CancelAllActiveTasksWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static Logger logger = LoggerFactory.getLogger(CancelAllActiveTasksWorkitemHandler.class);

    private RuntimeManager runtimeManager;

    public CancelAllActiveTasksWorkitemHandler() {
    }

    public CancelAllActiveTasksWorkitemHandler(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        long mainProcessInstanceId = Long.parseLong((String) workItem.getParameter("parentProcessId"));

        RuleFlowProcessInstance processInstance = (RuleFlowProcessInstance) Utils.getProcessInstance(runtimeManager, mainProcessInstanceId);
        processInstance.setVariable("cancelRequested", true);

        Set<NodeInstance> activeTasks = getActiveTasks(processInstance);
        logger.info("There are {} active tasks to be canceled in the pid {}.", activeTasks.size(), mainProcessInstanceId);
        CancelTaskOperation cancelTaskOperation = new CancelTaskOperation(runtimeManager);
        for (NodeInstance activeTask : activeTasks) {
            cancelTaskOperation.cancelTask(activeTask, processInstance);
        }
        manager.completeWorkItem(workItem.getId(), Collections.EMPTY_MAP);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

    }

    private Set<NodeInstance> getActiveTasks(WorkflowProcessInstance processInstance) {
        return processInstance.getNodeInstances().stream()
                .filter(nodeInstance -> nodeInstance instanceof WorkItemNodeInstance)
                .collect(Collectors.toSet());
    }
}
