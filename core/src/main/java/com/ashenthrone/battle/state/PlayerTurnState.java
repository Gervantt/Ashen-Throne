package com.ashenthrone.battle.state;

import com.ashenthrone.battle.ActionType;
import com.ashenthrone.battle.command.AttackCommand;
import com.ashenthrone.battle.command.BattleCommand;
import com.ashenthrone.battle.command.DefendCommand;
import com.ashenthrone.battle.command.SkillCommand;
import com.ashenthrone.battle.command.UseItemCommand;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.Hero;
import com.ashenthrone.screens.BattleScreen;
import com.ashenthrone.strategy.PhysicalAttack;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Active state while the player is choosing and confirming an action.
 *
 * Key bindings (temporary until AT-012 BattleInputAdapter):
 *   1 — Attack   2 — Defend   3 — Skill   4 — Item
 *   Enter/Space — confirm selection
 *
 * On confirm the chosen action is wrapped in a BattleCommand (AT-007),
 * executed via BattleScreen.executeCommand(), and pushed onto the undo history.
 * Pressing Z before confirming a new action undoes the previous command.
 * After execution, transitions to AnimationState → EnemyTurnState or VictoryState.
 *
 * TODO: AT-010 — route through BattleEngine.executePlayerAction(cmd)
 */
public class PlayerTurnState implements BattleState {

    private final BattleScreen screen;
    private ActionType selectedAction;

    public PlayerTurnState(BattleScreen screen) {
        this.screen = screen;
        this.selectedAction = ActionType.ATTACK;
    }

    // ---- BattleState ----

    @Override
    public void handleInput() {
        // TODO: AT-012 — replace with BattleInputAdapter.onActionSelected()
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) selectedAction = ActionType.ATTACK;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) selectedAction = ActionType.DEFEND;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) selectedAction = ActionType.SKILL;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) selectedAction = ActionType.ITEM;

        // AT-007: undo the previous turn's command before confirming a new action.
        // TODO: AT-012 — map to BattleInputAdapter.onCancel() or dedicated undo binding
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z) && screen.canUndo()) {
            screen.undoLastCommand();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            confirmAction();
        }
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

    // ---- Action execution ----

    private void confirmAction() {
        Hero hero = screen.getHero();
        List<Enemy> enemies = screen.getEnemies();
        Enemy target = firstAliveEnemy(enemies);

        // Ensure the hero always has a strategy when the player presses SKILL.
        // TODO: AT-013 — swap strategy via Skill submenu selection.
        if (hero.getCurrentStrategy() == null) {
            hero.setCurrentStrategy(new PhysicalAttack());
        }

        // AT-007: wrap each action in a BattleCommand, execute via screen so it lands
        // on the history stack and can be undone on the next player turn.
        // TODO: AT-010 — route through BattleEngine.executePlayerAction(cmd)
        List<Enemy> aliveEnemies = enemies.stream()
                .filter(Enemy::isAlive)
                .collect(Collectors.toList());

        BattleCommand cmd = switch (selectedAction) {
            case ATTACK -> target != null ? new AttackCommand(hero, target) : null;
            case DEFEND -> new DefendCommand(hero);
            // SKILL uses the hero's current strategy; pass all alive enemies so AoE works.
            case SKILL  -> !aliveEnemies.isEmpty()
                    ? new SkillCommand(hero, List.copyOf(aliveEnemies))
                    : null;
            case ITEM   -> new UseItemCommand(hero);
        };

        if (cmd != null) {
            screen.executeCommand(cmd);
        }

        BattleState nextState = enemies.stream().noneMatch(Enemy::isAlive)
                ? new VictoryState(screen)
                : new EnemyTurnState(screen);

        screen.setState(new AnimationState(screen, nextState));
    }

    private Enemy firstAliveEnemy(List<Enemy> enemies) {
        for (Enemy e : enemies) {
            if (e.isAlive()) return e;
        }
        return null;
    }

    // ---- Accessors (for UI rendering) ----

    public ActionType getSelectedAction() {
        return selectedAction;
    }
}