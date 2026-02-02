package com.odigeo.interview.coding.battleshipservice.service;

import com.odigeo.interview.coding.battleshipapi.contract.DeployShipsCommand;
import com.odigeo.interview.coding.battleshipapi.contract.GameFireCommand;
import com.odigeo.interview.coding.battleshipapi.contract.GameFireResponse;
import com.odigeo.interview.coding.battleshipapi.contract.GameJoinCommand;
import com.odigeo.interview.coding.battleshipapi.contract.GameStartCommand;
import com.odigeo.interview.coding.battleshipapi.event.GameCreatedEvent;
import com.odigeo.interview.coding.battleshipapi.event.GameFireEvent;
import com.odigeo.interview.coding.battleshipservice.exception.GameFinishedException;
import com.odigeo.interview.coding.battleshipservice.exception.GameJoinException;
import com.odigeo.interview.coding.battleshipservice.exception.GameStartException;
import com.odigeo.interview.coding.battleshipservice.exception.NotYourTurnException;
import com.odigeo.interview.coding.battleshipservice.exception.ShipDeploymentException;
import com.odigeo.interview.coding.battleshipservice.exception.ShipsAlreadyDeployedException;
import com.odigeo.interview.coding.battleshipservice.model.Cell;
import com.odigeo.interview.coding.battleshipservice.model.Coordinate;
import com.odigeo.interview.coding.battleshipservice.model.Game;
import com.odigeo.interview.coding.battleshipservice.model.ship.Ship;
import com.odigeo.interview.coding.battleshipservice.model.ship.ShipType;
import com.odigeo.interview.coding.battleshipservice.repository.GameRepositoryImpl;
import com.odigeo.interview.coding.battleshipservice.util.GameConfiguration;
import com.odigeo.interview.coding.battleshipservice.util.ShipDeploymentBuilder;
import com.odigeo.interview.coding.battleshipservice.util.ShipDeploymentValidator;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class GameServiceTest {

    @Mock
    private CoordinateService coordinateService;
    @Mock
    private FieldService fieldService;
    @Mock
    private ShipDeploymentValidator shipDeploymentValidator;
    @Mock
    private KafkaProducerService kafkaProducerService;
    @Mock
    private GameRepositoryImpl gameRepository;
    @Mock
    private Game game;

    @InjectMocks
    private GameService gameService;

    @BeforeMethod
    public void init() {
        initMocks(this);
        when(coordinateService.decodeCoordinate(any())).thenCallRealMethod();
        when(fieldService.allShipsSunk(any())).thenCallRealMethod();
        when(fieldService.isShipSunk(any(), any())).thenCallRealMethod();
    }

    @AfterMethod
    public void tearDown() {
        Mockito.reset(kafkaProducerService);
        Mockito.reset(gameRepository);
    }

    @Test
    public void testNewGameVsComputer() {
        GameStartCommand command = new GameStartCommand();
        command.setPlayerId("player1");
        command.setVsComputer(true);
        Game newGame = gameService.newGame(command);
        assertNotNull(newGame);
        assertNotNull(newGame.getId());
        assertEquals(newGame.getPlayerOneId(), command.getPlayerId());
        assertEquals(newGame.isVsComputer(), command.isVsComputer());
        verify(kafkaProducerService, times(1)).publish(any(GameCreatedEvent.class));
        verify(gameRepository, times(1)).saveOrUpdateGame(any(Game.class));
    }

    @Test
    public void testNewGameVsHumanPlayer() {
        GameStartCommand command = new GameStartCommand();
        command.setPlayerId("player1");
        command.setVsComputer(false);
        Game newGame = gameService.newGame(command);
        assertNotNull(newGame);
        assertNotNull(newGame.getId());
        assertEquals(newGame.getPlayerOneId(), command.getPlayerId());
        assertEquals(newGame.isVsComputer(), command.isVsComputer());
        verify(kafkaProducerService, never()).publish(any(GameCreatedEvent.class));
        verify(gameRepository, times(1)).saveOrUpdateGame(any(Game.class));
    }

    @Test
    public void testJoinGame() {
        when(game.getId()).thenReturn("12345");
        when(game.getPlayerTwoId()).thenReturn(null);
        when(gameRepository.getGame(any())).thenReturn(Optional.of(game));
        GameJoinCommand command = new GameJoinCommand();
        command.setPlayerId("player2");
        gameService.joinGame("12345", command);
        verify(gameRepository, times(1)).saveOrUpdateGame(game);
    }

    @Test(expectedExceptions = GameJoinException.class, expectedExceptionsMessageRegExp = "Another player is already playing this game")
    public void testJoinFullGame() {
        when(game.getId()).thenReturn("12345");
        when(game.getPlayerTwoId()).thenReturn("anotherPlayer");
        when(gameRepository.getGame(any())).thenReturn(Optional.of(game));
        GameJoinCommand command = new GameJoinCommand();
        command.setPlayerId("player2");
        gameService.joinGame("12345", command);
        verify(gameRepository, never()).saveOrUpdateGame(game);
    }

    @Test
    public void testDeployShips() {
        when(game.getId()).thenReturn("12345");
        when(game.getPlayerOneId()).thenReturn("player1");
        when(gameRepository.getGame(any())).thenReturn(Optional.of(game));
        DeployShipsCommand command = new DeployShipsCommand();
        command.setPlayerId("player1");
        command.setShipsDeploy(ShipDeploymentBuilder.buildShipsDeployment());
        gameService.deployShips("12345", command);
        verify(gameRepository, times(1)).saveOrUpdateGame(game);
    }

    @Test(expectedExceptions = ShipDeploymentException.class)
    public void testDeployShipsWithIncorrectShipType() {
        when(game.getId()).thenReturn("12345");
        when(game.getPlayerOneId()).thenReturn("player1");
        when(gameRepository.getGame(any())).thenReturn(Optional.of(game));
        DeployShipsCommand command = new DeployShipsCommand();
        command.setPlayerId("player1");
        List<DeployShipsCommand.ShipDeployment> shipsDeployment = ShipDeploymentBuilder.buildShipsDeployment();
        shipsDeployment.remove(shipsDeployment.size()-1);
        shipsDeployment.add(new DeployShipsCommand.ShipDeployment("YellowSubmarine", "A1", "B1"));
        command.setShipsDeploy(shipsDeployment);
        gameService.deployShips("12345", command);
    }

    @Test
    public void testDeployShipsAndPlayersReady() {
        when(game.getId()).thenReturn("12345");
        when(game.playerReady(any())).thenReturn(false);
        when(game.playersReady()).thenReturn(true);
        when(gameRepository.getGame(any())).thenReturn(Optional.of(game));
        DeployShipsCommand command = new DeployShipsCommand();
        command.setPlayerId("player1");
        command.setShipsDeploy(ShipDeploymentBuilder.buildShipsDeployment());
        gameService.deployShips("12345", command);
        verify(gameRepository, times(1)).saveOrUpdateGame(game);
    }

    @Test(expectedExceptions = ShipsAlreadyDeployedException.class, expectedExceptionsMessageRegExp = "player1's ships are already deployed")
    public void testDeployShipsAlreadyDeployed() {
        when(game.getId()).thenReturn("12345");
        when(game.playerReady(any())).thenReturn(true);
        when(gameRepository.getGame(any())).thenReturn(Optional.of(game));
        DeployShipsCommand command = new DeployShipsCommand();
        command.setPlayerId("player1");
        command.setShipsDeploy(ShipDeploymentBuilder.buildShipsDeployment());
        gameService.deployShips("12345", command);
        verify(gameRepository, never()).saveOrUpdateGame(game);
    }

    @Test(expectedExceptions = GameFinishedException.class, expectedExceptionsMessageRegExp = "The winner is player2")
    public void testFireWhenGameIsAlreadyFinished() {
        when(game.getId()).thenReturn("12345");
        when(game.getWinner()).thenReturn("player2");
        when(game.isFinished()).thenReturn(true);
        when(gameRepository.getGame(any())).thenReturn(Optional.of(game));
        GameFireCommand command = new GameFireCommand();
        command.setPlayerId("player1");
        command.setCoordinate("A1");
        gameService.fire("12345", command);
        verify(gameRepository, never()).saveOrUpdateGame(game);
    }

    @Test(expectedExceptions = GameStartException.class, expectedExceptionsMessageRegExp = "Players not ready")
    public void testFireWhenPlayersNotReady() {
        when(game.getId()).thenReturn("12345");
        when(game.isFinished()).thenReturn(false);
        when(game.playersReady()).thenReturn(false);
        when(gameRepository.getGame(any())).thenReturn(Optional.of(game));
        GameFireCommand command = new GameFireCommand();
        command.setPlayerId("player1");
        command.setCoordinate("A1");
        gameService.fire("12345", command);
        verify(gameRepository, never()).saveOrUpdateGame(game);
    }

    @Test(expectedExceptions = NotYourTurnException.class, expectedExceptionsMessageRegExp = "player1 is not your turn")
    public void testFireWhenIsNotPlayerTurn() {
        when(game.getId()).thenReturn("12345");
        when(game.isFinished()).thenReturn(false);
        when(game.playersReady()).thenReturn(true);
        when(game.isPlayerTurn(any(String.class))).thenReturn(false);
        when(gameRepository.getGame(any())).thenReturn(Optional.of(game));
        GameFireCommand command = new GameFireCommand();
        command.setPlayerId("player1");
        command.setCoordinate("A1");
        gameService.fire("12345", command);
        verify(gameRepository, never()).saveOrUpdateGame(game);
    }

    @Test(expectedExceptions = NotYourTurnException.class, expectedExceptionsMessageRegExp = "player1 is not your turn")
    public void testFireWhenIsNotPlayerTurnAndNeedToPingTheComputer() {
        when(game.getId()).thenReturn("12345");
        when(game.isFinished()).thenReturn(false);
        when(game.playersReady()).thenReturn(true);
        when(game.isPlayerTurn(any(String.class))).thenReturn(false);
        when(game.isVsComputer()).thenReturn(true);
        when(game.isPlayerTurn(1)).thenReturn(true);
        when(gameRepository.getGame(any())).thenReturn(Optional.of(game));
        GameFireCommand command = new GameFireCommand();
        command.setPlayerId("player1");
        command.setCoordinate("A1");
        gameService.fire("12345", command);
        verify(kafkaProducerService, times(1)).publish(any(GameFireEvent.class));
        verify(gameRepository, never()).saveOrUpdateGame(game);
    }

    @Test
    public void testFireHit() {
        final String[] gridCoordinate = new String[] {"A1", "A2"};
        Cell[][] field = buildFieldWithShip("Destroyer", gridCoordinate);
        when(game.getId()).thenReturn("12345");
        when(game.isVsComputer()).thenReturn(true);
        when(game.isFinished()).thenReturn(false);
        when(game.playersReady()).thenReturn(true);
        when(game.isPlayerTurn(any(String.class))).thenReturn(true);
        when(game.isPlayerTurn(1)).thenReturn(true);
        when(game.getOpponentField(any())).thenReturn(field);
        when(gameRepository.getGame(any())).thenReturn(Optional.of(game));
        GameFireCommand command = new GameFireCommand();
        command.setPlayerId("player1");
        command.setCoordinate(gridCoordinate[0]);
        GameFireResponse fireResponse = gameService.fire("12345", command);
        assertNotNull(fireResponse);
        assertEquals(fireResponse.getFireOutcome(), GameFireResponse.FireOutcome.HIT);
        verify(kafkaProducerService, times(1)).publish(any(GameFireEvent.class));
        verify(gameRepository, times(1)).saveOrUpdateGame(game);
    }

    @Test
    public void testFireSunkAndGameWon() {
        final String[] gridCoordinate = new String[] {"A1"};
        Cell[][] field = buildFieldWithShip("Destroyer", gridCoordinate);
        when(game.getId()).thenReturn("12345");
        when(game.isVsComputer()).thenReturn(true);
        when(game.isFinished()).thenReturn(false);
        when(game.playersReady()).thenReturn(true);
        when(game.isPlayerTurn(any(String.class))).thenReturn(true);
        when(game.isPlayerTurn(1)).thenReturn(true);
        when(game.getOpponentField(any())).thenReturn(field);
        when(gameRepository.getGame(any())).thenReturn(Optional.of(game));
        GameFireCommand command = new GameFireCommand();
        command.setPlayerId("player1");
        command.setCoordinate(gridCoordinate[0]);
        GameFireResponse fireResponse = gameService.fire("12345", command);
        assertNotNull(fireResponse);
        verify(kafkaProducerService, times(1)).publish(any(GameFireEvent.class));
        verify(gameRepository, times(1)).saveOrUpdateGame(game);
    }

    @Test
    public void testFireMiss() {
        final String[] gridCoordinate = new String[] {"A1"};
        Cell[][] field = buildFieldWithShip("Destroyer", gridCoordinate);
        when(game.getId()).thenReturn("12345");
        when(game.isVsComputer()).thenReturn(true);
        when(game.isFinished()).thenReturn(false);
        when(game.playersReady()).thenReturn(true);
        when(game.isPlayerTurn(any(String.class))).thenReturn(true);
        when(game.isPlayerTurn(1)).thenReturn(true);
        when(game.getOpponentField(any())).thenReturn(field);
        when(gameRepository.getGame(any())).thenReturn(Optional.of(game));
        GameFireCommand command = new GameFireCommand();
        command.setPlayerId("player1");
        command.setCoordinate("B6");
        GameFireResponse fireResponse = gameService.fire("12345", command);
        assertNotNull(fireResponse);
        assertEquals(fireResponse.getFireOutcome(), GameFireResponse.FireOutcome.MISS);
        verify(kafkaProducerService, times(1)).publish(any(GameFireEvent.class));
        verify(gameRepository, times(1)).saveOrUpdateGame(game);
    }

    private Cell[][] buildFieldWithShip(String shipType, String... gridCoordinates) {
        Cell[][] field = buildWater();
        deployShip(field, shipType, gridCoordinates);
        return field;
    }

    private void deployShip(Cell[][] field, String shipType, String[] gridCoordinates) {
        Ship ship = ShipType.getByTypeName(shipType).newInstance();
        List<Coordinate> coordinates = Arrays.stream(gridCoordinates)
                .map(gridCoordinate -> coordinateService.decodeCoordinate(gridCoordinate))
                .collect(Collectors.toList());
        coordinates.forEach(coordinate -> {
            ship.getCoordinates().add(coordinate);
            field[coordinate.getRow()][coordinate.getColumn()] = new Cell(ship);
        });
    }

    private Cell[][] buildWater() {
        Cell[][] field = new Cell[GameConfiguration.FIELD_HEIGHT][GameConfiguration.FIELD_WIDTH];
        for (int row = 0; row < GameConfiguration.FIELD_HEIGHT; row++) {
            for (int col = 0; col < GameConfiguration.FIELD_WIDTH; col++) {
                field[row][col] = new Cell();
            }
        }
        return field;
    }

}
