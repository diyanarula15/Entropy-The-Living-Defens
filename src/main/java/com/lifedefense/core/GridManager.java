package com.lifedefense.core;

import com.lifedefense.utils.Vector2i;
import java.util.*;

/**
 * Manages the grid state using a BitSet for memory efficiency.
 * Uses sparse representation with active cell tracking for O(n) updates.
 * This is the "Universe" in Conway's Game of Life.
 */
public class GridManager {
    private final int width;
    private final int height;
    
    // Double buffering: current and next generation
    private BitSet currentGen;
    private BitSet nextGen;
    
    // Active cell tracking: only these cells and their neighbors are checked
    // Using BitSet instead of Set<Vector2i> to avoid GC overhead (boxing/unboxing)
    private BitSet activeBitSet;
    private BitSet nextActiveBitSet;
    
    // Visual "Juice": Track when cells were born for animation
    private final long[] birthTimes;
    
    // Statistics
    private long generationCount;
    private int livingCellCount;

    public GridManager(int width, int height) {
        this.width = width;
        this.height = height;
        int size = width * height;
        this.currentGen = new BitSet(size);
        this.nextGen = new BitSet(size);
        this.activeBitSet = new BitSet(size);
        this.nextActiveBitSet = new BitSet(size);
        this.birthTimes = new long[size];
        this.generationCount = 0;
        this.livingCellCount = 0;
    }

    /**
     * Set a cell to alive (true) or dead (false).
     */
    public void setCell(int x, int y, boolean alive) {
        if (!isInBounds(x, y)) return;
        
        int idx = (int) toIndex(x, y);
        boolean wasAlive = currentGen.get(idx);
        
        if (alive && !wasAlive) {
            currentGen.set(idx);
            livingCellCount++;
            birthTimes[idx] = System.currentTimeMillis(); // Record birth time
            addToActiveSet(x, y, activeBitSet);
        } else if (!alive && wasAlive) {
            currentGen.clear(idx);
            livingCellCount--;
            addToActiveSet(x, y, activeBitSet);
        }
    }

    /**
     * Check if a cell is alive.
     */
    public boolean getCell(int x, int y) {
        if (!isInBounds(x, y)) return false;
        return currentGen.get((int) toIndex(x, y));
    }

    /**
     * Count living neighbors (0-8).
     */
    public int countNeighbors(int x, int y) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                if (getCell(x + dx, y + dy)) count++;
            }
        }
        return count;
    }

    /**
     * Advance the simulation by one generation using Conway's rules.
     * Only checks cells in the active set for performance.
     */
    public void updateGeneration() {
        nextGen.clear();
        nextActiveBitSet.clear();
        livingCellCount = 0;

        // Iterate over all set bits in activeBitSet
        for (int i = activeBitSet.nextSetBit(0); i >= 0; i = activeBitSet.nextSetBit(i + 1)) {
            int x = i % width;
            int y = i / width;
            checkCell(x, y);
        }

        // Swap buffers
        BitSet temp = currentGen;
        currentGen = nextGen;
        nextGen = temp;
        
        BitSet tempActive = activeBitSet;
        activeBitSet = nextActiveBitSet;
        nextActiveBitSet = tempActive;
        
        generationCount++;
    }

    /**
     * Apply Conway's Game of Life rules to a single cell.
     */
    private void checkCell(int x, int y) {
        if (!isInBounds(x, y)) return;

        int neighbors = countNeighbors(x, y);
        int idx = (int) toIndex(x, y);
        boolean alive = currentGen.get(idx);
        boolean nextState = alive;

        // Conway's rules:
        // 1. Underpopulation: alive cell with < 2 neighbors dies
        // 2. Overpopulation: alive cell with > 3 neighbors dies
        // 3. Survival: alive cell with 2-3 neighbors survives
        // 4. Reproduction: dead cell with exactly 3 neighbors becomes alive
        
        if (alive && (neighbors < 2 || neighbors > 3)) {
            nextState = false;
        } else if (!alive && neighbors == 3) {
            nextState = true;
        }

        // Update next generation
        if (nextState) {
            nextGen.set(idx);
            livingCellCount++;
            // If it was dead and became alive, update birth time
            if (!alive) {
                birthTimes[idx] = System.currentTimeMillis();
            }
            addToActiveSet(x, y, nextActiveBitSet);
        } else if (alive) {
            // Cell died, still need to update neighbors in next active set
            addToActiveSet(x, y, nextActiveBitSet);
        }
    }

    /**
     * Mark a cell and its neighbors as active for the next update.
     */
    private void addToActiveSet(int x, int y, BitSet targetSet) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int nx = x + dx;
                int ny = y + dy;
                if (isInBounds(nx, ny)) {
                    targetSet.set((int) toIndex(nx, ny));
                }
            }
        }
    }

    /**
     * Bounds checking for the grid.
     */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    private long toIndex(int x, int y) {
        return (long) y * width + x;
    }

    // ===== Getters =====
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getGenerationCount() {
        return generationCount;
    }

    public int getLivingCellCount() {
        return livingCellCount;
    }

    public long getBirthTime(int x, int y) {
        if (!isInBounds(x, y)) return 0;
        return birthTimes[(int) toIndex(x, y)];
    }

    /**
     * Get all currently living cells (for rendering/serialization).
     */
    public List<Vector2i> getAllLivingCells() {
        List<Vector2i> living = new ArrayList<>();
        for (int i = currentGen.nextSetBit(0); i >= 0; i = currentGen.nextSetBit(i + 1)) {
            int x = i % width;
            int y = i / width;
            living.add(new Vector2i(x, y));
        }
        return living;
    }

    /**
     * Clear the grid completely.
     */
    public void clear() {
        currentGen.clear();
        nextGen.clear();
        activeBitSet.clear();
        nextActiveBitSet.clear();
        livingCellCount = 0;
        generationCount = 0;
        Arrays.fill(birthTimes, 0);
    }

    /**
     * Serialize grid state to a list of living cell coordinates.
     */
    public List<Vector2i> serialize() {
        return getAllLivingCells();
    }

    /**
     * Deserialize grid state from a list of cell coordinates.
     */
    public void deserialize(List<Vector2i> cells) {
        clear();
        for (Vector2i cell : cells) {
            setCell(cell.x(), cell.y(), true);
        }
    }

    /**
     * Expose the underlying BitSet for GC-free iteration in the rendering loop.
     */
    public BitSet getLivingCellsBitSet() {
        return currentGen;
    }
}
