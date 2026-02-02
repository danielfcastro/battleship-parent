package com.odigeo.interview.coding.battleshipapi.contract;

public class GameFireCommand {

    private String playerId;
    private String coordinate;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }

}
