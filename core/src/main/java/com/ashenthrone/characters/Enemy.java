package com.ashenthrone.characters;

import com.ashenthrone.strategy.HealSelf;
import com.ashenthrone.strategy.PhysicalAttack;

public class Enemy extends AbstractCharacter implements Cloneable {

    private String type;

    Enemy() {

    }


    @Override
    protected void chooseAction() {
        boolean lowHp = (double) hp / maxHp < 0.30;
        if (lowHp && currentStrategy instanceof HealSelf) {

            return;
        }
        if (currentStrategy == null) {
            currentStrategy = new PhysicalAttack();
        }

    }

    @Override
    public Enemy clone() {
        try {
            Enemy copy = (Enemy) super.clone();

            return copy;
        } catch (CloneNotSupportedException e) {

            throw new IllegalStateException("Clone failed on a Cloneable class", e);
        }
    }


    public void applyWaveEscalation(int level) {
        if (level <= 0) return;
        maxHp = Math.max(1, Math.round(maxHp * (1f + 0.10f * level)));
        hp = maxHp;
        attack += 2 * level;
        defense += level;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
