package com.ashenthrone.battle.state;

import com.ashenthrone.battle.ActionType;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.core.GameSession;
import com.ashenthrone.input.BattleInputAdapter;
import com.ashenthrone.observer.EventManager;
import com.ashenthrone.observer.GameEvent;
import com.ashenthrone.battle.WaveIterator;
import com.ashenthrone.screens.BattleScreen;
import com.ashenthrone.screens.RealmSelectScreen;
import com.ashenthrone.transition.ScreenType;
import com.ashenthrone.transition.TransitionManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class VictoryState implements BattleState, BattleInputAdapter.ActionListener {

    private final BattleScreen screen;
    private boolean rewardGranted;

    public VictoryState(BattleScreen screen) {
        this.screen = screen;
        this.rewardGranted = false;

        screen.getInputAdapter().setListener(this);
    }

    @Override
    public void handleInput() {

    }

    @Override
    public void update(float delta) {
        if (!rewardGranted) {
            GameSession session = GameSession.getInstance();
            int reward = calculateGoldReward();
            session.addGold(reward);
            session.setLastGoldGained(reward);
            session.advanceEncounter();
            rewardGranted = true;
            EventManager.getInstance().publish(GameEvent.battleEnd("VICTORY"));
        }
    }

    @Override
    public void render(SpriteBatch batch) {

    }

    @Override
    public void onConfirm() {
        proceed();
    }

    @Override public void onActionSelected(ActionType type) {}
    @Override public void onTargetSelected(int enemyIndex)  {}
    @Override public void onCancel()                        {}

    private int calculateGoldReward() {
        int total = 0;
        for (Enemy enemy : screen.getEnemies()) {
            total += rewardFor(enemy);
        }
        return total;
    }

    private int rewardFor(Enemy enemy) {
        if (enemy == null) return 0;
        String type = enemy.getType();
        if ("HollowKing".equals(type)) return 100;
        if ("Wraith".equals(type) || "Treant".equals(type)) return randomBetween(30, 50);
        return randomBetween(10, 20);
    }

    private int randomBetween(int min, int max) {
        return min + com.badlogic.gdx.math.MathUtils.random(max - min);
    }

    private void proceed() {
        GameSession session = GameSession.getInstance();
        String realm = session.getCurrentRealm();
        WaveIterator iterator = session.getWaveIterator();
        TransitionManager tm = TransitionManager.getInstance();

        if (realm == null || iterator == null) {
            tm.goToVictory(screen.getHero(), false);
            return;
        }

        boolean isLastWave = !iterator.hasNext();

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
