package com.ashenthrone.factory;

import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.prototype.EnemyRegistry;

/**
 * Concrete Abstract Factory — Cursed Forest realm.
 * Produces HollowWolf (minion), Treant (elite), and HollowKing (boss).
 */
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
        // TODO: AT-015 — return a Background object when the visual layer is implemented
        return "backgrounds/cursed_forest.png";
    }
}
