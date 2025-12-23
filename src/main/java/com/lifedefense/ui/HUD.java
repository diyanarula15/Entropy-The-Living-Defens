package com.lifedefense.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.lifedefense.systems.GameState;

/**
 * HUD (Heads-Up Display) for rendering game information.
 */
public class HUD {
    private final BitmapFont font;
    private final GameState gameState;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;

    public HUD(GameState gameState, SpriteBatch batch, BitmapFont font, ShapeRenderer shapeRenderer) {
        this.gameState = gameState;
        this.batch = batch;
        this.font = font;
        this.shapeRenderer = shapeRenderer;
    }

    /**
     * Render all HUD elements in screen space.
     */
    public void render(int screenWidth, int screenHeight, int currentFPS, int currentTPS) {
        // 1. Draw Bars (ShapeRenderer)
        boolean wasDrawingBatch = batch.isDrawing();
        if (wasDrawingBatch) batch.end();

        shapeRenderer.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0, 0, screenWidth, screenHeight));
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Health Bar (Top Left)
        float barWidth = 200;
        float barHeight = 20;
        float padding = 10;
        
        // Background
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(padding, screenHeight - padding - barHeight, barWidth, barHeight);
        
        // Health Foreground
        float healthPercent = Math.max(0, gameState.getBaseHealth() / gameState.getMaxBaseHealth());
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(padding, screenHeight - padding - barHeight, barWidth * healthPercent, barHeight);

        // Heat Bar (Top Left, below Health)
        float heatY = screenHeight - padding - barHeight - padding - barHeight;
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(padding, heatY, barWidth, barHeight);
        
        // Heat Foreground
        float heatPercent = Math.min(1, gameState.getHeatLevel() / gameState.getMaxHeatLevel());
        if (gameState.isOverheated()) {
            shapeRenderer.setColor(Color.RED); // Flash red if overheated
        } else {
            shapeRenderer.setColor(Color.ORANGE);
        }
        shapeRenderer.rect(padding, heatY, barWidth * heatPercent, barHeight);

        shapeRenderer.end();

        // 2. Draw Text (SpriteBatch)
        batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0, 0, screenWidth, screenHeight));
        batch.begin();

        // Labels for bars
        font.setColor(Color.WHITE);
        font.draw(batch, "Base Health: " + (int)gameState.getBaseHealth(), padding + 5, screenHeight - padding - 5);
        font.draw(batch, "Heat Level: " + (int)gameState.getHeatLevel(), padding + 5, heatY + barHeight - 5);

        // Other Stats (Top Right)
        float rightX = screenWidth - 150;
        float topY = screenHeight - 20;
        
        font.draw(batch, "Score: " + gameState.getScore(), rightX, topY);
        font.draw(batch, "Wave: " + gameState.getWaveNumber(), rightX, topY - 20);
        font.draw(batch, "Enemies: " + gameState.getEnemies().size(), rightX, topY - 40);
        font.draw(batch, "FPS: " + currentFPS, rightX, topY - 60);

        batch.end();

        // Restore batch state if needed
        if (wasDrawingBatch) batch.begin();
    }

    private void renderHUDBackground(int width, int height) {
        // Deprecated
    }

    private void renderStats(int width, int height, int fps, int tps) {
        // Deprecated
    }

    public void dispose() {
        font.dispose();
    }
}
