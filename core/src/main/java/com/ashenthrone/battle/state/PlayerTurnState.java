package com.ashenthrone.battle.state;

import com.ashenthrone.battle.ActionType;
import com.ashenthrone.battle.command.UseItemCommand;
import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.core.GameSession;
import com.ashenthrone.input.BattleInputAdapter;
import com.ashenthrone.screens.BattleScreen;
import com.ashenthrone.screens.ShopScreen;
import com.ashenthrone.ui.ItemInventoryOverlay;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

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
 *   Z          — undo the previous command
 *   Escape     — open pause menu (AT-023)
 *
 * On confirm, the chosen action is wrapped in a BattleCommand (AT-007),
 * executed via BattleScreen.executeCommand(), then the state transitions to
 * AnimationState → EnemyTurnState or VictoryState.
 */
public class PlayerTurnState implements BattleState, BattleInputAdapter.ActionListener {

    private final BattleScreen screen;
    private ActionType selectedAction;
    private int        targetIndex;
    private boolean inventoryOpen;
    private ItemInventoryOverlay inventoryOverlay;

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
        // AT-011: keep the ActionMenu highlight in sync with the current selection.
        screen.getActionMenu().setSelected(selectedAction);
    }

    // ---- BattleInputAdapter.ActionListener ----

    @Override
    public void onActionSelected(ActionType type) {
        selectedAction = type;
        if (type == ActionType.ITEM) {
            openInventory();
        } else {
            closeInventory();
        }
    }

    @Override
    public void onTargetSelected(int enemyIndex) {
        targetIndex = enemyIndex;
    }

    @Override
    public void onConfirm() {
        confirmAction();
    }

    /** Z — undo the most recent command. */
    @Override
    public void onCancel() {
        if (inventoryOpen) {
            closeInventory();
            return;
        }
        if (screen.canUndo()) {
            screen.undoLastCommand();
        }
    }

    /** Escape — pause the battle (AT-023). Only acts while we're the active state. */
    @Override
    public void onPause() {
        if (screen.getCurrentState() != this) return;
        screen.setState(new PauseState(screen, this));
    }

    // ---- Action execution ----

    private void confirmAction() {
        AbstractCharacter hero = screen.getHero();
        List<Enemy> enemies = screen.getEnemies();

        // Resolve target: prefer targetIndex if alive, otherwise fall back to first alive.
        Enemy target = resolveTarget(enemies);

        if (selectedAction == ActionType.ATTACK) {
            // Hand off to the timing-bar mini-game; it builds the AttackCommand
            // once the player stops the cursor and then transitions onward.
            if (target == null) return;
            screen.setState(new TimingBarState(screen, target));
            return;
        }

        if (!inventoryOpen) {
            openInventory();
            return;
        }

        // ITEM consumes the selected Health Potion. Empty inventory keeps the turn.
        if (GameSession.getInstance().getConsumableCount(ShopScreen.ITEM_HEALTH_POTION) <= 0) {
            return;
        }
        UseItemCommand cmd = new UseItemCommand(hero);
        screen.executeCommand(cmd);
        if (!cmd.wasConsumed()) return;
        closeInventory();

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

    private void openInventory() {
        if (inventoryOpen) return;
        inventoryOpen = true;
        if (inventoryOverlay == null) {
            inventoryOverlay = new ItemInventoryOverlay(430f, 105f, 420f, 150f);
        }
        screen.addOverlay(inventoryOverlay);
    }

    private void closeInventory() {
        if (!inventoryOpen) return;
        inventoryOpen = false;
        if (inventoryOverlay != null) screen.removeOverlay(inventoryOverlay);
    }

    // ---- Accessors (for UI rendering, AT-011) ----

    public ActionType getSelectedAction() { return selectedAction; }
    public int        getTargetIndex()    { return targetIndex; }
}
