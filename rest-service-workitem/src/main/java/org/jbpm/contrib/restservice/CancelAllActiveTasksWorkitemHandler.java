package org.jbpm.contrib.restservice;

import org.jbpm.contrib.restservice.util.Helper;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
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

import static org.jbpm.contrib.restservice.Constant.MAIN_PROCESS_INSTANCE_ID_VARIABLE;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Wid(widfile="CancelTaskTimeout.wid", 
name="CancelTaskTimeout",
displayName="CancelTaskTimeout",
defaultHandler="mvel: new org.jbpm.contrib.restservice.CancelAllActiveTasksWorkitemHandler(runtimeManager)",
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
serviceInfo = @WidService(category = "REST Task cancel service", description = "",
    keywords = "rest",
    action = @WidAction(title = "Execute a cancel on currently running REST service task.")
)
)
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
        long mainProcessInstanceId = Long.parseLong((String) workItem.getParameter(MAIN_PROCESS_INSTANCE_ID_VARIABLE));

        RuleFlowProcessInstance processInstance = (RuleFlowProcessInstance) Helper.getProcessInstance(runtimeManager, mainProcessInstanceId);
        processInstance.setVariable("cancelRequested", true);

        Set<NodeInstance> activeTasks = getActiveTasks(processInstance);
        logger.info("There are {} active tasks to be canceled in the mainProcessInstance.id {}.", activeTasks.size(), mainProcessInstanceId);
        CancelTaskOperation cancelTaskOperation = new CancelTaskOperation(runtimeManager);
        for (NodeInstance activeTask : activeTasks) {
            cancelTaskOperation.cancelTask(activeTask, processInstance, false);
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
