package com.ashenthrone.factory;

import com.ashenthrone.battle.AbyssWaveIterator;
import com.ashenthrone.battle.WaveIterator;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.prototype.EnemyRegistry;

public class AbyssRealmFactory implements RealmFactory {

    private final EnemyRegistry registry = EnemyRegistry.getInstance();

    @Override
    public Enemy createMinion() {
        return registry.spawn("ShadowCrawler");
    }

    @Override
    public Enemy createElite() {
        return registry.spawn("Wraith");
    }

    @Override
    public Enemy createBoss() {
        return registry.spawn("HollowKing");
    }

    @Override
    public String createBackground() {
        return "backgrounds/abyss.png";
    }

    @Override
    public WaveIterator createWaveIterator() {
        return new AbyssWaveIterator(this);
    }
}
