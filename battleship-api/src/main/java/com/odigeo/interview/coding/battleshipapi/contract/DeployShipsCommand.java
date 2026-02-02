package com.odigeo.interview.coding.battleshipapi.contract;

import java.util.Arrays;
import java.util.List;

public class DeployShipsCommand {

    private String playerId;
    private List<ShipDeployment> shipsDeploy;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public List<ShipDeployment> getShipsDeploy() {
        return shipsDeploy;
    }

    public void setShipsDeploy(List<ShipDeployment> shipsDeploy) {
        this.shipsDeploy = shipsDeploy;
    }

    public static class ShipDeployment {

        private String shipType;
        private List<String> coordinates;

        public ShipDeployment() {
        }

        public ShipDeployment(String shipType, String... coordinates) {
            this.shipType = shipType;
            this.coordinates = Arrays.asList(coordinates);
        }

        public String getShipType() {
            return shipType;
        }

        public void setShipType(String shipType) {
            this.shipType = shipType;
        }

        public List<String> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<String> coordinates) {
            this.coordinates = coordinates;
        }
    }

}
