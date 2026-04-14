package com.ashenthrone.strategy;

import com.ashenthrone.characters.AbstractCharacter;

import java.util.List;

/**
 * Single-target arcane strike that bypasses half the target's defence.
 *
 * Damage formula: max(1, attacker.attack − target.defense / 2)
 * Effective against heavily-armoured enemies but no better than
 * PhysicalAttack against targets with low defence.
 */
public class MagicAttack implements AttackStrategy {

    @Override
    public void execute(AbstractCharacter attacker, List<AbstractCharacter> targets) {
        if (targets.isEmpty()) return;
        AbstractCharacter target = targets.get(0);
        if (target == null) return;
        int effectiveDefense = target.getDefense() / 2;
        int damage = Math.max(1, attacker.getAttack() - effectiveDefense);
        target.takeDamage(attacker, damage);
    }

    @Override
    public String getDescription() {
        return "Magic Attack";
    }
}