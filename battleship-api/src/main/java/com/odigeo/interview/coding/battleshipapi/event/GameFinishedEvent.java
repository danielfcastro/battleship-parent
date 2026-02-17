package com.odigeo.interview.coding.battleshipapi.event;

public class GameFinishedEvent extends KafkaEvent {

    private String gameId;
    private String winner;

    public GameFinishedEvent() {
    }

    public GameFinishedEvent(String gameId, String winner) {
        this.gameId = gameId;
        this.winner = winner;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

}
