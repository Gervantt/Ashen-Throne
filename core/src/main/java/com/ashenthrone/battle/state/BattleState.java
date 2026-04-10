package com.ashenthrone.battle.state;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * State interface for the Battle State Machine (AT-006).
 *
 * BattleScreen holds a reference to the current BattleState and delegates
 * all per-frame calls to it. Each state transitions to the next by calling
 * BattleScreen.setState().
 *
 * State flow:
 *   PlayerTurnState → AnimationState → EnemyTurnState → AnimationState → PlayerTurnState
 *   PlayerTurnState → AnimationState → VictoryState  (all enemies dead)
 *   EnemyTurnState  → AnimationState → DefeatState   (hero dead)
 */
public interface BattleState {

    /**
     * Poll and process player input for this frame.
     * Called before update() by BattleScreen.render().
     * TODO: AT-012 — replace direct Gdx.input calls with BattleInputAdapter callbacks.
     */
    void handleInput();

    /**
     * Advance game logic by {@code delta} seconds.
     * States that require a timer (e.g. AnimationState) use this to track elapsed time.
     */
    void update(float delta);

    /**
     * Draw this state's UI onto the provided batch.
     * The batch is already begun by BattleScreen before this call.
     * TODO: AT-011 — delegate to UIComponent composite tree.
     * TODO: AT-015 — full visual layout and polish.
     */
    void render(SpriteBatch batch);
}
