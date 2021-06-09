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
package org.jbpm.process.longrest.mockserver;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.SessionNotFoundException;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/services/rest/server/containers/default-per-pinstance/processes/instances/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkItems {

    private final Logger logger = LoggerFactory.getLogger(WorkItems.class);

    public static final String RUNTIME_MANAGER_KEY = "runtimeManager";

    @Context
    ServletContext servletContext;

    /**
     * Receive service response
     */
    @POST
    @Path("{instanceId}/signal/{signalName}")
    public Response signalProcess(
            @PathParam("signalName") String signalName,
            @PathParam("instanceId") long instanceId,
            Map<String, Object> result) {
        logger.info("Mock server received signal {} and sending it to process id: {}, result: {}.", signalName, instanceId, result);
        RuntimeEngine runtimeEngine = getRuntimeEngine(instanceId);
        try {
            logger.debug("Mock server getting kiesession for process id: {}.", instanceId);
            KieSession kieSession = runtimeEngine.getKieSession();
            logger.debug("Mock server signalling for process id: {}.", instanceId);
            kieSession.signalEvent(signalName, result);
            logger.debug("Mock server signaled {} to process id: {}.", signalName, instanceId);
        } catch (SessionNotFoundException e) {
            logger.warn("Session not found.", e);
        }
        disposeRuntimeEngine(runtimeEngine);

        return Response.status(200).entity(result).build();
    }

    private RuntimeEngine getRuntimeEngine(long processInstanceId) {
        ProcessInstanceIdContext processInstanceContext = ProcessInstanceIdContext.get(processInstanceId);
        return getRuntimeManager().getRuntimeEngine(processInstanceContext);
    }

    private void disposeRuntimeEngine(RuntimeEngine runtimeEngine) {
        getRuntimeManager().disposeRuntimeEngine(runtimeEngine);
    }

    private RuntimeManager getRuntimeManager() {
        return (RuntimeManager) servletContext.getAttribute(RUNTIME_MANAGER_KEY);
    }
}
