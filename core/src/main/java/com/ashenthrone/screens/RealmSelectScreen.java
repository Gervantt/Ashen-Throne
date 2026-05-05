package com.ashenthrone.screens;

import com.ashenthrone.core.AshenThroneGame;

/**
 * Realm tower / selection screen stub (AT-013).
 *
 * TODO: AT-019 — implement vertical tower with three sections
 * (The Abyss, Cursed Forest, Ashen Throne). Sections unlock sequentially based
 * on {@code GameSession.completedRealms}. Locked sections render with a lock
 * icon. Selection picks the appropriate {@code RealmFactory} (AT-004) and
 * spawns waves via {@code WaveIterator} (AT-026).
 */
public class RealmSelectScreen extends BaseScreen {

    public RealmSelectScreen(AshenThroneGame game) {
        super(game);
    }
}