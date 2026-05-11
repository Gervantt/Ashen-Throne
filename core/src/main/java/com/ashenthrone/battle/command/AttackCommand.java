package com.ashenthrone.battle.command;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.characters.AbstractCharacter;

/**
 * Executes a basic physical attack from attacker → target.
 *
 * Damage formula: max(1, attacker.getAttack() − target.getDefense()) × multiplier,
 * floored to 0 when the multiplier is 0 (timing-bar miss).
 *
 * The target's HP before the hit is recorded so undo() can restore it exactly,
 * regardless of any defending halving applied inside takeDamage().
 */
public class AttackCommand implements BattleCommand {

    private final AbstractCharacter attacker;
    private final AbstractCharacter target;
    private final float damageMultiplier;
    private int targetHpBefore;

    public AttackCommand(AbstractCharacter attacker, AbstractCharacter target) {
        this(attacker, target, 1f);
    }

    /**
     * @param damageMultiplier scaling factor applied to the base damage roll.
     *                         0 = miss (no damage, no SFX), 0.5 = weak, 1.0 = normal,
     *                         2.0 = critical. Other positive values also supported.
     */
    public AttackCommand(AbstractCharacter attacker, AbstractCharacter target, float damageMultiplier) {
        this.attacker = attacker;
        this.target = target;
        this.damageMultiplier = damageMultiplier;
    }

    @Override
    public void execute() {
        targetHpBefore = target.getHp();
        if (damageMultiplier <= 0f) {
            // Miss — no damage, no sound.
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
