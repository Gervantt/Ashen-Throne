package com.ashenthrone.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Fullscreen background placeholder for the battle scene (AT-015).
 *
 * Renders a flat dark fill until the realm-specific background texture is
 * supplied by {@code RealmFactory.createBackground()} (AT-019) and registered
 * through {@link com.ashenthrone.audio.GameAssetManager} (AT-021).
 */
public class BattleBackground extends UIComponent {

    private static final Color FILL = new Color(0.08f, 0.07f, 0.12f, 1f);

    public BattleBackground(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width  = width;
        this.height = height;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!visible) return;
        batch.setColor(FILL);
        batch.draw(pixel(), x, y, width, height);
        batch.setColor(Color.WHITE);
        // TODO: AT-021 — replace with realm-specific background Texture.
    }
}