package com.odigeo.interview.coding.battleshipservice.controller;

import com.odigeo.interview.coding.battleshipapi.contract.DeployShipsCommand;
import com.odigeo.interview.coding.battleshipapi.contract.GameFireCommand;
import com.odigeo.interview.coding.battleshipapi.contract.GameFireResponse;
import com.odigeo.interview.coding.battleshipapi.contract.GameJoinCommand;
import com.odigeo.interview.coding.battleshipapi.contract.GameResponse;
import com.odigeo.interview.coding.battleshipapi.contract.GameStartCommand;
import com.odigeo.interview.coding.battleshipservice.model.Game;
import com.odigeo.interview.coding.battleshipservice.service.GameService;
import com.odigeo.interview.coding.battleshipservice.util.ShipDeploymentBuilder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GameControllerTest {

    @InjectMocks
    GameController gameController;

    @Mock
    GameService gameService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

    }

    @AfterMethod
    public void tearDown() {
        Mockito.reset(gameService);
    }

    @Test
    public void testNewGame() {
        GameStartCommand gameStartCommand = new GameStartCommand();
        Game game = new Game();
        createGame(gameStartCommand, game);
        GameResponse gameResponse = gameController.newGame(gameStartCommand);
        Assert.assertNotNull(gameResponse);
        Assert.assertTrue(game.getId() == gameResponse.getId());
    }

    private void createGame(GameStartCommand gameStartCommand, Game game) {
        gameStartCommand.setPlayerId("Player1");
        gameStartCommand.setVsComputer(true);
        game.setId(gameStartCommand.getPlayerId());
        Mockito.when(gameService.newGame(Mockito.any())).thenReturn(game);
    }

    @Test
    public void testJoinGame() {
        GameJoinCommand gameJoinCommand = new GameJoinCommand();
        gameJoinCommand.setPlayerId("Player2");
        GameStartCommand gameStartCommand = new GameStartCommand();
        Game game = new Game();
        createGame(gameStartCommand, game);
        GameResponse gameResponse = gameController.newGame(gameStartCommand);
        gameController.joinGame(gameResponse.getId(), gameJoinCommand);
    }

    @Test
    public void testDeployShips() {
        GameStartCommand gameStartCommand = new GameStartCommand();
        Game game = new Game();
        createGame(gameStartCommand, game);
        GameResponse gameResponse = gameController.newGame(gameStartCommand);
        DeployShipsCommand deployShipsCommand = new DeployShipsCommand();
        deployShipsCommand.setPlayerId(gameStartCommand.getPlayerId());
        deployShipsCommand.setShipsDeploy(ShipDeploymentBuilder.buildShipsDeployment());
        gameController.deployShips(game.getId(),deployShipsCommand);
    }

    @Test
    public void testFire() {
        GameStartCommand gameStartCommand = new GameStartCommand();
        Game game = new Game();
        createGame(gameStartCommand, game);
        GameResponse gameResponse = gameController.newGame(gameStartCommand);
        GameFireCommand gameFireCommand = new GameFireCommand();
        gameFireCommand.setCoordinate("0,0");
        gameFireCommand.setPlayerId(gameStartCommand.getPlayerId());
        GameFireResponse gameFireResponse = new GameFireResponse();
        gameFireResponse.setGameWon(true);
        Mockito.when(gameController.fire(Mockito.any(),Mockito.any())).thenReturn(gameFireResponse);
        GameFireResponse gameFireResponseFinal = gameController.fire(game.getId(), gameFireCommand);
        Assert.assertTrue(gameFireResponseFinal.isGameWon());

    }
}
