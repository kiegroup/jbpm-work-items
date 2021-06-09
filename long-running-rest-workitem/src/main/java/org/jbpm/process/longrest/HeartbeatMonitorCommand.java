/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.process.longrest;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jbpm.process.longrest.util.ProcessUtils;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.service.ServiceRegistry;
import org.kie.api.executor.Command;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.kie.api.executor.Reoccurring;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.api.runtime.query.QueryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartbeatMonitorCommand implements Command, Reoccurring {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatMonitorCommand.class);
    private static final List<Integer> activeStates = Collections.singletonList(1);
    private String interval = "PT5S";

    @Override
    public ExecutionResults execute(CommandContext commandContext) throws Exception {
        logger.trace("Running heartbeat validation ...");
        Object intervalOverride = commandContext.getData(Constant.HEARTBEAT_VALIDATION_VARIABLE);
        if (intervalOverride != null) {
            if (!intervalOverride.equals("")) {
                interval = (String) intervalOverride;
            }
        }

        RuntimeDataService runtime = (RuntimeDataService) ServiceRegistry.get().service(ServiceRegistry.RUNTIME_DATA_SERVICE);
        ProcessService processService = (ProcessService) ServiceRegistry.get().service(ServiceRegistry.PROCESS_SERVICE);
        QueryContext queryContext = new QueryContext(0, Integer.MAX_VALUE);
        Collection<ProcessInstanceDesc> processInstances = runtime.getProcessInstancesByProcessName(activeStates, "executerest", null, queryContext);
        logger.debug("Found {} active process instances to validate heartbeat.", processInstances.size());

        for (ProcessInstanceDesc processInstanceDesc : processInstances) {
            try {
                ProcessInstance processInstance = processService.getProcessInstance(processInstanceDesc.getId());
                validateInstance((WorkflowProcessInstance) processInstance, processService);
            } catch (ProcessInstanceNotFoundException e) {
                logger.debug("Process instance {} probably completed during validation. Caught: {}", processInstanceDesc.getId(), e.getMessage());
            }
        }
        logger.trace("Completed heartbeat validation.");
        return new ExecutionResults();
    }

    @Override
    public Date getScheduleTime() {
        return Date.from(Instant.now().plus(Duration.parse(interval)));
    }

    private void validateInstance(WorkflowProcessInstance processInstance, ProcessService processService) {
        Long lastBeatVar = ProcessUtils.getProcessInstanceVariable(processInstance, Constant.LAST_HEARTBEAT_VARIABLE, 0L);

        String heartbeatTimeoutString = ProcessUtils.getProcessInstanceVariable(processInstance, Constant.HEARTBEAT_TIMEOUT_VARIABLE, "");
        logger.debug("Validating heartbeat for pid [{}], lastBeat: [{}], timeout set to: [{}].", processInstance.getId(), heartbeatTimeoutString);

        //presence of the lastBeat is used as active monitor flag
        if (lastBeatVar == 0L || heartbeatTimeoutString.equals("")) {
            return;
        }

        Duration heartbeatTimeout = Duration.parse(heartbeatTimeoutString);

        Instant lastBeat = Instant.ofEpochMilli(lastBeatVar);
        Duration sinceLastBeat = Duration.between(lastBeat, Instant.now());
        logger.debug("Heartbeat evaluation for pid: {}. LastBeat: {}, sinceLastBeat: {}.", processInstance.getId(), lastBeat, sinceLastBeat);
        if (sinceLastBeat.compareTo(heartbeatTimeout) > 0) {
            logger.info("Signalling pid: {} DIED ...", processInstance.getId());
            processService.signalProcessInstance(processInstance.getId(), "died", null);
            logger.debug("Signalled died for pid: {}.", processInstance.getId());
        }
        logger.debug("Heartbeat evaluation for pid: {} completed.", processInstance.getId());
    }

}
