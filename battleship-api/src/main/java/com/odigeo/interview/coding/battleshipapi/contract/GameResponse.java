package com.odigeo.interview.coding.battleshipapi.contract;

public class GameResponse {

    private String id;

    public GameResponse() {
    }

    public GameResponse(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
