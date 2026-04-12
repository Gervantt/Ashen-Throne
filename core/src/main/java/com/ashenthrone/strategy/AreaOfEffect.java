package com.ashenthrone.strategy;

import com.ashenthrone.characters.AbstractCharacter;

import java.util.List;

/**
 * Sweeping strike that hits every target in the list at 60 % normal damage.
 *
 * Damage per target: max(1, (int)((attacker.attack − target.defense) × 0.6))
 * Lower per-hit damage than PhysicalAttack, but the total output against
 * grouped enemies exceeds a single-target strike.
 */
public class AreaOfEffect implements AttackStrategy {

    @Override
    public void execute(AbstractCharacter attacker, List<AbstractCharacter> targets) {
        for (AbstractCharacter target : targets) {
            int damage = Math.max(1, (int) ((attacker.getAttack() - target.getDefense()) * 0.6));
            target.takeDamage(damage);
        }
    }

    @Override
    public String getDescription() {
        return "Area of Effect";
    }
}