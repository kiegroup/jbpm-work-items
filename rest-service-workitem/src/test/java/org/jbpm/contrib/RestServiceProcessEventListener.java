package org.jbpm.contrib;

import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RestServiceProcessEventListener extends DefaultProcessEventListener {

    private ActiveTasks activeProcesses;

    public RestServiceProcessEventListener(ActiveTasks activeProcesses) {
        this.activeProcesses = activeProcesses;
    }

    private final Logger logger = LoggerFactory.getLogger(RestServiceProcessEventListener.class);

    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        ProcessInstance processInstance = event.getProcessInstance();
        long processInstanceId = processInstance.getId();
        logger.debug("Started process: {}({})", processInstance.getProcessName(), processInstanceId);
        activeProcesses.started();
    }

    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) event.getProcessInstance();

        NodeInstance nodeInstance = event.getNodeInstance();
        logger.debug(
                "Leaving node {} ({}) with id: {} in the process: {} with id: {}.",
                nodeInstance.getNodeName(),
                nodeInstance.getClass(),
                nodeInstance.getId(),
                processInstance.getProcess().getName(),
                processInstance.getId());
    }

    public void afterProcessCompleted(ProcessCompletedEvent event) {
        ProcessInstance processInstance = event.getProcessInstance();
        logger.info("Process completed: {}({})", processInstance.getProcessName(), processInstance.getId());
        activeProcesses.completed();
    }
}