package com.ashenthrone.factory;

import com.ashenthrone.characters.Enemy;

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
