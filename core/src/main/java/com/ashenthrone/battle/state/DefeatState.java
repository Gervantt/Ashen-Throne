package com.ashenthrone.battle.state;

import com.ashenthrone.battle.ActionType;
import com.ashenthrone.core.GameSession;
import com.ashenthrone.input.BattleInputAdapter;
import com.ashenthrone.observer.EventManager;
import com.ashenthrone.observer.GameEvent;
import com.ashenthrone.screens.BattleScreen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Terminal state reached when the hero's HP drops to zero.
 *
 * Key bindings (translated by BattleInputAdapter, AT-012):
 *   Enter/Space — retry the current encounter
 *   Escape/Z    — return to the main menu and reset the session
 *
 * Screen navigation is a placeholder until AT-013 implements the full
 * screen flow through AshenThroneGame.setScreen().
 */
public class DefeatState implements BattleState, BattleInputAdapter.ActionListener {

    private final BattleScreen screen;
    private boolean eventPublished;

    public DefeatState(BattleScreen screen) {
        this.screen = screen;
        // AT-012: register as the active input listener for this state.
        screen.getInputAdapter().setListener(this);
    }

    // ---- BattleState ----

    @Override
    public void handleInput() {
        // Input arrives via ActionListener callbacks — no polling needed.
    }

    @Override
    public void update(float delta) {
        if (!eventPublished) {
            EventManager.getInstance().publish(GameEvent.battleEnd("DEFEAT"));
            eventPublished = true;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // TODO: AT-011 — render DefeatUI panel (retry / main menu buttons)
        // TODO: AT-015 — full visual layout
    }

    // ---- BattleInputAdapter.ActionListener ----

    /** Enter/Space — retry the encounter. */
    @Override
    public void onConfirm() {
        retryEncounter();
    }

    /** Escape/Z — exit to main menu. */
    @Override
    public void onCancel() {
        returnToMainMenu();
    }

    @Override public void onActionSelected(ActionType type) {}
    @Override public void onTargetSelected(int enemyIndex)  {}

    // ---- Navigation ----

    private void retryEncounter() {
        // TODO: AT-013 — re-create BattleScreen for the same encounter
        // via AshenThroneGame.getInstance().setScreen(...)
    }

    private void returnToMainMenu() {
        GameSession.getInstance().reset();
        // TODO: AT-013 — navigate to MainMenuScreen
        // via AshenThroneGame.getInstance().setScreen(new MainMenuScreen(...))
    }
}
