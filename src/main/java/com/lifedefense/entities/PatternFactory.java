package com.lifedefense.entities;

import com.lifedefense.utils.Vector2i;
import com.lifedefense.core.GridManager;

/**
 * Factory for creating pre-defined "tower" patterns.
 * Examples: Glider, Blinker, Block (stable 2x2), Pulsar, Spaceship.
 */
public class PatternFactory {
    
    /**
     * Simple oscillator - period 2.
     * Alternates between horizontal and vertical.
     */
    public static void createBlinker(GridManager grid, int x, int y) {
        grid.setCell(x, y, true);
        grid.setCell(x + 1, y, true);
        grid.setCell(x + 2, y, true);
    }

    /**
     * Stable 2x2 block. Never changes, acts as a wall.
     */
    public static void createBlock(GridManager grid, int x, int y) {
        grid.setCell(x, y, true);
        grid.setCell(x + 1, y, true);
        grid.setCell(x, y + 1, true);
        grid.setCell(x + 1, y + 1, true);
    }

    /**
     * Glider - the most famous pattern. Moves diagonally.
     */
    public static void createGlider(GridManager grid, int x, int y) {
        grid.setCell(x, y, true);
        grid.setCell(x + 2, y, true);
        grid.setCell(x + 1, y + 1, true);
        grid.setCell(x + 2, y + 1, true);
        grid.setCell(x + 1, y + 2, true);
    }

    /**
     * Lightweight spaceship (LWSS) - moves horizontally.
     */
    public static void createLWSS(GridManager grid, int x, int y) {
        // Row 0
        grid.setCell(x + 1, y, true);
        grid.setCell(x + 4, y, true);
        // Row 1
        grid.setCell(x, y + 1, true);
        // Row 2
        grid.setCell(x, y + 2, true);
        grid.setCell(x + 1, y + 2, true);
        grid.setCell(x + 2, y + 2, true);
        grid.setCell(x + 3, y + 2, true);
    }

    /**
     * Toad - period-2 oscillator.
     */
    public static void createToad(GridManager grid, int x, int y) {
        // Row 0
        grid.setCell(x + 1, y, true);
        grid.setCell(x + 2, y, true);
        grid.setCell(x + 3, y, true);
        // Row 1
        grid.setCell(x, y + 1, true);
        grid.setCell(x + 1, y + 1, true);
        grid.setCell(x + 2, y + 1, true);
    }

    /**
     * Beacon - period-2 oscillator (stable 2x2s with one cell inverted).
     */
    public static void createBeacon(GridManager grid, int x, int y) {
        grid.setCell(x, y, true);
        grid.setCell(x + 1, y, true);
        grid.setCell(x, y + 1, true);
        grid.setCell(x + 3, y + 2, true);
        grid.setCell(x + 2, y + 3, true);
        grid.setCell(x + 3, y + 3, true);
    }

    /**
     * Pulsar - period-3 oscillator, large.
     */
    public static void createPulsar(GridManager grid, int x, int y) {
        // Top and bottom rows of cells
        for (int i = 0; i < 4; i++) {
            grid.setCell(x + 2 + i, y, true);
            grid.setCell(x + 2 + i, y + 5, true);
            grid.setCell(x, y + 2 + i, true);
            grid.setCell(x + 5, y + 2 + i, true);
        }
    }

    /**
     * Glider gun (simple version) - produces gliders periodically.
     * This is a larger, more complex pattern.
     */
    public static void createSimpleGliderGun(GridManager grid, int x, int y) {
        // Left square
        createBlock(grid, x, y);
        
        // Left oscillator
        createBlinker(grid, x + 5, y + 1);
        
        // Middle structures
        grid.setCell(x + 10, y, true);
        grid.setCell(x + 10, y + 1, true);
        grid.setCell(x + 11, y + 1, true);
        
        // Right spaceship launcher
        grid.setCell(x + 12, y + 1, true);
        grid.setCell(x + 13, y, true);
        grid.setCell(x + 13, y + 2, true);
        grid.setCell(x + 14, y, true);
    }
}
