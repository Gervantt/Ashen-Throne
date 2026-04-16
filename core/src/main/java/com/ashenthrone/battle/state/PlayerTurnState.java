package com.ashenthrone.battle.state;

import com.ashenthrone.battle.ActionType;
import com.ashenthrone.battle.command.AttackCommand;
import com.ashenthrone.battle.command.BattleCommand;
import com.ashenthrone.battle.command.DefendCommand;
import com.ashenthrone.battle.command.SkillCommand;
import com.ashenthrone.battle.command.UseItemCommand;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.Hero;
import com.ashenthrone.input.BattleInputAdapter;
import com.ashenthrone.screens.BattleScreen;
import com.ashenthrone.strategy.PhysicalAttack;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Active state while the player is choosing and confirming an action (AT-006).
 *
 * Implements {@link BattleInputAdapter.ActionListener} so all input arrives
 * via game-level callbacks — no libGDX Input constants here (AT-012).
 *
 * Key bindings (translated by BattleInputAdapter):
 *   1-4        — select action (Attack / Defend / Skill / Item)
 *   Left/Right — cycle enemy target
 *   Enter/Space — confirm selection
 *   Z / Escape — undo the previous command
 *
 * On confirm, the chosen action is wrapped in a BattleCommand (AT-007),
 * executed via BattleScreen.executeCommand(), then the state transitions to
 * AnimationState → EnemyTurnState or VictoryState.
 */
public class PlayerTurnState implements BattleState, BattleInputAdapter.ActionListener {

    private final BattleScreen screen;
    private ActionType selectedAction;
    private int        targetIndex;

    public PlayerTurnState(BattleScreen screen) {
        this.screen         = screen;
        this.selectedAction = ActionType.ATTACK;
        this.targetIndex    = 0;
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
        // Input-driven state; no per-frame logic needed.
    }

    @Override
    public void render(SpriteBatch batch) {
        // TODO: AT-011 — delegate to ActionMenu UIComponent
        // TODO: AT-015 — styled action buttons, highlight selected action
        // TODO: AT-015 — show "(Z) Undo" button when screen.canUndo() is true
    }

    // ---- BattleInputAdapter.ActionListener ----

    @Override
    public void onActionSelected(ActionType type) {
        selectedAction = type;
    }

    @Override
    public void onTargetSelected(int enemyIndex) {
        targetIndex = enemyIndex;
    }

    @Override
    public void onConfirm() {
        confirmAction();
    }

    /** Z or Escape — undo the most recent command. */
    @Override
    public void onCancel() {
        if (screen.canUndo()) {
            screen.undoLastCommand();
        }
    }

    // ---- Action execution ----

    private void confirmAction() {
        Hero hero = screen.getHero();
        List<Enemy> enemies = screen.getEnemies();

        // Resolve target: prefer targetIndex if alive, otherwise fall back to first alive.
        Enemy target = resolveTarget(enemies);

        // Ensure the hero always has a strategy when the player presses SKILL.
        // TODO: AT-013 — swap strategy via Skill submenu selection.
        if (hero.getCurrentStrategy() == null) {
            hero.setCurrentStrategy(new PhysicalAttack());
        }

        // SKILL passes all alive enemies so AoE strategies can hit each one.
        List<Enemy> aliveEnemies = enemies.stream()
                .filter(Enemy::isAlive)
                .collect(Collectors.toList());

        BattleCommand cmd = switch (selectedAction) {
            case ATTACK -> target != null ? new AttackCommand(hero, target) : null;
            case DEFEND -> new DefendCommand(hero);
            case SKILL  -> !aliveEnemies.isEmpty()
                    ? new SkillCommand(hero, List.copyOf(aliveEnemies))
                    : null;
            case ITEM   -> new UseItemCommand(hero);
        };

        if (cmd != null) {
            // AT-010: route through BattleEngine.executePlayerAction()
            screen.executeCommand(cmd);
        }

        // AT-010: use DeathChecker via BattleEngine instead of inline stream check.
        BattleState nextState = "VICTORY".equals(screen.getBattleEngine().getResult())
                ? new VictoryState(screen)
                : new EnemyTurnState(screen);

        screen.setState(new AnimationState(screen, nextState));
    }

    /**
     * Returns the enemy at {@code targetIndex} if alive, otherwise the first alive enemy.
     * Returns null if there are no alive enemies.
     */
    private Enemy resolveTarget(List<Enemy> enemies) {
        if (targetIndex >= 0 && targetIndex < enemies.size() && enemies.get(targetIndex).isAlive()) {
            return enemies.get(targetIndex);
        }
        for (Enemy e : enemies) {
            if (e.isAlive()) return e;
        }
        return null;
    }

    // ---- Accessors (for UI rendering, AT-011) ----

    public ActionType getSelectedAction() { return selectedAction; }
    public int        getTargetIndex()    { return targetIndex; }
}
