package com.ashenthrone.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Panel extends UIComponent {

    private final Color background;

    public Panel(float x, float y, float width, float height, Color background) {
        this.x = x;
        this.y = y;
        this.width  = width;
        this.height = height;
        this.background = background;
    }

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
