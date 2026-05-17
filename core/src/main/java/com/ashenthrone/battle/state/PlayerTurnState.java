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

        screen.getInputAdapter().setListener(this);
    }

    @Override
    public void handleInput() {

    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void render(SpriteBatch batch) {

        screen.getActionMenu().setSelected(selectedAction);
    }

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

    @Override
    public void onPause() {
        if (screen.getCurrentState() != this) return;
        screen.setState(new PauseState(screen, this));
    }

    private void confirmAction() {
        AbstractCharacter hero = screen.getHero();
        List<Enemy> enemies = screen.getEnemies();

        Enemy target = resolveTarget(enemies);

        if (selectedAction == ActionType.ATTACK) {

            if (target == null) return;
            screen.setState(new TimingBarState(screen, target));
            return;
        }

        if (!inventoryOpen) {
            openInventory();
            return;
        }

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

    public ActionType getSelectedAction() { return selectedAction; }
    public int        getTargetIndex()    { return targetIndex; }
}
