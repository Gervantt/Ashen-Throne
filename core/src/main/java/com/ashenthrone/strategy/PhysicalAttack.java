package com.ashenthrone.strategy;

import com.ashenthrone.characters.AbstractCharacter;

import java.util.List;

/**
 * Single-target physical strike.
 *
 * Damage formula: max(1, attacker.attack − target.defense)
 * Defense is applied in full, making this the weakest option against armoured foes.
 */
public class PhysicalAttack implements AttackStrategy {

    @Override
    public void execute(AbstractCharacter attacker, List<AbstractCharacter> targets) {
        if (targets.isEmpty()) return;
        AbstractCharacter target = targets.get(0);
        if (target == null) return;
        int damage = Math.max(1, attacker.getAttack() - target.getDefense());
        target.takeDamage(damage);
    }

    @Override
    public String getDescription() {
        return "Physical Attack";
    }
}