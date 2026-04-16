package com.ashenthrone.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * A container UIComponent that optionally draws a solid background,
 * then delegates rendering to all of its children (AT-011 Composite).
 *
 * Usage:
 *   Panel root = new Panel(0, 0, 1280, 720);          // transparent root
 *   Panel bg   = new Panel(20, 20, 200, 50, Color.DARK_GRAY);
 *   root.addChild(bg);
 *   root.render(batch);   // draws bg, then all children recursively
 */
public class Panel extends UIComponent {

    private final Color background;

    /** Panel with a solid background color. */
    public Panel(float x, float y, float width, float height, Color background) {
        this.x = x;
        this.y = y;
        this.width  = width;
        this.height = height;
        this.background = background;
    }

    /** Transparent panel (pure container). */
    public Panel(float x, float y, float width, float height) {
        this(x, y, width, height, null);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!visible) return;
        if (background != null) {
            batch.setColor(background);
            batch.draw(pixel(), x, y, width, height);
            batch.setColor(Color.WHITE);
        }
        super.render(batch);
    }
}
