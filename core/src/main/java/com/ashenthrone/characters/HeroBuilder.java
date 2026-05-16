package com.ashenthrone.characters;

import com.ashenthrone.strategy.AttackStrategy;

public class HeroBuilder {

    private final Hero hero;

    public HeroBuilder() {
        hero = new Hero();
    }

    public HeroBuilder name(String name) {
        hero.name = name;
        return this;
    }

    public HeroBuilder hp(int hp) {
        hero.hp = hp;
        hero.maxHp = hp;
        return this;
    }

    public HeroBuilder attack(int attack) {
        hero.attack = attack;
        return this;
    }

    public HeroBuilder defense(int defense) {
        hero.defense = defense;
        return this;
    }

    public HeroBuilder speed(int speed) {
        hero.speed = speed;
        return this;
    }

    public HeroBuilder skill(AttackStrategy strategy) {
        hero.setCurrentStrategy(strategy);
        return this;
    }

    public Hero build() {
        if (hero.name == null)    throw new IllegalStateException("Hero must have a name");
        if (hero.maxHp <= 0)     throw new IllegalStateException("Hero must have positive HP");
        if (hero.attack <= 0)    throw new IllegalStateException("Hero must have positive attack");
        if (hero.defense < 0)    throw new IllegalStateException("Hero must have non-negative defense");
        if (hero.speed <= 0)     throw new IllegalStateException("Hero must have positive speed");
        return hero;
    }
}
