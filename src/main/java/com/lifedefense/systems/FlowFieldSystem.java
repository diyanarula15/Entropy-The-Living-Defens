package com.lifedefense.systems;

import com.badlogic.gdx.math.Vector2;
import com.lifedefense.core.GridManager;
import com.lifedefense.utils.Vector2i;
import java.util.*;

/**
 * Flow field pathfinding system.
 * Pre-computes a vector field guiding all enemies toward the base.
 * Enemies just follow the vector at their current tile—no per-enemy pathfinding needed.
 */
public class FlowFieldSystem {
    private final GridManager grid;
    // Data-oriented: separate arrays for X and Y components to avoid 1M Vector2 objects
    private float[][] flowX;
    private float[][] flowY;
    private Vector2i basePosition;
    
    private final float cellSize = 10.0f;
    
    // Reusable vector for queries
    private final Vector2 tmpVector = new Vector2();

    public FlowFieldSystem(GridManager grid, Vector2i basePosition) {
        this.grid = grid;
        this.basePosition = basePosition;
        this.flowX = new float[grid.getWidth()][grid.getHeight()];
        this.flowY = new float[grid.getWidth()][grid.getHeight()];
        generateFlowField();
    }

    /**
     * Generate a flow field using flood-fill outward from the base.
     * Each cell contains a vector pointing toward the base.
     */
    private void generateFlowField() {
        // Distance field: calculate shortest path distance to base
        int[][] distances = new int[grid.getWidth()][grid.getHeight()];
        for (int i = 0; i < grid.getWidth(); i++) {
            Arrays.fill(distances[i], Integer.MAX_VALUE);
        }

        // BFS from base
        Queue<Vector2i> queue = new LinkedList<>();
        queue.add(basePosition);
        distances[basePosition.x()][basePosition.y()] = 0;

        while (!queue.isEmpty()) {
            Vector2i current = queue.poll();
            int currentDist = distances[current.x()][current.y()];

            // Check all 8 neighbors
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0) continue;
                    
                    int nx = current.x() + dx;
                    int ny = current.y() + dy;

                    if (!grid.isInBounds(nx, ny)) continue;
                    
                    // Don't path through living cells (treat as obstacles)
                    if (grid.getCell(nx, ny)) continue;

                    int cost = (dx != 0 && dy != 0) ? 14 : 10; // Approx sqrt(2)*10
                    int newDist = currentDist + cost;
                    
                    if (newDist < distances[nx][ny]) {
                        distances[nx][ny] = newDist;
                        queue.add(new Vector2i(nx, ny));
                    }
                }
            }
        }

        // Build flow field: each cell points toward neighbor with lowest distance
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                calculateFlowVectorAtCell(x, y, distances);
            }
        }
    }

    /**
     * Calculate the flow vector for a single cell by finding the best neighbor.
     */
    private void calculateFlowVectorAtCell(int x, int y, int[][] distances) {
        if (distances[x][y] == Integer.MAX_VALUE) {
            flowX[x][y] = 0;
            flowY[x][y] = 0;
            return;
        }

        int bestDist = distances[x][y];
        float bestDx = 0;
        float bestDy = 0;

        // Find neighbor with lowest distance
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;

                int nx = x + dx;
                int ny = y + dy;
                
                if (grid.isInBounds(nx, ny)) {
                    if (distances[nx][ny] < bestDist) {
                        bestDist = distances[nx][ny];
                        bestDx = dx;
                        bestDy = dy;
                    }
                }
            }
        }
        
        // Normalize
        float len = (float) Math.sqrt(bestDx * bestDx + bestDy * bestDy);
        if (len > 0) {
            flowX[x][y] = bestDx / len;
            flowY[x][y] = bestDy / len;
        } else {
            flowX[x][y] = 0;
            flowY[x][y] = 0;
        }
    }

    /**
     * Get the flow vector at a specific world position.
     * Returns a shared Vector2 instance - do not store!
     */
    public Vector2 getFlowVector(Vector2 position) {
        int x = (int) (position.x / cellSize);
        int y = (int) (position.y / cellSize);
        
        if (grid.isInBounds(x, y)) {
            tmpVector.set(flowX[x][y], flowY[x][y]);
        } else {
            tmpVector.setZero();
        }
        return tmpVector;
    }

    public void regenerateFlowField() {
        generateFlowField();
    }
}
