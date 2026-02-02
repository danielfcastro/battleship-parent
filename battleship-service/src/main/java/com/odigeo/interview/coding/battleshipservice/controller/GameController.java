package com.odigeo.interview.coding.battleshipservice.controller;

import com.odigeo.interview.coding.battleshipapi.contract.DeployShipsCommand;
import com.odigeo.interview.coding.battleshipapi.contract.GameFireCommand;
import com.odigeo.interview.coding.battleshipapi.contract.GameFireResponse;
import com.odigeo.interview.coding.battleshipapi.contract.GameJoinCommand;
import com.odigeo.interview.coding.battleshipapi.contract.GameResponse;
import com.odigeo.interview.coding.battleshipapi.contract.GameStartCommand;
import com.odigeo.interview.coding.battleshipservice.model.Game;
import com.odigeo.interview.coding.battleshipservice.service.GameService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/games")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GameController {

    @Inject
    private GameService service;

    public GameController() { }

    @POST
    @Path("/new")
    public GameResponse newGame(GameStartCommand gameStartCommand) {
        Game game = service.newGame(gameStartCommand);
        return new GameResponse(game.getId());
    }

    @POST
    @Path("/{gameId}/join")
    public void joinGame(@PathParam("gameId") String gameId, GameJoinCommand gameJoinCommand) {
        service.joinGame(gameId, gameJoinCommand);
    }

    @POST
    @Path("/{gameId}/fields/ships/deploy")
    public void deployShips(@PathParam("gameId") String gameId, DeployShipsCommand deployShipsCommand) {
        service.deployShips(gameId, deployShipsCommand);
    }

    @POST
    @Path("/{gameId}/fields/fire")
    public GameFireResponse fire(@PathParam("gameId") String gameId, GameFireCommand gameFireCommand) {
        return service.fire(gameId, gameFireCommand);
    }

}
