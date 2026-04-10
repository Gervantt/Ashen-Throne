package com.ashenthrone.battle.state;

import com.ashenthrone.core.GameSession;
import com.ashenthrone.screens.BattleScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Terminal state reached when all enemies have been defeated.
 *
 * On entry (first update() call), grants gold proportional to the current
 * encounter index and advances the encounter counter in GameSession.
 *
 * The player presses Enter/Space to proceed to the next encounter.
 * Screen navigation is a placeholder until AT-013 implements the full
 * screen flow through AshenThroneGame.setScreen().
 */
public class VictoryState implements BattleState {

    /** Base gold reward; multiplied by (encounterIndex + 1). */
    private static final int BASE_GOLD_REWARD = 10;

    private final BattleScreen screen;
    private boolean rewardGranted;

    public VictoryState(BattleScreen screen) {
        this.screen = screen;
        this.rewardGranted = false;
    }

    // ---- BattleState ----

    @Override
    public void handleInput() {
        // TODO: AT-012 — replace with BattleInputAdapter.onConfirm()
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            proceedToNextEncounter();
        }
    }

    @Override
    public void update(float delta) {
        if (!rewardGranted) {
            int reward = BASE_GOLD_REWARD * (GameSession.getInstance().getCurrentEncounterIndex() + 1);
            GameSession.getInstance().addGold(reward);
            GameSession.getInstance().advanceEncounter();
            rewardGranted = true;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // TODO: AT-011 — render VictoryUI panel (gold earned, proceed button)
        // TODO: AT-015 — full visual layout
    }

    // ---- Navigation ----

    private void proceedToNextEncounter() {
        // TODO: AT-013 — navigate to VictoryScreen or next BattleScreen
        // via AshenThroneGame.getInstance().setScreen(...)
    }
}