package com.ashenthrone.factory;

import com.ashenthrone.characters.Enemy;

import java.util.ArrayList;
import java.util.List;

public abstract class EnemySpawner {


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


    public Enemy spawnBoss() {
        return getFactory().createBoss();
    }


    protected abstract Enemy createEnemy();

    protected abstract RealmFactory getFactory();
}
