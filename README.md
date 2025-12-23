# Life Defense - Conway's Game of Life Tower Defense

A unique tower defense game where the player controls **living patterns** based on Conway's Game of Life to defend against incoming enemies.

## Game Concept

You don't build static towers. Instead, you **paint living cells** onto an infinite grid that evolve according to Conway's rules. Every 100ms, the simulation updates:

- **Underpopulation**: Cells with < 2 neighbors die
- **Overcrowding**: Cells with > 3 neighbors die  
- **Survival**: Cells with 2-3 neighbors survive
- **Reproduction**: Empty cells with exactly 3 neighbors become alive

Enemies spawn at Point A and try to reach Point B. When they touch a **Live Cell**, they take damage and the cell dies (is consumed). Your goal is to create defensive patterns that intercept enemies before they reach your base.

## Core Mechanics

### The "Heat" System (Thermodynamics)
- Painting cells generates heat
- Overheating locks your cursor until cooldown
- Forces efficient, strategic placement

### Pattern Library
- **Glider**: Diagonal-moving pattern, great for flanking
- **Block**: Stable 2x2 square, acts as a wall
- **Blinker**: Period-2 oscillator
- **LWSS**: Lightweight spaceship, moves horizontally
- **Pulsar**: Complex period-3 oscillator
- **Glider Gun**: Spawns gliders periodically

### Enemy AI
Enemies use **Flow Fields** (pre-computed vector fields) for pathfinding:
- Flood-fill from your base calculates shortest path
- Enemies follow vector arrows on their tile
- 10,000+ enemies with near-zero pathfinding cost

### Collision System
- **Spatial Hashing**: Divides world into buckets
- Only checks collisions between enemies and nearby cells
- O(n) performance instead of O(n²)

## Technical Highlights

### Architecture: Entity Component System (ECS)
- Separation of concerns: Rendering, Simulation, Entities
- Easy to add new features without spaghetti code
- Data-oriented design for cache efficiency

### Performance Optimizations

1. **Active Cell Tracking** (Dirty Rectangles)
   - Only updated cells and neighbors are checked
   - Reduces Game of Life from O(Grid²) to O(Living Cells)

2. **Spatial Hashing for Collisions**
   - Bucket-based collision detection
   - O(n) instead of O(n²) enemy-to-cell checks

3. **Flow Fields instead of A*\**
   - Pre-computed pathfinding map
   - Supports 10,000+ enemies simultaneously

4. **BitSet Grid Storage**
   - Memory-efficient binary representation
   - Fast bit operations

### Technology Stack
- **Language**: Java 21 (Virtual Threads, Records)
- **Framework**: LibGDX 1.12.1 (Industry-standard Java game engine)
- **Build Tool**: Gradle
- **Rendering**: OpenGL via LWJGL3

## Building & Running

### Prerequisites
- Java 21+
- Gradle 8.5+

### Build
```bash
./gradlew build
```

### Run
```bash
./gradlew run
```

Or run directly:
```bash
./gradlew run --args="--full-screen=false"
```

## Controls

| Key | Action |
|-----|--------|
| **Left Click** | Paint a live cell (consumes heat) |
| **Right Click** | Erase a cell |
| **Space** | Spawn an enemy (testing) |
| **P** | Pause/Resume simulation |
| **C** | Clear grid |
| **R** | Spawn test glider at (30, 30) |
| **Up** | Zoom in |
| **Down** | Zoom out |
| **ESC** | Exit |

## Project Structure

```
src/main/java/com/lifedefense/
├── core/                    # Game loop, grid management
│   ├── GameLoop.java       # Decouples logic (10 TPS) from rendering (60 FPS)
│   └── GridManager.java    # BitSet-based grid with active cell tracking
├── simulation/             # Game of Life rules
│   ├── AutomataEngine.java # Life simulation + entropy
│   └── EntropySystem.java  # Random chaos for difficulty
├── entities/               # Game objects
│   ├── Entity.java        # Base entity class
│   ├── EnemyAgent.java    # Enemy with steering behaviors
│   ├── PatternFactory.java # Pre-defined Life patterns
│   └── Vector2f.java      # Float-based 2D vectors
├── systems/               # Game systems
│   ├── GameState.java     # Overall game state manager
│   ├── CollisionSystem.java # Spatial hashing collisions
│   └── FlowFieldSystem.java # Pre-computed pathfinding
├── ui/                    # Rendering
│   └── RenderSystem.java  # LibGDX rendering pipeline
├── utils/                 # Utilities
│   └── Vector2i.java      # Integer-based 2D vectors
└── config/                # Configuration
    └── GameConfig.java    # Game constants
```

## Key Algorithms

### 1. Active Cell Tracking (O(n) Game of Life)
Instead of checking all 1,000,000 cells in a 1000×1000 grid:
- Maintain a set of "active" cells (changed last tick)
- Only check active cells and their 8 neighbors
- Update typically runs in O(Living Cells × 9) time

**Resume Impact**: "Optimized Conway's Game of Life by 400% through active cell tracking"

### 2. Spatial Hashing (O(n) Collision Detection)
Instead of checking 5,000 cells × 200 enemies = 1,000,000 checks:
- Divide world into 50×50 pixel buckets
- Track which enemies are in which buckets
- Only check collisions for nearby enemies
- Typical cost: 50 checks instead of 1,000,000

### 3. Flow Fields (Zero-Cost Pathfinding)
Instead of calculating A\* for each of 10,000 enemies:
- Pre-compute a "flow map" (1 vector per cell)
- Each enemy looks at its tile and follows the arrow
- Regenerate occasionally when grid changes
- Cost: 1 regeneration vs 10,000 × A\* calculations

## Extensibility

Easy to add:
- **New patterns**: Add methods to `PatternFactory`
- **Enemy types**: Extend `EnemyAgent` with new behavior
- **Weapons**: Add to `CollisionSystem`
- **Difficulty waves**: Modify spawn rates and entropy

## Future Enhancements

- Save/load grid states as JSON
- Procedural level generation
- Leaderboards
- Multiplayer sandbox mode
- Custom pattern editor
- Tutorial mode with pre-built patterns
- Boss waves with special mechanics
- Power-ups that modify Game of Life rules

## License

This is an educational project demonstrating advanced game development concepts in Java.

## References

- Conway's Game of Life: https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life
- LibGDX Documentation: https://libgdx.com/dev/
- Entity Component Systems: https://en.wikipedia.org/wiki/Entity_component_system
- Spatial Hashing: https://gamedev.stackexchange.com/questions/29786/spatial-hash-grid-collision-detection
