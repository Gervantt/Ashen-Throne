package com.ashenthrone.strategy;

import com.ashenthrone.characters.AbstractCharacter;

import java.util.List;

/**
 * Restores 30 % of the attacker's maximum HP.
 *
 * The {@code targets} list is ignored — this skill always acts on the
 * attacker. Healing is capped at maxHp by {@link AbstractCharacter#heal}.
 */
public class HealSelf implements AttackStrategy {

    @Override
    public void execute(AbstractCharacter attacker, List<AbstractCharacter> targets) {
        int amount = (int) Math.round(attacker.getMaxHp() * 0.30);
        attacker.heal(amount);
    }

    @Override
    public String getDescription() {
        return "Heal Self";
    }
}