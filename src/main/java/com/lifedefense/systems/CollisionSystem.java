package com.lifedefense.systems;

import com.badlogic.gdx.math.Vector2;
import com.lifedefense.entities.EnemyAgent;
import com.lifedefense.core.GridManager;
import java.util.*;

/**
 * Efficient collision detection between enemies and the grid.
 * Uses the grid itself as a spatial structure to avoid O(N^2) checks.
 */
public class CollisionSystem {
    private final GridManager grid;
    private final float cellDamage;
    private final float slowFactor;
    
    // Reusable vector to avoid GC
    private final Vector2 tmp = new Vector2();

    public CollisionSystem(GridManager grid) {
        this.grid = grid;
        this.cellDamage = 20.0f; // Damage per touch
        this.slowFactor = 0.7f; // Slow multiplier for stable blocks
    }

    /**
     * Check collisions between enemies and live cells.
     * Return list of enemies that should be removed.
     */
    public List<EnemyAgent> checkCollisions(List<EnemyAgent> enemies) {
        List<EnemyAgent> toRemove = new ArrayList<>();

        for (EnemyAgent enemy : enemies) {
            if (!enemy.isActive()) continue;

            Vector2 enemyPos = enemy.getPosition();
            float radius = enemy.getRadius();

            // Check cells around enemy position
            // Grid cells are 10x10.
            int cx = (int) (enemyPos.x / 10); 
            int cy = (int) (enemyPos.y / 10);

            // Check a 5x5 area around the enemy to be safe
            for (int dy = -2; dy <= 2; dy++) {
                for (int dx = -2; dx <= 2; dx++) {
                    int cellX = cx + dx;
                    int cellY = cy + dy;

                    if (grid.getCell(cellX, cellY)) {
                        // Calculate distance to cell center
                        float cellCenterX = cellX * 10 + 5;
                        float cellCenterY = cellY * 10 + 5;
                        
                        float dst2 = enemyPos.dst2(cellCenterX, cellCenterY);
                        float combinedRadius = radius + 5.0f; // Cell "radius" approx 5
                        
                        if (dst2 < combinedRadius * combinedRadius) {
                            enemy.takeDamage(cellDamage);
                            grid.setCell(cellX, cellY, false);

                            if (!enemy.isActive()) {
                                toRemove.add(enemy);
                                break; // Enemy dead, stop checking
                            }
                        }
                    }
                }
                if (!enemy.isActive()) break;
            }
        }
        return toRemove;
    }

    public float getCellDamage() {
        return cellDamage;
    }

    public float getSlowFactor() {
        return slowFactor;
    }
}

