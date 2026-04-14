package com.ashenthrone.battle.state;

import com.ashenthrone.core.GameSession;
import com.ashenthrone.observer.EventManager;
import com.ashenthrone.observer.GameEvent;
import com.ashenthrone.screens.BattleScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Terminal state reached when the hero's HP drops to zero.
 *
 * Key bindings (temporary until AT-012 BattleInputAdapter):
 *   R      — retry the current encounter (restart BattleScreen with same enemies)
 *   Escape — return to the main menu and reset the session
 *
 * Screen navigation is a placeholder until AT-013 implements the full
 * screen flow through AshenThroneGame.setScreen().
 */
public class DefeatState implements BattleState {

    private final BattleScreen screen;
    private boolean eventPublished;

    public DefeatState(BattleScreen screen) {
        this.screen = screen;
    }

    // ---- BattleState ----

    @Override
    public void handleInput() {
        // TODO: AT-012 — replace with BattleInputAdapter callbacks
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            retryEncounter();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            returnToMainMenu();
        }
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