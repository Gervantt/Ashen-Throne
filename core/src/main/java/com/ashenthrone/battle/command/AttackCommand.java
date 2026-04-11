package com.ashenthrone.battle.command;

import com.ashenthrone.characters.AbstractCharacter;

/**
 * Executes a basic physical attack from attacker → target.
 *
 * Damage formula: max(1, attacker.getAttack() − target.getDefense()).
 * The target's HP before the hit is recorded so undo() can restore it exactly,
 * regardless of any defending halving applied inside takeDamage().
 */
public class AttackCommand implements BattleCommand {

    private final AbstractCharacter attacker;
    private final AbstractCharacter target;
    private int targetHpBefore;

    public AttackCommand(AbstractCharacter attacker, AbstractCharacter target) {
        this.attacker = attacker;
        this.target = target;
    }

    @Override
    public void execute() {
        targetHpBefore = target.getHp();
        int damage = Math.max(1, attacker.getAttack() - target.getDefense());
        target.takeDamage(damage);
    }

    @Override
    public void undo() {
        target.setHp(targetHpBefore);
    }
}