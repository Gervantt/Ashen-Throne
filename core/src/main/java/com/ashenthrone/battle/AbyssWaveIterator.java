package com.ashenthrone.battle;

import com.ashenthrone.characters.Enemy;
import com.ashenthrone.factory.RealmFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Abyss realm waves (AT-026):
 *   wave 1: 2× minion       (Stonewarden in flavour text)
 *   wave 2: 1× elite + 2× minion (Emberclaw + Stonewardens)
 *
 * <p>Enemies come from the bound {@link RealmFactory} so the concrete
 * templates (Abstract Factory, AT-004) decide flavour while this iterator
 * decides composition.
 */
public class AbyssWaveIterator implements WaveIterator {

    private static final int TOTAL_WAVES = 2;

    private final RealmFactory factory;
    private int waveNumber; // 0 == not started

    public AbyssWaveIterator(RealmFactory factory) {
        if (factory == null) throw new IllegalArgumentException("factory must not be null");
        this.factory = factory;
        this.waveNumber = 0;
    }

    @Override public boolean hasNext()                { return waveNumber < TOTAL_WAVES; }
    @Override public int     getCurrentWaveNumber()   { return waveNumber; }
    @Override public int     getTotalWaves()          { return TOTAL_WAVES; }
    @Override public void    reset()                  { waveNumber = 0; }

    @Override
    public List<Enemy> next() {
        if (!hasNext()) throw new java.util.NoSuchElementException("no more waves in Abyss");
        waveNumber++;
        return currentWave();
    }

    @Override
    public List<Enemy> currentWave() {
        List<Enemy> wave = new ArrayList<>();
        switch (waveNumber) {
            case 1 -> {
                wave.add(factory.createMinion());
                wave.add(factory.createMinion());
            }
            case 2 -> {
                wave.add(factory.createElite());
                wave.add(factory.createMinion());
                wave.add(factory.createMinion());
            }
            default -> { /* 0 or out-of-range → empty list */ }
        }
        return wave;
    }
}
