package com.ashenthrone.battle;

import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.Hero;

import java.util.List;

/**
 * Evaluates win and lose conditions after each action.
 *
 * The two terminal conditions are:
 *   VICTORY — every enemy in the encounter is dead
 *   DEFEAT  — the hero's HP has reached zero
 *
 * Victory takes priority: if a hero ability kills the last enemy and the hero
 * is also at 0 HP (edge-case AoE), "VICTORY" is returned.
 */
public class DeathChecker {

    /** True if all enemies in the list are dead. */
    public boolean areAllEnemiesDefeated(List<Enemy> enemies) {
        return enemies.stream().noneMatch(Enemy::isAlive);
    }

    /** True if the hero has no HP remaining. */
    public boolean isHeroDefeated(Hero hero) {
        return !hero.isAlive();
    }

    /**
     * True if either terminal condition has been met.
     * Call after every player action and enemy action.
     */
    public boolean isOver(Hero hero, List<Enemy> enemies) {
        return areAllEnemiesDefeated(enemies) || isHeroDefeated(hero);
    }

    /**
     * Returns "VICTORY", "DEFEAT", or {@code null} if the battle is still ongoing.
     * Victory takes precedence over defeat.
     */
    public String getResult(Hero hero, List<Enemy> enemies) {
        if (areAllEnemiesDefeated(enemies)) return "VICTORY";
        if (isHeroDefeated(hero))           return "DEFEAT";
        return null;
    }
}