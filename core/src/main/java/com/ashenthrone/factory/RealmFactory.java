package com.ashenthrone.factory;

import com.ashenthrone.battle.WaveIterator;
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
     * Consumers resolve the path through
     * {@link com.ashenthrone.audio.GameAssetManager} when the texture is needed.
     */
    String createBackground();

    /**
     * Returns a fresh {@link WaveIterator} (AT-026) over this realm's waves.
     * Each call yields a new iterator whose state is reset to before wave 1.
     */
    WaveIterator createWaveIterator();
}
