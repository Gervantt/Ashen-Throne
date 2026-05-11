package com.ashenthrone.input;

import com.ashenthrone.battle.ActionType;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

/**
 * Adapter that translates raw libGDX input events into game-level callbacks (AT-012).
 *
 * This is the only class in the game that may reference {@link com.badlogic.gdx.Input}
 * constants or {@link com.badlogic.gdx.Gdx#input}. All battle states and other game
 * logic interact with input exclusively through the {@link ActionListener} interface.
 *
 * Key bindings:
 *   1-2        → onActionSelected (Attack / Item)
 *   Enter/Space → onConfirm
 *   Escape     → onPause   (AT-023 — opens the in-battle pause menu)
 *   Z          → onCancel  (used for undo in PlayerTurnState)
 *   Left/Right  → onTargetSelected with previous/next enemy index (wraps around)
 *
 * Touch: calls onTargetSelected(0) for now; precise hit-testing deferred to AT-015.
 *
 * Usagalse:
 *   BattleScreen creates one instance and registers it with Gdx.input.setInputProcessor().
 *   Each state that becomes active calls setListener(this) in its constructor.
 */
public class BattleInputAdapter extends InputAdapter {

    /**
     * Game-level input callbacks. Implement this interface in each battle state
     * that needs to react to player input — never reference libGDX Input constants
     * outside of this adapter.
     */
    public interface ActionListener {
        void onActionSelected(ActionType type);
        /** @param enemyIndex 0-based absolute index into the live enemy list */
        void onTargetSelected(int enemyIndex);
        void onConfirm();
        void onCancel();
        /** Escape pressed mid-battle — open the pause menu (AT-023). */
        default void onPause() {}
        /** Vertical navigation for menu states. {@code dy} is -1 (up) or +1 (down). */
        default void onNavigate(int dy) {}
    }

    /**
     * Resolves a window-space pixel to an enemy index, or -1 for "no hit".
     * BattleScreen wires this so the adapter stays decoupled from layout
     * details (which is the whole point of being an adapter).
     */
    @FunctionalInterface
    public interface EnemyHitTester {
        int hit(int screenX, int screenY);
    }

    private ActionListener listener;
    private EnemyHitTester hitTester;
    private int selectedEnemyIndex = 0;
    private int enemyCount = 1;

    // ---- Configuration ----

    /** Replaces the active listener. Call this in each state's constructor. */
    public void setListener(ActionListener listener) {
        this.listener = listener;
    }

    /** Plug in the screen-space → enemy-index resolver. Set once by BattleScreen. */
    public void setEnemyHitTester(EnemyHitTester hitTester) {
        this.hitTester = hitTester;
    }

    /**
     * Updates the upper bound for arrow-key target navigation.
     * Call from BattleScreen when the enemy list changes (on battle start or enemy death).
     */
    public void setEnemyCount(int enemyCount) {
        this.enemyCount = Math.max(1, enemyCount);
        selectedEnemyIndex = Math.min(selectedEnemyIndex, this.enemyCount - 1);
    }

    /** Returns the currently selected enemy index (kept in sync by arrow keys). */
    public int getSelectedEnemyIndex() {
        return selectedEnemyIndex;
    }

    // ---- InputAdapter overrides ----

    @Override
    public boolean keyDown(int keycode) {
        if (listener == null) return false;

        switch (keycode) {
            case Input.Keys.NUM_1 -> listener.onActionSelected(ActionType.ATTACK);
            case Input.Keys.NUM_2 -> listener.onActionSelected(ActionType.ITEM);

            case Input.Keys.ENTER, Input.Keys.SPACE -> listener.onConfirm();

            case Input.Keys.Z      -> listener.onCancel();
            case Input.Keys.ESCAPE -> listener.onPause();

            case Input.Keys.UP, Input.Keys.W   -> listener.onNavigate(-1);
            case Input.Keys.DOWN, Input.Keys.S -> listener.onNavigate(+1);

            case Input.Keys.LEFT -> {
                selectedEnemyIndex = Math.floorMod(selectedEnemyIndex - 1, enemyCount);
                listener.onTargetSelected(selectedEnemyIndex);
            }
            case Input.Keys.RIGHT -> {
                selectedEnemyIndex = Math.floorMod(selectedEnemyIndex + 1, enemyCount);
                listener.onTargetSelected(selectedEnemyIndex);
            }

            default -> { return false; }
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (listener == null) return false;
        int idx = (hitTester != null) ? hitTester.hit(screenX, screenY) : 0;
        if (idx < 0) return false;
        selectedEnemyIndex = Math.min(idx, enemyCount - 1);
        listener.onTargetSelected(selectedEnemyIndex);
        return true;
    }
}
