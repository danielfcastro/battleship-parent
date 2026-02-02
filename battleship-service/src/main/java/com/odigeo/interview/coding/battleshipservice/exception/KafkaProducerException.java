package com.odigeo.interview.coding.battleshipservice.exception;

public class KafkaProducerException extends RuntimeException {

    public KafkaProducerException(Throwable throwable) {
        super(throwable);
    }

}
