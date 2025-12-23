package com.lifedefense.ui;

import com.lifedefense.core.GridManager;
import com.lifedefense.entities.PatternFactory;
import com.lifedefense.utils.Vector2i;

/**
 * Input handler for pattern placement and sandbox controls.
 */
public class SandboxInputHandler {
    private final GridManager grid;
    
    private enum PatternType {
        BLINKER, BLOCK, GLIDER, LWSS, TOAD, BEACON, PULSAR, GLIDER_GUN
    }

    public SandboxInputHandler(GridManager grid) {
        this.grid = grid;
    }

    /**
     * Place a pattern at the given grid coordinates.
     */
    public void placePattern(PatternType type, int x, int y) {
        switch (type) {
            case BLINKER -> PatternFactory.createBlinker(grid, x, y);
            case BLOCK -> PatternFactory.createBlock(grid, x, y);
            case GLIDER -> PatternFactory.createGlider(grid, x, y);
            case LWSS -> PatternFactory.createLWSS(grid, x, y);
            case TOAD -> PatternFactory.createToad(grid, x, y);
            case BEACON -> PatternFactory.createBeacon(grid, x, y);
            case PULSAR -> PatternFactory.createPulsar(grid, x, y);
            case GLIDER_GUN -> PatternFactory.createSimpleGliderGun(grid, x, y);
        }
    }

    /**
     * Get pattern info for UI display.
     */
    public String getPatternInfo(PatternType type) {
        return switch (type) {
            case BLINKER -> "Blinker (Period 2)";
            case BLOCK -> "Block (Stable 2x2)";
            case GLIDER -> "Glider (Mobile)";
            case LWSS -> "Lightweight Spaceship";
            case TOAD -> "Toad (Period 2)";
            case BEACON -> "Beacon (Period 2)";
            case PULSAR -> "Pulsar (Period 3)";
            case GLIDER_GUN -> "Glider Gun";
        };
    }
}
