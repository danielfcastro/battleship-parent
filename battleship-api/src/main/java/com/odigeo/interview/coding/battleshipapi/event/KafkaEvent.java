package com.odigeo.interview.coding.battleshipapi.event;

import com.google.gson.Gson;

public abstract class KafkaEvent {

    public String json() {
        return new Gson().toJson(this);
    }

}
