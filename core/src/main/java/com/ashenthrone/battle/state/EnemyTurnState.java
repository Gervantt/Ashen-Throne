package com.ashenthrone.battle.state;

import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.Hero;
import com.ashenthrone.screens.BattleScreen;
import com.ashenthrone.strategy.AttackStrategy;
import com.ashenthrone.strategy.PhysicalAttack;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Active state while enemies are taking their turns.
 *
 * All living enemies act sequentially on the first update() call, ordered
 * by speed (fastest first). After all actions are resolved, the state
 * transitions to AnimationState → PlayerTurnState, or AnimationState →
 * DefeatState if the hero has been killed.
 *
 * Damage is calculated inline (placeholder) until AT-007 wraps actions in
 * AttackCommands and AT-010 provides DamageCalculator through BattleEngine.
 */
public class EnemyTurnState implements BattleState {

    private final BattleScreen screen;
    private boolean actionsExecuted;

    public EnemyTurnState(BattleScreen screen) {
        this.screen = screen;
        this.actionsExecuted = false;
    }

    // ---- BattleState ----

    @Override
    public void handleInput() {
        // No player input accepted during the enemy turn.
    }

    @Override
    public void update(float delta) {
        if (actionsExecuted) return;

        executeEnemyActions();
        actionsExecuted = true;

        BattleState nextState = screen.getHero().isAlive()
                ? new PlayerTurnState(screen)
                : new DefeatState(screen);

        screen.setState(new AnimationState(screen, nextState));
    }

    @Override
    public void render(SpriteBatch batch) {
        // TODO: AT-011 — render "Enemy Turn" turn indicator
        // TODO: AT-015 — full visual layout
    }

    // ---- Enemy action resolution ----

    private void executeEnemyActions() {
        Hero hero = screen.getHero();

        List<Enemy> actingEnemies = screen.getEnemies().stream()
                .filter(Enemy::isAlive)
                .sorted(Comparator.comparingInt(Enemy::getSpeed).reversed())
                .collect(Collectors.toList());

        for (Enemy enemy : actingEnemies) {
            if (!hero.isAlive()) break;

            // Resolve the enemy's strategy for this turn.
            // chooseAction() is protected (Template Method), so the state machine
            // reads the strategy set at spawn time (EnemyRegistry) and falls back
            // to PhysicalAttack when none is assigned.
            // TODO: AT-010 — route through BattleEngine.executeEnemyTurns()
            // TODO: AT-007 — wrap in AttackCommand so BattleEngine can undo/replay
            AttackStrategy strategy = enemy.getCurrentStrategy();
            if (strategy == null) {
                strategy = new PhysicalAttack();
            }
            strategy.execute(enemy, List.of(hero));
        }
    }
}
