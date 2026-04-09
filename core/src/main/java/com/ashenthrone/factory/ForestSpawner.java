package com.ashenthrone.factory;

import com.ashenthrone.characters.Enemy;

/**
 * Concrete Factory Method implementor — Cursed Forest realm.
 * Normal waves consist of HollowWolf minions; boss waves yield HollowKing.
 */
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
