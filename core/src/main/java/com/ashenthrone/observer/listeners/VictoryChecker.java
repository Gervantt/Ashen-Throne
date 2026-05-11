package com.ashenthrone.observer.listeners;

import com.ashenthrone.characters.Enemy;
import com.ashenthrone.observer.EventListener;
import com.ashenthrone.observer.GameEvent;

/**
 * Tracks the battle outcome by observing CHARACTER_DIED and BATTLE_END events.
 *
 * On CHARACTER_DIED: records whether a Hero or an Enemy fell, allowing
 * other systems to query the pending result before BATTLE_END fires.
 *
 * On BATTLE_END: stores the final result string so it can be displayed
 * or acted upon (e.g. by AT-013 screen navigation).
 *
 * Read-only observer: the authoritative win/lose decision lives in
 * {@link com.ashenthrone.battle.BattleEngine#getResult()} via
 * {@link com.ashenthrone.battle.DeathChecker}; this listener exists so UI
 * code can query post-battle stats without re-scanning the combatant list.
 */
public class VictoryChecker implements EventListener {

    private boolean heroDefeated;
    private int enemiesDefeated;
    private String battleResult; // null until BATTLE_END fires

    @Override
    public void onEvent(GameEvent event) {
        switch (event.getType()) {
            case CHARACTER_DIED -> {
                // Anything that isn't an Enemy is on the hero side — this
                // includes both bare Hero and any CharacterDecorator chain
                // that wraps a Hero (AT-020 equipment).
                if (event.getCharacter() instanceof Enemy) {
                    enemiesDefeated++;
                } else {
                    heroDefeated = true;
                }
            }
            case BATTLE_END -> battleResult = event.getResult();
            default -> { /* DAMAGE_DEALT, ITEM_USED — not relevant here */ }
        }
    }

    /** True if the hero's death event has been observed. */
    public boolean isHeroDefeated()     { return heroDefeated; }

    /** Number of enemy deaths observed this battle. */
    public int getEnemiesDefeated()     { return enemiesDefeated; }

    /**
     * The result of the battle ("VICTORY" or "DEFEAT"), or {@code null}
     * if BATTLE_END has not yet been published.
     */
    public String getBattleResult()     { return battleResult; }

    /** Resets all state; call when starting a new battle. */
    public void reset() {
        heroDefeated    = false;
        enemiesDefeated = 0;
        battleResult    = null;
    }
}