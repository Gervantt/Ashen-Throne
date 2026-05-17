package com.ashenthrone.equipment;

import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.characters.CharacterDecorator;

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
