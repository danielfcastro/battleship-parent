package com.odigeo.interview.coding.battleshipapi.contract;

public class GameFireResponse {

    private FireOutcome fireOutcome;
    private String shipTypeSunk;
    private boolean gameWon;

    public GameFireResponse() {
    }

    public GameFireResponse(FireOutcome fireOutcome) {
        this.fireOutcome = fireOutcome;
    }

    public GameFireResponse(FireOutcome fireOutcome, String shipTypeSunk) {
        this.fireOutcome = fireOutcome;
        this.shipTypeSunk = shipTypeSunk;
    }

    public FireOutcome getFireOutcome() {
        return fireOutcome;
    }

    public void setFireOutcome(FireOutcome fireOutcome) {
        this.fireOutcome = fireOutcome;
    }

    public String getShipTypeSunk() {
        return shipTypeSunk;
    }

    public void setShipTypeSunk(String shipTypeSunk) {
        this.shipTypeSunk = shipTypeSunk;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }

    public enum FireOutcome {
        MISS, HIT, SUNK;
    }

}
