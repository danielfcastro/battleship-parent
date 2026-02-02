package com.odigeo.interview.coding.battleshipcomputerservice.service;

import com.odigeo.interview.coding.battleshipapi.contract.GameFireResponse;
import com.odigeo.interview.coding.battleshipcomputerservice.util.BattleshipClientCommandBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BattleshipService {

    private static final Logger logger = LoggerFactory.getLogger(BattleshipService.class);

    @Inject
    private BattleshipClient client;

    @Inject
    private CoordinateService coordinateService;

    public void joinGame(String gameId) {
        client.joinGame(gameId, BattleshipClientCommandBuilder.buildGameJoinCommand());
        logger.info("[gameId={}] Computer joined the game", gameId);
    }

    public void deployShips(String gameId) {
        client.deployShips(gameId, BattleshipClientCommandBuilder.buildDeployShipsCommand());
        logger.info("[gameId={}] Computer deployed its ships", gameId);
    }

    public void fire(String gameId) {
        final String coordinate = thinkWhereToFire();
        GameFireResponse gameFireResponse = client.fire(gameId, BattleshipClientCommandBuilder.buildGameFireCommand(coordinate));
        logger.info("[gameId={}] Computer {} the ship on {}", gameId, gameFireResponse.getFireOutcome(), coordinate);
        if (gameFireResponse.isGameWon()) {
            logger.info("[gameId={}] Computer WON the game", gameId);
        }
    }

    private String thinkWhereToFire() {
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            // Nothing to do
        }
        return coordinateService.randomCoordinate();
    }

}
