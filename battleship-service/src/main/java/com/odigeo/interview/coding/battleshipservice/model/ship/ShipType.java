package com.odigeo.interview.coding.battleshipservice.model.ship;

import java.util.stream.Stream;

public enum ShipType {

    DESTROYER("Destroyer", 2) {
        @Override
        public Ship newInstance() {
            return new Destroyer();
        }
    },
    SUBMARINE("Submarine", 3) {
        @Override
        public Ship newInstance() {
            return new Submarine();
        }
    },
    CRUISER("Cruiser", 3) {
        @Override
        public Ship newInstance() {
            return new Cruiser();
        }
    },
    BATTLESHIP("Battleship", 4) {
        @Override
        public Ship newInstance() {
            return new Battleship();
        }
    },
    AIRCRAFT_CARRIER("AircraftCarrier", 5) {
        @Override
        public Ship newInstance() {
            return new AircraftCarrier();
        }
    };

    private final String shipTypeName;
    private final int shipLength;

    ShipType(String shipTypeName, int shipLength) {
        this.shipTypeName = shipTypeName;
        this.shipLength = shipLength;
    }

    public abstract Ship newInstance();

    public int getShipLength() {
        return shipLength;
    }

    public String getShipTypeName() {
        return shipTypeName;
    }

    public static ShipType getByTypeName(String shipTypeName) {
        return Stream.of(ShipType.values())
                .filter(shipType -> shipTypeName.equals(shipType.getShipTypeName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing ship factory configuration"));
    }

}
