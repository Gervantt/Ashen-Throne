package com.ashenthrone.equipment;

import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.characters.CharacterDecorator;

/**
 * Equipment decorator — Fire Amulet.
 * Grants +5 attack by wrapping the character's getAttack().
 */
public class FireAmulet extends CharacterDecorator {

    private static final int ATTACK_BONUS = 5;

    public FireAmulet(AbstractCharacter wrapped) {
        super(wrapped);
    }

    @Override
    public int getAttack() {
        return super.getAttack() + ATTACK_BONUS;
    }
}
