package com.ashenthrone.battle;

import com.ashenthrone.characters.Enemy;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Determines the order in which enemies act during the enemy phase.
 *
 * Enemies are sorted by speed descending — the fastest enemy acts first.
 * Only alive enemies are included.
 *
 * TODO: extend for initiative-based mixed hero/enemy turn order when
 *       AT-013 introduces a richer encounter loop.
 */
public class TurnManager {

    /**
     * Returns alive enemies sorted by speed descending.
     * The original list is not modified.
     */
    public List<Enemy> getActingOrder(List<Enemy> enemies) {
        return enemies.stream()
                .filter(Enemy::isAlive)
                .sorted(Comparator.comparingInt(Enemy::getSpeed).reversed())
                .collect(Collectors.toList());
    }
}