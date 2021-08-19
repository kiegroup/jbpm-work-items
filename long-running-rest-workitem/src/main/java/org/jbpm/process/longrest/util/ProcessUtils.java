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

import org.jbpm.process.longrest.Constant;
import org.jbpm.process.longrest.SystemVariables;
import org.kie.api.runtime.process.WorkItem;

import static org.jbpm.process.longrest.Constant.CONTAINER_ID_VARIABLE;
import static org.jbpm.process.longrest.Constant.PROCESS_INSTANCE_ID_VARIABLE;

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

    /**
     * Reads hostname from the system property or environment variable.
     * System property overrides the env variable.
     * Https overrides the http variable.
     *
     * @return hostName
     */
    public static String getKieHost() {
        String host = System.getProperty(Constant.HOSTNAME_HTTPS);
        if (host != null) {
            host = "https://" + host;
        }
        if (host == null) {
            host = System.getProperty(Constant.HOSTNAME_HTTP);
            if (host != null) {
                host = "http://" + host;
            }
        }
        if (host == null) {
            host = System.getenv(Constant.HOSTNAME_HTTPS);
            if (host != null) {
                host = "https://" + host;
            }
        }
        if (host == null) {
            host = System.getenv(Constant.HOSTNAME_HTTP);
            if (host != null) {
                host = "http://" + host;
            }
        }
        return host;
    }

    public static SystemVariables getSystemVariables() {
        //use url friendly '$(var)' instead of commonly used '${var}' (URI.create(String) fails when '${var}' is in the path)
        String baseUrl = getKieHost() + "/services/rest/server/containers/$(" + CONTAINER_ID_VARIABLE + ")/processes/instances/";
        return new SystemVariables(
                baseUrl + "$(" + PROCESS_INSTANCE_ID_VARIABLE + ")/signal/RESTResponded",
                "POST",
                baseUrl + "$(" + PROCESS_INSTANCE_ID_VARIABLE + ")/signal/imAlive",
                "POST"
        );
    }
}
