package com.odigeo.interview.coding.battleshipservice.util;

import com.odigeo.interview.coding.battleshipapi.contract.DeployShipsCommand;
import com.odigeo.interview.coding.battleshipservice.exception.ShipDeploymentException;
import com.odigeo.interview.coding.battleshipservice.model.ship.Ship;
import com.odigeo.interview.coding.battleshipservice.model.ship.ShipType;
import com.odigeo.interview.coding.battleshipservice.service.CoordinateService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ShipDeploymentBuilder {

    private ShipDeploymentBuilder() {}

    public static List<DeployShipsCommand.ShipDeployment> buildShipsDeployment() {
        DeployShipsCommand.ShipDeployment destroyer = new DeployShipsCommand.ShipDeployment("Destroyer", "A1", "B1");
        DeployShipsCommand.ShipDeployment submarine = new DeployShipsCommand.ShipDeployment("Submarine", "D1", "E1", "F1");
        DeployShipsCommand.ShipDeployment cruiser = new DeployShipsCommand.ShipDeployment("Cruiser", "H1", "H2", "H3");
        DeployShipsCommand.ShipDeployment battleship = new DeployShipsCommand.ShipDeployment("Battleship", "A4", "A5", "A6", "A7");
        DeployShipsCommand.ShipDeployment aircraftCarrier = new DeployShipsCommand.ShipDeployment("AircraftCarrier", "B2", "C2", "D2", "E2", "F2");
        return new ArrayList<>(Arrays.asList(destroyer, submarine, cruiser, battleship, aircraftCarrier));
    }

    public static List<Ship> buildValidDeployment() {
        DeployShipsCommand.ShipDeployment destroyer = new DeployShipsCommand.ShipDeployment("Destroyer", "A1", "B1");
        DeployShipsCommand.ShipDeployment submarine = new DeployShipsCommand.ShipDeployment("Submarine", "D1", "E1", "F1");
        DeployShipsCommand.ShipDeployment cruiser = new DeployShipsCommand.ShipDeployment("Cruiser", "H1", "H2", "H3");
        DeployShipsCommand.ShipDeployment battleship = new DeployShipsCommand.ShipDeployment("Battleship", "A4", "A5", "A6", "A7");
        DeployShipsCommand.ShipDeployment aircraftCarrier = new DeployShipsCommand.ShipDeployment("AircraftCarrier", "B2", "C2", "D2", "E2", "F2");
        ArrayList<DeployShipsCommand.ShipDeployment> shipDeployments = new ArrayList<>(Arrays.asList(destroyer, submarine, cruiser, battleship, aircraftCarrier));
        return mapShipsDeployment(shipDeployments);
    }

    public static List<Ship> buildDuplicatedShipsDeployment() {
        DeployShipsCommand.ShipDeployment destroyer = new DeployShipsCommand.ShipDeployment("Destroyer", "A1", "B1");
        DeployShipsCommand.ShipDeployment submarine = new DeployShipsCommand.ShipDeployment("Submarine", "D1", "E1", "F1");
        DeployShipsCommand.ShipDeployment cruiser = new DeployShipsCommand.ShipDeployment("Cruiser", "H1", "H2", "H3");
        DeployShipsCommand.ShipDeployment battleship = new DeployShipsCommand.ShipDeployment("Battleship", "A4", "A5", "A6", "A7");
        DeployShipsCommand.ShipDeployment destroyer2 = new DeployShipsCommand.ShipDeployment("Destroyer", "A1", "B1");
        ArrayList<DeployShipsCommand.ShipDeployment> shipDeployments = new ArrayList<>(Arrays.asList(destroyer, submarine, cruiser, battleship, destroyer2));
        return mapShipsDeployment(shipDeployments);
    }

    public static List<Ship> buildWrongNumberOfShipsDeployment() {
        DeployShipsCommand.ShipDeployment destroyer = new DeployShipsCommand.ShipDeployment("Destroyer", "A1", "B1");
        DeployShipsCommand.ShipDeployment submarine = new DeployShipsCommand.ShipDeployment("Submarine", "D1", "E1", "F1");
        DeployShipsCommand.ShipDeployment cruiser = new DeployShipsCommand.ShipDeployment("Cruiser", "H1", "H2", "H3");
        DeployShipsCommand.ShipDeployment battleship = new DeployShipsCommand.ShipDeployment("Battleship", "A4", "A5", "A6", "A7");
        ArrayList<DeployShipsCommand.ShipDeployment> shipDeployments = new ArrayList<>(Arrays.asList(destroyer, submarine, cruiser, battleship));
        return mapShipsDeployment(shipDeployments);
    }

    public static List<Ship> buildOverlappingShipsDeployment() {
        DeployShipsCommand.ShipDeployment destroyer = new DeployShipsCommand.ShipDeployment("Destroyer", "A1", "B1");
        DeployShipsCommand.ShipDeployment submarine = new DeployShipsCommand.ShipDeployment("Submarine", "D1", "E1", "F1");
        DeployShipsCommand.ShipDeployment cruiser = new DeployShipsCommand.ShipDeployment("Cruiser", "H1", "H2", "H3");
        DeployShipsCommand.ShipDeployment battleship = new DeployShipsCommand.ShipDeployment("Battleship", "A4", "A5", "A6", "A7");
        DeployShipsCommand.ShipDeployment aircraftCarrier = new DeployShipsCommand.ShipDeployment("AircraftCarrier", "A1", "B1", "C1", "D1", "E1");
        ArrayList<DeployShipsCommand.ShipDeployment> shipDeployments = new ArrayList<>(Arrays.asList(destroyer, submarine, cruiser, battleship, aircraftCarrier));
        return mapShipsDeployment(shipDeployments);
    }

    public static List<Ship> buildWrongShipLengthDeployment() {
        DeployShipsCommand.ShipDeployment destroyer = new DeployShipsCommand.ShipDeployment("Destroyer", "A1", "B1");
        DeployShipsCommand.ShipDeployment submarine = new DeployShipsCommand.ShipDeployment("Submarine", "D1", "E1", "F1");
        DeployShipsCommand.ShipDeployment cruiser = new DeployShipsCommand.ShipDeployment("Cruiser", "H1", "H2", "H3");
        DeployShipsCommand.ShipDeployment battleship = new DeployShipsCommand.ShipDeployment("Battleship", "A4", "A5", "A6", "A7");
        DeployShipsCommand.ShipDeployment aircraftCarrier = new DeployShipsCommand.ShipDeployment("AircraftCarrier", "B2");
        ArrayList<DeployShipsCommand.ShipDeployment> shipDeployments = new ArrayList<>(Arrays.asList(destroyer, submarine, cruiser, battleship, aircraftCarrier));
        return mapShipsDeployment(shipDeployments);
    }

    public static List<Ship> buildShipOutOfGridDeployment() {
        DeployShipsCommand.ShipDeployment destroyer = new DeployShipsCommand.ShipDeployment("Destroyer", "A1", "B1");
        DeployShipsCommand.ShipDeployment submarine = new DeployShipsCommand.ShipDeployment("Submarine", "D1", "E1", "F1");
        DeployShipsCommand.ShipDeployment cruiser = new DeployShipsCommand.ShipDeployment("Cruiser", "H1", "H2", "H3");
        DeployShipsCommand.ShipDeployment battleship = new DeployShipsCommand.ShipDeployment("Battleship", "A4", "A5", "A6", "A7");
        DeployShipsCommand.ShipDeployment aircraftCarrier = new DeployShipsCommand.ShipDeployment("AircraftCarrier", "G0", "G1", "G2", "G3", "G4");
        ArrayList<DeployShipsCommand.ShipDeployment> shipDeployments = new ArrayList<>(Arrays.asList(destroyer, submarine, cruiser, battleship, aircraftCarrier));
        return mapShipsDeployment(shipDeployments);
    }

    public static List<Ship> buildShipNotContiguousDeployment() {
        DeployShipsCommand.ShipDeployment destroyer = new DeployShipsCommand.ShipDeployment("Destroyer", "A1", "B1");
        DeployShipsCommand.ShipDeployment submarine = new DeployShipsCommand.ShipDeployment("Submarine", "D1", "E1", "F1");
        DeployShipsCommand.ShipDeployment cruiser = new DeployShipsCommand.ShipDeployment("Cruiser", "H1", "H2", "H3");
        DeployShipsCommand.ShipDeployment battleship = new DeployShipsCommand.ShipDeployment("Battleship", "A4", "A5", "A6", "A7");
        DeployShipsCommand.ShipDeployment aircraftCarrier = new DeployShipsCommand.ShipDeployment("AircraftCarrier", "G1", "G3", "G4", "G5", "G6");
        ArrayList<DeployShipsCommand.ShipDeployment> shipDeployments = new ArrayList<>(Arrays.asList(destroyer, submarine, cruiser, battleship, aircraftCarrier));
        return mapShipsDeployment(shipDeployments);
    }

    private static List<Ship> mapShipsDeployment(List<DeployShipsCommand.ShipDeployment> shipDeployments) {
        List<Ship> ships = new ArrayList<>();
        for (DeployShipsCommand.ShipDeployment shipDeployment: shipDeployments) {
            ships.add(mapShipsDeployment(shipDeployment));
        }
        return ships;
    }

    private static Ship mapShipsDeployment(DeployShipsCommand.ShipDeployment shipDeployment) {
        try {
            Ship ship = ShipType.getByTypeName(shipDeployment.getShipType()).newInstance();
            ship.setCoordinates(shipDeployment.getCoordinates().stream()
                    .map(coordinate -> new CoordinateService().decodeCoordinate(coordinate))
                    .collect(Collectors.toList()));
            return ship;
        } catch (Exception e) {
            throw new ShipDeploymentException(shipDeployment.getShipType(), shipDeployment.getCoordinates(), e);
        }
    }

}
