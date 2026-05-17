package com.ashenthrone.battle;

import java.util.Random;

public class DamageCalculator {

    private static final double CRIT_CHANCE     = 0.15;
    private static final double CRIT_MULTIPLIER = 1.5;

    private final Random random;
    private boolean lastCrit;

    public DamageCalculator() {
        this.random = new Random();
    }


    public int calculate(int attack, int effectiveDefense) {
        int base = Math.max(1, attack - effectiveDefense);
        lastCrit = random.nextDouble() < CRIT_CHANCE;
        if (lastCrit) {
            return (int) Math.round(base * CRIT_MULTIPLIER);
        }
        return base;
    }

    public boolean wasLastCrit() {
        return lastCrit;
    }
}