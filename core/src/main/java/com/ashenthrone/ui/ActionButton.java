package com.ashenthrone.ui;

import com.ashenthrone.battle.ActionType;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * A single action button in the battle menu (AT-011).
 *
 * Renders a dark-grey rectangle with the action label centered inside.
 * When selected, a yellow highlight border is drawn around the button.
 */
public class ActionButton extends UIComponent {

    private static final Color BG_COLOR       = new Color(0.15f, 0.15f, 0.2f, 0.9f);
    private static final Color SELECTED_COLOR = new Color(0.9f, 0.75f, 0.1f, 1f);
    private static final float BORDER         = 2f;

    private final ActionType type;
    private final String label;
    private boolean selected;

    private final GlyphLayout layout = new GlyphLayout();

    public ActionButton(ActionType type, float x, float y, float width, float height) {
        this.type  = type;
        this.label = labelFor(type);
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public ActionType getType() {
        return type;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!visible) return;

        // Yellow border when selected
        if (selected) {
            batch.setColor(SELECTED_COLOR);
            batch.draw(pixel(), x - BORDER, y - BORDER, width + BORDER * 2, height + BORDER * 2);
        }

        // Button background
        batch.setColor(BG_COLOR);
        batch.draw(pixel(), x, y, width, height);
        batch.setColor(Color.WHITE);

        // Centered label
        font().setColor(selected ? SELECTED_COLOR : Color.WHITE);
        layout.setText(font(), label);
        float textX = x + (width  - layout.width)  / 2f;
        float textY = y + (height + layout.height) / 2f;
        font().draw(batch, layout, textX, textY);
    }

    private static String labelFor(ActionType type) {
        return switch (type) {
            case ATTACK -> "1 Attack";
            case DEFEND -> "2 Defend";
            case SKILL  -> "3 Skill";
            case ITEM   -> "4 Item";
        };
    }
}
