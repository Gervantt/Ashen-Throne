package com.ashenthrone.equipment;

import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.characters.CharacterDecorator;

/**
 * Equipment decorator — Shadow Blade.
 * Grants +10 attack by wrapping the character's getAttack().
 */
public class ShadowBlade extends CharacterDecorator {

    private static final int ATTACK_BONUS = 10;

    public ShadowBlade(AbstractCharacter wrapped) {
        super(wrapped);
    }

    @Override
    public int getAttack() {
        return super.getAttack() + ATTACK_BONUS;
    }
}
