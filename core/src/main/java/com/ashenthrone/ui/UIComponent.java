package com.ashenthrone.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base for all battle HUD components (AT-011 Composite).
 *
 * Leaf components override render() to draw themselves.
 * Container components (Panel, ActionMenu) add children via addChild()
 * and call super.render() to propagate drawing down the tree.
 *
 * Shared rendering resources (white pixel texture, default font) are
 * lazily initialised here so every subclass can use them without
 * duplicating creation logic.
 */
public abstract class UIComponent {

    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected boolean visible = true;

    private final List<UIComponent> children = new ArrayList<>();

    // ---- Shared resources (lazy, created on first use after GL context is ready) ----

    private static Texture sharedPixel;
    private static BitmapFont sharedFont;

    /** 1×1 white texture for tinted rectangle drawing via SpriteBatch. */
    protected static Texture pixel() {
        if (sharedPixel == null) {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(1f, 1f, 1f, 1f);
            pm.fill();
            sharedPixel = new Texture(pm);
            pm.dispose();
        }
        return sharedPixel;
    }

    /** Default libGDX bitmap font (built-in, no asset loading required). */
    protected static BitmapFont font() {
        if (sharedFont == null) {
            sharedFont = new BitmapFont();
        }
        return sharedFont;
    }

    // ---- Composite interface ----

    /** Adds a child component. Children are rendered in insertion order. */
    public void addChild(UIComponent child) {
        children.add(child);
    }

    /** Propagates update to all children. */
    public void update(float delta) {
        for (UIComponent child : children) {
            child.update(delta);
        }
    }

    /**
     * Renders all visible children. Subclasses should call super.render(batch)
     * after drawing their own content so children appear on top.
     */
    public void render(SpriteBatch batch) {
        if (!visible) return;
        for (UIComponent child : children) {
            if (child.visible) {
                child.render(batch);
            }
        }
    }

    /** Disposes shared resources and all children. Call once on shutdown. */
    public void dispose() {
        for (UIComponent child : children) {
            child.dispose();
        }
    }

    /**
     * Disposes the shared static resources (pixel texture and font).
     * Call this once when the game shuts down, not per-component.
     */
    public static void disposeShared() {
        if (sharedPixel != null) { sharedPixel.dispose(); sharedPixel = null; }
        if (sharedFont  != null) { sharedFont.dispose();  sharedFont  = null; }
    }
}
