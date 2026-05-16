package com.ashenthrone.factory;

import com.ashenthrone.characters.Enemy;

public class ForestSpawner extends EnemySpawner {

    private final CursedForestFactory factory = new CursedForestFactory();

    @Override
    protected Enemy createEnemy() {
        return factory.createMinion();
    }

    @Override
    protected RealmFactory getFactory() {
        return factory;
    }
}
