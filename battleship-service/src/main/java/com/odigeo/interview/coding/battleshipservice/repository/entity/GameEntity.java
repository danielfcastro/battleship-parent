package com.odigeo.interview.coding.battleshipservice.repository.entity;

import java.time.Instant;

public class GameEntity {

    private String id;
    private String playerOneId;
    private String playerTwoId;
    private boolean vsComputer;
    private Integer playerTurn;
    private String playerOneField;
    private String playerTwoField;
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

    public String getPlayerOneField() {
        return playerOneField;
    }

    public void setPlayerOneField(String playerOneField) {
        this.playerOneField = playerOneField;
    }

    public String getPlayerTwoField() {
        return playerTwoField;
    }

    public void setPlayerTwoField(String playerTwoField) {
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

}
