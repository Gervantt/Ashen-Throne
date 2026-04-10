package com.ashenthrone.battle.state;

import com.ashenthrone.screens.BattleScreen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Transient state that holds the battle loop for a fixed duration while
 * attack/damage animations play out, then hands off to the next state.
 *
 * Duration is set to 0.75 s (within the 0.5–1 s window specified by AT-006).
 * Visual animation is a placeholder until AT-015 adds damage number popups
 * and screen shake.
 */
public class AnimationState implements BattleState {

    /** Seconds to wait before transitioning to nextState. */
    private static final float ANIMATION_DURATION = 0.75f;

    private final BattleScreen screen;
    private final BattleState nextState;
    private float elapsed;

    public AnimationState(BattleScreen screen, BattleState nextState) {
        this.screen = screen;
        this.nextState = nextState;
        this.elapsed = 0f;
    }

    // ---- BattleState ----

    @Override
    public void handleInput() {
        // Player input is blocked while animations play.
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
        // TODO: AT-011 — render damage number popups via UIComponent
        // TODO: AT-015 — floating damage numbers (tween up + fade), screen shake
    }
}