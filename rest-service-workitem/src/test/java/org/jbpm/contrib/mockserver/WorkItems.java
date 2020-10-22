/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.contrib.mockserver;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author Ryszard Kozmik
 */
@Path("/server/containers/mock/processes/instances/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkItems {

    private final Logger logger = LoggerFactory.getLogger(WorkItems.class);

    @Context
    ServletContext servletContext;

    @PUT
    @Path("{instanceId}/workitems/{id}/completed")
    public Response complete(
            @PathParam("id") int taskId,
            @PathParam("instanceId") int instanceId,
            Map<String, Object> result)
    {
        logger.info("Completing workitem id: {}, result: {}.", taskId, result);
        RuntimeEngine runtimeEngine = getRuntimeEngine(instanceId);
        KieSession kieSession = runtimeEngine.getKieSession();
        kieSession.getWorkItemManager().completeWorkItem(taskId, result);
        disposeRuntimeEngine(runtimeEngine);

        Map<String, Object> response = new HashMap<>();
        return Response.status(200).entity(response).build();
    }

    /**
     * Receive service response
     */
    @POST
    @Path("{instanceId}/signal/{signalName}")
    public Response signalProcess(
            @PathParam("signalName") String signalName,
            @PathParam("instanceId") long instanceId,
            Map<String, Object> result)
    {
        logger.info("Sending {} signal to process id: {}, result: {}.", signalName, instanceId, result);
        RuntimeEngine runtimeEngine = getRuntimeEngine(instanceId);
        KieSession kieSession = runtimeEngine.getKieSession();
        kieSession.signalEvent(signalName, result);
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
        return (RuntimeManager) servletContext.getAttribute("runtimeManager");
    }

}
