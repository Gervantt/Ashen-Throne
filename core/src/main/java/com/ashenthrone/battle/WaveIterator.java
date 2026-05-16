package com.ashenthrone.battle;

import com.ashenthrone.characters.Enemy;

import java.util.List;

public interface WaveIterator {

    boolean hasNext();

    List<Enemy> next();

    List<Enemy> currentWave();

    int getCurrentWaveNumber();

    int getTotalWaves();

    void reset();
}
