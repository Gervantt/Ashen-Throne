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
     * Per-frame input hook. Most states leave this empty: input is delivered
     * asynchronously through {@link com.ashenthrone.input.BattleInputAdapter.ActionListener}
     * (AT-012), which states implement directly. Kept on the interface so a
     * state can still poll if it needs to (e.g. continuous hold detection).
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
     * The shared battle HUD (AT-011 Composite tree built in BattleScreen) is
     * rendered separately on top of this pass, so most states leave this
     * empty and put modal content in {@link #renderOverlay}.
     */
    void render(SpriteBatch batch);

    /**
     * Optional second render pass invoked <em>after</em> the battle HUD, so
     * the state can draw modal/overlay content on top of everything else
     * (AT-023 pause menu). Defaults to a no-op.
     */
    default void renderOverlay(SpriteBatch batch) {}
}
