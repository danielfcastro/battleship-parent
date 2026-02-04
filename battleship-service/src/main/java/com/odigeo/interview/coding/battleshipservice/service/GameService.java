package com.odigeo.interview.coding.battleshipservice.service;

import com.odigeo.interview.coding.battleshipapi.contract.DeployShipsCommand;
import com.odigeo.interview.coding.battleshipapi.contract.GameFireCommand;
import com.odigeo.interview.coding.battleshipapi.contract.GameFireResponse;
import com.odigeo.interview.coding.battleshipapi.contract.GameJoinCommand;
import com.odigeo.interview.coding.battleshipapi.contract.GameStartCommand;
import com.odigeo.interview.coding.battleshipapi.event.GameCreatedEvent;
import com.odigeo.interview.coding.battleshipapi.event.GameFireEvent;
import com.odigeo.interview.coding.battleshipservice.exception.GameJoinException;
import com.odigeo.interview.coding.battleshipservice.exception.GameNotFoundException;
import com.odigeo.interview.coding.battleshipservice.exception.ShipDeploymentException;
import com.odigeo.interview.coding.battleshipservice.exception.ShipsAlreadyDeployedException;
import com.odigeo.interview.coding.battleshipservice.model.Cell;
import com.odigeo.interview.coding.battleshipservice.model.Coordinate;
import com.odigeo.interview.coding.battleshipservice.model.Game;
import com.odigeo.interview.coding.battleshipservice.model.ship.Ship;
import com.odigeo.interview.coding.battleshipservice.model.ship.ShipType;
import com.odigeo.interview.coding.battleshipservice.repository.GameRepository;
import com.odigeo.interview.coding.battleshipservice.util.ShipDeploymentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private final CoordinateService coordinateService;
    private final FieldService fieldService;
    private final KafkaProducerService kafkaProducerService;
    private final GameRepository repository;
    private final ShipDeploymentValidator shipDeploymentValidator;

    @Inject
    public GameService(CoordinateService coordinateService, FieldService fieldService,
                       KafkaProducerService kafkaProducerService, GameRepository repository,
                       ShipDeploymentValidator shipDeploymentValidator) {
        this.coordinateService = coordinateService;
        this.fieldService = fieldService;
        this.kafkaProducerService = kafkaProducerService;
        this.repository = repository;
        this.shipDeploymentValidator = shipDeploymentValidator;
    }

    public Game newGame(GameStartCommand command) {
        Game game = new Game();
        game.setId(UUID.randomUUID().toString());
        game.setPlayerOneId(command.getPlayerId());
        game.setVsComputer(command.isVsComputer());
        if (command.isVsComputer()) {
            kafkaProducerService.publish(new GameCreatedEvent(game.getId()));
        }
        game.setCreatedAt(Instant.now());
        game.setPlayerTurn(1);
        repository.saveOrUpdateGame(game);
        logger.info("New game created {}", game.getId());
        return game;
    }

    public void joinGame(String gameId, GameJoinCommand command) {
        Game game = repository.getGame(gameId).orElseThrow(() -> new GameNotFoundException(gameId));

        if (game.getPlayerTwoId() != null) {
            throw new GameJoinException("Another player is already playing this game");
        }

        game.setPlayerTwoId(command.getPlayerId());
        repository.saveOrUpdateGame(game);
    }

    public void deployShips(String gameId, DeployShipsCommand command) {
        Game game = repository.getGame(gameId).orElseThrow(() -> new GameNotFoundException(gameId));

        if (game.playerReady(command.getPlayerId())) {
            throw new ShipsAlreadyDeployedException(command.getPlayerId());
        }
        List<Ship> shipsDeployment = mapShipsDeployment(command.getShipsDeploy());
        shipDeploymentValidator.validate(shipsDeployment);
        Cell[][] playerField = fieldService.buildField(shipsDeployment);
        game.setPlayerField(command.getPlayerId(), playerField);

        if (game.playersReady()) {
            game.setStartedAt(Instant.now());
        }

        repository.saveOrUpdateGame(game);
    }

    private List<Ship> mapShipsDeployment(List<DeployShipsCommand.ShipDeployment> shipDeployments) {
        List<Ship> ships = new ArrayList<>();
        for (DeployShipsCommand.ShipDeployment shipDeployment: shipDeployments) {
            try {
                Ship ship = ShipType.getByTypeName(shipDeployment.getShipType()).newInstance();
                ship.setCoordinates(shipDeployment.getCoordinates().stream()
                        .map(coordinateService::decodeCoordinate)
                        .collect(Collectors.toList()));
                ships.add(ship);
            } catch (Exception e) {
                throw new ShipDeploymentException(shipDeployment.getShipType(), shipDeployment.getCoordinates(), e);
            }
        }
        return ships;
    }

    @SuppressWarnings("java:S3776") // Complex game logic requires nested conditions for fire outcome
    public GameFireResponse fire(String gameId, GameFireCommand command) {
        Game game = repository.getGame(gameId).orElseThrow(() -> new GameNotFoundException(gameId));

        // Decode coordinate
        Coordinate coordinate = coordinateService.decodeCoordinate(command.getCoordinate());

        // Check if we need to notify computer player BEFORE changing turn
        boolean shouldNotifyComputer = game.isVsComputer() && game.isPlayerTurn(1);

        // Execute fire logic in domain model (Rich Domain Model pattern)
        Game.FireResult fireResult = game.fire(command.getPlayerId(), coordinate, fieldService);

        // Publish event for computer player if needed (after successful fire, turn has changed)
        if (shouldNotifyComputer) {
            kafkaProducerService.publish(new GameFireEvent(game.getId()));
        }

        // Persist game state
        repository.saveOrUpdateGame(game);

        // Map domain result to API response
        return mapToGameFireResponse(fireResult);
    }

    private GameFireResponse mapToGameFireResponse(Game.FireResult fireResult) {
        GameFireResponse response;
        switch (fireResult.getOutcome()) {
            case MISS:
                response = new GameFireResponse(GameFireResponse.FireOutcome.MISS);
                break;
            case HIT:
                response = new GameFireResponse(GameFireResponse.FireOutcome.HIT);
                break;
            case SUNK:
                response = new GameFireResponse(GameFireResponse.FireOutcome.SUNK, fireResult.getSunkShipType());
                if (fireResult.isGameWon()) {
                    response.setGameWon(true);
                }
                break;
            default:
                throw new IllegalStateException("Unknown fire outcome: " + fireResult.getOutcome());
        }
        return response;
    }

}
