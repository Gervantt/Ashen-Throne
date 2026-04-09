package com.ashenthrone.factory;

import com.ashenthrone.characters.Enemy;

/**
 * Concrete Factory Method implementor — Abyss realm.
 * Normal waves consist of ShadowCrawler minions; boss waves yield HollowKing.
 */
public class AbyssSpawner extends EnemySpawner {

    private final AbyssRealmFactory factory = new AbyssRealmFactory();

    @Override
    protected Enemy createEnemy() {
        return factory.createMinion();
    }

    @Override
    protected RealmFactory getFactory() {
        return factory;
    }
}
