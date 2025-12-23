package com.lifedefense.systems;

import com.badlogic.gdx.math.Vector2;
import com.lifedefense.entities.EnemyAgent;
import com.lifedefense.core.GridManager;
import com.lifedefense.utils.Vector2i;
import java.util.*;

/**
 * Manages all game entities and overall game state.
 */
public class GameState {
    private final GridManager grid;
    private final CollisionSystem collisionSystem;
    private final FlowFieldSystem flowFieldSystem;
    
    public enum GamePhase {
        PLANNING,
        SIMULATION
    }
    
    public enum PatternType {
        SINGLE,
        BLOCK,
        GLIDER,
        SPINNER
    }

    private GamePhase currentPhase;
    private PatternType selectedPattern;
    
    private final List<EnemyAgent> enemies;
    private Vector2i basePosition;
    private Vector2i spawnPosition;
    
    private float baseHealth;
    private final float maxBaseHealth = 1000.0f;
    
    private float heatLevel;
    private final float maxHeatLevel = 1000.0f; // Increased from 100
    private final float heatCooldownRate = 100.0f; // Increased from 5.0
    
    private int score;
    private int waveNumber;
    private int enemiesKilled;

    public GameState(GridManager grid, Vector2i basePosition, Vector2i spawnPosition) {
        this.grid = grid;
        this.basePosition = basePosition;
        this.spawnPosition = spawnPosition;
        
        this.collisionSystem = new CollisionSystem(grid);
        this.flowFieldSystem = new FlowFieldSystem(grid, basePosition);
        
        this.currentPhase = GamePhase.PLANNING;
        this.selectedPattern = PatternType.SINGLE;
        
        this.enemies = new ArrayList<>();
        this.baseHealth = maxBaseHealth;
        this.heatLevel = 0;
        this.score = 0;
        this.waveNumber = 1;
        this.enemiesKilled = 0;
    }

    /**
     * Update all game entities.
     */
    public void updateEntities(float deltaTime) {
        // Update enemies
        for (EnemyAgent enemy : enemies) {
            if (enemy.isActive()) {
                // Apply flow field steering
                Vector2 flow = flowFieldSystem.getFlowVector(enemy.getPosition());
                if (flow.len2() > 0) {
                    // Set velocity based on flow direction
                    enemy.getVelocity().set(flow).scl(enemy.getMaxSpeed());
                }
                enemy.update(deltaTime);
                
                // Check if just reached target
                if (enemy.hasReachedTarget() && enemy.isActive()) {
                    baseHealth -= 5.0f;
                    enemy.setActive(false); // Mark for removal
                }
            }
        }

        // Check collisions
        List<EnemyAgent> toRemove = collisionSystem.checkCollisions(enemies);
        for (EnemyAgent enemy : toRemove) {
            // Only give score if it wasn't already dead/reached
            if (enemy.isActive()) {
                enemy.setActive(false);
                enemiesKilled++;
                score += 10;
            }
        }

        // Remove dead/inactive enemies
        enemies.removeIf(e -> !e.isActive());

        // Cool down heat
        heatLevel = Math.max(0, heatLevel - (heatCooldownRate * deltaTime));
    }

    /**
     * Spawn an enemy at a random edge position.
     */
    public void spawnEnemy() {
        // Pick a random edge: 0=Top, 1=Right, 2=Bottom, 3=Left
        int edge = (int)(Math.random() * 4);
        float x = 0, y = 0;
        int w = grid.getWidth();
        int h = grid.getHeight();
        
        switch (edge) {
            case 0: // Top
                x = (float)(Math.random() * w);
                y = h - 1;
                break;
            case 1: // Right
                x = w - 1;
                y = (float)(Math.random() * h);
                break;
            case 2: // Bottom
                x = (float)(Math.random() * w);
                y = 0;
                break;
            case 3: // Left
                x = 0;
                y = (float)(Math.random() * h);
                break;
        }

        EnemyAgent enemy = new EnemyAgent(
                new Vector2(x * 10, y * 10), // Convert grid to world
                new Vector2(basePosition.x() * 10, basePosition.y() * 10)
        );
        enemies.add(enemy);
    }

    /**
     * Paint a cell onto the grid. Consumes heat.
     */
    public boolean paintCell(int x, int y, boolean alive) {
        float heatCost = alive ? 2.0f : 0.5f; // Reduced cost (was 5.0/2.0)
        
        if (heatLevel + heatCost > maxHeatLevel) {
            return false; // Overheated, can't paint
        }

        // Only charge heat if the cell actually changed
        boolean changed = false;
        if (alive && !grid.getCell(x, y)) {
            grid.setCell(x, y, true);
            changed = true;
        } else if (!alive && grid.getCell(x, y)) {
            grid.setCell(x, y, false);
            changed = true;
        }

        if (changed) {
            heatLevel += heatCost;
            flowFieldSystem.regenerateFlowField();
        }
        return true;
    }

    /**
     * Check if game is over (base health <= 0).
     */
    public boolean isGameOver() {
        return baseHealth <= 0;
    }

    /**
     * Increment the wave number.
     */
    public void incrementWave() {
        waveNumber++;
    }

    public void setPhase(GamePhase phase) {
        this.currentPhase = phase;
    }

    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    public void setSelectedPattern(PatternType pattern) {
        this.selectedPattern = pattern;
    }

    public PatternType getSelectedPattern() {
        return selectedPattern;
    }

    /**
     * Apply the selected pattern at the given coordinates.
     */
    public boolean applyPattern(int x, int y) {
        // Calculate cost based on pattern
        float cost = 0;
        switch (selectedPattern) {
            case SINGLE: cost = 2.0f; break;
            case BLOCK: cost = 8.0f; break;
            case GLIDER: cost = 10.0f; break;
            case SPINNER: cost = 6.0f; break;
        }

        if (heatLevel + cost > maxHeatLevel) {
            return false;
        }

        boolean success = false;
        switch (selectedPattern) {
            case SINGLE:
                success = paintSingle(x, y);
                break;
            case BLOCK:
                success = paintBlock(x, y);
                break;
            case GLIDER:
                success = paintGlider(x, y);
                break;
            case SPINNER:
                success = paintSpinner(x, y);
                break;
        }

        if (success) {
            heatLevel += cost;
            flowFieldSystem.regenerateFlowField();
        }
        return success;
    }

    private boolean paintSingle(int x, int y) {
        if (!grid.getCell(x, y)) {
            grid.setCell(x, y, true);
            return true;
        }
        return false;
    }

    private boolean paintBlock(int x, int y) {
        // 2x2 block
        boolean changed = false;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                if (!grid.getCell(x + i, y + j)) {
                    grid.setCell(x + i, y + j, true);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean paintGlider(int x, int y) {
        // Standard glider facing bottom-right
        // . O .
        // . . O
        // O O O
        int[][] shape = {
            {0, 1, 0},
            {0, 0, 1},
            {1, 1, 1}
        };
        return paintShape(x, y, shape);
    }

    private boolean paintSpinner(int x, int y) {
        // Blinker (vertical line of 3)
        int[][] shape = {
            {0, 1, 0},
            {0, 1, 0},
            {0, 1, 0}
        };
        return paintShape(x, y, shape);
    }

    private boolean paintShape(int x, int y, int[][] shape) {
        boolean changed = false;
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    if (!grid.getCell(x + j, y + (shape.length - 1 - i))) { // Flip Y for grid coords
                        grid.setCell(x + j, y + (shape.length - 1 - i), true);
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    // ===== Getters =====
    public GridManager getGrid() {
        return grid;
    }

    public List<EnemyAgent> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }

    public float getBaseHealth() {
        return baseHealth;
    }

    public float getMaxBaseHealth() {
        return maxBaseHealth;
    }

    public float getHeatLevel() {
        return heatLevel;
    }

    public float getMaxHeatLevel() {
        return maxHeatLevel;
    }

    public boolean isOverheated() {
        return heatLevel >= maxHeatLevel;
    }

    public int getScore() {
        return score;
    }

    public int getWaveNumber() {
        return waveNumber;
    }

    public int getEnemiesKilled() {
        return enemiesKilled;
    }

    public Vector2i getBasePosition() {
        return basePosition;
    }

    public Vector2i getSpawnPosition() {
        return spawnPosition;
    }

    public CollisionSystem getCollisionSystem() {
        return collisionSystem;
    }

    public FlowFieldSystem getFlowFieldSystem() {
        return flowFieldSystem;
    }
}

