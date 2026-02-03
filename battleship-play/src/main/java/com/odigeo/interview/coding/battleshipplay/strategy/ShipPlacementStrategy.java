package com.odigeo.interview.coding.battleshipplay.strategy;

import com.odigeo.interview.coding.battleshipplay.model.Board;
import com.odigeo.interview.coding.battleshipplay.model.ShipPlacement;

import java.util.*;

/**
 * Strategy for placing ships on the board
 */
public class ShipPlacementStrategy {

    private static final Random random = new Random();

    /**
     * Generates random ship placements that don't overlap
     */
    public List<ShipPlacement> generateRandomPlacements() {
        List<ShipPlacement> placements = new ArrayList<>();
        boolean[][] occupied = new boolean[Board.BOARD_SIZE][Board.BOARD_SIZE];

        // Place ships in order of size (largest first for easier placement)
        ShipPlacement.ShipType[] ships = ShipPlacement.ShipType.values();

        for (ShipPlacement.ShipType shipType : ships) {
            ShipPlacement placement = placeShip(shipType, occupied);
            placements.add(placement);
            markOccupied(placement, occupied);
        }

        return placements;
    }

    private ShipPlacement placeShip(ShipPlacement.ShipType shipType, boolean[][] occupied) {
        int maxAttempts = 1000;
        int attempts = 0;

        while (attempts < maxAttempts) {
            attempts++;

            // Random starting position
            int row = random.nextInt(Board.BOARD_SIZE);
            int col = random.nextInt(Board.BOARD_SIZE);

            // Random orientation (true = horizontal, false = vertical)
            boolean horizontal = random.nextBoolean();

            List<String> coordinates = generateCoordinates(row, col, shipType.getLength(), horizontal);

            if (coordinates != null && !hasOverlap(coordinates, occupied)) {
                return new ShipPlacement(shipType.getTypeName(), coordinates);
            }
        }

        throw new RuntimeException("Failed to place " + shipType.getTypeName() + " after " + maxAttempts + " attempts");
    }

    private List<String> generateCoordinates(int startRow, int startCol, int length, boolean horizontal) {
        List<String> coordinates = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            int row = horizontal ? startRow : startRow + i;
            int col = horizontal ? startCol + i : startCol;

            // Check bounds
            if (row >= Board.BOARD_SIZE || col >= Board.BOARD_SIZE) {
                return null;
            }

            coordinates.add(Board.encodeCoordinate(row, col));
        }

        return coordinates;
    }

    private boolean hasOverlap(List<String> coordinates, boolean[][] occupied) {
        for (String coordinate : coordinates) {
            int[] pos = Board.parseCoordinate(coordinate);
            if (occupied[pos[0]][pos[1]]) {
                return true;
            }
        }
        return false;
    }

    private void markOccupied(ShipPlacement placement, boolean[][] occupied) {
        for (String coordinate : placement.getCoordinates()) {
            int[] pos = Board.parseCoordinate(coordinate);
            occupied[pos[0]][pos[1]] = true;
        }
    }

    /**
     * Generates smart placements with ships not adjacent (more realistic)
     */
    public List<ShipPlacement> generateSmartPlacements() {
        List<ShipPlacement> placements = new ArrayList<>();
        boolean[][] occupied = new boolean[Board.BOARD_SIZE][Board.BOARD_SIZE];

        ShipPlacement.ShipType[] ships = ShipPlacement.ShipType.values();

        for (ShipPlacement.ShipType shipType : ships) {
            ShipPlacement placement = placeShipWithBuffer(shipType, occupied);
            placements.add(placement);
            markOccupiedWithBuffer(placement, occupied);
        }

        return placements;
    }

    private ShipPlacement placeShipWithBuffer(ShipPlacement.ShipType shipType, boolean[][] occupied) {
        int maxAttempts = 1000;
        int attempts = 0;

        while (attempts < maxAttempts) {
            attempts++;

            int row = random.nextInt(Board.BOARD_SIZE);
            int col = random.nextInt(Board.BOARD_SIZE);
            boolean horizontal = random.nextBoolean();

            List<String> coordinates = generateCoordinates(row, col, shipType.getLength(), horizontal);

            if (coordinates != null && !hasOverlap(coordinates, occupied)) {
                return new ShipPlacement(shipType.getTypeName(), coordinates);
            }
        }

        // Fallback to regular placement without buffer
        boolean[][] basicOccupied = new boolean[Board.BOARD_SIZE][Board.BOARD_SIZE];
        for (ShipPlacement placement : new ArrayList<ShipPlacement>()) {
            markOccupied(placement, basicOccupied);
        }
        return placeShip(shipType, basicOccupied);
    }

    private void markOccupiedWithBuffer(ShipPlacement placement, boolean[][] occupied) {
        for (String coordinate : placement.getCoordinates()) {
            int[] pos = Board.parseCoordinate(coordinate);
            int row = pos[0];
            int col = pos[1];

            // Mark the cell and surrounding cells as occupied
            for (int r = Math.max(0, row - 1); r <= Math.min(Board.BOARD_SIZE - 1, row + 1); r++) {
                for (int c = Math.max(0, col - 1); c <= Math.min(Board.BOARD_SIZE - 1, col + 1); c++) {
                    occupied[r][c] = true;
                }
            }
        }
    }
}
