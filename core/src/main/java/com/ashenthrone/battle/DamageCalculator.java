package com.ashenthrone.battle;

import java.util.Random;

/**
 * Canonical damage formula used by BattleEngine for all character attacks.
 *
 * Base formula: max(1, attack − effectiveDefense)
 * The caller is responsible for computing effectiveDefense (full defence for
 * physical attacks, half defence for magical attacks). This keeps
 * DamageCalculator free of strategy-type awareness.
 *
 * Critical hits: a 15 % random roll multiplies the base damage by 1.5×.
 * Call {@link #wasLastCrit()} immediately after {@link #calculate} to check
 * whether the last roll was a critical hit (useful for battle-log messages).
 */
public class DamageCalculator {

    private static final double CRIT_CHANCE     = 0.15;
    private static final double CRIT_MULTIPLIER = 1.5;

    private final Random random;
    private boolean lastCrit;

    public DamageCalculator() {
        this.random = new Random();
    }

    /**
     * Calculates damage for a single hit.
     *
     * @param attack          the attacker's effective attack stat
     * @param effectiveDefense the target's defence after any penetration modifier
     * @return final damage value (always ≥ 1)
     */
    public int calculate(int attack, int effectiveDefense) {
        int base = Math.max(1, attack - effectiveDefense);
        lastCrit = random.nextDouble() < CRIT_CHANCE;
        if (lastCrit) {
            return (int) Math.round(base * CRIT_MULTIPLIER);
        }
        return base;
    }

    /**
     * Returns true if the most recent {@link #calculate} call rolled a critical hit.
     * Only valid immediately after a {@code calculate} call.
     */
    public boolean wasLastCrit() {
        return lastCrit;
    }
}