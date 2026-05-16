package com.ashenthrone.strategy;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.characters.AbstractCharacter;

import java.util.List;

public class MagicAttack implements AttackStrategy {

    @Override
    public void execute(AbstractCharacter attacker, List<AbstractCharacter> targets) {
        if (targets.isEmpty()) return;
        AbstractCharacter target = targets.get(0);
        if (target == null) return;
        int effectiveDefense = target.getDefense() / 2;
        int damage = Math.max(1, attacker.getAttack() - effectiveDefense);
        AudioManager.getInstance().playSFX("fireball");
        target.takeDamage(attacker, damage);
    }

    @Override
    public String getDescription() {
        return "Magic Attack";
    }
}