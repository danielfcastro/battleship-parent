package com.odigeo.interview.coding.battleshipservice.model;

import java.time.Instant;

public class Game {

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
            throw new IllegalArgumentException(String.format("Player %s does not exist in the game.", playerId));
        }
    }

    public Cell[][] getPlayerField(String playerId) {
        if (playerId.equals(getPlayerOneId())) {
            return getPlayerOneField();
        } else if (playerId.equals(getPlayerTwoId())) {
            return getPlayerTwoField();
        } else {
            throw new IllegalArgumentException(String.format("Player %s does not exist in the game.", playerId));
        }
    }

    public Cell[][] getOpponentField(String playerId) {
        if (playerId.equals(getPlayerOneId())) {
            return getPlayerTwoField();
        } else if (playerId.equals(getPlayerTwoId())) {
            return getPlayerOneField();
        } else {
            throw new IllegalArgumentException(String.format("Player %s does not exist in the game.", playerId));
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
            throw new IllegalArgumentException(String.format("Player %s does not exist in the game.", playerId));
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
}
