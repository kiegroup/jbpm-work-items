package org.jbpm.contrib.mockserver;

import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
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
        WorkItemManager workItemManager = JBPMServer.getInstance().getRuntimeEngine().getKieSession().getWorkItemManager();

        workItemManager.completeWorkItem(taskId, result);

        Map<String, Object> response = new HashMap<>();
        return Response.status(200).entity(response).build();

    }

}
