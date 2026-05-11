package com.ashenthrone.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Fullscreen battle background (AT-015 + AT-019/AT-021).
 *
 * <p>Loads the realm-specific texture passed in {@code texturePath} (the path
 * returned by {@link com.ashenthrone.factory.RealmFactory#createBackground()}).
 * Falls back to a flat dark fill when no path is supplied or the file is
 * missing, so battles still render without art.
 */
public class BattleBackground extends UIComponent {

    private static final Color FILL = new Color(0.08f, 0.07f, 0.12f, 1f);

    private final Texture texture;

    /** Solid-color background — no texture. */
    public BattleBackground(float x, float y, float width, float height) {
        this(null, x, y, width, height);
    }

    /**
     * Texture-backed background. {@code texturePath} is resolved through
     * {@link Gdx#files} and may be null or missing; a tinted rectangle stands
     * in for missing art.
     */
    public BattleBackground(String texturePath, float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width  = width;
        this.height = height;
        this.texture = loadOrNull(texturePath);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!visible) return;
        if (texture != null) {
            batch.setColor(Color.WHITE);
            batch.draw(texture, x, y, width, height);
        } else {
            batch.setColor(FILL);
            batch.draw(pixel(), x, y, width, height);
            batch.setColor(Color.WHITE);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (texture != null) texture.dispose();
    }

    private static Texture loadOrNull(String path) {
        if (path == null) return null;
        try {
            com.badlogic.gdx.files.FileHandle fh = Gdx.files.internal(path);
            if (fh.exists()) return new Texture(fh);
        } catch (Exception e) {
            Gdx.app.log("BattleBackground", "Failed to load '" + path + "': " + e.getMessage());
        }
        return null;
    }
}
