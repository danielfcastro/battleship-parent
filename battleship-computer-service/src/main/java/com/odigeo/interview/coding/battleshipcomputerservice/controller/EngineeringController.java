package com.odigeo.interview.coding.battleshipcomputerservice.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/engineering")
@Produces(MediaType.TEXT_PLAIN)
public class EngineeringController {

    public EngineeringController() { }

    @GET
    @Path("/ping")
    public String ping() {
        return "pong";
    }

}
