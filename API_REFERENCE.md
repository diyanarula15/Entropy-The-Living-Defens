# API Reference

## Core Classes

### GameLoop
Decouples simulation (10 TPS) from rendering (60 FPS).

```java
GameLoop gameLoop = new GameLoop();

// In game loop:
if (gameLoop.update(deltaTime)) {
    // Simulation tick - happens every 100ms
    automataEngine.tick();
}

// Controls
gameLoop.setPaused(boolean);
int tps = gameLoop.getCurrentTPS();
```

### GridManager
Manages the Game of Life universe with BitSet storage.

```java
GridManager grid = new GridManager(width, height);

// Cell operations
grid.setCell(x, y, true);   // Make alive
grid.setCell(x, y, false);  // Make dead
boolean alive = grid.getCell(x, y);
int neighbors = grid.countNeighbors(x, y);

// Simulation
grid.updateGeneration();

// Stats
int count = grid.getLivingCellCount();
long gen = grid.getGenerationCount();
List<Vector2i> cells = grid.getAllLivingCells();

// Utility
grid.clear();
grid.serialize();   // Get List<Vector2i>
grid.deserialize(cells);  // Load List<Vector2i>
```

## Simulation Classes

### AutomataEngine
Runs Game of Life with optional entropy.

```java
AutomataEngine engine = new AutomataEngine(grid);

// Each tick
engine.tick();

// Control entropy
engine.setEntropyEnabled(true);
EntropySystem entropy = engine.getEntropySystem();
entropy.setCellDeathRate(0.002f);  // Probability 0-1
```

### EntropySystem
Introduces chaos to prevent perfect patterns.

```java
EntropySystem entropy = new EntropySystem(grid);

entropy.setMutationRate(0.001f);  // Random flips
entropy.setCellDeathRate(0.002f); // Random deaths
entropy.setCellBirthRate(0.0005f); // Random births

entropy.applyEntropy();  // Called by AutomataEngine.tick()
```

## Entity Classes

### EnemyAgent
Represents an enemy unit.

```java
Vector2f start = new Vector2f(100, 100);
Vector2f target = new Vector2f(500, 500);
EnemyAgent enemy = new EnemyAgent(start, target);

// Each frame
enemy.update(deltaTime);

// Combat
enemy.takeDamage(20.0f);
float health = enemy.getHealth();
boolean dead = !enemy.isActive();

// Physics
enemy.setVelocity(new Vector2f(50, 0));
enemy.addForce(steeringForce);
enemy.slow(0.7f);  // Multiply velocity

// Info
Vector2f pos = enemy.getPosition();
float radius = enemy.getRadius();
boolean reached = enemy.hasReachedTarget();
```

### PatternFactory
Pre-defined Life patterns.

```java
PatternFactory.createBlinker(grid, x, y);    // Period 2 oscillator
PatternFactory.createBlock(grid, x, y);      // Stable 2×2
PatternFactory.createGlider(grid, x, y);     // Diagonal mover
PatternFactory.createLWSS(grid, x, y);       // Horizontal spaceship
PatternFactory.createToad(grid, x, y);       // Period 2
PatternFactory.createBeacon(grid, x, y);     // Period 2
PatternFactory.createPulsar(grid, x, y);     // Period 3
PatternFactory.createSimpleGliderGun(grid, x, y);  // Spawns gliders
```

## System Classes

### GameState
Master game controller integrating all systems.

```java
Vector2i base = new Vector2i(100, 100);
Vector2i spawn = new Vector2i(10, 100);
GameState state = new GameState(grid, base, spawn);

// Entities
state.spawnEnemy();
List<EnemyAgent> enemies = state.getEnemies();

// Painting
if (state.paintCell(x, y, true)) {
    // Painted successfully
} else {
    // Overheated
}

// Status
state.updateEntities(deltaTime);
float health = state.getBaseHealth();
float heat = state.getHeatLevel();
boolean overheated = state.isOverheated();
boolean gameOver = state.isGameOver();

// Stats
int score = state.getScore();
int killed = state.getEnemiesKilled();
int wave = state.getWaveNumber();
```

### CollisionSystem
Detects and resolves collisions using spatial hashing.

```java
CollisionSystem collisions = new CollisionSystem(grid);

collisions.updateEnemyBuckets(enemies);
collisions.updateCellBuckets();

// Check collisions - returns enemies to remove
List<EnemyAgent> toRemove = collisions.checkCollisions(enemies);
```

### FlowFieldSystem
Pre-computed pathfinding using flow fields.

```java
Vector2i basePos = new Vector2i(100, 100);
FlowFieldSystem flow = new FlowFieldSystem(grid, basePos);

// Get direction for enemy at position
Vector2f direction = flow.getFlowVector(enemyPos);

// Regenerate when grid changes
flow.regenerateFlowField();

// Change base
flow.setBasePosition(newBase);
```

## Utility Classes

### Vector2i
Integer-based 2D coordinates for grid positions.

```java
Vector2i v = new Vector2i(10, 20);
Vector2i v2 = Vector2i.of(5, 15);

Vector2i sum = v.add(v2);           // (15, 35)
Vector2i offset = v.add(1, 2);      // (11, 22)

// Grid indexing
long index = v.toIndex(gridWidth);
Vector2i recovered = Vector2i.fromIndex(index, gridWidth);

String s = v.toString();  // "(10, 20)"
```

### Vector2f
Float-based 2D vectors for physics.

```java
Vector2f v = new Vector2f(1.0f, 2.0f);
Vector2f v2 = Vector2f.of(3.0f, 4.0f);

// Vector math
Vector2f sum = v.add(v2);           // (4, 6)
Vector2f diff = v.subtract(v2);     // (-2, -2)
Vector2f scaled = v.multiply(2.0f); // (2, 4)

// Magnitude
float len = v.length();              // ~2.24
Vector2f normalized = v.normalize(); // length = 1

// Distance
float dist = v.distance(v2);

// Dot product
float dot = v.dot(v2);

// Conversion
Vector2i asInt = v.toVector2i();
Vector2f fromInt = Vector2f.fromVector2i(intVec);
```

### SerializationUtils
Save and load game state.

```java
// Save grid to JSON
SerializationUtils.saveGrid(grid, "savegame.json");

// Load grid from JSON
SerializationUtils.loadGrid(grid, "savegame.json");

// Save pattern
List<Vector2i> cells = Arrays.asList(...);
SerializationUtils.savePattern("MyPattern", cells, "pattern.json");

// Load pattern
cells = SerializationUtils.loadPattern("pattern.json");
```

## Rendering Classes

### RenderSystem
Main rendering pipeline using LibGDX.

```java
RenderSystem renderer = new RenderSystem(gameState, automataEngine);

// Each frame
renderer.handleMouseInput();
renderer.handleKeyboardInput();
renderer.render(screenWidth, screenHeight, currentFPS, currentTPS);

// Control camera
renderer.setZoom(1.5f);
float zoom = renderer.getZoom();

renderer.dispose();  // Cleanup
```

### HUD
Heads-Up Display showing game stats.

```java
HUD hud = new HUD(gameState, batch, font);

// Called by RenderSystem.render()
hud.render(screenWidth, screenHeight, fps, tps);

hud.dispose();
```

## Configuration

### GameConfig
Game constants and tuning parameters.

```java
public static final int GRID_WIDTH = 200;
public static final float CELL_SIZE = 10.0f;
public static final float TICK_DURATION = 0.1f;
public static final float CELL_DAMAGE = 20.0f;
public static final float MAX_HEAT = 100.0f;
// ... more constants
```

## Input

### Keyboard Controls

| Key | Action |
|-----|--------|
| **P** | Pause/Resume |
| **C** | Clear grid |
| **R** | Spawn test glider |
| **G** | Spawn glider gun |
| **B** | Spawn block |
| **S** | Save grid to savegame.json |
| **L** | Load grid from savegame.json |
| **↑** | Zoom in |
| **↓** | Zoom out |
| **ESC** | Exit game |

### Mouse Controls

| Input | Action |
|-------|--------|
| **Left Click** | Paint live cell (costs heat) |
| **Right Click** | Erase cell |

## Data Structures

### BitSet (Grid Storage)
- Efficient binary representation: 1 bit per cell
- Fast iteration with `nextSetBit()`
- Used internally by GridManager

### HashMap (Spatial Buckets)
- Maps bucket IDs to lists of enemies
- O(1) lookup for nearby buckets
- Used by CollisionSystem

### Vector Field (Pathfinding)
- 2D array of Vector2f directions
- One vector per grid cell
- Computed once, reused for all enemies
- Regenerated when grid changes

### Active Set (Dirty Tracking)
- Set<Vector2i> of cells to check next tick
- Only cells that changed + neighbors
- Dramatically reduces Game of Life update time
