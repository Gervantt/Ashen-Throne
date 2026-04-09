package com.ashenthrone.factory;

import com.ashenthrone.characters.Enemy;

/**
 * Abstract Factory — defines a family of enemy products for a single realm.
 * Each realm implementation returns thematically appropriate enemies.
 */
public interface RealmFactory {

    /** Returns a freshly cloned minion-tier enemy for this realm. */
    Enemy createMinion();

    /** Returns a freshly cloned elite-tier enemy for this realm. */
    Enemy createElite();

    /** Returns a freshly cloned boss-tier enemy for this realm. */
    Enemy createBoss();

    /**
     * Returns the asset path for this realm's background image.
     * TODO: AT-015 — change return type to Background once the visual layer exists.
     */
    String createBackground();
}
