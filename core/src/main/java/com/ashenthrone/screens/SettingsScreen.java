package com.ashenthrone.screens;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.core.AshenThroneGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Settings screen (AT-017).
 *
 * Two sections:
 *   1. Display — toggle between fullscreen and windowed (1280×720).
 *   2. Controls — read-only list of in-game key bindings.
 *
 * Display preference is persisted via libGDX {@link Preferences} under the
 * key {@code "fullscreen"}; the chosen mode is applied immediately and on
 * subsequent launches by reading the same pref at startup (callers that
 * boot the application can use {@link #applySavedDisplayMode()}).
 */
public class SettingsScreen extends BaseScreen {

    private static final int SCREEN_W = 1280;
    private static final int SCREEN_H = 720;

    private static final String PREFS_NAME = "ashen-throne";
    private static final String PREF_FULLSCREEN = "fullscreen";

    private static final String[][] KEY_BINDINGS = {
            { "1 / 2 / 3 / 4", "Select action" },
            { "Arrows / Click", "Select target" },
            { "Enter / Space",  "Confirm" },
            { "Escape",         "Cancel / Pause" },
            { "U",              "Undo last action" },
    };

    private SpriteBatch batch;
    private BitmapFont  titleFont;
    private BitmapFont  bodyFont;
    private Texture     pixel;
    private GlyphLayout layout;
    private Viewport    viewport;
    private final Vector3 touchTmp = new Vector3();

    private boolean fullscreen;

    // Three interactive buttons: Toggle Display, Apply, Back.
    private static final int BTN_TOGGLE = 0;
    private static final int BTN_BACK   = 1;
    private int hovered = BTN_TOGGLE;

    public SettingsScreen(AshenThroneGame game) {
        super(game);
    }

    /**
     * Reads the persisted display mode and applies it. Safe to call before
     * the first frame; should be invoked from {@code AshenThroneGame.create}
     * once asset/window setup is done.
     */
    public static void applySavedDisplayMode() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        boolean fs = prefs.getBoolean(PREF_FULLSCREEN, false);
        applyDisplayMode(fs);
    }

    private static void applyDisplayMode(boolean fullscreen) {
        if (fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(SCREEN_W, SCREEN_H);
        }
    }

    @Override
    public void show() {
        batch      = new SpriteBatch();
        titleFont  = new BitmapFont();
        bodyFont   = new BitmapFont();
        layout     = new GlyphLayout();
        viewport   = new FitViewport(SCREEN_W, SCREEN_H);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        titleFont.getData().setScale(2.4f);
        bodyFont.getData().setScale(1.3f);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        fullscreen = prefs.getBoolean(PREF_FULLSCREEN, false);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    activate(BTN_BACK);
                    return true;
                }
                if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
                    hovered = (hovered - 1 + 2) % 2;
                    return true;
                }
                if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
                    hovered = (hovered + 1) % 2;
                    return true;
                }
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    activate(hovered);
                    return true;
                }
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                int idx = buttonAt(screenX, screenY);
                if (idx >= 0) hovered = idx;
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                int idx = buttonAt(screenX, screenY);
                if (idx >= 0) {
                    hovered = idx;
                    activate(idx);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.06f, 0.04f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        titleFont.setColor(new Color(0.85f, 0.7f, 0.3f, 1f));
        layout.setText(titleFont, "SETTINGS");
        titleFont.draw(batch, layout,
                (SCREEN_W - layout.width) / 2f, SCREEN_H - 60f);

        // ---- Display section ----
        bodyFont.setColor(new Color(0.95f, 0.85f, 0.5f, 1f));
        bodyFont.draw(batch, "Display", 120f, SCREEN_H - 130f);

        bodyFont.setColor(Color.LIGHT_GRAY);
        bodyFont.draw(batch,
                "Mode: " + (fullscreen ? "Fullscreen" : "Windowed (1280x720)"),
                160f, SCREEN_H - 165f);

        drawButton("Toggle Display Mode",
                140f, SCREEN_H - 230f, 320f, 48f, hovered == BTN_TOGGLE);

        // ---- Controls section ----
        bodyFont.setColor(new Color(0.95f, 0.85f, 0.5f, 1f));
        bodyFont.draw(batch, "Controls", 120f, SCREEN_H - 320f);

        bodyFont.setColor(Color.LIGHT_GRAY);
        float listY = SCREEN_H - 360f;
        for (int i = 0; i < KEY_BINDINGS.length; i++) {
            float y = listY - i * 28f;
            bodyFont.draw(batch, KEY_BINDINGS[i][0], 160f, y);
            bodyFont.draw(batch, KEY_BINDINGS[i][1], 380f, y);
        }

        // ---- Back button ----
        drawButton("Back", (SCREEN_W - 200f) / 2f, 40f, 200f, 50f, hovered == BTN_BACK);

        batch.end();
    }

    private void drawButton(String label, float x, float y, float w, float h, boolean isHovered) {
        Color bg     = isHovered ? new Color(0.30f, 0.20f, 0.10f, 1f) : new Color(0.12f, 0.10f, 0.14f, 1f);
        Color border = isHovered ? new Color(0.95f, 0.75f, 0.35f, 1f) : new Color(0.45f, 0.35f, 0.20f, 1f);

        batch.setColor(border);
        batch.draw(pixel, x - 2, y - 2, w + 4, h + 4);
        batch.setColor(bg);
        batch.draw(pixel, x, y, w, h);
        batch.setColor(Color.WHITE);

        bodyFont.setColor(isHovered ? new Color(1f, 0.95f, 0.7f, 1f) : Color.LIGHT_GRAY);
        layout.setText(bodyFont, label);
        bodyFont.draw(batch, layout,
                x + (w - layout.width) / 2f,
                y + (h + layout.height) / 2f);
    }

    private int buttonAt(int screenX, int screenY) {
        touchTmp.set(screenX, screenY, 0);
        viewport.unproject(touchTmp);
        float vx = touchTmp.x;
        float vy = touchTmp.y;
        if (inRect(vx, vy, 140f, SCREEN_H - 230f, 320f, 48f)) return BTN_TOGGLE;
        if (inRect(vx, vy, (SCREEN_W - 200f) / 2f, 40f, 200f, 50f)) return BTN_BACK;
        return -1;
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.update(width, height, true);
    }

    private static boolean inRect(float px, float py, float x, float y, float w, float h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    private void activate(int index) {
        switch (index) {
            case BTN_TOGGLE -> {
                AudioManager.getInstance().playSFX("transition_whoosh");
                fullscreen = !fullscreen;
                Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
                prefs.putBoolean(PREF_FULLSCREEN, fullscreen);
                prefs.flush();
                applyDisplayMode(fullscreen);
            }
            case BTN_BACK -> com.ashenthrone.transition.TransitionManager.getInstance()
                    .goTo(com.ashenthrone.transition.ScreenType.MAIN_MENU);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        titleFont.dispose();
        bodyFont.dispose();
        pixel.dispose();
    }
}