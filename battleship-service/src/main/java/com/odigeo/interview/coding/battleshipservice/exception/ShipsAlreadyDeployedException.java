package com.odigeo.interview.coding.battleshipservice.exception;

public class ShipsAlreadyDeployedException extends BattleshipException {

    public ShipsAlreadyDeployedException(String playerId) {
        super(String.format("%s's ships are already deployed", playerId));
    }

}
