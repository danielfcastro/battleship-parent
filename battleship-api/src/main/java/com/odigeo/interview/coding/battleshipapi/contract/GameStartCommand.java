package com.odigeo.interview.coding.battleshipapi.contract;

public class GameStartCommand {

    private String playerId;
    private boolean vsComputer = true;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public boolean isVsComputer() {
        return vsComputer;
    }

    public void setVsComputer(boolean vsComputer) {
        this.vsComputer = vsComputer;
    }
}
