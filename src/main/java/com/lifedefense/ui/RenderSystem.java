package com.lifedefense.ui;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.lifedefense.core.*;
import com.lifedefense.entities.*;
import com.lifedefense.simulation.AutomataEngine;
import com.lifedefense.systems.GameState;
import java.util.BitSet;

/**
 * Main rendering system using LibGDX.
 * Handles all drawing and camera management.
 */
public class RenderSystem {
    private static final float CELL_SIZE = 10.0f;
    private static final int LIVE_CELL_COLOR = 0x00FF00FF; // Green
    private static final int ENEMY_COLOR = 0xFF0000FF; // Red
    private static final int BASE_COLOR = 0x0000FFFF; // Blue
    
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final HUD hud;
    private final OrthographicCamera camera;
    
    private final GameState gameState;
    private final AutomataEngine automataEngine;
    
    private float zoom;
    private float cameraX;
    private float cameraY;

    public RenderSystem(GameState gameState, AutomataEngine automataEngine) {
        this.gameState = gameState;
        this.automataEngine = automataEngine;
        
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.hud = new HUD(gameState, batch, font, shapeRenderer);
        
        this.camera = new OrthographicCamera();
        this.zoom = 1.0f;
        this.cameraX = gameState.getBasePosition().x() * CELL_SIZE;
        this.cameraY = gameState.getBasePosition().y() * CELL_SIZE;
    }

    /**
     * Render the entire game frame.
     */
    private float overheatTimer = 0.0f;

    public void render(int screenWidth, int screenHeight, int currentFPS, int currentTPS) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Ensure camera viewport is updated
        if (camera.viewportWidth != screenWidth || camera.viewportHeight != screenHeight) {
            camera.setToOrtho(false, screenWidth, screenHeight);
        }
        updateCamera();

        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Render grid
        renderGrid();

        // Render live cells
        renderLiveCells();

        // Render enemies
        renderEnemies();

        // Render base
        renderBase();

        // Render cursor
        renderCursor();

        // Render HUD (screen-space overlay)
        hud.render(screenWidth, screenHeight, currentFPS, currentTPS);
        
        // Render Overheat Warning
        if (overheatTimer > 0) {
            overheatTimer -= Gdx.graphics.getDeltaTime();
            batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0, 0, screenWidth, screenHeight));
            batch.begin();
            font.setColor(1, 0, 0, 1);
            font.getData().setScale(2.0f);
            font.draw(batch, "OVERHEATED!", screenWidth / 2f - 80, screenHeight / 2f + 100);
            font.getData().setScale(1.0f);
            font.setColor(1, 1, 1, 1);
            batch.end();
        }
    }

    public void renderMenu(int width, int height) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0, 0, width, height));
        batch.begin();
        font.getData().setScale(2.0f);
        font.draw(batch, "LIFE DEFENSE", width / 2f - 100, height / 2f + 50);
        font.getData().setScale(1.0f);
        font.draw(batch, "Protect the Blue Base from Red Viruses!", width / 2f - 120, height / 2f);
        font.draw(batch, "Left Click: Paint Life (Wall) | Right Click: Erase", width / 2f - 150, height / 2f - 30);
        font.draw(batch, "Space: Spawn Wave | P: Pause", width / 2f - 100, height / 2f - 50);
        font.draw(batch, "Press ENTER to Start", width / 2f - 80, height / 2f - 100);
        batch.end();
    }

    public void renderPaused(int width, int height) {
        batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0, 0, width, height));
        batch.begin();
        font.getData().setScale(2.0f);
        font.draw(batch, "PAUSED", width / 2f - 50, height / 2f);
        font.getData().setScale(1.0f);
        font.draw(batch, "Press P to Resume", width / 2f - 60, height / 2f - 40);
        batch.end();
    }

    public void renderGameOver(int width, int height, int score) {
        batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0, 0, width, height));
        batch.begin();
        font.getData().setScale(2.0f);
        font.setColor(1, 0, 0, 1);
        font.draw(batch, "GAME OVER", width / 2f - 80, height / 2f + 50);
        font.setColor(1, 1, 1, 1);
        font.getData().setScale(1.0f);
        font.draw(batch, "Final Score: " + score, width / 2f - 60, height / 2f);
        font.draw(batch, "Press ENTER to Restart", width / 2f - 80, height / 2f - 40);
        batch.end();
    }

    private void updateCamera() {
        camera.position.set(cameraX, cameraY, 0);
        camera.zoom = zoom;
        camera.update();
    }

    private void renderGrid() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.5f);

        int gridWidth = gameState.getGrid().getWidth();
        int gridHeight = gameState.getGrid().getHeight();

        for (int x = 0; x <= gridWidth; x += 5) {
            shapeRenderer.line(x * CELL_SIZE, 0, x * CELL_SIZE, gridHeight * CELL_SIZE);
        }
        for (int y = 0; y <= gridHeight; y += 5) {
            shapeRenderer.line(0, y * CELL_SIZE, gridWidth * CELL_SIZE, y * CELL_SIZE);
        }

        shapeRenderer.end();
    }

    private void renderLiveCells() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        GridManager grid = gameState.getGrid();
        BitSet liveCells = grid.getLivingCellsBitSet();
        int width = grid.getWidth();
        long now = System.currentTimeMillis();

        // Iterate BitSet directly for GC-free rendering
        for (int i = liveCells.nextSetBit(0); i >= 0; i = liveCells.nextSetBit(i + 1)) {
            int x = i % width;
            int y = i / width;
            
            float worldX = x * CELL_SIZE;
            float worldY = y * CELL_SIZE;
            
            // Juice: Scale up based on birth time
            long birthTime = grid.getBirthTime(x, y);
            float age = (now - birthTime) / 200.0f; // 200ms animation
            float scale = Math.min(1.0f, age);
            
            // Center the scaling
            float size = CELL_SIZE * scale;
            float offset = (CELL_SIZE - size) / 2.0f;
            
            shapeRenderer.setColor(0.0f, 1.0f, 0.0f, 0.9f);
            shapeRenderer.rect(worldX + offset, worldY + offset, size, size);
        }

        shapeRenderer.end();
    }

    private void renderEnemies() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1.0f, 0.0f, 0.0f, 0.9f);

        for (EnemyAgent enemy : gameState.getEnemies()) {
            float x = enemy.getPosition().x;
            float y = enemy.getPosition().y;
            float radius = enemy.getRadius();
            shapeRenderer.circle(x, y, radius);

            // Draw health bar
            shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.8f);
            shapeRenderer.rect(x - radius, y - radius - 5, radius * 2, 3);
            
            float healthPercent = enemy.getHealth() / enemy.getMaxHealth();
            shapeRenderer.setColor(1.0f - healthPercent, healthPercent, 0.0f, 0.8f);
            shapeRenderer.rect(x - radius, y - radius - 5, radius * 2 * healthPercent, 3);
            
            shapeRenderer.setColor(1.0f, 0.0f, 0.0f, 0.9f);
        }

        shapeRenderer.end();
    }

    private void renderBase() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.0f, 0.0f, 1.0f, 0.7f);

        var basePos = gameState.getBasePosition();
        float x = basePos.x() * CELL_SIZE;
        float y = basePos.y() * CELL_SIZE;
        shapeRenderer.rect(x - CELL_SIZE, y - CELL_SIZE, CELL_SIZE * 2, CELL_SIZE * 2);

        shapeRenderer.end();
    }



    /**
     * Handle mouse input for painting.
     */
    public void handleMouseInput() {
        // Debug mouse position
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) || Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            Vector2 worldPos = screenToWorld(Gdx.input.getX(), Gdx.input.getY());
            int gridX = (int) (worldPos.x / CELL_SIZE);
            int gridY = (int) (worldPos.y / CELL_SIZE);
            
            // Debug mouse position removed
            
            boolean success = false;
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                success = gameState.paintCell(gridX, gridY, true);
            } else {
                success = gameState.paintCell(gridX, gridY, false);
            }
            
            if (!success) {
                // Show overheat feedback (could be a sound or visual)
                // Gdx.app.log("Input", "Action failed (Overheated?)");
                overheatTimer = 0.5f;
            }
        }
    }

    public void renderCursor() {
        Vector2 worldPos = screenToWorld(Gdx.input.getX(), Gdx.input.getY());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1.0f, 1.0f, 0.0f, 0.5f);
        shapeRenderer.circle(worldPos.x, worldPos.y, 5.0f);
        shapeRenderer.end();
    }

    /**
     * Handle keyboard input for controls.
     */
    public void handleKeyboardInput() {
        float moveSpeed = 500.0f * Gdx.graphics.getDeltaTime() * zoom;
        
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            cameraY += moveSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            cameraY -= moveSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            cameraX -= moveSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            cameraX += moveSpeed;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            Gdx.app.log("Input", "Space pressed - Spawning enemy");
            gameState.spawnEnemy();
        }
        
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            zoom *= 0.99f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            zoom /= 0.99f;
        }
        
        // Ensure camera stays within reasonable bounds
        // cameraX = Math.max(0, Math.min(gameState.getGrid().getWidth() * CELL_SIZE, cameraX));
        // cameraY = Math.max(0, Math.min(gameState.getGrid().getHeight() * CELL_SIZE, cameraY));
    }

    private Vector2 screenToWorld(int screenX, int screenY) {
        Vector3 vec3 = new Vector3(screenX, screenY, 0);
        camera.unproject(vec3);
        return new Vector2(vec3.x, vec3.y);
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = Math.max(0.1f, Math.min(10.0f, zoom));
    }

    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        hud.dispose();
    }
}