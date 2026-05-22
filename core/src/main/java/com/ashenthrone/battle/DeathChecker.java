package com.ashenthrone.battle;

import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.characters.Enemy;

import java.util.List;

public class DeathChecker {

    private boolean areAllEnemiesDefeated(List<Enemy> enemies) {
        return enemies.stream().noneMatch(Enemy::isAlive);
    }

    private boolean isHeroDefeated(AbstractCharacter hero) {
        return !hero.isAlive();
    }

    public boolean isOver(AbstractCharacter hero, List<Enemy> enemies) {
        return areAllEnemiesDefeated(enemies) || isHeroDefeated(hero);
    }

    public String getResult(AbstractCharacter hero, List<Enemy> enemies) {
        if (areAllEnemiesDefeated(enemies)) return "VICTORY";
        if (isHeroDefeated(hero))           return "DEFEAT";
        return null;
    }
}