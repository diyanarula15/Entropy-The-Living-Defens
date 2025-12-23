package com.lifedefense.simulation;

import com.lifedefense.core.GridManager;
import java.util.Random;

/**
 * Runs the Game of Life simulation with optional entropy system.
 * The entropy system introduces randomness to prevent "perfect" patterns.
 */
public class AutomataEngine {
    private final GridManager grid;
    private final EntropySystem entropySystem;
    private final Random random;
    
    private boolean entropyEnabled;

    public AutomataEngine(GridManager grid) {
        this.grid = grid;
        this.entropySystem = new EntropySystem(grid);
        this.random = new Random();
        this.entropyEnabled = false;
    }

    /**
     * Execute one generation update with optional entropy.
     */
    public void tick() {
        // Apply entropy mutations (optional chaos)
        if (entropyEnabled) {
            entropySystem.applyEntropy();
        }

        // Standard Game of Life update
        grid.updateGeneration();
    }

    /**
     * Enable/disable entropy system for difficulty scaling.
     */
    public void setEntropyEnabled(boolean enabled) {
        this.entropyEnabled = enabled;
    }

    public boolean isEntropyEnabled() {
        return entropyEnabled;
    }

    public EntropySystem getEntropySystem() {
        return entropySystem;
    }

    public GridManager getGrid() {
        return grid;
    }
}
