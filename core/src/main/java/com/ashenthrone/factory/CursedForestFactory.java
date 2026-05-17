package com.ashenthrone.factory;

import com.ashenthrone.battle.ForestWaveIterator;
import com.ashenthrone.battle.WaveIterator;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.prototype.EnemyRegistry;

public class CursedForestFactory implements RealmFactory {

    private final EnemyRegistry registry = EnemyRegistry.getInstance();

    @Override
    public Enemy createMinion() {
        return registry.spawn("HollowWolf");
    }

    @Override
    public Enemy createElite() {
        return registry.spawn("Treant");
    }

    @Override
    public Enemy createBoss() {
        return registry.spawn("HollowKing");
    }

    @Override
    public String createBackground() {
        return "backgrounds/cursed_forest.png";
    }

    @Override
    public WaveIterator createWaveIterator() {
        return new ForestWaveIterator(this);
    }
}
