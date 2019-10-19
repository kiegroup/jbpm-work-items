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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jbpm.contrib.RestServiceWorkitemIntegrationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author Ryszard Kozmik
 */
@Path("/server/containers/mock/processes/instances/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkItems {

    private final Logger logger = LoggerFactory.getLogger(WorkItems.class);

    @PUT
    @Path("{instanceId}/workitems/{id}/completed")
    public Response complete(
            @PathParam("id") int taskId,
            @PathParam("instanceId") int instanceId,
            Map<String, Object> result)
    {
        logger.info("Completing workitem id: {}, result: {}.", taskId, result);
        RestServiceWorkitemIntegrationTest.completeWorkItem(taskId, result);

        Map<String, Object> response = new HashMap<>();
        return Response.status(200).entity(response).build();

    }

}
