package com.ashenthrone.battle.state;

import com.ashenthrone.screens.BattleScreen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Active state while enemies are taking their turns.
 *
 * All enemy action logic is now delegated to
 * {@link com.ashenthrone.battle.BattleEngine#executeEnemyTurns()} (AT-010).
 * BattleEngine coordinates turn order (TurnManager), damage calculation
 * (DamageCalculator), status effects (StatusEffectProcessor), and win/lose
 * detection (DeathChecker) — EnemyTurnState is a pure state-machine node.
 *
 * After enemies act, transitions to:
 *   AnimationState → PlayerTurnState  (hero still alive)
 *   AnimationState → DefeatState      (hero was killed)
 */
public class EnemyTurnState implements BattleState {

    private final BattleScreen screen;
    private boolean actionsExecuted;

    public EnemyTurnState(BattleScreen screen) {
        this.screen          = screen;
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

        // AT-010: delegate all enemy-phase logic to BattleEngine.
        screen.getBattleEngine().executeEnemyTurns();
        actionsExecuted = true;

        // AT-010: use DeathChecker via BattleEngine to decide the next state.
        BattleState nextState = "DEFEAT".equals(screen.getBattleEngine().getResult())
                ? new DefeatState(screen)
                : new PlayerTurnState(screen);

        screen.setState(new AnimationState(screen, nextState));
    }

    @Override
    public void render(SpriteBatch batch) {
        // TODO: AT-011 — render "Enemy Turn" turn indicator
        // TODO: AT-015 — full visual layout
    }
}