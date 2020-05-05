package org.jbpm.contrib;

import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    }

    public void beforeProcessCompleted(ProcessCompletedEvent event) {
        logger.info("Process instance id {} completed.", event.getProcessInstance().getId());
    }
}