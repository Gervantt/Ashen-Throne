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

public class DefeatState implements BattleState, BattleInputAdapter.ActionListener {

    private final BattleScreen screen;
    private boolean eventPublished;

    public DefeatState(BattleScreen screen) {
        this.screen = screen;

        screen.getInputAdapter().setListener(this);
    }

    @Override
    public void handleInput() {

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

    }

    @Override
    public void onConfirm() {
        retryEncounter();
    }

    @Override
    public void onCancel() {
        returnToMainMenu();
    }

    @Override public void onActionSelected(ActionType type) {}
    @Override public void onTargetSelected(int enemyIndex)  {}

    private void retryEncounter() {
        TransitionManager.getInstance().goToDefeat(screen.getHero());
    }

    private void returnToMainMenu() {
        GameSession.getInstance().abandonActiveRun();
        TransitionManager.getInstance().goTo(ScreenType.MAIN_MENU);
    }
}
