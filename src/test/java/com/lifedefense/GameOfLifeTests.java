package com.lifedefense;

import com.lifedefense.core.GridManager;
import com.lifedefense.entities.PatternFactory;
import com.lifedefense.utils.Vector2i;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for core Game of Life mechanics.
 */
public class GameOfLifeTests {
    private GridManager grid;

    @BeforeEach
    public void setUp() {
        grid = new GridManager(100, 100);
    }

    @Test
    public void testBlinkerOscillation() {
        // Create a blinker pattern
        PatternFactory.createBlinker(grid, 50, 50);
        
        // Should have 3 cells
        assertEquals(3, grid.getLivingCellCount());
        
        // After one generation, shape should rotate
        grid.updateGeneration();
        assertEquals(3, grid.getLivingCellCount(), "Blinker should maintain 3 cells");
        
        // After another generation, should return to original
        grid.updateGeneration();
        assertEquals(3, grid.getLivingCellCount());
    }

    @Test
    public void testBlockStability() {
        // Create a stable 2x2 block
        PatternFactory.createBlock(grid, 50, 50);
        
        assertEquals(4, grid.getLivingCellCount());
        
        // Block should never change
        for (int i = 0; i < 10; i++) {
            grid.updateGeneration();
            assertEquals(4, grid.getLivingCellCount(), 
                        "Block should remain stable at generation " + i);
        }
    }

    @Test
    public void testGridBounds() {
        // Test boundary checking
        assertTrue(grid.isInBounds(0, 0));
        assertTrue(grid.isInBounds(99, 99));
        assertFalse(grid.isInBounds(-1, 0));
        assertFalse(grid.isInBounds(0, -1));
        assertFalse(grid.isInBounds(100, 0));
        assertFalse(grid.isInBounds(0, 100));
    }

    @Test
    public void testCellToggle() {
        Vector2i pos = new Vector2i(50, 50);
        
        assertFalse(grid.getCell(pos.x(), pos.y()));
        
        grid.setCell(pos.x(), pos.y(), true);
        assertTrue(grid.getCell(pos.x(), pos.y()));
        assertEquals(1, grid.getLivingCellCount());
        
        grid.setCell(pos.x(), pos.y(), false);
        assertFalse(grid.getCell(pos.x(), pos.y()));
        assertEquals(0, grid.getLivingCellCount());
    }

    @Test
    public void testNeighborCounting() {
        // Create a 3x3 square of living cells
        for (int x = 50; x <= 52; x++) {
            for (int y = 50; y <= 52; y++) {
                grid.setCell(x, y, true);
            }
        }
        
        // Center cell (51, 51) should have 8 neighbors
        assertEquals(8, grid.countNeighbors(51, 51));
        
        // Corner cell (50, 50) should have 3 neighbors
        assertEquals(3, grid.countNeighbors(50, 50));
        
        // Edge cell (50, 51) should have 5 neighbors
        assertEquals(5, grid.countNeighbors(50, 51));
    }

    @Test
    public void testGenerationIncrement() {
        assertEquals(0, grid.getGenerationCount());
        
        grid.updateGeneration();
        assertEquals(1, grid.getGenerationCount());
        
        grid.updateGeneration();
        assertEquals(2, grid.getGenerationCount());
    }

    @Test
    public void testGridClear() {
        // Add some cells
        grid.setCell(10, 10, true);
        grid.setCell(20, 20, true);
        grid.setCell(30, 30, true);
        
        assertEquals(3, grid.getLivingCellCount());
        
        // Clear
        grid.clear();
        assertEquals(0, grid.getLivingCellCount());
        assertEquals(0, grid.getGenerationCount());
    }
}
