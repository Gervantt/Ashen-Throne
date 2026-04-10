package com.ashenthrone.equipment;

import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.characters.CharacterDecorator;

/**
 * Equipment decorator — Cursed Ring.
 * Grants +8 defense at the cost of -2 attack.
 */
public class CursedRing extends CharacterDecorator {

    private static final int DEFENSE_BONUS = 8;
    private static final int ATTACK_PENALTY = 2;

    public CursedRing(AbstractCharacter wrapped) {
        super(wrapped);
    }

    @Override
    public int getAttack() {
        return super.getAttack() - ATTACK_PENALTY;
    }

    @Override
    public int getDefense() {
        return super.getDefense() + DEFENSE_BONUS;
    }
}
