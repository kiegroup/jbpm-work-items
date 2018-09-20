package org.jbpm.contrib.restservice;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Task;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import static org.jbpm.contrib.restservice.Utils.TIMEOUT_TASK_ID_VARIABLE;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class TaskTimeoutWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static Logger logger = LoggerFactory.getLogger(TaskTimeoutWorkitemHandler.class);

    private RuntimeManager runtimeManager;

    public TaskTimeoutWorkitemHandler() {
    }

    public TaskTimeoutWorkitemHandler(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        TaskService taskService = runtimeManager.getRuntimeEngine(EmptyContext.get()).getTaskService();

        WorkflowProcessInstance processInstance = Utils.getProcessInstance(runtimeManager, workItem.getProcessInstanceId());
        long taskId = (long) processInstance.getVariable(TIMEOUT_TASK_ID_VARIABLE);

        Task activeTask = taskService.getTaskById(taskId);
//        CancelTaskOperation cancelTaskOperation = new CancelTaskOperation(runtimeManager);
//        cancelTaskOperation.cancelTask(taskService, activeTask);
        manager.completeWorkItem(workItem.getId(), Collections.EMPTY_MAP);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

    }

}
