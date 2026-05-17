package com.ashenthrone.battle.state;

import com.ashenthrone.screens.BattleScreen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class AnimationState implements BattleState {

    private static final float ANIMATION_DURATION = 0.75f;

    private final BattleScreen screen;
    private final BattleState nextState;
    private float elapsed;

    public AnimationState(BattleScreen screen, BattleState nextState) {
        this.screen = screen;
        this.nextState = nextState;
        this.elapsed = 0f;
    }

    @Override
    public void handleInput() {

    }

    @Override
    public void update(float delta) {
        elapsed += delta;
        if (elapsed >= ANIMATION_DURATION) {
            screen.setState(nextState);
        }
    }

    @Override
    public void render(SpriteBatch batch) {

    }
}