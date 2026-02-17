package com.odigeo.interview.coding.battleshipplay;

import com.odigeo.interview.coding.battleshipapi.contract.DeployShipsCommand;
import com.odigeo.interview.coding.battleshipapi.contract.GameFireResponse;
import com.odigeo.interview.coding.battleshipapi.contract.GameResponse;
import com.odigeo.interview.coding.battleshipplay.client.BattleshipClient;
import com.odigeo.interview.coding.battleshipplay.model.Board;
import com.odigeo.interview.coding.battleshipplay.model.ShipPlacement;
import com.odigeo.interview.coding.battleshipplay.strategy.FiringStrategy;
import com.odigeo.interview.coding.battleshipplay.strategy.ShipPlacementStrategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;

/**
 * Main class for the Battleship game CLI
 */
public class BattleshipGame {

    private final BattleshipClient client;
    private final String playerId;
    private String gameId;
    private final Board myBoard;
    private final Board opponentBoard;
    private final FiringStrategy firingStrategy;

    private int shotsFired = 0;
    private int hits = 0;
    private int misses = 0;

    public BattleshipGame() {
        this.client = new BattleshipClient();
        this.playerId = "Player-" + UUID.randomUUID().toString().substring(0, 8);
        this.myBoard = new Board();
        this.opponentBoard = new Board();
        this.firingStrategy = new FiringStrategy(opponentBoard);
    }

    @SuppressWarnings("java:S106") // CLI application requires console output
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║     BATTLESHIP GAME vs COMPUTER      ║");
        System.out.println("╚═══════════════════════════════════════╝\n");

        BattleshipGame game = new BattleshipGame();

        try {
            game.start();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("\n❌ Error: " + e.getMessage());
            System.err.println("Stack trace:");
            for (StackTraceElement element : e.getStackTrace()) {
                System.err.println("\t" + element);
            }
            System.exit(1);
        } catch (Exception e) {
            System.err.println("\n❌ Error: " + e.getMessage());
            System.err.println("Stack trace:");
            for (StackTraceElement element : e.getStackTrace()) {
                System.err.println("\t" + element);
            }
            System.exit(1);
        }
    }

    @SuppressWarnings("java:S106") // CLI application requires console output
    public void start() throws IOException, InterruptedException {
        System.out.println("Player ID: " + playerId);
        System.out.println("\n📡 Connecting to Battleship Server...");

        // Create game
        GameResponse gameResponse = client.createGame(playerId, true);
        this.gameId = gameResponse.getId();
        System.out.println("✓ Game created: " + gameId);

        // Give computer time to join
        Thread.sleep(1000);

        // Place ships
        System.out.println("\n🚢 Placing your ships...");
        ShipPlacementStrategy placementStrategy = new ShipPlacementStrategy();
        List<ShipPlacement> placements = placementStrategy.generateRandomPlacements();

        displayShipPlacements(placements);
        deployShips(placements);

        System.out.println("✓ Ships deployed successfully!");

        // Wait for computer to deploy
        System.out.println("\n⏳ Waiting for computer to deploy ships...");
        Thread.sleep(1000);

        // Main game loop
        System.out.println("\n⚔️  BATTLE BEGINS! ⚔️\n");
        playGame();
    }

    private void deployShips(List<ShipPlacement> placements) throws IOException {
        DeployShipsCommand command = new DeployShipsCommand();
        command.setPlayerId(playerId);

        List<DeployShipsCommand.ShipDeployment> shipDeployments = new java.util.ArrayList<>();
        for (ShipPlacement placement : placements) {
            DeployShipsCommand.ShipDeployment deployment = new DeployShipsCommand.ShipDeployment();
            deployment.setShipType(placement.getShipType());
            deployment.setCoordinates(placement.getCoordinates());
            shipDeployments.add(deployment);

            // Mark on our board
            for (String coord : placement.getCoordinates()) {
                myBoard.markShot(coord, Board.CellState.SHIP);
            }
        }

        command.setShipsDeploy(shipDeployments);
        client.deployShips(gameId, command);
    }

    @SuppressWarnings({"java:S3776", "java:S135", "java:S106"}) // CLI game loop requires complex flow control and console output
    private void playGame() throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean gameOver = false;
        boolean isMyTurn = true; // Player 1 goes first

        while (!gameOver) {
            if (isMyTurn) {
                // Player's turn
                displayBoards();
                System.out.println("\n🎯 YOUR TURN");
                System.out.print("Enter coordinate to fire (e.g., A5) or 'auto' for AI suggestion: ");

                String input = reader.readLine().trim().toUpperCase();
                String targetCoordinate;

                if ("AUTO".equals(input)) {
                    targetCoordinate = firingStrategy.getNextTarget();
                    System.out.println("🤖 AI suggests: " + targetCoordinate);
                } else {
                    targetCoordinate = input;
                    if (!Board.isValidCoordinate(targetCoordinate)) {
                        System.out.println("❌ Invalid coordinate! Try again.");
                        continue;
                    }
                    if (opponentBoard.hasBeenFired(targetCoordinate)) {
                        System.out.println("❌ Already fired at " + targetCoordinate + "! Try again.");
                        continue;
                    }
                }

                gameOver = fireShotAndProcess(targetCoordinate);
                isMyTurn = false;

            } else {
                // Computer's turn
                System.out.println("\n🤖 COMPUTER'S TURN");
                System.out.println("⏳ Computer is thinking...");
                Thread.sleep(1500); // Dramatic pause

                // The computer will fire via Kafka, we just need to wait
                // In a real implementation, we'd poll the game state or use websockets
                System.out.println("💥 Computer fired! (Check server logs)");

                isMyTurn = true;
                Thread.sleep(500);
            }
        }
    }

    @SuppressWarnings("java:S106") // CLI application requires console output
    private boolean fireShotAndProcess(String coordinate) throws IOException {
        shotsFired++;

        try {
            GameFireResponse response = client.fire(gameId, playerId, coordinate);

            GameFireResponse.FireOutcome outcome = response.getFireOutcome();
            boolean hit = outcome != GameFireResponse.FireOutcome.MISS;
            boolean sunk = outcome == GameFireResponse.FireOutcome.SUNK;

            if (hit) {
                hits++;
                opponentBoard.markShot(coordinate, Board.CellState.HIT);

                if (sunk) {
                    opponentBoard.incrementShipsSunk();
                    String sunkShipType = response.getShipTypeSunk();
                    // Normalizing ship type name for display and internal tracking
                    if ("AircraftCarrier".equals(sunkShipType)) {
                        sunkShipType = "Aircraft Carrier";
                    }
                    opponentBoard.markShipSunk(sunkShipType);
                    System.out.println("💥 BOOM! You SUNK an enemy " + sunkShipType + " at " + coordinate + "! [Ships destroyed: " + opponentBoard.getShipsSunk() + "/5]");
                } else {
                    System.out.println("🎯 HIT at " + coordinate + "! [Outcome: " + outcome + "]");
                }

                firingStrategy.updateAfterShot(coordinate, true, sunk);
            } else {
                misses++;
                opponentBoard.markShot(coordinate, Board.CellState.MISS);
                System.out.println("💨 MISS at " + coordinate);

                firingStrategy.updateAfterShot(coordinate, false, false);
            }

            if (response.isGameWon()) {
                System.out.println("\n╔═══════════════════════════════════════╗");
                System.out.println("║          🎉 VICTORY! 🎉              ║");
                System.out.println("║   You defeated the computer!         ║");
                System.out.println("╚═══════════════════════════════════════╝");
                displayStatistics();
                return true;
            }

            return false;

        } catch (IOException e) {
            if (e.getMessage().contains("GameFinishedException")) {
                System.out.println("\n╔═══════════════════════════════════════╗");
                System.out.println("║          😞 DEFEAT 😞                ║");
                System.out.println("║   The computer won this battle!      ║");
                System.out.println("╚═══════════════════════════════════════╝");
                displayStatistics();
                return true;
            } else if (e.getMessage().contains("NotYourTurnException")) {
                System.out.println("⚠️  Not your turn yet!");
                return false;
            } else {
                throw e;
            }
        }
    }

    @SuppressWarnings("java:S106") // CLI application requires console output
    private void displayBoards() {
        String separator = "==================================================";
        System.out.println("\n" + separator);
        System.out.println("YOUR BOARD (Your Ships):");
        myBoard.display();

        System.out.println("\nOPPONENT'S BOARD (Your Tracking):");
        opponentBoard.display();
        System.out.println(separator);

        System.out.printf("%n📊 Stats: Shots: %d | Hits: %d | Misses: %d | Accuracy: %.1f%%%n",
                shotsFired, hits, misses,
                shotsFired > 0 ? (hits * 100.0 / shotsFired) : 0.0);
        System.out.println("\n⚓ ENEMY FLEET STATUS:");
        System.out.print(opponentBoard.getFleetStatus());
    }

    @SuppressWarnings("java:S106") // CLI application requires console output
    private void displayShipPlacements(List<ShipPlacement> placements) {
        System.out.println("\nYour ship placements:");
        for (ShipPlacement placement : placements) {
            System.out.println("  • " + placement);
        }
    }

    @SuppressWarnings("java:S106") // CLI application requires console output
    private void displayStatistics() {
        System.out.println("\n📊 FINAL STATISTICS:");
        System.out.println("  Enemy Ships Sunk: " + opponentBoard.getShipsSunk() + " / 5");
        System.out.println("  Total Shots Fired: " + shotsFired);
        System.out.println("  Hits: " + hits);
        System.out.println("  Misses: " + misses);
        System.out.printf("  Accuracy: %.1f%%%n", shotsFired > 0 ? (hits * 100.0 / shotsFired) : 0.0);

        displayBoards();

        System.out.println("\nThank you for playing Battleship!");
    }
}
