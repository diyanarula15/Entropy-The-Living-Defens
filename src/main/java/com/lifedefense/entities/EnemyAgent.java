package com.lifedefense.entities;

import com.badlogic.gdx.math.Vector2;

/**
 * An enemy unit that spawns at point A and tries to reach point B.
 * Uses steering behaviors to navigate around obstacles (Live Cells).
 */
public class EnemyAgent extends Entity {
    private final Vector2 targetPosition;
    private final float maxSpeed;
    private final float radius;
    
    private float health;
    private final float maxHealth;
    private boolean reachedTarget;

    // Pre-allocated temp vector for calculations to avoid GC
    private final Vector2 tmp = new Vector2();

    public EnemyAgent(Vector2 startPosition, Vector2 targetPosition) {
        super(startPosition);
        this.targetPosition = targetPosition;
        this.maxSpeed = 50.0f; // pixels per second
        this.radius = 5.0f;
        this.maxHealth = 100.0f;
        this.health = maxHealth;
        this.reachedTarget = false;
    }

    @Override
    public void update(float deltaTime) {
        if (!active || reachedTarget) return;

        // Velocity is set by GameState (FlowFieldSystem)
        // Just update position
        // position += velocity * deltaTime
        tmp.set(velocity).scl(deltaTime);
        position.add(tmp);

        // Check if reached target
        if (position.dst(targetPosition) < radius) {
            reachedTarget = true;
            // Do not set active = false here; let GameState handle the logic
        }
    }

    /**
     * Take damage from touching a live cell.
     */
    public void takeDamage(float damage) {
        health -= damage;
        if (health <= 0) {
            active = false;
        }
    }

    /**
     * Apply a steering force to avoid obstacles.
     */
    public void addForce(Vector2 force) {
        velocity.add(force);
        // Clamp velocity to max speed
        if (velocity.len() > maxSpeed) {
            velocity.nor().scl(maxSpeed);
        }
    }

    /**
     * Slow down the enemy (e.g., when passing through stable blocks).
     */
    public void slow(float slowFactor) {
        velocity.scl(slowFactor);
    }

    @Override
    public float getRadius() {
        return radius;
    }

    // ===== Getters =====
    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public Vector2 getTargetPosition() {
        return targetPosition;
    }

    public boolean hasReachedTarget() {
        return reachedTarget;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }
}
