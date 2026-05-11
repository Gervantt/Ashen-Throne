package com.ashenthrone.battle.state;

import com.ashenthrone.battle.ActionType;
import com.ashenthrone.core.GameSession;
import com.ashenthrone.input.BattleInputAdapter;
import com.ashenthrone.observer.EventManager;
import com.ashenthrone.observer.GameEvent;
import com.ashenthrone.screens.BattleScreen;
import com.ashenthrone.screens.RealmSelectScreen;
import com.ashenthrone.transition.ScreenType;
import com.ashenthrone.transition.TransitionManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Terminal state reached when all enemies in a wave have been defeated.
 *
 * <p>On entry: grants a per-encounter gold reward and publishes
 * {@code BATTLE_END:VICTORY}.
 *
 * <p>On confirm (AT-024 dispatch):
 * <ul>
 *   <li>Throne realm cleared      → {@link EndGameScreen} (game complete).</li>
 *   <li>Last wave of other realm  → {@link VictoryScreen} in "realm complete" mode.</li>
 *   <li>Intermediate wave         → {@link VictoryScreen} in "next wave" mode.</li>
 * </ul>
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
        // Visual handled by VictoryScreen on confirm.
    }

    // ---- BattleInputAdapter.ActionListener ----

    @Override
    public void onConfirm() {
        proceed();
    }

    @Override public void onActionSelected(ActionType type) {}
    @Override public void onTargetSelected(int enemyIndex)  {}
    @Override public void onCancel()                        {}

    // ---- Navigation (AT-024) ----

    private void proceed() {
        GameSession session = GameSession.getInstance();
        String realm = session.getCurrentRealm();
        TransitionManager tm = TransitionManager.getInstance();

        // Legacy/sandbox flow: no active realm — keep old single-screen behaviour.
        if (realm == null) {
            tm.goToVictory(screen.getHero(), false);
            return;
        }

        int waveJustCleared = session.getCurrentWaveInRealm();
        int totalWaves = RealmSelectScreen.totalWaves(realm);
        boolean isLastWave = (waveJustCleared >= totalWaves - 1);

        if (isLastWave && RealmSelectScreen.KEY_THRONE.equals(realm)) {
            session.markRealmCompleted(realm);
            tm.goTo(ScreenType.END_GAME);
        } else if (isLastWave) {
            tm.goToVictory(screen.getHero(), true);
        } else {
            tm.goToVictory(screen.getHero(), false);
        }
    }
}