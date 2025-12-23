package com.lifedefense.core;

/**
 * Decouples game logic ticks from rendering frames.
 * Logic runs at a fixed 10 ticks/sec (100ms per tick).
 * Rendering can run at 60 FPS independently.
 */
public class GameLoop {
    private static final float TICK_DURATION = 0.1f; // 100ms per tick
    
    private float accumulator;
    private long lastTickTime;
    private int ticksSinceLastSecond;
    private float timeSinceLastSecond;
    private int currentTPS;
    
    private boolean paused;

    public GameLoop() {
        this.accumulator = 0;
        this.lastTickTime = System.nanoTime();
        this.ticksSinceLastSecond = 0;
        this.timeSinceLastSecond = 0;
        this.currentTPS = 0;
        this.paused = false;
    }

    /**
     * Update game loop. Returns true if a simulation tick should occur.
     */
    public boolean update(float deltaTime) {
        if (paused) {
            lastTickTime = System.nanoTime();
            return false;
        }

        accumulator += deltaTime;
        timeSinceLastSecond += deltaTime;

        if (timeSinceLastSecond >= 1.0f) {
            currentTPS = ticksSinceLastSecond;
            ticksSinceLastSecond = 0;
            timeSinceLastSecond = 0;
        }

        if (accumulator >= TICK_DURATION) {
            accumulator -= TICK_DURATION;
            ticksSinceLastSecond++;
            return true;
        }

        return false;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (!paused) {
            lastTickTime = System.nanoTime();
            accumulator = 0;
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public int getCurrentTPS() {
        return currentTPS;
    }

    public float getTickDuration() {
        return TICK_DURATION;
    }
}
