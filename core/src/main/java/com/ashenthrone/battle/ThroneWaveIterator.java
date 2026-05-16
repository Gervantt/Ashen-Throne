package com.ashenthrone.battle;

import com.ashenthrone.characters.Enemy;
import com.ashenthrone.factory.RealmFactory;

import java.util.ArrayList;
import java.util.List;

public class ThroneWaveIterator implements WaveIterator {

    private static final int TOTAL_WAVES = 1;

    private final RealmFactory factory;
    private int waveNumber;

    public ThroneWaveIterator(RealmFactory factory) {
        if (factory == null) throw new IllegalArgumentException("factory must not be null");
        this.factory = factory;
        this.waveNumber = 0;
    }

    @Override public boolean hasNext()              { return waveNumber < TOTAL_WAVES; }
    @Override public int     getCurrentWaveNumber() { return waveNumber; }
    @Override public int     getTotalWaves()        { return TOTAL_WAVES; }
    @Override public void    reset()                { waveNumber = 0; }

    @Override
    public List<Enemy> next() {
        if (!hasNext()) throw new java.util.NoSuchElementException("no more waves in Ashen Throne");
        waveNumber++;
        return currentWave();
    }

    @Override
    public List<Enemy> currentWave() {
        List<Enemy> wave = new ArrayList<>();
        if (waveNumber == 1) wave.add(factory.createBoss());
        return wave;
    }
}
