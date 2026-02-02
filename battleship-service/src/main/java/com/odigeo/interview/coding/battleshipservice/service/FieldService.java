package com.odigeo.interview.coding.battleshipservice.service;

import com.odigeo.interview.coding.battleshipservice.model.Cell;
import com.odigeo.interview.coding.battleshipservice.model.Coordinate;
import com.odigeo.interview.coding.battleshipservice.model.ship.Ship;
import com.odigeo.interview.coding.battleshipservice.util.GameConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class FieldService {

    @Inject
    private CoordinateService coordinateService;

    public boolean allShipsSunk(Cell[][] field) {
        return false;
    }

    public boolean isShipSunk(Cell[][] field, Ship ship) {
        return false;
    }

    public Cell[][] buildField(List<Ship> shipsDeployment) {
        Cell[][] field = buildWater();
        deployShips(field, shipsDeployment);
        return field;
    }

    private Cell[][] buildWater() {
        Cell[][] field = new Cell[GameConfiguration.FIELD_HEIGHT][GameConfiguration.FIELD_WIDTH];
        for (int row = 0; row < GameConfiguration.FIELD_HEIGHT; row++) {
            for (int col = 0; col < GameConfiguration.FIELD_WIDTH; col++) {
                field[row][col] = new Cell();
            }
        }
        return field;
    }

    private void deployShips(Cell[][] field, List<Ship> ships) {
        ships.forEach(ship ->
            ship.getCoordinates().forEach(coordinate ->
                    field[coordinate.getRow()][coordinate.getColumn()] = new Cell(ship)
            )
        );
    }

}
