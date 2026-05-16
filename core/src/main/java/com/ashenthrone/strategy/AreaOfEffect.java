package com.ashenthrone.strategy;

import com.ashenthrone.characters.AbstractCharacter;

import java.util.List;

public class AreaOfEffect implements AttackStrategy {

    @Override
    public void execute(AbstractCharacter attacker, List<AbstractCharacter> targets) {
        for (AbstractCharacter target : targets) {
            int damage = Math.max(1, (int) ((attacker.getAttack() - target.getDefense()) * 0.6));
            target.takeDamage(attacker, damage);
        }
    }

    @Override
    public String getDescription() {
        return "Area of Effect";
    }
}