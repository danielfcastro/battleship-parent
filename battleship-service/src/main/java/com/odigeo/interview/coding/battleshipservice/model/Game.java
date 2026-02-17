package com.odigeo.interview.coding.battleshipservice.model;

import com.odigeo.interview.coding.battleshipservice.exception.GameFinishedException;
import com.odigeo.interview.coding.battleshipservice.exception.GameStartException;
import com.odigeo.interview.coding.battleshipservice.exception.NotYourTurnException;
import com.odigeo.interview.coding.battleshipservice.model.ship.Ship;

import java.time.Instant;

public class Game {

    private static final String PLAYER_NOT_FOUND_MESSAGE = "Player %s does not exist in the game.";

    private String id;
    private String playerOneId;
    private String playerTwoId;
    private boolean vsComputer;
    private Integer playerTurn;
    private Cell[][] playerOneField;
    private Cell[][] playerTwoField;
    private Instant createdAt;
    private Instant startedAt;
    private Instant finishedAt;
    private String winner;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlayerOneId() {
        return playerOneId;
    }

    public void setPlayerOneId(String playerOneId) {
        this.playerOneId = playerOneId;
    }

    public String getPlayerTwoId() {
        return playerTwoId;
    }

    public void setPlayerTwoId(String playerTwoId) {
        this.playerTwoId = playerTwoId;
    }

    public boolean isVsComputer() {
        return vsComputer;
    }

    public void setVsComputer(boolean vsComputer) {
        this.vsComputer = vsComputer;
    }

    public Integer getPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(Integer playerTurn) {
        this.playerTurn = playerTurn;
    }

    public Cell[][] getPlayerOneField() {
        return playerOneField;
    }

    public void setPlayerOneField(Cell[][] playerOneField) {
        this.playerOneField = playerOneField;
    }

    public Cell[][] getPlayerTwoField() {
        return playerTwoField;
    }

    public void setPlayerTwoField(Cell[][] playerTwoField) {
        this.playerTwoField = playerTwoField;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public void setPlayerField(String playerId, Cell[][] playerField) {
        if (playerId.equals(getPlayerOneId())) {
            setPlayerOneField(playerField);
        } else if (playerId.equals(getPlayerTwoId())) {
            setPlayerTwoField(playerField);
        } else {
            throw new IllegalArgumentException(String.format(PLAYER_NOT_FOUND_MESSAGE, playerId));
        }
    }

    public Cell[][] getPlayerField(String playerId) {
        if (playerId.equals(getPlayerOneId())) {
            return getPlayerOneField();
        } else if (playerId.equals(getPlayerTwoId())) {
            return getPlayerTwoField();
        } else {
            throw new IllegalArgumentException(String.format(PLAYER_NOT_FOUND_MESSAGE, playerId));
        }
    }

    public Cell[][] getOpponentField(String playerId) {
        if (playerId.equals(getPlayerOneId())) {
            return getPlayerTwoField();
        } else if (playerId.equals(getPlayerTwoId())) {
            return getPlayerOneField();
        } else {
            throw new IllegalArgumentException(String.format(PLAYER_NOT_FOUND_MESSAGE, playerId));
        }
    }

    public void setNextPlayerTurn() {
        setPlayerTurn((getPlayerTurn() % 2) + 1);
    }

    public boolean isPlayerTurn(String playerId) {
        if (playerId.equals(getPlayerOneId())) {
            return isPlayerTurn(1);
        } else if (playerId.equals(getPlayerTwoId())) {
            return isPlayerTurn(2);
        } else {
            throw new IllegalArgumentException(String.format(PLAYER_NOT_FOUND_MESSAGE, playerId));
        }
    }

    public boolean isPlayerTurn(int playerNumber) {
        return getPlayerTurn() != null && getPlayerTurn() == playerNumber;
    }

    public boolean isFinished() {
        return getFinishedAt() != null;
    }

    public boolean playersReady() {
        return getPlayerOneId() != null
                && getPlayerTwoId() != null
                && getPlayerOneField() != null
                && getPlayerTwoField() != null;
    }

    public boolean playerReady(String playerId) {
        return getPlayerField(playerId) != null;
    }

    /**
     * Executes a fire action at the specified coordinate for the given player.
     * This method encapsulates the core game logic within the domain model.
     *
     * @param playerId The ID of the player firing
     * @param coordinate The coordinate to fire at
     * @param fieldService Service to check ship status
     * @return FireResult containing the outcome of the fire action
     * @throws GameFinishedException if the game is already finished
     * @throws GameStartException if both players are not ready
     * @throws NotYourTurnException if it's not the player's turn
     */
    public FireResult fire(String playerId, Coordinate coordinate, FieldOperations fieldOperations) {
        // Validation: Check game state
        if (isFinished()) {
            throw new GameFinishedException(getWinner());
        }

        if (!playersReady()) {
            throw new GameStartException("Players not ready");
        }

        if (!isPlayerTurn(playerId)) {
            throw new NotYourTurnException(playerId);
        }

        // Execute fire logic
        Cell[][] opponentField = getOpponentField(playerId);
        Cell cell = opponentField[coordinate.getRow()][coordinate.getColumn()];

        FireResult result;
        if (cell.isWater()) {
            cell.hit();
            result = new FireResult(FireOutcome.MISS, null, false, false);
        } else {
            cell.hit();
            Ship ship = cell.getShip();
            boolean shipSunk = fieldOperations.isShipSunk(opponentField, ship);

            if (shipSunk) {
                boolean allShipsSunk = fieldOperations.allShipsSunk(opponentField);
                if (allShipsSunk) {
                    setWinner(playerId);
                    setFinishedAt(Instant.now());
                    result = new FireResult(FireOutcome.SUNK, ship.getShipType().getShipTypeName(), true, true);
                } else {
                    result = new FireResult(FireOutcome.SUNK, ship.getShipType().getShipTypeName(), true, false);
                }
            } else {
                result = new FireResult(FireOutcome.HIT, null, false, false);
            }
        }

        setNextPlayerTurn();
        return result;
    }

    /**
     * Value object representing the result of a fire action.
     */
    public static class FireResult {
        private final FireOutcome outcome;
        private final String sunkShipType;
        private final boolean shipSunk;
        private final boolean gameWon;

        public FireResult(FireOutcome outcome, String sunkShipType, boolean shipSunk, boolean gameWon) {
            this.outcome = outcome;
            this.sunkShipType = sunkShipType;
            this.shipSunk = shipSunk;
            this.gameWon = gameWon;
        }

        public FireOutcome getOutcome() {
            return outcome;
        }

        public String getSunkShipType() {
            return sunkShipType;
        }

        public boolean isShipSunk() {
            return shipSunk;
        }

        public boolean isGameWon() {
            return gameWon;
        }
    }

    /**
     * Enum representing the possible outcomes of a fire action.
     */
    public enum FireOutcome {
        HIT, MISS, SUNK
    }

    /**
     * Interface for field service operations needed by Game.
     * This allows Game to depend on an abstraction rather than a concrete implementation (DIP).
     */
    public interface FieldOperations {
        boolean isShipSunk(Cell[][] field, Ship ship);
        boolean allShipsSunk(Cell[][] field);
    }
}
