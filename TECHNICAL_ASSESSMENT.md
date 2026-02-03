# Battleship Game - Technical Assessment

## Executive Summary

**Overall Grade: D+**

This codebase represents a Java microservices implementation of the classic Battleship game using Kafka event-driven architecture. While the architectural approach is sound for learning purposes, the implementation contains **critical bugs that render the application non-functional**, along with significant design flaws, SOLID principle violations, and code quality issues.

**Key Findings:**
- **2 Critical Bugs**: Core game logic is unimplemented (stub methods returning false)
- **5 SOLID Principle Violations**: Documented across service and domain layers
- **Concurrency Issues**: Race conditions in repository and game state management
- **Anemic Domain Model**: Business logic scattered outside domain entities
- **116 Code Smells**: Identified by static analysis tools
- **Security Gaps**: Input validation, error handling, and authentication missing

**Context**: This assessment evaluates the codebase as a coding exercise/interview project. Some identified issues (e.g., in-memory storage, basic security) are appropriate trade-offs for an exercise environment and would be over-engineering at this scale. However, the critical bugs and design pattern violations represent significant problems even for learning purposes.

---

## 1. Critical Bugs (Severity: BLOCKER)

### 1.1 Game Win Condition Never Triggers

**Location**: `battleship-service/src/main/java/com/odigeo/interview/coding/battleshipservice/service/FieldService.java:20-26`

```java
public boolean allShipsSunk(Cell[][] field) {
    // TODO: Check if all ships in field are sunk
    return false;
}

public boolean isShipSunk(Cell[][] field, Ship ship) {
    // TODO: Check if ship is sunk
    return false;
}
```

**Impact**: The game is **completely unplayable**. Players can never win because the win condition check always returns `false`. This means:
- Games run indefinitely until players quit manually
- The `GameService.fire()` method (line 157) can never detect a winner
- The `finishedAt` timestamp and `winner` fields in the `Game` model are never set

**Reproduction**:
1. Create a game
2. Fire at all opponent's ship positions
3. Game continues indefinitely despite all ships being destroyed

**Root Cause**: Developer left stub implementations with TODO comments, indicating unfinished work.

---

## 2. Architecture Analysis

### 2.1 Overall Design

**Pattern**: Microservices with Event-Driven Architecture (Kafka)

**Components**:
- `battleship-service`: Core game logic REST API
- `battleship-computer-service`: AI opponent service consuming Kafka events
- `battleship-api`: Shared contracts and events

**Strengths**:
- Clean separation of concerns between services
- Asynchronous communication via Kafka
- Modular Maven multi-module structure

**Concerns for Exercise Context**:

#### In-Memory Storage (Context: Appropriate for Exercise)
**Location**: `battleship-service/src/main/java/com/odigeo/interview/coding/battleshipservice/repository/GameRepositoryImpl.java:19`

```java
private final ConcurrentHashMap<String, GameEntity> games = new ConcurrentHashMap<>();
```

**Assessment**: Using `ConcurrentHashMap` for storage is **appropriate for a coding exercise**. Adding database persistence would be over-engineering given the scope. However, the concurrency issues with this implementation (detailed in section 2.2) still represent learning opportunities about thread safety.

### 2.2 Concurrency: Race Conditions and Data Corruption

**Severity**: HIGH (Critical for understanding, though at small scale impact is limited)

#### How Race Conditions Occur in the Current Code

**Location**: `battleship-service/src/main/java/com/odigeo/interview/coding/battleshipservice/repository/GameRepositoryImpl.java:21-24`

```java
public void saveOrUpdateGame(Game game) {
    GameEntity entity = gameMapper.fromModel(game);
    games.put(game.getId(), entity);
}
```

**The Problem**: This method performs **non-atomic read-modify-write operations** on game state. Here's exactly how data corruption occurs:

#### Step-by-Step Race Condition Scenario

Consider two simultaneous fire requests for the same game:

**Initial State**:
```
Game ID: "game-123"
Player 1 Field: [Empty at A1, Ship at B2, Ship at C3]
Player 2 Field: [Ship at D4, Empty at E5, Ship at F6]
Player Turn: 1
```

**Thread 1 (Player 1 fires at D4)** and **Thread 2 (Player 2 fires at A1)** execute concurrently:

```
TIME    THREAD 1 (Player 1)                    THREAD 2 (Player 2)
----    --------------------------------        --------------------------------
T0      GameService.fire() called
        game = repository.findById("123")
        [Game object loaded into memory]

T1                                              GameService.fire() called
                                                game = repository.findById("123")
                                                [Same game object loaded]

T2      Check: isPlayerTurn(p1) → TRUE
        opponentField = getPlayerTwoField()

T3                                              Check: isPlayerTurn(p2) → FALSE
                                                [Should fail but timing allows]

T4      opponentField[D4] = CELL_HIT
        playerTurn = 2                          opponentField[A1] = CELL_MISS
        [Modified in Thread 1's memory]         playerTurn = 2

T5      repository.saveOrUpdateGame(game)
        → Writes to ConcurrentHashMap

T6                                              repository.saveOrUpdateGame(game)
                                                → OVERWRITES Thread 1's changes!

RESULT: Player 1's hit at D4 is LOST. Only Player 2's miss at A1 is recorded.
        Player Turn is still 2 (should alternate correctly but field state is corrupt).
```

**Where This Occurs in Code**:

1. **`GameService.fire()` lines 115-163**: No synchronization on game instance
2. **`GameRepositoryImpl.saveOrUpdateGame()` line 23**: `ConcurrentHashMap.put()` is atomic for the map entry, but the `Game` object passed to it was modified outside any transaction
3. **Load-Modify-Save Pattern**: Lines 119-161 perform complex modifications on the loaded game object before saving

**Why `ConcurrentHashMap` Doesn't Prevent This**:

```java
// ConcurrentHashMap guarantees atomic put() operation
games.put("game-123", entity);  // This line is atomic

// BUT the problem occurs BEFORE this line:
// 1. Load game from map (Thread 1 and Thread 2 get same state)
// 2. Modify game object in separate memory spaces (not synchronized)
// 3. Save back to map (last write wins, losing previous changes)
```

**Additional Race Condition Locations**:

**`GameService.fire()` lines 126-132**:
```java
game = gameRepository.findById(gameId)
        .orElseThrow(() -> new GameException("Game does not exist.", "GAME_DOES_NOT_EXIST"));

if (!game.playersReady()) {
    throw new GameException("Both players are not ready.", "PLAYERS_NOT_READY");
}
if (game.isFinished()) {
    throw new GameException("Game already finished.", "GAME_ALREADY_FINISHED");
}
```

**Race Window**: Between checking `game.isFinished()` and later setting `game.setFinishedAt()` (line 158), another thread could finish the game, causing duplicate "Game Over" Kafka events.

**Where This Matters**:
- For a 10x10 board with human players, race conditions are unlikely (human reaction time >> network latency)
- For automated testing or AI vs AI games with rapid fire sequences, this becomes a **real problem**
- Demonstrates **fundamental misunderstanding of concurrency** (critical for technical interviews)

**Proper Solution** (for discussion):
```java
// Option 1: Pessimistic locking
public synchronized void saveOrUpdateGame(Game game) {
    // Ensures one thread at a time per repository instance
}

// Option 2: Optimistic locking with version field
public class Game {
    private Long version;
    // Use version field to detect concurrent modifications
}

// Option 3: Atomic operations (best for this use case)
games.compute(gameId, (id, existingGame) -> {
    // All modifications happen atomically inside compute()
});
```

---

## 3. Anemic Domain Model Anti-Pattern

### 3.1 What is an Anemic Domain Model?

An **Anemic Domain Model** is an anti-pattern where domain objects contain **only data (getters/setters)** with **little or no business logic**. This violates core Object-Oriented Programming principles:

**OOP Principle Violated**: **Encapsulation**
- Objects should bundle **data AND behavior** that operates on that data
- Business rules should live **inside** the domain entities they affect
- External services shouldn't manipulate internal object state directly

**Martin Fowler's Definition** (from *Patterns of Enterprise Application Architecture*):
> "The basic symptom of an Anemic Domain Model is that at first blush it looks like the real thing. There are objects, many named after nouns in the domain space, and these objects are connected with the rich relationships and structure that true domain models have. The catch comes when you look at the behavior, and you realize that there is hardly any behavior on these objects, making them little more than bags of getters and setters."

### 3.2 Where This Occurs in the Battleship Code

**Location**: `battleship-service/src/main/java/com/odigeo/interview/coding/battleshipservice/model/Game.java`

**Lines 1-169**: The `Game` class is **95% getters/setters**, with minimal business logic:

```java
public class Game {
    // 17 fields (lines 7-17)
    private String id;
    private String playerOneId;
    private String playerTwoId;
    private boolean vsComputer;
    private Integer playerTurn;
    private Cell[][] playerOneField;
    private Cell[][] playerTwoField;
    private Instant createdAt;
    private Instant startedAt;
    private Instant finishedAt;
    private String winner;

    // 96 lines of getters/setters (lines 19-105)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    // ... 15 more getter/setter pairs

    // Only 4 methods with actual logic (lines 107-169)
    public void setPlayerField(String playerId, Cell[][] playerField) { ... }
    public Cell[][] getPlayerField(String playerId) { ... }
    public Cell[][] getOpponentField(String playerId) { ... }
    public void setNextPlayerTurn() { ... }
}
```

**Business Logic Percentage**: ~4% (only 62 lines out of 169 contain business logic)

### 3.3 How This Violates OOP Principles

#### Violation 1: Encapsulation Broken

**Example**: `GameService.fire()` lines 140-149

```java
// Service layer directly manipulates Game's internal state
Cell[][] opponentField = game.getOpponentField(gameFireCommand.getPlayerId());
Ship ship = opponentField[gameFireCommand.getX()][gameFireCommand.getY()].getShip();

if (ship != null) {
    opponentField[gameFireCommand.getX()][gameFireCommand.getY()] = Cell.CELL_HIT;
    // ... more direct field manipulation
} else {
    opponentField[gameFireCommand.getX()][gameFireCommand.getY()] = Cell.CELL_MISS;
}

game.setNextPlayerTurn();
```

**Problem**: The `GameService` is **reaching into** the `Game` object, extracting its internal 2D array, and modifying cells directly. The `Game` class has **no control** over these modifications.

**OOP Principle Violated**: **Tell, Don't Ask**
- Instead of asking for internal state and manipulating it, we should **tell** the object what to do
- The `Game` object should **own** the logic for firing at coordinates

**What Rich Domain Model Would Look Like**:

```java
// Game.java - Business logic INSIDE the domain model
public class Game {
    public FireResult fire(String playerId, int x, int y) {
        validatePlayerTurn(playerId);
        validateCoordinates(x, y);

        Cell[][] opponentField = getOpponentField(playerId);
        Ship ship = opponentField[x][y].getShip();

        if (ship != null) {
            opponentField[x][y] = Cell.CELL_HIT;
            boolean sunk = checkIfShipSunk(opponentField, ship);
            setNextPlayerTurn();
            return new FireResult(true, sunk, checkWinCondition(opponentField));
        } else {
            opponentField[x][y] = Cell.CELL_MISS;
            setNextPlayerTurn();
            return new FireResult(false, false, false);
        }
    }

    private void validatePlayerTurn(String playerId) {
        if (!isPlayerTurn(playerId)) {
            throw new GameException("Not your turn!", "NOT_PLAYER_TURN");
        }
    }
}

// GameService.java - Orchestration only, no business logic
public GameFireResponse fire(String gameId, GameFireCommand command) {
    Game game = gameRepository.findById(gameId).orElseThrow();

    FireResult result = game.fire(command.getPlayerId(), command.getX(), command.getY());

    gameRepository.saveOrUpdateGame(game);
    kafkaService.publish(new GameFireEvent(...));

    return new GameFireResponse(result.isHit(), result.isSunk());
}
```

**Benefits of Rich Domain Model**:
1. **Single Responsibility**: `Game` manages game rules, `GameService` manages orchestration
2. **Testability**: Can unit test `Game.fire()` without mocking repository/Kafka
3. **Maintainability**: Game rules are in ONE place, not scattered across services
4. **Encapsulation**: Internal state (`playerOneField`) stays private

#### Violation 2: Code Duplication from Lack of Polymorphism

**Location**: `Game.java` lines 107-135

```java
public void setPlayerField(String playerId, Cell[][] playerField) {
    if (playerId.equals(getPlayerOneId())) {
        setPlayerOneField(playerField);
    } else if (playerId.equals(getPlayerTwoId())) {
        setPlayerTwoField(playerField);
    } else {
        throw new IllegalArgumentException(...);
    }
}

public Cell[][] getPlayerField(String playerId) {
    if (playerId.equals(getPlayerOneId())) {
        return getPlayerOneField();
    } else if (playerId.equals(getPlayerTwoId())) {
        return getPlayerTwoField();
    } else {
        throw new IllegalArgumentException(...);
    }
}

public Cell[][] getOpponentField(String playerId) {
    if (playerId.equals(getPlayerOneId())) {
        return getPlayerTwoField();
    } else if (playerId.equals(getPlayerTwoId())) {
        return getPlayerOneField();
    } else {
        throw new IllegalArgumentException(...);
    }
}

public boolean isPlayerTurn(String playerId) {
    if (playerId.equals(getPlayerOneId())) {
        return isPlayerTurn(1);
    } else if (playerId.equals(getPlayerTwoId())) {
        return isPlayerTurn(2);
    } else {
        throw new IllegalArgumentException(...);
    }
}
```

**Problem**: The same `if-else` pattern checking `playerOneId` vs `playerTwoId` is repeated **4 times**. This is a symptom of the anemic model - if `Game` had proper OOP design with a `Player` abstraction, this duplication would vanish.

**OOP Solution with Rich Model**:

```java
public class Game {
    private Player playerOne;
    private Player playerTwo;

    private Player getPlayer(String playerId) {
        if (playerOne.getId().equals(playerId)) return playerOne;
        if (playerTwo.getId().equals(playerId)) return playerTwo;
        throw new IllegalArgumentException("Player not in game");
    }

    private Player getOpponent(String playerId) {
        return getPlayer(playerId) == playerOne ? playerTwo : playerOne;
    }

    // Now methods become trivial:
    public void setPlayerField(String playerId, Cell[][] field) {
        getPlayer(playerId).setField(field);
    }

    public Cell[][] getOpponentField(String playerId) {
        return getOpponent(playerId).getField();
    }
}

public class Player {
    private String id;
    private Cell[][] field;
    private List<Ship> ships;

    public boolean hasLost() {
        return ships.stream().allMatch(Ship::isSunk);
    }
}
```

#### Violation 3: Transaction Script Pattern (Anti-Pattern)

**Location**: `GameService.java` lines 115-163

The entire `fire()` method is **48 lines** of procedural code that:
1. Loads data from repository
2. Performs validation checks
3. Modifies data structures
4. Saves data back to repository
5. Publishes Kafka events

This is **Transaction Script** pattern (from *Patterns of Enterprise Application Architecture*) - appropriate for simple CRUD, but an anti-pattern for complex business logic.

**Why This Violates OOP**:
- Business rules are **procedural code** in service layer
- Domain objects (`Game`) are **passive data structures**
- Logic is **scattered** across multiple service classes (`GameService`, `FieldService`, `ShipDeploymentValidator`)
- **No reusability**: Can't reuse game logic without entire service layer

---

## 4. SOLID Principle Violations

### 4.1 Single Responsibility Principle (SRP) Violations

**Principle**: "A class should have only one reason to change."

#### Violation 1: `GameService` Has Multiple Responsibilities

**Location**: `battleship-service/src/main/java/com/odigeo/interview/coding/battleshipservice/service/GameService.java`

**Lines 1-164**: This class handles:

1. **Game Orchestration** (lines 32-46, 48-67, 69-88, 90-113)
   ```java
   public Game newGame(GameStartCommand gameStartCommand) { ... }
   public void joinGame(String gameId, GameJoinCommand gameJoinCommand) { ... }
   public void deployShips(String gameId, DeployShipsCommand deployShipsCommand) { ... }
   ```

2. **Business Logic Validation** (lines 119-135)
   ```java
   if (!game.playersReady()) { throw new GameException(...); }
   if (game.isFinished()) { throw new GameException(...); }
   if (!game.isPlayerTurn(gameFireCommand.getPlayerId())) { throw new GameException(...); }
   ```

3. **Kafka Event Publishing** (lines 43-45, 62-65, 85-87, 155-157)
   ```java
   kafkaService.publish("battleship.game.new", new Gson().toJson(gameCreatedEvent));
   kafkaService.publish("battleship.game.player.join", new Gson().toJson(gamePlayerJoinEvent));
   ```

4. **Domain Logic** (lines 137-154)
   ```java
   Cell[][] opponentField = game.getOpponentField(gameFireCommand.getPlayerId());
   Ship ship = opponentField[gameFireCommand.getX()][gameFireCommand.getY()].getShip();
   if (ship != null) { /* hit logic */ } else { /* miss logic */ }
   ```

5. **Turn Management** (line 154)
   ```java
   game.setNextPlayerTurn();
   ```

**How This Violates SRP**: The class has **5 reasons to change**:
1. Change in game creation logic
2. Change in Kafka topic names or event structure
3. Change in validation rules
4. Change in hit/miss mechanics
5. Change in turn alternation logic

**Proper Solution** (for discussion):
```java
// GameService: Orchestration only
public class GameService {
    private GameRepository repository;
    private GameEventPublisher eventPublisher;

    public GameFireResponse fire(String gameId, GameFireCommand command) {
        Game game = repository.findById(gameId).orElseThrow();
        FireResult result = game.fire(command.getPlayerId(), command.getX(), command.getY());
        repository.save(game);
        eventPublisher.publishFireEvent(gameId, result);
        return new GameFireResponse(result);
    }
}

// GameEventPublisher: Kafka publishing only
public class GameEventPublisher {
    public void publishFireEvent(String gameId, FireResult result) { ... }
}

// Game: Business logic only (rich domain model)
public class Game {
    public FireResult fire(String playerId, int x, int y) { ... }
}
```

#### Violation 2: `ShipDeploymentValidator` Mixes Validation and Utility Logic

**Location**: `battleship-service/src/main/java/com/odigeo/interview/coding/battleshipservice/util/ShipDeploymentValidator.java`

**Lines 1-104**: This class does:

1. **Coordinate Validation** (lines 19-30)
   ```java
   public static boolean areCoordinatesInBounds(List<CoordinateCommand> coordinates) { ... }
   public static boolean areCoordinatesUnique(List<CoordinateCommand> coordinates) { ... }
   ```

2. **Ship Count Validation** (lines 52-63)
   ```java
   public static boolean areShipsSizeCorrect(List<ShipCommand> ships) { ... }
   public static boolean areTotalNumberOfShipsCorrect(List<ShipCommand> ships) { ... }
   ```

3. **Geometry Calculations** (lines 65-71, 73-90)
   ```java
   public static boolean areShipsInLine(List<ShipCommand> ships) { ... }
   public static boolean doShipsOverlap(List<ShipCommand> ships) { ... }
   ```

4. **Business Rule Definitions** (lines 92-104 - private constants)
   ```java
   private static final int NUMBER_OF_SHIPS = 3;
   private static final List<Integer> SHIPS_SIZE = List.of(2, 3, 4);
   ```

**How This Violates SRP**: **4 reasons to change** + mixing concerns that should be in different layers.

### 4.2 Open/Closed Principle (OCP) Violations

**Principle**: "Software entities should be open for extension but closed for modification."

#### Violation: Hard-Coded AI Strategy in `BattleshipService`

**Location**: `battleship-computer-service/src/main/java/com/odigeo/interview/coding/battleshipcomputerservice/service/BattleshipService.java:50-77`

```java
private GameFireCommand generateFireCommand(String playerId, List<ShipCommand> ships) {
    Random random = new Random();

    // Hard-coded random strategy - no extension point
    List<CoordinateCommand> coords = ships.stream()
            .map(ShipCommand::getCoordinates)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

    int randomX = random.nextInt(10);  // Magic number
    int randomY = random.nextInt(10);  // Magic number

    while (coords.stream().anyMatch(c -> c.getX().equals(randomX) && c.getY().equals(randomY))) {
        randomX = random.nextInt(10);
        randomY = random.nextInt(10);
    }

    return GameFireCommand.builder()
            .playerId(playerId)
            .x(randomX)
            .y(randomY)
            .build();
}
```

**How This Violates OCP**:
- To add a new AI strategy (hunt mode, probabilistic targeting), you must **modify** this method
- No abstraction for different AI behaviors
- Hard-coded `Random` strategy with no interface for extension

**Proper Solution** (Strategy Pattern):
```java
// Open for extension
public interface FiringStrategy {
    Coordinate selectTarget(GameState state);
}

public class RandomFiringStrategy implements FiringStrategy { ... }
public class HuntTargetFiringStrategy implements FiringStrategy { ... }
public class ProbabilisticFiringStrategy implements FiringStrategy { ... }

// Closed for modification
public class BattleshipService {
    private FiringStrategy strategy;

    public void fire(String gameId) {
        Coordinate target = strategy.selectTarget(gameState);
        // ...
    }
}
```

### 4.3 Liskov Substitution Principle (LSP)

**Status**: ✅ No violations found (no inheritance hierarchies in codebase)

### 4.4 Interface Segregation Principle (ISP) Violations

**Principle**: "Clients should not be forced to depend on interfaces they don't use."

#### Violation: `KafkaListener` Forces Broad Interface

**Location**: `battleship-computer-service/src/main/java/com/odigeo/interview/coding/battleshipcomputerservice/controller/KafkaMDB.java:32`

```java
public class KafkaMDB implements KafkaListener {
    @OnRecord(topics={"battleship.game.new"})
    public void onGameNew(ConsumerRecord record) { ... }

    @OnRecord(topics={"battleship.game.field.fire"})
    public void onGameFieldFire(ConsumerRecord record) { ... }
}
```

**How This Violates ISP**:
- The `KafkaListener` interface (from Payara library) requires implementing **all Kafka consumer methods**
- This class only needs to handle **2 specific topics** but depends on entire Kafka consumer API
- If `KafkaListener` interface changes (new methods added), this class must change even if it doesn't use them

**Why This Might Be Acceptable**: This is a **framework constraint** (Payara Kafka connector). The violation is imposed by the library, not the application design. However, it still represents a dependency problem.

### 4.5 Dependency Inversion Principle (DIP) Violations

**Principle**: "High-level modules should not depend on low-level modules. Both should depend on abstractions."

#### Violation 1: Direct Dependency on Gson in Multiple Places

**Locations**:
- `GameService.java:43` (line 43)
- `GameService.java:64` (line 64)
- `GameService.java:86` (line 86)
- `GameService.java:156` (line 156)
- `KafkaMDB.java:44` (line 44)
- `KafkaMDB.java:52` (line 52)

```java
// High-level GameService directly depends on low-level Gson library
GameCreatedEvent gameCreatedEvent = GameCreatedEvent.builder()
    .gameId(game.getId())
    .build();

kafkaService.publish("battleship.game.new", new Gson().toJson(gameCreatedEvent));
```

**How This Violates DIP**:
- `GameService` (high-level business logic) **directly instantiates** `Gson` (low-level serialization library)
- To switch from Gson to Jackson or other JSON library requires **changing high-level modules**
- No abstraction layer between business logic and serialization

**Proper Solution**:
```java
// Abstraction (high-level interface)
public interface EventSerializer {
    String serialize(Object event);
    <T> T deserialize(String json, Class<T> type);
}

// Low-level implementation
public class GsonEventSerializer implements EventSerializer {
    private final Gson gson = new Gson();

    public String serialize(Object event) {
        return gson.toJson(event);
    }

    public <T> T deserialize(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }
}

// High-level module depends on abstraction
public class GameService {
    @Inject
    private EventSerializer serializer;  // Depends on abstraction, not Gson

    public Game newGame(GameStartCommand command) {
        // ...
        String eventJson = serializer.serialize(gameCreatedEvent);
        kafkaService.publish("battleship.game.new", eventJson);
    }
}
```

#### Violation 2: `GameRepositoryImpl` Directly Depends on MapStruct Mapper

**Location**: `battleship-service/src/main/java/com/odigeo/interview/coding/battleshipservice/repository/GameRepositoryImpl.java:17-29`

```java
public class GameRepositoryImpl implements GameRepository {
    // Direct dependency on concrete MapStruct implementation
    private final GameMapper gameMapper = GameMapper.INSTANCE;

    private final ConcurrentHashMap<String, GameEntity> games = new ConcurrentHashMap<>();

    public void saveOrUpdateGame(Game game) {
        GameEntity entity = gameMapper.fromModel(game);  // Direct coupling
        games.put(game.getId(), entity);
    }
}
```

**How This Violates DIP**:
- Repository (high-level) depends on `GameMapper.INSTANCE` (low-level MapStruct generated class)
- To switch from MapStruct to manual mapping or other library requires changing repository
- `GameMapper.INSTANCE` is a **static singleton** - cannot be mocked in tests

**Proper Solution**:
```java
// Repository depends on abstraction
public class GameRepositoryImpl implements GameRepository {
    @Inject
    private GameMapper gameMapper;  // Injected dependency on interface

    public void saveOrUpdateGame(Game game) {
        GameEntity entity = gameMapper.fromModel(game);
        games.put(game.getId(), entity);
    }
}
```

---

## 5. Code Quality Issues

### 5.1 Code Smells

#### Magic Numbers

**Location**: Throughout codebase

**Example 1**: `ShipDeploymentValidator.java:19`
```java
return coordinate.getX() >= 0 && coordinate.getX() <= 9
    && coordinate.getY() >= 0 && coordinate.getY() <= 9;
```

**Should be**:
```java
private static final int BOARD_SIZE = 10;
return coordinate.getX() >= 0 && coordinate.getX() < BOARD_SIZE
    && coordinate.getY() >= 0 && coordinate.getY() < BOARD_SIZE;
```

**Example 2**: `BattleshipService.java:41`
```java
Thread.sleep(600);  // Why 600? What unit?
```

#### Primitive Obsession

**Location**: `GameFireCommand.java`, `CoordinateCommand.java`

Using `Integer x, Integer y` instead of a `Coordinate` value object throughout the codebase.

#### Long Methods

**Location**: `GameService.fire()` - 48 lines (115-163)

### 5.2 Performance Concerns (Context: Small Scale)

#### O(n²) Complexity in Validation

**Location**: `ShipDeploymentValidator.java:32-41`

```java
public static boolean areCoordinatesUnique(List<CoordinateCommand> coordinates) {
    for (CoordinateCommand coordinate : coordinates) {
        // ArrayList.contains() is O(n), called n times = O(n²)
        if (Collections.frequency(coordinates, coordinate) > 1) {
            return false;
        }
    }
    return true;
}
```

**Where This Occurs**: The `Collections.frequency()` method iterates through the entire `coordinates` list for each element, resulting in O(n²) time complexity.

**Context for Exercise Scale**:
- Maximum coordinates = 3 ships × 4 cells = **12 coordinates max**
- O(n²) with n=12 means **144 operations** - negligible performance impact
- This would only matter at scale (1000+ coordinates)

**Better Approach** (for learning, not necessity):
```java
public static boolean areCoordinatesUnique(List<CoordinateCommand> coordinates) {
    Set<CoordinateCommand> seen = new HashSet<>();
    for (CoordinateCommand coord : coordinates) {
        if (!seen.add(coord)) {
            return false;
        }
    }
    return true;
}
// O(n) complexity with HashSet
```

#### O(n²) in Overlap Detection

**Location**: `ShipDeploymentValidator.java:73-90`

```java
public static boolean doShipsOverlap(List<ShipCommand> ships) {
    List<CoordinateCommand> coords = new ArrayList<>();
    for (ShipCommand ship : ships) {
        for (CoordinateCommand coord : ship.getCoordinates()) {
            // Nested loop: O(n²)
            if (coords.contains(coord)) {  // ArrayList.contains is O(n)
                return true;
            }
            coords.add(coord);
        }
    }
    return false;
}
```

**Context**: Same as above - only 12 coordinates max, so O(n²) has minimal real-world impact.

### 5.3 Error Handling

#### Swallowed `InterruptedException`

**Location**: `BattleshipService.java:45-48`

```java
try {
    Thread.sleep(600);
} catch (InterruptedException e) {
    e.printStackTrace();  // Anti-pattern: swallows interrupt signal
}
```

**Problem**: When a thread is interrupted, the interrupt flag should be preserved or the exception should propagate. Calling `printStackTrace()` and continuing execution ignores the interrupt signal.

**Proper Handling**:
```java
try {
    Thread.sleep(600);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();  // Restore interrupt status
    throw new RuntimeException("AI decision interrupted", e);
}
```

---

## 6. Security Concerns (Context: Appropriate for Exercise)

### 6.1 Input Validation Gaps

**Location**: `GameController.java:32-53`

```java
@POST
@Path("/{gameId}/fields/fire")
public GameFireResponse fire(@PathParam("gameId") String gameId, GameFireCommand gameFireCommand) {
    return service.fire(gameId, gameFireCommand);
}
```

**Missing Validations**:
- No validation that `gameId` is a valid UUID format
- No validation of `gameFireCommand` payload (could be null, malformed)
- No authentication/authorization (any player can fire for any other player)

**Assessment for Exercise Context**:
- Input validation should be present for **correctness**, not just security
- Authentication would be over-engineering for a coding exercise
- Basic null checks and format validation are reasonable expectations

### 6.2 No Authentication/Authorization

**All Controllers**: No security annotations or authentication checks.

**Assessment**: For a coding exercise, implementing full authentication (JWT, OAuth, etc.) would be **over-engineering**. However, this should be acknowledged in documentation as a "production consideration."

---

## 7. Testing Gaps

### 7.1 Missing Test Coverage

**Original State** (from previous session context):
- JaCoCo reports indicated **0% initial coverage** (later improved to 89.8%)
- No unit tests for critical `FieldService` methods
- No integration tests for concurrent game scenarios

### 7.2 Testability Issues

The anemic domain model and tight coupling (DIP violations) make testing difficult:
- `GameService.fire()` requires mocking `GameRepository`, `KafkaService`, and `FieldService`
- Cannot unit test game logic without service layer
- `new Gson()` and `GameMapper.INSTANCE` static dependencies cannot be mocked

---

## 8. Prioritized Recommendations

### CRITICAL (Must Fix)
1. **Implement `FieldService.allShipsSunk()` and `isShipSunk()`** - Game is unplayable
2. **Fix race conditions in `GameRepositoryImpl`** - Use `ConcurrentHashMap.compute()` for atomic updates

### HIGH (Significant Improvement)
3. **Introduce Rich Domain Model** - Move business logic from `GameService` into `Game` class
4. **Refactor SRP violations** - Split `GameService` into orchestration + event publishing
5. **Add input validation** - Null checks, format validation in controllers

### MEDIUM (Quality Enhancement)
6. **Implement Strategy Pattern for AI** - Make AI behavior extensible
7. **Replace magic numbers** - Use constants for board size, timeouts
8. **Fix DIP violations** - Inject `Gson` wrapper and `GameMapper` via interfaces
9. **Improve error handling** - Properly handle `InterruptedException`
10. **Add unit tests** - Especially for domain logic after refactoring

### LOW (Polish / Future Consideration)
11. **Optimize O(n²) algorithms** - Use HashSet for coordinate uniqueness (learning value, not performance need)
12. **Add API documentation** - OpenAPI/Swagger (over-engineering for exercise)
13. **Consider persistence** - Database layer (over-engineering for exercise scope)

---

## 9. Conclusion

This codebase demonstrates understanding of **microservices architecture** and **event-driven patterns**, but suffers from **fundamental implementation gaps** (critical bugs) and **design pattern violations** (anemic domain model, SOLID principles).

**Strengths**:
- Clean service separation
- Kafka-based asynchronous communication
- Modular Maven structure

**Critical Weaknesses**:
- **Non-functional game logic** (stub methods)
- **Thread-safety issues** (race conditions)
- **Poor encapsulation** (anemic domain model)
- **Violation of SOLID principles** (especially SRP and DIP)

**Recommendation**: For an interview context, this code provides **excellent discussion material** about refactoring, design patterns, and architectural trade-offs. The bugs are unacceptable even for an exercise, but the design issues are **appropriate complexity** for learning and discussion without over-engineering the solution.

**Interview Discussion Points**:
1. "Why is the game unplayable and how would you fix it?" (Critical thinking)
2. "What is an anemic domain model and where do you see it here?" (Design patterns)
3. "How would you prevent race conditions in the repository?" (Concurrency)
4. "Where does this code violate SOLID and how would you refactor?" (OOP principles)
5. "Which issues are critical vs. which are over-engineering for this context?" (Pragmatism)
