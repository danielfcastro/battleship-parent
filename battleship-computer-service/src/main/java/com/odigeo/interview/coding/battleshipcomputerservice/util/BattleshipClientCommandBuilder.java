package com.odigeo.interview.coding.battleshipcomputerservice.util;

import com.odigeo.interview.coding.battleshipapi.contract.DeployShipsCommand;
import com.odigeo.interview.coding.battleshipapi.contract.GameFireCommand;
import com.odigeo.interview.coding.battleshipapi.contract.GameJoinCommand;

import java.util.ArrayList;

public final class BattleshipClientCommandBuilder {

    private BattleshipClientCommandBuilder() {}

    public static DeployShipsCommand buildDeployShipsCommand() {
        DeployShipsCommand command = new DeployShipsCommand();
        command.setPlayerId(ComputerConfiguration.PLAYER_NAME);
        command.setShipsDeploy(new ArrayList<>());
        command.getShipsDeploy().add(new DeployShipsCommand.ShipDeployment("Destroyer", "A1", "B1"));
        command.getShipsDeploy().add(new DeployShipsCommand.ShipDeployment("Submarine", "D1", "E1", "F1"));
        command.getShipsDeploy().add(new DeployShipsCommand.ShipDeployment("Cruiser", "H1", "H2", "H3"));
        command.getShipsDeploy().add(new DeployShipsCommand.ShipDeployment("Battleship", "A4", "A5", "A6", "A7"));
        command.getShipsDeploy().add(new DeployShipsCommand.ShipDeployment("AircraftCarrier", "B2", "C2", "D2", "E2", "F2"));
        return command;
    }

    public static GameFireCommand buildGameFireCommand(String coordinate) {
        GameFireCommand command = new GameFireCommand();
        command.setPlayerId(ComputerConfiguration.PLAYER_NAME);
        command.setCoordinate(coordinate);
        return command;
    }

    public static GameJoinCommand buildGameJoinCommand() {
        GameJoinCommand command = new GameJoinCommand();
        command.setPlayerId(ComputerConfiguration.PLAYER_NAME);
        return command;
    }
}
