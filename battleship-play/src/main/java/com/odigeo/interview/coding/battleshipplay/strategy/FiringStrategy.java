package com.odigeo.interview.coding.battleshipplay.strategy;

import com.odigeo.interview.coding.battleshipplay.model.Board;

import java.util.*;

/**
 * Smart firing strategy that uses hunt and target mode
 */
public class FiringStrategy {

    private static final Random random = new Random();
    private final Board trackingBoard;
    private final Queue<String> targetQueue;
    private Mode currentMode;

    public FiringStrategy(Board trackingBoard) {
        this.trackingBoard = trackingBoard;
        this.targetQueue = new LinkedList<>();
        this.currentMode = Mode.HUNT;
    }

    /**
     * Determines the next coordinate to fire at
     */
    public String getNextTarget() {
        if (currentMode == Mode.TARGET && !targetQueue.isEmpty()) {
            return targetQueue.poll();
        }

        // Switch to hunt mode if no targets
        currentMode = Mode.HUNT;
        return huntMode();
    }

    /**
     * Updates strategy based on the result of the last shot
     */
    public void updateAfterShot(String coordinate, boolean hit, boolean sunk) {
        if (hit && !sunk) {
            // We hit something, switch to target mode
            currentMode = Mode.TARGET;
            addAdjacentTargets(coordinate);
        } else if (sunk) {
            // Ship sunk, clear targets and go back to hunt mode
            currentMode = Mode.HUNT;
            targetQueue.clear();
        }
    }

    /**
     * Hunt mode: Use checkerboard pattern for efficient searching
     */
    private String huntMode() {
        List<String> checkerboardTargets = getCheckerboardTargets();

        if (!checkerboardTargets.isEmpty()) {
            return checkerboardTargets.get(random.nextInt(checkerboardTargets.size()));
        }

        // Fallback to random if checkerboard is exhausted
        List<String> availableTargets = trackingBoard.getAvailableTargets();
        if (availableTargets.isEmpty()) {
            throw new IllegalStateException("No available targets!");
        }

        return availableTargets.get(random.nextInt(availableTargets.size()));
    }

    /**
     * Gets checkerboard pattern targets (only fire at every other cell)
     * This is optimal since smallest ship is 2 cells
     */
    private List<String> getCheckerboardTargets() {
        List<String> targets = new ArrayList<>();

        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                // Checkerboard pattern: (row + col) % 2 == 0
                if ((row + col) % 2 == 0) {
                    String coordinate = Board.encodeCoordinate(row, col);
                    if (!trackingBoard.hasBeenFired(coordinate)) {
                        targets.add(coordinate);
                    }
                }
            }
        }

        return targets;
    }

    /**
     * Adds adjacent cells to the target queue when we get a hit
     */
    private void addAdjacentTargets(String coordinate) {
        int[] pos = Board.parseCoordinate(coordinate);
        int row = pos[0];
        int col = pos[1];

        // Add cells in 4 directions (not diagonals, ships are straight lines)
        int[][] directions = {
                {row - 1, col}, // Up
                {row + 1, col}, // Down
                {row, col - 1}, // Left
                {row, col + 1}  // Right
        };

        for (int[] dir : directions) {
            int r = dir[0];
            int c = dir[1];

            // Validate bounds before encoding
            if (r >= 0 && r < Board.BOARD_SIZE && c >= 0 && c < Board.BOARD_SIZE) {
                String target = Board.encodeCoordinate(r, c);
                if (!trackingBoard.hasBeenFired(target) && !targetQueue.contains(target)) {
                    targetQueue.offer(target);
                }
            }
        }
    }

    private enum Mode {
        HUNT,   // Random/checkerboard searching
        TARGET  // Following up on a hit
    }
}
