package com.ashenthrone.factory;

import com.ashenthrone.battle.WaveIterator;
import com.ashenthrone.characters.Enemy;

public interface RealmFactory {

    Enemy createMinion();

    Enemy createElite();

    Enemy createBoss();

    String createBackground();

    WaveIterator createWaveIterator();
}
