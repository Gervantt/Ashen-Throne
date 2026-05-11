package com.ashenthrone.battle.state;

import com.ashenthrone.battle.ActionType;
import com.ashenthrone.core.GameSession;
import com.ashenthrone.input.BattleInputAdapter;
import com.ashenthrone.observer.EventManager;
import com.ashenthrone.observer.GameEvent;
import com.ashenthrone.screens.BattleScreen;
import com.ashenthrone.transition.ScreenType;
import com.ashenthrone.transition.TransitionManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Terminal state reached when the hero's HP drops to zero.
 *
 * Key bindings (translated by BattleInputAdapter, AT-012):
 *   Enter/Space — retry the current encounter
 *   Escape/Z    — return to the main menu and keep run progress
 *
 * Both actions now navigate through AshenThroneGame.setScreen() (AT-013).
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
        // Retry / Main Menu UI lives on DefeatScreen (AT-013/AT-024); this
        // state is transient — the next BATTLE_END tick navigates away.
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
        TransitionManager.getInstance().goToDefeat(screen.getHero());
    }

    private void returnToMainMenu() {
        GameSession.getInstance().abandonActiveRun();
        TransitionManager.getInstance().goTo(ScreenType.MAIN_MENU);
    }
}
