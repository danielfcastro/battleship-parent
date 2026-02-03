package com.odigeo.interview.coding.battleshipplay.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ship placement with its coordinates
 */
public class ShipPlacement {

    private final String shipType;
    private final List<String> coordinates;

    public ShipPlacement(String shipType, List<String> coordinates) {
        this.shipType = shipType;
        this.coordinates = new ArrayList<>(coordinates);
    }

    public String getShipType() {
        return shipType;
    }

    public List<String> getCoordinates() {
        return new ArrayList<>(coordinates);
    }

    @Override
    public String toString() {
        return shipType + " at " + coordinates;
    }

    /**
     * Ship types according to the game specification
     */
    public enum ShipType {
        AIRCRAFT_CARRIER("AircraftCarrier", 5),
        BATTLESHIP("Battleship", 4),
        CRUISER("Cruiser", 3),
        DESTROYER("Destroyer", 2),
        SUBMARINE("Submarine", 1);

        private final String typeName;
        private final int length;

        ShipType(String typeName, int length) {
            this.typeName = typeName;
            this.length = length;
        }

        public String getTypeName() {
            return typeName;
        }

        public int getLength() {
            return length;
        }
    }
}
