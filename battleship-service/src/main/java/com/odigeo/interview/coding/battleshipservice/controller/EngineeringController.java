package com.odigeo.interview.coding.battleshipservice.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/engineering")
@Produces(MediaType.TEXT_PLAIN)
@SuppressWarnings("java:S1192") // Simple health check endpoint duplicated across microservices
public class EngineeringController {

    @GET
    @Path("/ping")
    public String ping() {
        return "pong";
    }

}
