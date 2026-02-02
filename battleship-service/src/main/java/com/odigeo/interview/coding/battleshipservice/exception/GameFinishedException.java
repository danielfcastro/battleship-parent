package com.odigeo.interview.coding.battleshipservice.exception;

public class GameFinishedException extends BattleshipException {

    public GameFinishedException(String playedId) {
        super(String.format("The winner is %s", playedId));
    }

}
