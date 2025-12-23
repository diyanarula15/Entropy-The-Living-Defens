package com.lifedefense.simulation;

import com.lifedefense.core.GridManager;
import com.lifedefense.utils.Vector2i;
import java.util.Random;

/**
 * Introduces chaos into the simulation to prevent "perfect" invincible patterns.
 * This adds difficulty and unpredictability.
 */
public class EntropySystem {
    private final GridManager grid;
    private final Random random;
    
    private float mutationRate; // Probability of random cell flip per tick (0.0 - 1.0)
    private float cellDeathRate; // Probability of killing a living cell
    private float cellBirthRate; // Probability of random birth

    public EntropySystem(GridManager grid) {
        this.grid = grid;
        this.random = new Random();
        this.mutationRate = 0.001f;
        this.cellDeathRate = 0.002f;
        this.cellBirthRate = 0.0005f;
    }

    /**
     * Apply entropy mutations to the current generation.
     * Called before the standard Game of Life update.
     */
    public void applyEntropy() {
        // Random cell deaths
        var livingCells = grid.getAllLivingCells();
        for (Vector2i cell : livingCells) {
            if (random.nextFloat() < cellDeathRate) {
                grid.setCell(cell.x(), cell.y(), false);
            }
        }

        // Random births in random locations
        for (int i = 0; i < grid.getLivingCellCount() / 100; i++) {
            int x = random.nextInt(grid.getWidth());
            int y = random.nextInt(grid.getHeight());
            if (!grid.getCell(x, y) && random.nextFloat() < cellBirthRate) {
                grid.setCell(x, y, true);
            }
        }
    }

    // ===== Configuration =====
    public void setMutationRate(float rate) {
        this.mutationRate = Math.max(0, Math.min(1, rate));
    }

    public void setCellDeathRate(float rate) {
        this.cellDeathRate = Math.max(0, Math.min(1, rate));
    }

    public void setCellBirthRate(float rate) {
        this.cellBirthRate = Math.max(0, Math.min(1, rate));
    }

    public float getMutationRate() {
        return mutationRate;
    }

    public float getCellDeathRate() {
        return cellDeathRate;
    }

    public float getCellBirthRate() {
        return cellBirthRate;
    }
}
