package com.ashenthrone.battle;

import com.ashenthrone.characters.AbstractCharacter;

import java.util.List;

public class StatusEffectProcessor {

    public void process(AbstractCharacter character) {

    }

    public void processAll(List<? extends AbstractCharacter> characters) {
        for (AbstractCharacter c : characters) {
            process(c);
        }
    }
}