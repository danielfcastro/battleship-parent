package com.odigeo.interview.coding.battleshipapi.contract;

public class GameFireResponse {

    private FireOutcome fireOutcome;
    private boolean gameWon;

    public GameFireResponse() {
    }

    public GameFireResponse(FireOutcome fireOutcome) {
        this.fireOutcome = fireOutcome;
    }

    public FireOutcome getFireOutcome() {
        return fireOutcome;
    }

    public void setFireOutcome(FireOutcome fireOutcome) {
        this.fireOutcome = fireOutcome;
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
