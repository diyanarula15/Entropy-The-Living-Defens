package com.lifedefense;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.lifedefense.core.*;
import com.lifedefense.simulation.AutomataEngine;
import com.lifedefense.systems.GameState;
import com.lifedefense.ui.RenderSystem;
import com.lifedefense.utils.Vector2i;
import com.lifedefense.utils.SerializationUtils;

/**
 * Main game class - entry point for LibGDX application.
 */
public class LifeDefenseGame extends ApplicationAdapter {
    private static final int GRID_WIDTH = 200;
    private static final int GRID_HEIGHT = 150;
    
    private GridManager grid;
    private GameLoop gameLoop;
    private AutomataEngine automataEngine;
    private GameState gameState;
    private RenderSystem renderSystem;
    
    private float spawnTimer;
    private float currentSpawnInterval;
    private float difficultyTimer;
    
    private int frameCount;
    private long lastSecondTime;
    private int currentFPS;

    public enum State { MENU, PLAYING, PAUSED, GAME_OVER }
    private State currentState = State.MENU;

    @Override
    public void create() {
        // Initialize core systems
        grid = new GridManager(GRID_WIDTH, GRID_HEIGHT);
        gameLoop = new GameLoop();
        automataEngine = new AutomataEngine(grid);
        
        // Game state
        Vector2i basePosition = new Vector2i(GRID_WIDTH / 2, GRID_HEIGHT / 2);
        Vector2i spawnPosition = new Vector2i(10, GRID_HEIGHT / 2);
        gameState = new GameState(grid, basePosition, spawnPosition);
        
        // Rendering
        if (renderSystem != null) renderSystem.dispose(); // Clean up old system
        renderSystem = new RenderSystem(gameState, automataEngine);
        
        spawnTimer = 0;
        currentSpawnInterval = 1.0f; // Start with 1 enemy per second
        difficultyTimer = 0;
        
        frameCount = 0;
        lastSecondTime = System.currentTimeMillis();
        currentFPS = 60;
        
        Gdx.app.log("LifeDefense", "Game initialized: " + GRID_WIDTH + "x" + GRID_HEIGHT);
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        frameCount++;
        
        // Track FPS
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSecondTime >= 1000) {
            currentFPS = frameCount;
            frameCount = 0;
            lastSecondTime = currentTime;
        }

        switch (currentState) {
            case MENU:
                renderSystem.renderMenu(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    Gdx.app.log("LifeDefense", "Starting Game...");
                    create(); // Reset game
                    currentState = State.PLAYING;
                }
                break;

            case PLAYING:
                // Handle input
                renderSystem.handleMouseInput();
                renderSystem.handleKeyboardInput();
                
                // Handle keyboard shortcuts
                if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                    currentState = State.PAUSED;
                }
                
                // Toggle Phase (Spacebar)
                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    if (gameState.getCurrentPhase() == GameState.GamePhase.PLANNING) {
                        gameState.setPhase(GameState.GamePhase.SIMULATION);
                    } else {
                        gameState.setPhase(GameState.GamePhase.PLANNING);
                    }
                }
                
                // Pattern Selection
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) gameState.setSelectedPattern(GameState.PatternType.SINGLE);
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) gameState.setSelectedPattern(GameState.PatternType.BLOCK);
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) gameState.setSelectedPattern(GameState.PatternType.GLIDER);
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) gameState.setSelectedPattern(GameState.PatternType.SPINNER);

                // Update game loop
                if (gameState.getCurrentPhase() == GameState.GamePhase.SIMULATION) {
                    if (gameLoop.update(deltaTime)) {
                        // Simulation tick (100ms)
                        automataEngine.tick();
                        gameState.getFlowFieldSystem().regenerateFlowField();
                    }

                    // Update game entities every frame
                    gameState.updateEntities(deltaTime);

                    // Dynamic Spawning Logic
                    spawnTimer += deltaTime;
                    if (spawnTimer >= currentSpawnInterval && gameState.getEnemies().size() < 500) {
                        gameState.spawnEnemy();
                        spawnTimer = 0;
                    }
                    
                    // Increase difficulty over time
                    difficultyTimer += deltaTime;
                    if (difficultyTimer >= 10.0f) { // Every 10 seconds
                        difficultyTimer = 0;
                        currentSpawnInterval = Math.max(0.1f, currentSpawnInterval * 0.9f); // 10% faster spawns
                        gameState.incrementWave();
                        Gdx.app.log("LifeDefense", "Wave " + gameState.getWaveNumber() + "! Spawn Interval: " + currentSpawnInterval);
                    }
                }

                // Render
                renderSystem.render(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 
                                   currentFPS, gameLoop.getCurrentTPS());

                // Check game over
                if (gameState.isGameOver()) {
                    currentState = State.GAME_OVER;
                }
                break;

            case PAUSED:
                renderSystem.render(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), currentFPS, gameLoop.getCurrentTPS());
                renderSystem.renderPaused(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                    currentState = State.PLAYING;
                }
                break;

            case GAME_OVER:
                renderSystem.render(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), currentFPS, gameLoop.getCurrentTPS());
                renderSystem.renderGameOver(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), gameState.getScore());
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    create(); // Reset game
                    currentState = State.PLAYING;
                }
                break;
        }
    }

    private void handleShortcuts() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        
        // Debug/Sandbox keys
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            grid.clear();
            Gdx.app.log("LifeDefense", "Grid cleared");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            // Spawn a test glider
            com.lifedefense.entities.PatternFactory.createGlider(grid, 30, 30);
            Gdx.app.log("LifeDefense", "Glider spawned at (30, 30)");
        }
        
        // Save/Load
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            try {
                SerializationUtils.saveGrid(grid, "savegame.json");
                Gdx.app.log("LifeDefense", "Grid saved to savegame.json");
            } catch (Exception e) {
                Gdx.app.error("LifeDefense", "Save failed: " + e.getMessage());
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            try {
                SerializationUtils.loadGrid(grid, "savegame.json");
                gameState.getFlowFieldSystem().regenerateFlowField();
                Gdx.app.log("LifeDefense", "Grid loaded from savegame.json");
            } catch (Exception e) {
                Gdx.app.error("LifeDefense", "Load failed: " + e.getMessage());
            }
        }
    }

    @Override
    public void dispose() {
        if (renderSystem != null) renderSystem.dispose();
    }

    @Override
    public void resize(int width, int height) {
        // Handle window resize
    }

    public static void main(String[] args) {
        com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration config =
                new com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(1280, 960);
        config.setTitle("Life Defense - Conway's Game of Life Tower Defense");
        config.setForegroundFPS(60);
        new com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application(new LifeDefenseGame(), config);
    }
}
