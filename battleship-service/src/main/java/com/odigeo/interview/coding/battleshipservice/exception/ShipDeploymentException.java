package com.odigeo.interview.coding.battleshipservice.exception;

import java.util.List;

public class ShipDeploymentException extends BattleshipException {

    public ShipDeploymentException(String shipType, List<String> coordinates) {
        this(shipType, coordinates, null);
    }

    public ShipDeploymentException(String shipType, List<String> coordinates, Exception e) {
        super(String.format("Ship %s with coordinates %s is not deployed correctly on the field", shipType, coordinates), e);
    }

    public ShipDeploymentException(String message) {
        super(message);
    }
}
