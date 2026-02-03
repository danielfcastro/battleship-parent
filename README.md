# Battleship - Microservices Game

A microservices-based implementation of the classic Battleship game using Java EE, WildFly, Kafka, and Docker.

## Architecture

- **battleship-api**: Shared API contracts (DTOs, events)
- **battleship-service**: Main game service (REST API, game logic)
- **battleship-computer-service**: AI player service (Kafka consumer)
- **battleship-play**: Command-line game client
- **Kafka**: Event-driven communication between services
- **Zookeeper**: Kafka coordination

## Quick Start

### Prerequisites

- Docker and Docker Compose
- Java 8+ (for running the game client)
- Maven 3.6+ (for building)

### 1. Build the Project

```bash
mvn clean package
```

### 2. Start Services

The startup script handles everything automatically:

```bash
./start.sh
```

This script will:
- Create the Docker network if needed
- Build and start all containers in the correct order
- Wait for Kafka to be fully operational
- Wait for application services to deploy successfully
- Confirm when everything is ready

**Expected startup time**: 2-3 minutes

### 3. Play the Game

Once services are healthy:

```bash
cd battleship-play/target
java -jar battleship-play-1.0.1-SNAPSHOT.jar
```

## Changes since last commit (Bug fixes)

The following fixes have been implemented to resolve logic and display issues in the game:

- **Fix: Sunk ships detection and end of game**
  - **Status**: ✅ Resolved
  - **What changed**: Correct implementation of the `isShipSunk` and `allShipsSunk` methods in `battleship-service` (`FieldService`).
  - **Concept - Business Logic Integrity**: Previously, the game lacked the essential rules to determine its conclusion. By implementing these methods, we ensured the integrity of the game state, allowing the `GameService` to correctly identify when a ship's coordinates are fully hit and when the entire fleet is destroyed.
  - **Why**: To ensure that, when hitting all parts of a ship, the service returns `SUNK` and, when all ships are destroyed, the game is marked as completed (`gameWon = true`).

- **Fix: Sunk ship type identification in the API response**
  - **Status**: ✅ Resolved
  - **What changed**: Added the `shipTypeSunk` field in `battleship-api` (`GameFireResponse`) and populated it in `battleship-service` (`GameService.fire`). In the client (`battleship-play`), the `Board` class was updated to track individual ship status.
  - **Concept - Data Normalisation & API Contract**: The API was enhanced to provide more granular information. We also implemented name normalisation (e.g., `AircraftCarrier` to `Aircraft Carrier`) to ensure UI consistency. This follows the principle of "Single Source of Truth", where the server dictates the specific ship sunk, rather than the client guessing based on a fixed list.
  - **Why**: To prevent the client from marking incorrect ships as sunk; the UI now accurately reflects the real state reported by the server.

- **Updated tests and utilities to ensure behaviour**
  - **Status**: ✅ Improved
  - **What changed**: Tests in `battleship-service` were expanded (`FieldServiceTest`, `GameServiceTest`) and the `ShipDeploymentBuilder` utility was updated.
  - **Concept - Regression Testing**: By adding tests that specifically target the previously failing logic (sinking and victory conditions), we created a safety net that prevents these bugs from reoccurring during future refactors.
  - **Why**: To ensure business rules are correctly implemented and automatically verified across all layers.

## Improvements based on Technical Review

Following the recommendations in the `CORE_REVIEW_REPORT.md`, several architectural and logic gaps were addressed to move the project closer to production readiness:

### 1. Robust Core Business Logic
The critical bug regarding the inability to win or sink ships (identified as a **CRITICAL BUG** in the review) has been fully resolved. 
- **Implementation**: The `FieldService` now performs real-time validation of the grid state against ship coordinates.
- **Clarity**: This transition from "Dead Code" stubs to functional logic ensures that the core loop of the Battleship game (Fire -> Hit/Miss -> Sink -> Win) is complete and reliable.

### 2. Enhanced API Communication
The review highlighted a need for better data flow between services.
- **Resolution**: By extending the `GameFireResponse` contract, we reduced the coupling between the client's visual representation and the server's internal state. The client no longer relies on fragile assumptions about ship order.
- **Concept - Encapsulation**: Information about which ship was hit is now encapsulated within the API response, making the system more modular and less prone to UI/Logic desynchronisation.

### 3. Test Suite Maturity
The report noted "Testing Gaps" especially in critical methods.
- **Resolution**: New test cases were added to `FieldServiceTest` and `GameServiceTest` specifically to validate the "Win" condition and "Ship Sunk" notification.
- **Outcome**: The test suite now provides proof of functionality for the most critical game rules, increasing confidence in the system's stability.

## Manual Startup (Alternative)

If you prefer manual control:

```bash
# 1. Create network
docker network create battleship_net

# 2. Start services
docker-compose up --build

# 3. Wait for logs to show:
#    - "WildFly Full ... started in ... - Started XXX of XXX services"
#    - No "WFLYSRV0026: ... (with errors)" messages

# 4. Test readiness
curl http://localhost:8080/battleship-service/api/engineering/ping
# Should return: "pong"

# 5. Play
cd battleship-play/target
java -jar battleship-play-1.0.1-SNAPSHOT.jar
```

## How It Works

### Startup Orchestration

The solution uses multiple layers to ensure correct startup:

1. **Docker Compose Dependencies**:
   ```yaml
   depends_on:
     kafka:
       condition: service_healthy
   ```
   Services wait for Kafka's health check to pass

2. **Kafka Health Check**:
   - Tests that Kafka port 29092 is accepting connections
   - Allows 30 seconds startup time + 20 retries

3. **wait-for-it.sh Script**:
   - Each application service runs this before starting WildFly
   - Waits up to 60 seconds for Kafka DNS resolution and connection
   - Only then starts WildFly server

4. **WildFly Deployment**:
   - Deploys Kafka Resource Adapter (kafka-rar.rar)
   - Successfully connects to Kafka
   - Deploys application WAR files

### Why This Approach?

The original code had a race condition: WildFly would try to deploy the Kafka Resource Adapter before Kafka was ready, causing deployment failures. The solution:

- ✅ No manual restart required
- ✅ Reliable startup order
- ✅ Graceful handling of Kafka initialization
- ✅ Production-ready approach

## Services

| Service | Port | Health Check |
|---------|------|--------------|
| Battleship Service | 8080 | `/battleship-service/api/engineering/ping` |
| Computer Service | 8081 | `/battleship-computer-service/api/engineering/ping` |
| Kafka | 9092 | Port connectivity |
| Zookeeper | 2181 | Port connectivity |

## Game Flow

1. **Create Game**: Player starts a new game vs computer
2. **Deploy Ships**: Ships are randomly placed on both boards
3. **Computer Joins**: Computer service receives Kafka event and joins
4. **Battle**: Players take turns firing at coordinates
5. **Victory**: First to sink all ships wins

## Development

### Build Individual Modules

```bash
# API only
cd battleship-api && mvn clean install

# Service only  
cd battleship-service && mvn clean package

# Computer service only
cd battleship-computer-service && mvn clean package

# Game client only
cd battleship-play && mvn clean package
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f battleship_service
docker-compose logs -f battleship_computer_service
docker-compose logs -f kafka
```

### Stop Services

```bash
docker-compose down

# To also remove volumes
docker-compose down -v
```

## Troubleshooting

### Services not starting

```bash
# Check container status
docker-compose ps

# Check logs for specific service
docker-compose logs battleship_service

# Restart specific service
docker restart battleship_service
```

### "Network battleship_net not found"

```bash
docker network create battleship_net
```

### Port already in use

```bash
# Find what's using port 8080
lsof -i :8080

# Kill it or change ports in docker-compose.yml
```

### Game client connection refused

Ensure services are healthy:
```bash
curl http://localhost:8080/battleship-service/api/engineering/ping
```

Should return `"pong"`. If 404, services aren't fully deployed yet.

## Project Structure

```
.
├── battleship-api/              # Shared contracts
│   └── src/main/java/.../contract/
│       ├── GameStartCommand.java
│       ├── GameFireCommand.java
│       └── ...
├── battleship-service/          # Main game service
│   ├── src/main/java/.../
│   │   ├── controller/          # REST endpoints
│   │   ├── service/             # Business logic
│   │   ├── model/               # Domain models
│   │   └── repository/          # Data access
│   ├── Dockerfile
│   └── wait-for-it.sh          # Startup orchestration
├── battleship-computer-service/ # AI opponent
│   ├── src/main/java/.../
│   │   ├── controller/          # Kafka consumers
│   │   └── service/             # AI logic
│   ├── Dockerfile
│   └── wait-for-it.sh
├── battleship-play/             # CLI game client
│   └── src/main/java/.../
│       ├── client/              # HTTP client
│       ├── model/               # Board, ships
│       ├── strategy/            # AI strategies
│       └── BattleshipGame.java  # Main class
├── docker-compose.yml           # Orchestration
├── start.sh                     # Automated startup
└── README.md
```

## Documentation

- [Code Review Report](CODE_REVIEW_REPORT.md) - Comprehensive analysis
- [Deployment Guide](DEPLOYMENT_GUIDE.md) - Detailed deployment strategies
- [Game Client README](battleship-play/README.md) - How to play

## Tech Stack

- **Java 8**
- **Java EE** (JAX-RS, CDI, EJB)
- **WildFly 21** (Application Server)
- **Apache Kafka** (Event Streaming)
- **Docker & Docker Compose** (Containerization)
- **Maven** (Build Tool)
- **Gson** (JSON Serialization)
- **MapStruct** (Object Mapping)
- **TestNG** (Testing)
- **Mockito** (Mocking)

## License

Educational/Interview Project
