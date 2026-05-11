package com.ashenthrone.battle;

import com.ashenthrone.characters.AbstractCharacter;

import java.util.List;

/**
 * Applies per-turn status effects (poison, buffs, debuffs) to characters.
 *
 * Placeholder — a full status-effect system is out of scope for the current
 * sprint. Once status effects are modelled (planned alongside AT-009 Observer
 * integration), this processor will iterate a character's active effects and
 * tick each one.
 *
 * BattleEngine calls {@link #processAll} at the start of the enemy phase so
 * the hook is already in place.
 */
public class StatusEffectProcessor {

    /**
     * Processes all active status effects on a single character.
     *
     * <p>Currently a no-op — no status-effect model exists on
     * {@link AbstractCharacter}. The {@link com.ashenthrone.battle.BattleEngine}
     * still calls this between phases so a future status system can plug in
     * without re-threading the call site.
     */
    public void process(AbstractCharacter character) {
        // Intentionally empty — no status effects modelled yet.
    }

    /** Convenience method — processes every character in the list. */
    public void processAll(List<? extends AbstractCharacter> characters) {
        for (AbstractCharacter c : characters) {
            process(c);
        }
    }
}