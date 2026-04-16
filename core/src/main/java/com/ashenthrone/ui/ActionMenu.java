package com.ashenthrone.ui;

import com.ashenthrone.battle.ActionType;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Horizontal action menu containing four ActionButtons (AT-011 Composite).
 *
 * The four buttons (Attack, Defend, Skill, Item) are laid out in a row.
 * Call setSelected(ActionType) each frame from PlayerTurnState to keep
 * the highlight in sync with the player's current selection.
 */
public class ActionMenu extends UIComponent {

    private static final float BUTTON_GAP = 10f;

    /** Direct reference kept alongside the children list for fast iteration. */
    private final List<ActionButton> buttons = new ArrayList<>();

    public ActionMenu(float x, float y, float width, float height) {
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;

        ActionType[] types = ActionType.values();
        float buttonWidth  = (width - BUTTON_GAP * (types.length - 1)) / types.length;
        float buttonHeight = height - 10f;
        float buttonY      = y + 5f;

        for (int i = 0; i < types.length; i++) {
            float buttonX = x + i * (buttonWidth + BUTTON_GAP);
            ActionButton btn = new ActionButton(types[i], buttonX, buttonY, buttonWidth, buttonHeight);
            buttons.add(btn);
            addChild(btn);
        }
    }

    /**
     * Highlights the button matching {@code selected}; all others are deselected.
     * Call this from PlayerTurnState.render() every frame.
     */
    public void setSelected(ActionType selected) {
        for (ActionButton btn : buttons) {
            btn.setSelected(btn.getType() == selected);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!visible) return;
        super.render(batch);
    }
}
