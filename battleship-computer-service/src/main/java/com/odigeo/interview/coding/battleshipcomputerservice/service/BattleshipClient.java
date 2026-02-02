package com.odigeo.interview.coding.battleshipcomputerservice.service;

import com.odigeo.interview.coding.battleshipapi.contract.DeployShipsCommand;
import com.odigeo.interview.coding.battleshipapi.contract.GameFireCommand;
import com.odigeo.interview.coding.battleshipapi.contract.GameFireResponse;
import com.odigeo.interview.coding.battleshipapi.contract.GameJoinCommand;
import com.odigeo.interview.coding.battleshipcomputerservice.exception.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Singleton
public class BattleshipClient {

    private static final Logger logger = LoggerFactory.getLogger(BattleshipClient.class);

    private static final String BASE_URL = "http://battleship_service:8080/battleship-service/api";
    private final WebTarget target;

    public BattleshipClient() {
        this.target = ClientBuilder.newClient().target(BASE_URL);
    }

    public BattleshipClient(WebTarget target) {
        this.target = target;
    }

    public void joinGame(String gameId, GameJoinCommand gameJoinCommand) {
        Response response = target.path("/games/" + gameId + "/join")
                .request()
                .post(Entity.entity(gameJoinCommand, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            throw new ClientException("game.join", response.getStatus(), response.readEntity(String.class));
        }
    }

    public void deployShips(String gameId, DeployShipsCommand deployShipsCommand) {
        Response response = target.path("/games/" + gameId + "/fields/ships/deploy")
                .request()
                .post(Entity.entity(deployShipsCommand, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            throw new ClientException("game.field.ships.deploy", response.getStatus(), response.readEntity(String.class));
        }
    }

    public GameFireResponse fire(String gameId, GameFireCommand gameFireCommand) {
        Response response = target.path("/games/" + gameId + "/fields/fire")
                .request()
                .post(Entity.entity(gameFireCommand, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            throw new ClientException("game.field.fire", response.getStatus(), response.readEntity(String.class));
        } else {
            return response.readEntity(GameFireResponse.class);
        }
    }

}
