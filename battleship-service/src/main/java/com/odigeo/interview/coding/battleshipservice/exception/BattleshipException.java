package com.odigeo.interview.coding.battleshipservice.exception;

public class BattleshipException extends RuntimeException {

    public BattleshipException(String s) {
        super(s);
    }

    public BattleshipException(String s, Throwable throwable) {
        super(s, throwable);
    }

}
