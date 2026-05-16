package com.ashenthrone.battle;

import com.ashenthrone.characters.Enemy;
import com.ashenthrone.factory.RealmFactory;

import java.util.ArrayList;
import java.util.List;

public class AbyssWaveIterator implements WaveIterator {

    private static final int TOTAL_WAVES = 2;

    private final RealmFactory factory;
    private int waveNumber;

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
            default -> {  }
        }
        return wave;
    }
}
