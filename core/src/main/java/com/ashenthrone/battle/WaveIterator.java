package com.ashenthrone.battle;

import com.ashenthrone.characters.Enemy;

import java.util.List;

/**
 * Iterator over the waves of a single realm (AT-026, 16th GoF pattern).
 *
 * <p>{@link com.ashenthrone.factory.RealmFactory#createWaveIterator()} produces
 * the concrete iterator for a realm; {@link com.ashenthrone.screens.BattleScreen}
 * (via the screen-flow code) calls {@link #next()} to spawn the next wave's
 * enemies and {@link #hasNext()} to detect that the realm is cleared.
 *
 * <p>Wave numbers are 1-based for display:
 * <ul>
 *   <li>Before the first {@code next()} call, {@code getCurrentWaveNumber() == 0}.</li>
 *   <li>After the n-th {@code next()} call, {@code getCurrentWaveNumber() == n}.</li>
 * </ul>
 *
 * <p>{@link #currentWave()} re-spawns the wave at the current position without
 * advancing — used by retry flows so a defeated wave can be reconstructed
 * with fresh enemy clones.
 */
public interface WaveIterator {

    /** True if there is another wave to spawn. */
    boolean hasNext();

    /** Advance to the next wave and return its enemies (fresh clones). */
    List<Enemy> next();

    /**
     * Re-spawn the current wave (the most recently advanced-to) as fresh
     * clones, without advancing. Returns an empty list if {@link #next()}
     * has never been called.
     */
    List<Enemy> currentWave();

    /** 1-based wave number after the most recent {@link #next()} call; 0 before any. */
    int getCurrentWaveNumber();

    /** Total wave count in this realm. */
    int getTotalWaves();

    /** Reset to the pre-iteration state so {@code next()} yields wave 1 again. */
    void reset();
}
