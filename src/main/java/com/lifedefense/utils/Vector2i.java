package com.lifedefense.utils;

/**
 * Simple immutable 2D integer vector for grid coordinates.
 */
public record Vector2i(int x, int y) {
    public Vector2i {
        // Record constructor validation
    }

    public static Vector2i of(int x, int y) {
        return new Vector2i(x, y);
    }

    public Vector2i add(int dx, int dy) {
        return new Vector2i(x + dx, y + dy);
    }

    public Vector2i add(Vector2i other) {
        return new Vector2i(x + other.x, y + other.y);
    }

    public long toIndex(int width) {
        return (long) y * width + x;
    }

    public static Vector2i fromIndex(long index, int width) {
        return new Vector2i((int) (index % width), (int) (index / width));
    }

    @Override
    public String toString() {
        return "(%d, %d)".formatted(x, y);
    }
}
