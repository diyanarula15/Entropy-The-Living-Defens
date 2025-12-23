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

        // Entropy Bar (Top Right) - Renamed from Heat
        float entropyBarX = screenWidth - padding - barWidth;
        float entropyBarY = screenHeight - padding - barHeight;
        
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(entropyBarX, entropyBarY, barWidth, barHeight);
        
        // Entropy Foreground
        float entropyPercent = Math.min(1, gameState.getHeatLevel() / gameState.getMaxHeatLevel());
        if (gameState.isOverheated()) {
            shapeRenderer.setColor(Color.RED); // Flash red if overheated
        } else {
            shapeRenderer.setColor(Color.CYAN); // Mana color
        }
        shapeRenderer.rect(entropyBarX, entropyBarY, barWidth * entropyPercent, barHeight);

        shapeRenderer.end();

        // 2. Draw Text (SpriteBatch)
        batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0, 0, screenWidth, screenHeight));
        batch.begin();

        // Labels for bars
        font.setColor(Color.WHITE);
        font.draw(batch, "Base Health: " + (int)gameState.getBaseHealth(), padding + 5, screenHeight - padding - 5);
        font.draw(batch, "Entropy: " + (int)gameState.getHeatLevel(), entropyBarX + 5, entropyBarY + barHeight - 5);

        // Phase Indicator (Top Center)
        String phaseText = gameState.getCurrentPhase() == GameState.GamePhase.PLANNING ? "PLANNING (PAUSED)" : "SIMULATION (RUNNING)";
        font.getData().setScale(1.5f);
        if (gameState.getCurrentPhase() == GameState.GamePhase.PLANNING) font.setColor(Color.YELLOW);
        else font.setColor(Color.GREEN);
        
        // Center text
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, phaseText);
        font.draw(batch, phaseText, (screenWidth - layout.width) / 2, screenHeight - 20);
        font.getData().setScale(1.0f);
        font.setColor(Color.WHITE);

        // Pattern Toolbar (Bottom Center)
        String patternText = "Selected Pattern: " + gameState.getSelectedPattern().toString();
        String controlsText = "[1] Single  [2] Block  [3] Glider  [4] Spinner  |  [SPACE] Toggle Phase  |  [R-Click] Erase";
        
        layout.setText(font, patternText);
        font.draw(batch, patternText, (screenWidth - layout.width) / 2, 60);
        
        layout.setText(font, controlsText);
        font.draw(batch, controlsText, (screenWidth - layout.width) / 2, 30);

        // Other Stats (Top Left, below Health)
        font.draw(batch, "Wave: " + gameState.getWaveNumber(), padding, screenHeight - 60);
        font.draw(batch, "Enemies: " + gameState.getEnemies().size(), padding, screenHeight - 80);
        font.draw(batch, "FPS: " + currentFPS, padding, screenHeight - 100);

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
