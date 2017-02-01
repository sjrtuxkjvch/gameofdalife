package org.str.backend;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/service")
public interface GameOfLifeService {

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Path("/nextstate")
    public GameOfLifeState calculateNextState(@QueryParam("state") GameOfLifeState state);

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/builtin/{id}")
    public GameOfLifeState getBuiltInState(@PathParam("id") String identifier);
}
