package com.odigeo.interview.coding.battleshipservice.model.ship;

import com.odigeo.interview.coding.battleshipservice.model.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Ship {

    private final UUID id;
    private final ShipType shipType;
    private List<Coordinate> coordinates;

    public Ship(ShipType shipType) {
        this.id = UUID.randomUUID();
        this.shipType = shipType;
    }

    public UUID getId() {
        return id;
    }

    public ShipType getShipType() {
        return shipType;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public List<Coordinate> getCoordinates() {
        if (coordinates == null) {
            coordinates = new ArrayList<>();
        }
        return coordinates;
    }
}
