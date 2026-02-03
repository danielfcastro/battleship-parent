package com.odigeo.interview.coding.battleshipservice.service;

import com.odigeo.interview.coding.battleshipservice.model.Cell;
import com.odigeo.interview.coding.battleshipservice.model.ship.Ship;
import com.odigeo.interview.coding.battleshipservice.util.GameConfiguration;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class FieldService {

    public boolean allShipsSunk(Cell[][] field) {
        for (Cell[] row : field) {
            for (Cell cell : row) {
                if (!cell.isWater() && !cell.isHit()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isShipSunk(Cell[][] field, Ship ship) {
        return ship.getCoordinates().stream()
                .allMatch(coordinate -> field[coordinate.getRow()][coordinate.getColumn()].isHit());
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
