package com.ashenthrone.factory;

import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.prototype.EnemyRegistry;

/**
 * Concrete Abstract Factory — Abyss realm.
 * Produces ShadowCrawler (minion), Wraith (elite), and HollowKing (boss).
 */
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
        // TODO: AT-015 — return a Background object when the visual layer is implemented
        return "backgrounds/abyss.png";
    }
}
