# Quick Start Guide

## Installation & Running

### 1. Navigate to Project
```bash
cd /Users/apple/Desktop/game
```

### 2. Build the Project
```bash
./gradlew build
```

### 3. Run the Game
```bash
./gradlew run
```

The game window will open at 1280×960 resolution.

---

## First Time Playing

### Basic Strategy
1. **Pause (P)** the game to understand the grid
2. **Click and drag** to paint live cells (green squares)
3. **Watch them evolve** according to Game of Life rules
4. **Create patterns** to intercept enemies (red circles)
5. **Manage heat** - painting uses heat, heat regenerates slowly

### Essential Controls
- **Left Mouse**: Paint cells (costs heat)
- **Right Mouse**: Erase cells
- **P**: Pause/Unpause simulation
- **S**: Save grid as JSON
- **L**: Load grid from JSON
- **ESC**: Exit game

### Test Patterns (Press Key to Spawn)
- **R**: Glider (diagonal mover) at (30, 30)
- **G**: Glider Gun (spawns gliders) at (50, 50)
- **B**: Block (stable wall) at (80, 80)
- **C**: Clear entire grid

---

## Game Mechanics Explained

### The Game Loop
1. **Every 100ms** (1 Tick): Grid updates using Game of Life rules
2. **Every Frame** (60 FPS): Enemies move, check collisions, render

### Game of Life Rules (Every Tick)
- **Cell with 0-1 neighbors**: Dies (underpopulation)
- **Cell with 2-3 neighbors**: Survives
- **Cell with 4+ neighbors**: Dies (overcrowding)
- **Empty cell with 3 neighbors**: Becomes alive (reproduction)

### Combat
- **Enemy touches live cell**: Enemy takes 20 damage, cell dies
- **Enemy reaches base (blue square)**: Base loses 5 health
- **Base health reaches 0**: Game Over

### Heat System
- **Painting a cell**: +5 heat
- **Erasing a cell**: +2 heat
- **Max heat**: 100
- **Overheated**: Can't paint until heat cools down
- **Cooldown rate**: -5 heat per second

---

## Strategy Tips

### Defensive Positions
1. **Gliders**: Create diagonal walls of gliders to intercept enemies
2. **Blocks**: Use stable 2×2 blocks as fixed walls
3. **Blinkers**: Oscillating patterns can catch enemies in their sweep

### Efficient Patterns
- **Block (Stable 2×2)**: Most efficient wall, never changes
- **Blinker**: Creates moving wall with period 2
- **Glider**: Mobile intercept unit, moves toward enemies

### Advanced Tactics
1. **Flow Field Prediction**: Enemies follow arrows toward your base
2. **Chokepoints**: Build patterns at narrow passages
3. **Timing**: Blinkers and oscillators create moving hazards
4. **Combinations**: Chain patterns to create complex defenses

---

## Troubleshooting

### Game Runs Slowly
- **Solution**: Reduce number of enemies spawning
  - Wait longer between enemy spawns (naturally happens)
  - Clear grid if too many cells (Press C)

### Enemies Moving in Weird Paths
- **Reason**: They follow the flow field (vector field) pointing to base
- **Expected**: Enemies will navigate around your live cells

### Can't Paint New Cells
- **Reason**: You're overheated (heat bar is full)
- **Solution**: Wait 2-3 seconds for cooldown, or erase cells

### Grid Doesn't Seem to Update
- **Check 1**: Is the game paused? (Press P to unpause)
- **Check 2**: Watch the Generation counter in HUD (should increase)

---

## Keyboard Reference

```
GAME CONTROLS
─────────────────────────────
Paint Cell          Left Click
Erase Cell          Right Click
Pause/Resume        P
Clear Grid          C

TESTING SHORTCUTS
─────────────────────────────
Spawn Glider        R (at 30,30)
Spawn Gun           G (at 50,50)
Spawn Block         B (at 80,80)
Zoom In             Up Arrow
Zoom Out            Down Arrow

SAVE/LOAD
─────────────────────────────
Save Grid           S (to savegame.json)
Load Grid           L (from savegame.json)

EXIT
─────────────────────────────
Exit Game           ESC
```

---

## HUD Explanation

The HUD (top-left corner) shows:

```
Score: 500                          # Points from enemies killed
Base Health: 950 / 1000             # How long you can last
Heat: 23.4 / 100.0                  # Painting cost (regenerates)
Generation: 245 | Living Cells: 847 # Simulation state
Enemies: 5 | Killed: 23             # Enemy statistics
FPS: 60 | TPS: 10                   # Frame and Tick rates
```

---

## How to Save & Share

### Saving Your Grid
1. Create an awesome defensive pattern
2. Press **S** to save
3. File is saved as `savegame.json`

### Loading Your Grid
1. Place `savegame.json` in game directory
2. Press **L** to load
3. Your pattern is restored!

### Share with Friends
- Send them the `savegame.json` file
- They can put it in their game directory and load it

---

## Advanced Features

### Entropy System
Random mutations can be enabled for harder difficulty:
- Random cell deaths
- Random cell births
- Prevents finding "perfect" invincible patterns

*(Currently toggleable in code, not UI yet)*

### Flow Fields
Enemies don't use simple straight-line pathfinding. Instead:
1. The game pre-computes a **flow field** (vector field)
2. Each grid cell contains a vector pointing toward your base
3. Enemies just follow the arrow
4. Creates natural, efficient movement around obstacles

### Spatial Hashing
Collision detection is optimized:
- World divided into 50×50 pixel buckets
- Only enemies and cells in same bucket are checked
- Handles 10,000+ enemies efficiently

---

## Next Steps

1. **Try the Patterns**: Spawn R, G, B shortcuts
2. **Learn the Rules**: Read README.md for game mechanics
3. **Experiment**: Create your own patterns
4. **Save Victories**: Record your best defenses
5. **Read the Code**: See DEVELOPMENT.md for architecture

---

## Performance Expectations

### Typical Performance
- **60 FPS**: Consistent rendering
- **10 TPS**: Consistent Game of Life ticks
- **200 Enemies**: Smooth gameplay
- **10,000+ Cells**: No lag

### Stress Test (Max Performance)
- **Full 200×150 grid** (~30,000 cells)
- **200 enemies** simultaneously
- **Still 60 FPS** with all systems enabled

---

## Tips for Best Experience

1. **Start Simple**: Create basic patterns before complex ones
2. **Use Pause**: Press P to plan your defenses
3. **Monitor Heat**: Keep an eye on heat bar
4. **Watch Generation Counter**: See simulation speed
5. **Save Often**: Backup your awesome patterns

---

## Need Help?

- **Game Mechanics**: See README.md
- **Architecture**: See DEVELOPMENT.md
- **API Details**: See API_REFERENCE.md
- **Code Examples**: Look at PatternFactory.java

---

**Happy Defending!** 🎮✨

Build impossible patterns, defend against the horde, and master the Game of Life!
