package com.ashenthrone.factory;

import com.ashenthrone.characters.Enemy;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class implementing the Factory Method pattern.
 * {@link #spawnWave(int)} is the template method; {@link #createEnemy()} is the factory method
 * that subclasses override to control which enemy type populates the wave.
 */
public abstract class EnemySpawner {

    /**
     * Spawns a wave of {@code count} enemies by calling {@link #createEnemy()} repeatedly.
     *
     * @param count number of enemies to spawn; must be >= 1
     * @throws IllegalArgumentException if count is less than 1
     */
    public List<Enemy> spawnWave(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("Wave count must be at least 1, got: " + count);
        }
        List<Enemy> wave = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            wave.add(createEnemy());
        }
        return wave;
    }

    /**
     * Spawns the realm's boss enemy. Boss encounters are single and non-repeating,
     * so this is kept separate from {@link #spawnWave(int)}.
     */
    public Enemy spawnBoss() {
        return getFactory().createBoss();
    }

    /**
     * Factory Method — subclasses decide which enemy type populates a normal wave.
     * Default waves use the minion slot; mixed waves (minion + elite) are a future concern.
     */
    protected abstract Enemy createEnemy();

    /**
     * Returns the concrete {@link RealmFactory} used by this spawner.
     * Required so {@link #spawnBoss()} can delegate without duplicating boss logic in subclasses.
     */
    protected abstract RealmFactory getFactory();
}
