package com.ashenthrone.input;

import com.ashenthrone.battle.ActionType;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public class BattleInputAdapter extends InputAdapter {


    public interface ActionListener {
        void onActionSelected(ActionType type);
        void onTargetSelected(int enemyIndex);
        void onConfirm();
        void onCancel();
        default void onPause() {}
        default void onNavigate(int dy) {}
    }


    @FunctionalInterface
    public interface EnemyHitTester {
        int hit(int screenX, int screenY);
    }

    private ActionListener listener;
    private EnemyHitTester hitTester;
    private int selectedEnemyIndex = 0;
    private int enemyCount = 1;

    public void setListener(ActionListener listener) {
        this.listener = listener;
    }

    public void setEnemyHitTester(EnemyHitTester hitTester) {
        this.hitTester = hitTester;
    }


    public void setEnemyCount(int enemyCount) {
        this.enemyCount = Math.max(1, enemyCount);
        selectedEnemyIndex = Math.min(selectedEnemyIndex, this.enemyCount - 1);
    }

    public int getSelectedEnemyIndex() {
        return selectedEnemyIndex;
    }

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
