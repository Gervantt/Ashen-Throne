package com.ashenthrone.factory;

import com.ashenthrone.battle.ThroneWaveIterator;
import com.ashenthrone.battle.WaveIterator;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.prototype.EnemyRegistry;

/**
 * Concrete Abstract Factory — Ashen Throne realm (AT-019).
 *
 * The final realm. A single wave hosts the Hollow King boss fight. Minion
 * and elite slots fall back to the boss prototype since this realm has no
 * lower tiers.
 */
public class AshenThroneRealmFactory implements RealmFactory {

    private final EnemyRegistry registry = EnemyRegistry.getInstance();

    @Override
    public Enemy createMinion() {
        return registry.spawn("HollowKing");
    }

    @Override
    public Enemy createElite() {
        return registry.spawn("HollowKing");
    }

    @Override
    public Enemy createBoss() {
        return registry.spawn("HollowKing");
    }

    @Override
    public String createBackground() {
        return "backgrounds/ashen_throne.png";
    }

    @Override
    public WaveIterator createWaveIterator() {
        return new ThroneWaveIterator(this);
    }
}
