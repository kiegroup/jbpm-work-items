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

import java.util.Optional;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkflowProcessInstance;

public class ProcessUtils {

    public static <T> T getParameter(WorkItem workItem, String parameterName, T defaultValue) {
        Object parameter = workItem.getParameter(parameterName);
        if (parameter != null) {
            return (T) parameter;
        } else {
            return defaultValue;
        }
    }

    /**
     * @deprecated see {@link ProcessUtils#getParameter(WorkItem, String, Object)}
     */
    @Deprecated
    public static String getStringParameter(WorkItem workItem, String parameterName) {
        Object parameter = workItem.getParameter(parameterName);
        if (parameter != null) {
            return (String) parameter;
        } else {
            return "";
        }
    }

    /**
     * @deprecated see {@link ProcessUtils#getParameter(WorkItem, String, Object)}
     */
    @Deprecated
    public static int getIntParameter(WorkItem workItem, String parameterName, int defaultValue) {
        Object parameter = workItem.getParameter(parameterName);
        if (parameter != null) {
            return (Integer) parameter;
        } else {
            return defaultValue;
        }
    }

    public static <T> T getProcessInstanceVariable(WorkflowProcessInstance processInstance, String name, T defaultValue) {
        Object value = processInstance.getVariable(name);
        if (value == null) {
            return defaultValue;
        } else {
            return (T) value;
        }
    }
}
