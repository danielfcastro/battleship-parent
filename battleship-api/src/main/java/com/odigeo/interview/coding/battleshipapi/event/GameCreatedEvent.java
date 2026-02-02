package com.odigeo.interview.coding.battleshipapi.event;

public class GameCreatedEvent extends KafkaEvent {

    private String gameId;

    public GameCreatedEvent() {
    }

    public GameCreatedEvent(String gameId) {
        this.gameId = gameId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

}
