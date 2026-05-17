package com.ashenthrone.strategy;

import com.ashenthrone.characters.AbstractCharacter;

import java.util.List;

public interface AttackStrategy {


    void execute(AbstractCharacter attacker, List<AbstractCharacter> targets);

    String getDescription();
}