package com.ashenthrone.strategy;

import com.ashenthrone.characters.AbstractCharacter;

import java.util.List;

/**
 * Strategy pattern — encapsulates a single combat action.
 *
 * Implementations cover the four skill archetypes defined in AT-008:
 *   PhysicalAttack, MagicAttack, HealSelf, AreaOfEffect.
 *
 * The {@code targets} list meaning varies by implementation:
 *   - Single-target strategies use targets.get(0).
 *   - HealSelf ignores targets and acts on the attacker directly.
 *   - AreaOfEffect iterates all entries in targets.
 *
 * The Hero's active strategy is swapped via
 * {@link AbstractCharacter#setCurrentStrategy(AttackStrategy)} when the player
 * navigates the Skill submenu (planned for AT-013 screen flow).
 */
public interface AttackStrategy {

    /**
     * Execute this strategy.
     *
     * @param attacker the character using the skill
     * @param targets  relevant targets; may be empty if no valid target exists
     */
    void execute(AbstractCharacter attacker, List<AbstractCharacter> targets);

    /** Short display name shown in the battle UI. */
    String getDescription();
}