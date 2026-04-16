package com.ashenthrone.battle.state;

import com.ashenthrone.battle.ActionType;
import com.ashenthrone.core.GameSession;
import com.ashenthrone.input.BattleInputAdapter;
import com.ashenthrone.observer.EventManager;
import com.ashenthrone.observer.GameEvent;
import com.ashenthrone.screens.BattleScreen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Terminal state reached when all enemies have been defeated.
 *
 * On entry (first update() call), grants gold proportional to the current
 * encounter index and advances the encounter counter in GameSession.
 *
 * The player presses Enter/Space (translated by BattleInputAdapter, AT-012)
 * to proceed to the next encounter.
 * Screen navigation is a placeholder until AT-013 implements the full
 * screen flow through AshenThroneGame.setScreen().
 */
public class VictoryState implements BattleState, BattleInputAdapter.ActionListener {

    /** Base gold reward; multiplied by (encounterIndex + 1). */
    private static final int BASE_GOLD_REWARD = 10;

    private final BattleScreen screen;
    private boolean rewardGranted;

    public VictoryState(BattleScreen screen) {
        this.screen = screen;
        this.rewardGranted = false;
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
        if (!rewardGranted) {
            int reward = BASE_GOLD_REWARD * (GameSession.getInstance().getCurrentEncounterIndex() + 1);
            GameSession.getInstance().addGold(reward);
            GameSession.getInstance().advanceEncounter();
            rewardGranted = true;
            EventManager.getInstance().publish(GameEvent.battleEnd("VICTORY"));
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // TODO: AT-015 — VictoryUI panel (gold earned, proceed button)
    }

    // ---- BattleInputAdapter.ActionListener ----

    @Override
    public void onConfirm() {
        proceedToNextEncounter();
    }

    @Override public void onActionSelected(ActionType type) {}
    @Override public void onTargetSelected(int enemyIndex)  {}
    @Override public void onCancel()                        {}

    // ---- Navigation ----

    private void proceedToNextEncounter() {
        // TODO: AT-013 — navigate to VictoryScreen or next BattleScreen
        // via AshenThroneGame.getInstance().setScreen(...)
    }
}
