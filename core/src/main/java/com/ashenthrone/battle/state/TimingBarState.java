package com.ashenthrone.battle.state;

import com.ashenthrone.battle.ActionType;
import com.ashenthrone.battle.command.AttackCommand;
import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.input.BattleInputAdapter;
import com.ashenthrone.screens.BattleScreen;
import com.ashenthrone.ui.TimingBar;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Transient state shown after the player confirms an Attack.
 *
 * Renders a horizontal {@link TimingBar} whose cursor sweeps left-right.
 * Pressing Confirm (Enter/Space) stops the cursor; the zone it lands on
 * determines the damage multiplier (critical/great/weak/miss). The
 * resulting {@link AttackCommand} is executed and we transition to
 * {@link AnimationState} → {@link EnemyTurnState}/{@link VictoryState}.
 *
 * Z/Escape cancels the attempt and returns to PlayerTurnState so the
 * player can re-select.
 */
public class TimingBarState implements BattleState, BattleInputAdapter.ActionListener {

    /** Seconds the result label stays visible before the attack actually executes. */
    private static final float RESULT_DWELL = 0.35f;

    private final BattleScreen screen;
    private final Enemy target;
    private final TimingBar bar;

    private float resultElapsed;
    private boolean resolved;

    public TimingBarState(BattleScreen screen, Enemy target) {
        this.screen = screen;
        this.target = target;
        // Center the bar above the action menu.
        float w = 480f, h = 24f;
        float bx = (BattleScreen.SCREEN_W - w) / 2f;
        float by = 110f;
        this.bar = new TimingBar(bx, by, w, h);
        screen.addOverlay(bar);
        screen.getInputAdapter().setListener(this);
    }

    // ---- BattleState ----

    @Override public void handleInput() {}

    @Override
    public void update(float delta) {
        bar.update(delta);
        if (bar.isStopped() && !resolved) {
            resultElapsed += delta;
            if (resultElapsed >= RESULT_DWELL) {
                resolveAndExit();
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // Bar lives on the HUD overlay; nothing to draw here.
    }

    // ---- Input ----

    @Override public void onActionSelected(ActionType type) { /* locked while timing */ }
    @Override public void onTargetSelected(int enemyIndex)  { /* locked */ }

    @Override
    public void onConfirm() {
        if (!bar.isStopped()) {
            bar.stop();
        }
    }

    /** Z — cancel the attempt and return to action selection. */
    @Override
    public void onCancel() {
        screen.removeOverlay(bar);
        screen.setState(new PlayerTurnState(screen));
    }

    @Override
    public void onPause() {
        if (screen.getCurrentState() != this) return;
        screen.setState(new PauseState(screen, this));
    }

    // ---- Resolution ----

    private void resolveAndExit() {
        resolved = true;
        screen.removeOverlay(bar);

        AbstractCharacter hero = screen.getHero();
        TimingBar.Result result = bar.stop();
        AttackCommand cmd = new AttackCommand(hero, target, result.multiplier);
        screen.executeCommand(cmd);
        if (result.multiplier > 0f) {
            screen.playHeroActionAnimation();
        }

        BattleState nextState = "VICTORY".equals(screen.getBattleEngine().getResult())
                ? new VictoryState(screen)
                : new EnemyTurnState(screen);
        screen.setState(new AnimationState(screen, nextState));
    }
}
