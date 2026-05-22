package com.ashenthrone.battle;

import com.ashenthrone.battle.command.BattleCommand;
import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.characters.Enemy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class BattleEngine {

    private final TurnManager      turnManager      = new TurnManager();
    private final DamageCalculator damageCalculator = new DamageCalculator();
    private final DeathChecker     deathChecker     = new DeathChecker();

    private AbstractCharacter hero;
    private List<Enemy>  enemies;

    private final Deque<BattleCommand> commandHistory = new ArrayDeque<>();

    public void startBattle(AbstractCharacter hero, List<Enemy> enemies) {
        if (hero == null)    throw new IllegalArgumentException("hero must not be null");
        if (enemies == null) throw new IllegalArgumentException("enemies must not be null");
        this.hero    = hero;
        this.enemies = new ArrayList<>(enemies);
        commandHistory.clear();
    }

    public void executePlayerAction(BattleCommand cmd) {
        requireStarted();
        if (cmd == null) return;
        cmd.execute();
        commandHistory.push(cmd);
    }

    public void undoLastCommand() {
        if (!commandHistory.isEmpty()) {
            commandHistory.pop().undo();
        }
    }

    public boolean canUndo() {
        return !commandHistory.isEmpty();
    }

    public void executeEnemyTurns() {

        requireStarted();
        List<Enemy> actingEnemies = turnManager.getActingOrder(enemies);

        for (Enemy enemy : actingEnemies) {
            if (!hero.isAlive()) break;
            int damage = damageCalculator.calculate(enemy.getAttack(), hero.getDefense());
            hero.takeDamage(damage);
        }
    }

    public boolean isOver() {
        requireStarted();
        return deathChecker.isOver(hero, enemies);
    }

    public String getResult() {
        requireStarted();
        return deathChecker.getResult(hero, enemies);
    }

    public AbstractCharacter getHero() {
        requireStarted();
        return hero;
    }

    public List<Enemy> getEnemies() {
        requireStarted();
        return Collections.unmodifiableList(enemies);
    }

    private void requireStarted() {
        if (hero == null) {
            throw new IllegalStateException("BattleEngine.startBattle() must be called before use");
        }
    }
}