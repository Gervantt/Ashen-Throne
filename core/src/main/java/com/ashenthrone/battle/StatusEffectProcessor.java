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
     * Currently a no-op; extend once status effects are implemented.
     */
    public void process(AbstractCharacter character) {
        // TODO: iterate character.getStatusEffects() and tick each effect
        // TODO: publish DAMAGE_DEALT events for damage-over-time effects (AT-009)
    }

    /** Convenience method — processes every character in the list. */
    public void processAll(List<? extends AbstractCharacter> characters) {
        for (AbstractCharacter c : characters) {
            process(c);
        }
    }
}