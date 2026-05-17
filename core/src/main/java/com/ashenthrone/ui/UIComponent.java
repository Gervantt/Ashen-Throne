package com.ashenthrone.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

public abstract class UIComponent {

    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected boolean visible = true;

    private final List<UIComponent> children = new ArrayList<>();

    private static Texture sharedPixel;
    private static BitmapFont sharedFont;

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

    protected static BitmapFont font() {
        if (sharedFont == null) {
            sharedFont = new BitmapFont();
        }
        return sharedFont;
    }

    public void addChild(UIComponent child) {
        children.add(child);
    }

    public void update(float delta) {
        for (UIComponent child : children) {
            child.update(delta);
        }
    }


    public void render(SpriteBatch batch) {
        if (!visible) return;
        for (UIComponent child : children) {
            if (child.visible) {
                child.render(batch);
            }
        }
    }

    public void dispose() {
        for (UIComponent child : children) {
            child.dispose();
        }
    }


    public static void disposeShared() {
        if (sharedPixel != null) { sharedPixel.dispose(); sharedPixel = null; }
        if (sharedFont  != null) { sharedFont.dispose();  sharedFont  = null; }
    }
}
