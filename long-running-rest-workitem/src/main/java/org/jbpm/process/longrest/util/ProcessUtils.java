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
package org.jbpm.process.longrest.util;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

public class ProcessUtils {

    public static WorkflowProcessInstance getProcessInstance(RuntimeManager runtimeManager, long processInstanceId) {
        return (WorkflowProcessInstance)getKsession(runtimeManager, processInstanceId).getProcessInstance(processInstanceId);
    }

    public static KieSession getKsession(RuntimeManager runtimeManager, Long processInstanceId) {
        if (runtimeManager != null) {
            RuntimeEngine engine = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
            return engine.getKieSession();
        }
        return null;
    }

    public static String getStringParameter(WorkItem workItem, String parameterName) {
        Object parameter = workItem.getParameter(parameterName);
        if (parameter != null) {
            return (String) parameter;
        } else {
            return "";
        }
    }

    public static int getIntParameter(WorkItem workItem, String parameterName, int defaultValue) {
        Object parameter = workItem.getParameter(parameterName);
        if (parameter != null) {
            return (Integer) parameter;
        } else {
            return defaultValue;
        }
    }
}
