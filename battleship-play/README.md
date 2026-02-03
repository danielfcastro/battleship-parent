# Battleship Play - Command Line Game Client

A Java command-line application that plays Battleship against the computer through the battleship-service REST API.

## Features

- **Interactive CLI Interface**: Play Battleship from your terminal
- **Smart AI Opponent**: Uses hunt & target strategy for efficient gameplay
- **Automatic Ship Placement**: Random ship placement with validation
- **Real-time Tracking**: Visual board display showing your ships and opponent tracking
- **Statistics**: Track shots fired, hits, misses, and accuracy
- **Manual or AI-Assisted**: Choose coordinates manually or get AI suggestions

## Game Rules

The game follows classic Battleship rules:
- 10x10 grid (A-J columns, 1-10 rows)
- 5 ships per player:
  - **Aircraft Carrier** (5 cells)
  - **Battleship** (4 cells)
  - **Cruiser** (3 cells)
  - **Submarine** (3 cells)
  - **Destroyer** (2 cells)
- Ships cannot overlap and must be placed horizontally or vertically
- Players take turns firing at coordinates
- First to sink all opponent ships wins

## Prerequisites

1. **Battleship Service Running**: The battleship-service must be running on `http://localhost:8080`
2. **Java 8+**: Java Runtime Environment installed
3. **Maven**: To build the project

## Building

From the project root:

```bash
mvn clean package
```

Or just the battleship-play module:

```bash
cd battleship-play
mvn clean package
```

## Running

### Option 1: Using Java directly

```bash
cd battleship-play/target
java -jar battleship-play-1.0.1-SNAPSHOT.jar
```

### Option 2: Using Maven

```bash
cd battleship-play
mvn exec:java -Dexec.mainClass="com.odigeo.interview.coding.battleshipplay.BattleshipGame"
```

## Gameplay

1. **Game Start**: The game creates a new game and connects to the server
2. **Ship Deployment**: Ships are automatically placed randomly on your board
3. **Battle Phase**: Take turns with the computer
   - Enter a coordinate (e.g., `A5`, `J10`)
   - Or type `auto` to get an AI suggestion
4. **Victory/Defeat**: First to sink all ships wins!

## Example Session

```
╔═══════════════════════════════════════╗
║     BATTLESHIP GAME vs COMPUTER      ║
╚═══════════════════════════════════════╝

Player ID: Player-a3f2d891

📡 Connecting to Battleship Server...
✓ Game created: 8f3b2c4d-9e1a-4b5f-8c7d-6a1b2c3d4e5f

🚢 Placing your ships...

Your ship placements:
  • AircraftCarrier at [A1, A2, A3, A4, A5]
  • Battleship at [C1, C2, C3, C4]
  • Cruiser at [E1, E2, E3]
  • Submarine at [G1, G2, G3]
  • Destroyer at [I1, I2]

✓ Ships deployed successfully!

⏳ Waiting for computer to deploy ships...

⚔️  BATTLE BEGINS! ⚔️

==================================================
YOUR BOARD (Your Ships):

    A B C D E F G H I J
  +---------------------+
 1| S . S . S . S . S . |
 2| S . S . S . S . S . |
 3| S . S . S . S . . . |
 4| S . S . . . . . . . |
 5| S . . . . . . . . . |
 6| . . . . . . . . . . |
 7| . . . . . . . . . . |
 8| . . . . . . . . . . |
 9| . . . . . . . . . . |
10| . . . . . . . . . . |
  +---------------------+

OPPONENT'S BOARD (Your Tracking):

    A B C D E F G H I J
  +---------------------+
 1| . . . . . . . . . . |
 2| . . . . . . . . . . |
 3| . . . . . . . . . . |
 4| . . . . . . . . . . |
 5| . . . . . . . . . . |
 6| . . . . . . . . . . |
 7| . . . . . . . . . . |
 8| . . . . . . . . . . |
 9| . . . . . . . . . . |
10| . . . . . . . . . . |
  +---------------------+
==================================================

📊 Stats: Shots: 0 | Hits: 0 | Misses: 0 | Accuracy: 0.0%

🎯 YOUR TURN
Enter coordinate to fire (e.g., A5) or 'auto' for AI suggestion: E5
💨 MISS at E5

🤖 COMPUTER'S TURN
⏳ Computer is thinking...
💥 Computer fired! (Check server logs)

...
```

## Architecture

### Components

- **BattleshipClient**: HTTP client for REST API communication
- **Board**: Represents the game board with tracking capabilities
- **ShipPlacementStrategy**: Handles random ship placement logic
- **FiringStrategy**: Implements smart hunt & target AI algorithm
- **BattleshipGame**: Main game loop and CLI interface

### AI Strategy

The firing strategy uses two modes:

1. **Hunt Mode** (Searching):
   - Uses checkerboard pattern for efficiency
   - Only fires at every other cell (optimal for finding 2+ cell ships)

2. **Target Mode** (After a hit):
   - Fires at adjacent cells (up, down, left, right)
   - Continues until ship is sunk
   - Returns to hunt mode

## Configuration

To change the server URL, edit `BattleshipClient.java`:

```java
private static final String BASE_URL = "http://localhost:8080/battleship-service/api";
```

## Limitations

- Currently only supports vs Computer mode
- No saved game state (play must complete in one session)
- Requires manual coordination between player turns (simplified for demo)

## Future Enhancements

- [ ] Player vs Player mode
- [ ] Save/Load game state
- [ ] Manual ship placement option
- [ ] WebSocket for real-time updates
- [ ] Colored terminal output
- [ ] Game replay functionality
- [ ] Multiple difficulty levels for AI

## Troubleshooting

**Connection Refused**:
- Ensure battleship-service is running on port 8080
- Check Docker containers: `docker ps`

**Game Won't Start**:
- Verify all services are healthy
- Check Kafka is running

**Invalid Coordinates**:
- Use format: Letter (A-J) + Number (1-10)
- Examples: A1, B5, J10

## License

Part of the Battleship Interview Coding Project
