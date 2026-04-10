package com.ashenthrone.battle.state;

import com.ashenthrone.battle.ActionType;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.Hero;
import com.ashenthrone.screens.BattleScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

/**
 * Active state while the player is choosing and confirming an action.
 *
 * Key bindings (temporary until AT-012 BattleInputAdapter):
 *   1 — Attack   2 — Defend   3 — Skill   4 — Item
 *   Enter/Space — confirm selection
 *
 * On confirm the chosen action is executed inline (placeholder logic until
 * AT-007 wraps actions in BattleCommands and AT-010 routes them through
 * BattleEngine). After execution, transitions to AnimationState, which then
 * hands off to EnemyTurnState or VictoryState.
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
    }

    // ---- Action execution ----

    private void confirmAction() {
        Hero hero = screen.getHero();
        List<Enemy> enemies = screen.getEnemies();
        Enemy target = firstAliveEnemy(enemies);

        // TODO: AT-007 — wrap each branch in a BattleCommand (execute + undo)
        // TODO: AT-010 — route through BattleEngine.executePlayerAction(cmd)
        switch (selectedAction) {
            case ATTACK:
                if (target != null) {
                    int damage = Math.max(1, hero.getAttack() - target.getDefense());
                    target.takeDamage(damage);
                }
                break;
            case DEFEND:
                hero.setDefending(true);
                break;
            case SKILL:
                // TODO: AT-008 — execute hero's current AttackStrategy
                if (target != null) {
                    int damage = Math.max(1, hero.getAttack() - target.getDefense());
                    target.takeDamage(damage);
                }
                break;
            case ITEM:
                // TODO: AT-007 — UseItemCommand consumes item from inventory
                break;
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