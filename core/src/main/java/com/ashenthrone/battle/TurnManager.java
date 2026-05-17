package com.ashenthrone.battle;

import com.ashenthrone.characters.Enemy;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TurnManager {

    public List<Enemy> getActingOrder(List<Enemy> enemies) {
        return enemies.stream()
                .filter(Enemy::isAlive)
                .sorted(Comparator.comparingInt(Enemy::getSpeed).reversed())
                .collect(Collectors.toList());
    }
}