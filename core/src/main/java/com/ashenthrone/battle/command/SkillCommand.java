package com.ashenthrone.battle.command;

import com.ashenthrone.characters.AbstractCharacter;

/**
 * Executes the hero's active skill against a target.
 *
 * Placeholder until AT-008 introduces AttackStrategy. Currently falls back
 * to the same formula as AttackCommand. Once AT-008 is done, execute() should
 * call {@code attacker.currentStrategy.execute(attacker, targets)} and record
 * the strategy's effect for reversal.
 */
public class SkillCommand implements BattleCommand {

    private final AbstractCharacter attacker;
    private final AbstractCharacter target;
    private int targetHpBefore;

    public SkillCommand(AbstractCharacter attacker, AbstractCharacter target) {
        this.attacker = attacker;
        this.target = target;
    }

    @Override
    public void execute() {
        targetHpBefore = target.getHp();
        // TODO: AT-008 — call attacker.currentStrategy.execute(attacker, List.of(target))
        int damage = Math.max(1, attacker.getAttack() - target.getDefense());
        target.takeDamage(damage);
    }

    @Override
    public void undo() {
        // TODO: AT-008 — reverse the strategy's specific effect (heal reversal, AoE restore, etc.)
        target.setHp(targetHpBefore);
    }
}