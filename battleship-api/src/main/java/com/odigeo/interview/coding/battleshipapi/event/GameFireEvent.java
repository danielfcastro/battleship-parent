package com.odigeo.interview.coding.battleshipapi.event;

public class GameFireEvent extends KafkaEvent {

    private String gameId;

    public GameFireEvent() {
    }

    public GameFireEvent(String gameId) {
        this.gameId = gameId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

}
