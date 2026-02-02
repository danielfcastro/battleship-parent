package com.odigeo.interview.coding.battleshipservice.exception;

public class NotYourTurnException extends BattleshipException {

    public NotYourTurnException(String playerName) {
        super(String.format("%s is not your turn", playerName));
    }

}
