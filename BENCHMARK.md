# Performance Benchmark & Verification

This document outlines how the project meets the "10k Stress Test" and "GC Zero" requirements.

## 1. Active Cell Tracking (O(N) vs O(N^2))

The `GridManager` uses a `BitSet` to track active cells.
- **Naive Implementation**: Iterates 1,000,000 cells (1000x1000) every frame.
- **Optimized Implementation**: Iterates only `activeBitSet`.
    - If 10,000 cells are alive, we check ~90,000 bits (including neighbors).
    - `BitSet.nextSetBit()` is extremely fast (CPU intrinsic).

## 2. Garbage Collection (GC) Zero

We have eliminated object allocation in the main game loop:
- **Vectors**: Replaced immutable `Vector2f` records with mutable `com.badlogic.gdx.math.Vector2`.
- **Pooling**: `EnemyAgent` uses pre-allocated `tmp` vectors for physics calculations.
- **Collections**: 
    - `CollisionSystem` uses direct grid lookups (O(1)) instead of `HashMap` buckets.
    - `FlowFieldSystem` uses `float[][]` arrays instead of `Vector2f[][]` (saving 1M objects).
    - `RenderSystem` iterates `BitSet` directly instead of creating `List<Vector2i>`.

## 3. Memory Footprint

- **Grid**: 2 `BitSet`s of 1,000,000 bits = ~250KB.
- **Flow Field**: 2 `float[][]` arrays = ~8MB.
- **Entities**: 10,000 enemies * ~100 bytes = ~1MB.
- **Total**: Well under the 512MB limit.

## 4. "Juice" (Visual Feedback)

- **Spawn Animation**: Cells scale up from 0 to 1 over 200ms when born.
- **Implementation**: `GridManager` tracks `birthTimes[]` (primitive long array), and `RenderSystem` calculates scale in the vertex shader (or CPU-side rect size).

## How to Verify

1. **VisualVM**: Attach to the running process. Monitor "Allocated Bytes/sec". It should be near zero during gameplay (excluding initial startup).
2. **FPS Counter**: The HUD displays current FPS and TPS.
3. **Stress Test**: 
    - Pause the game (`P`).
    - Paint a massive block of cells.
    - Unpause.
    - FPS should remain stable at 60.
