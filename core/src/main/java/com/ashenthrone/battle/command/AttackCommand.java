package com.ashenthrone.battle.command;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.characters.AbstractCharacter;

public class AttackCommand implements BattleCommand {

    private final AbstractCharacter attacker;
    private final AbstractCharacter target;
    private final float damageMultiplier;
    private int targetHpBefore;

    public AttackCommand(AbstractCharacter attacker, AbstractCharacter target) {
        this(attacker, target, 1f);
    }


    public AttackCommand(AbstractCharacter attacker, AbstractCharacter target, float damageMultiplier) {
        this.attacker = attacker;
        this.target = target;
        this.damageMultiplier = damageMultiplier;
    }

    @Override
    public void execute() {
        targetHpBefore = target.getHp();
        if (damageMultiplier <= 0f) {

            return;
        }
        int base = Math.max(1, attacker.getAttack() - target.getDefense());
        int damage = Math.max(1, Math.round(base * damageMultiplier));
        target.takeDamage(damage);
        AudioManager.getInstance().playSFX("sword_hit");
    }

    @Override
    public void undo() {
        target.setHp(targetHpBefore);
    }
}
