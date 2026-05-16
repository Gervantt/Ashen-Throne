package com.ashenthrone.battle.state;

import com.ashenthrone.screens.BattleScreen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EnemyTurnState implements BattleState {

    private final BattleScreen screen;
    private boolean actionsExecuted;

    public EnemyTurnState(BattleScreen screen) {
        this.screen          = screen;
        this.actionsExecuted = false;
    }

    @Override
    public void handleInput() {

    }

    @Override
    public void update(float delta) {
        if (actionsExecuted) return;

        screen.getBattleEngine().executeEnemyTurns();

        screen.playEnemyActionAnimations();
        actionsExecuted = true;

        BattleState nextState = "DEFEAT".equals(screen.getBattleEngine().getResult())
                ? new DefeatState(screen)
                : new PlayerTurnState(screen);

        screen.setState(new AnimationState(screen, nextState));
    }

    @Override
    public void render(SpriteBatch batch) {

    }
}