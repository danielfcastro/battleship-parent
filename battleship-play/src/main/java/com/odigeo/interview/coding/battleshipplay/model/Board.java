package com.odigeo.interview.coding.battleshipplay.model;

import java.util.*;

/**
 * Represents a 10x10 battleship game board
 */
public class Board {

    public static final int BOARD_SIZE = 10;
    private static final String COLUMNS = "ABCDEFGHIJ";

    private final CellState[][] grid;
    private final Set<String> firedShots;
    private int shipsSunk;
    private final Map<String, Boolean> shipsStatus; // Ship type -> sunk status

    public Board() {
        this.grid = new CellState[BOARD_SIZE][BOARD_SIZE];
        this.firedShots = new HashSet<>();
        this.shipsSunk = 0;
        this.shipsStatus = new LinkedHashMap<>();

        // Initialize fleet
        shipsStatus.put("Aircraft Carrier", false);
        shipsStatus.put("Battleship", false);
        shipsStatus.put("Destroyer", false);
        shipsStatus.put("Submarine", false);
        shipsStatus.put("Cruiser", false);

        // Initialize all cells as unknown
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                grid[row][col] = CellState.UNKNOWN;
            }
        }
    }

    public void markShot(String coordinate, CellState state) {
        int[] position = parseCoordinate(coordinate);
        grid[position[0]][position[1]] = state;
        firedShots.add(coordinate);
    }

    public boolean hasBeenFired(String coordinate) {
        return firedShots.contains(coordinate);
    }

    public CellState getCellState(String coordinate) {
        int[] position = parseCoordinate(coordinate);
        return grid[position[0]][position[1]];
    }

    public List<String> getAvailableTargets() {
        List<String> targets = new ArrayList<>();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                String coordinate = encodeCoordinate(row, col);
                if (!hasBeenFired(coordinate)) {
                    targets.add(coordinate);
                }
            }
        }
        return targets;
    }
    @SuppressWarnings("java:S106")
    public void display() {
        System.out.println("\n    A B C D E F G H I J");
        System.out.println("  +---------------------+");

        for (int row = 0; row < BOARD_SIZE; row++) {
            System.out.printf("%2d| ", row + 1);
            for (int col = 0; col < BOARD_SIZE; col++) {
                CellState state = grid[row][col];
                System.out.print(state.getSymbol() + " ");
            }
            System.out.println("|");
        }

        System.out.println("  +---------------------+");
        System.out.println("\nLegend: . = Unknown, O = Miss, X = Hit, S = Ship (own board)");
    }

    public static String encodeCoordinate(int row, int col) {
        return COLUMNS.charAt(col) + String.valueOf(row + 1);
    }

    public static int[] parseCoordinate(String coordinate) {
        if (coordinate == null || coordinate.length() < 2) {
            throw new IllegalArgumentException("Invalid coordinate: " + coordinate);
        }

        char colChar = coordinate.charAt(0);
        int col = COLUMNS.indexOf(Character.toUpperCase(colChar));
        int row = Integer.parseInt(coordinate.substring(1)) - 1;

        if (col < 0 || row < 0 || row >= BOARD_SIZE) {
            throw new IllegalArgumentException("Coordinate out of bounds: " + coordinate);
        }

        return new int[]{row, col};
    }

    public static boolean isValidCoordinate(String coordinate) {
        try {
            parseCoordinate(coordinate);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void incrementShipsSunk() {
        this.shipsSunk++;
    }

    public int getShipsSunk() {
        return shipsSunk;
    }

    public int getHitCount() {
        int hits = 0;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (grid[row][col] == CellState.HIT) {
                    hits++;
                }
            }
        }
        return hits;
    }

    @SuppressWarnings("java:S1192") // String literals are intentional for CLI display formatting
    public String getFleetStatus() {
        StringBuilder status = new StringBuilder();
        for (Map.Entry<String, Boolean> entry : shipsStatus.entrySet()) {
            String shipName = entry.getKey();
            boolean sunk = entry.getValue();
            status.append(String.format("  %s %-18s %s%n",
                sunk ? "☠️" : "🚢",
                shipName,
                sunk ? "[SUNK]" : "[AFLOAT]"));
        }
        return status.toString();
    }

    public void markShipSunk(String shipType) {
        shipsStatus.computeIfPresent(shipType, (k, v) -> true);
    }

    public enum CellState {
        UNKNOWN('.'),
        MISS('O'),
        HIT('X'),
        SHIP('S');

        private final char symbol;

        CellState(char symbol) {
            this.symbol = symbol;
        }

        public char getSymbol() {
            return symbol;
        }
    }
}
