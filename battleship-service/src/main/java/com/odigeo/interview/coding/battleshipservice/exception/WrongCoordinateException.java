package com.odigeo.interview.coding.battleshipservice.exception;

public class WrongCoordinateException extends BattleshipException {

    public WrongCoordinateException(String coordinate) {
        super(String.format("Coordinate %s is wrong", coordinate));
    }

}
