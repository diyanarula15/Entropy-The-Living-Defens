package com.lifedefense.config;

/**
 * Game configuration and constants.
 */
public class GameConfig {
    // Grid settings
    public static final int GRID_WIDTH = 200;
    public static final int GRID_HEIGHT = 150;
    
    // Rendering
    public static final float CELL_SIZE = 10.0f;
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 960;
    public static final int TARGET_FPS = 60;
    
    // Simulation
    public static final float TICK_DURATION = 0.1f; // 100ms per tick
    public static final int TICKS_PER_SECOND = 10;
    
    // Enemy spawning
    public static final int SPAWN_INTERVAL = 60; // frames
    public static final float ENEMY_MAX_SPEED = 50.0f;
    public static final float ENEMY_RADIUS = 5.0f;
    
    // Combat
    public static final float CELL_DAMAGE = 20.0f;
    public static final float SLOW_FACTOR = 0.7f;
    
    // Heat/Mana system
    public static final float MAX_HEAT = 100.0f;
    public static final float HEAT_COOLDOWN_RATE = 5.0f; // per second
    public static final float PAINT_HEAT_COST = 5.0f;
    public static final float ERASE_HEAT_COST = 2.0f;
    
    // Base
    public static final float MAX_BASE_HEALTH = 1000.0f;
    public static final float ENEMY_REACH_BASE_DAMAGE = 5.0f;
    
    // Difficulty
    public static final float ENTROPY_MUTATION_RATE = 0.001f;
    public static final float ENTROPY_DEATH_RATE = 0.002f;
    public static final float ENTROPY_BIRTH_RATE = 0.0005f;
}
