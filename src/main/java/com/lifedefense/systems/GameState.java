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

