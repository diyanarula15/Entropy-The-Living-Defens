package com.lifedefense.entities;

import com.badlogic.gdx.math.Vector2;

/**
 * Represents an entity in the game world.
 * Uses a simple ECS-inspired component pattern.
 */
public abstract class Entity {
    protected Vector2 position;
    protected Vector2 velocity;
    protected boolean active;
    protected long entityId;
    
    private static long nextId = 0;

    public Entity(Vector2 position) {
        this.position = position;
        this.velocity = new Vector2(0, 0);
        this.active = true;
        this.entityId = nextId++;
    }

    /**
     * Update entity logic.
     */
    public abstract void update(float deltaTime);

    /**
     * Get the radius/size of the entity for collision purposes.
     */
    public abstract float getRadius();

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getEntityId() {
        return entityId;
    }
}
