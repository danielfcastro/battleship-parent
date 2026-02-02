package com.odigeo.interview.coding.battleshipservice.exception;

public class GameNotFoundException extends BattleshipException {

    public GameNotFoundException(String gameId) {
        super(String.format("Game %s not found", gameId));
    }

}
