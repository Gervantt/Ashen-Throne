package com.ashenthrone.screens;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.core.AshenThroneGame;
import com.ashenthrone.core.GameSession;
import com.ashenthrone.transition.ScreenType;
import com.ashenthrone.transition.TransitionManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
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
 * Main menu (AT-016).
 *
 * Four vertically stacked buttons centered on screen: Play, Shop, Settings,
 * Exit. Navigated by mouse hover/click or arrow-keys + Enter.
 *
 * Audio: starts the main-theme music on {@link #show()} and stops it on
 * {@link #hide()}; every button press fires the {@code transition_whoosh}
 * SFX. Both go through {@link AudioManager}, whose methods are still
 * skeleton (AT-014) — concrete sounds wire in at AT-021.
 *
 * Background is a flat dark fantasy placeholder fill until AT-021 supplies
 * the texture.
 */
public class MainMenuScreen extends BaseScreen {

    private static final int SCREEN_W = 1280;
    private static final int SCREEN_H = 720;

    private static final String[] LABELS = { "Play", "Shop", "Settings", "Exit" };
    private static final float BTN_W = 260f;
    private static final float BTN_H = 56f;
    private static final float BTN_GAP = 18f;

    private SpriteBatch  batch;
    private BitmapFont   titleFont;
    private BitmapFont   buttonFont;
    private Texture      pixel;
    private GlyphLayout  layout;
    private Viewport     viewport;
    private final Vector3 touchTmp = new Vector3();

    private int hovered = 0;

    public MainMenuScreen(AshenThroneGame game) {
        super(game);
    }

    @Override
    public void show() {
        batch      = new SpriteBatch();
        titleFont  = new BitmapFont();
        buttonFont = new BitmapFont();
        layout     = new GlyphLayout();
        viewport   = new FitViewport(SCREEN_W, SCREEN_H);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        titleFont.getData().setScale(4f);
        buttonFont.getData().setScale(1.6f);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        AudioManager.getInstance().playMusic("main_theme");

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
                    hovered = (hovered - 1 + LABELS.length) % LABELS.length;
                    return true;
                }
                if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
                    hovered = (hovered + 1) % LABELS.length;
                    return true;
                }
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    activate(hovered);
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    activate(LABELS.length - 1); // Exit
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

        // TODO: AT-021 — replace with main-menu background Texture.

        // Title.
        titleFont.setColor(new Color(0.85f, 0.7f, 0.3f, 1f));
        layout.setText(titleFont, "ASHEN THRONE");
        titleFont.draw(batch, layout,
                (SCREEN_W - layout.width) / 2f,
                SCREEN_H - 120f);

        // Buttons.
        float totalH = LABELS.length * BTN_H + (LABELS.length - 1) * BTN_GAP;
        float firstY = (SCREEN_H + totalH) / 2f - BTN_H;

        for (int i = 0; i < LABELS.length; i++) {
            float bx = (SCREEN_W - BTN_W) / 2f;
            float by = firstY - i * (BTN_H + BTN_GAP);
            drawButton(LABELS[i], bx, by, i == hovered);
        }

        batch.end();
    }

    private void drawButton(String label, float x, float y, boolean isHovered) {
        Color bg     = isHovered ? new Color(0.30f, 0.20f, 0.10f, 1f) : new Color(0.12f, 0.10f, 0.14f, 1f);
        Color border = isHovered ? new Color(0.95f, 0.75f, 0.35f, 1f) : new Color(0.45f, 0.35f, 0.20f, 1f);

        // Border (frame).
        batch.setColor(border);
        batch.draw(pixel, x - 2, y - 2, BTN_W + 4, BTN_H + 4);
        // Fill.
        batch.setColor(bg);
        batch.draw(pixel, x, y, BTN_W, BTN_H);
        batch.setColor(Color.WHITE);

        buttonFont.setColor(isHovered ? new Color(1f, 0.95f, 0.7f, 1f) : Color.LIGHT_GRAY);
        layout.setText(buttonFont, label);
        buttonFont.draw(batch, layout,
                x + (BTN_W - layout.width) / 2f,
                y + (BTN_H + layout.height) / 2f);
    }

    /** Returns the button index under window-space coordinates, or -1. */
    private int buttonAt(int screenX, int screenY) {
        touchTmp.set(screenX, screenY, 0);
        viewport.unproject(touchTmp); // window pixels → virtual 1280x720 coords.
        float vx = touchTmp.x;
        float vy = touchTmp.y;
        float totalH = LABELS.length * BTN_H + (LABELS.length - 1) * BTN_GAP;
        float firstY = (SCREEN_H + totalH) / 2f - BTN_H;
        float bx = (SCREEN_W - BTN_W) / 2f;
        for (int i = 0; i < LABELS.length; i++) {
            float by = firstY - i * (BTN_H + BTN_GAP);
            if (vx >= bx && vx <= bx + BTN_W
                    && vy >= by && vy <= by + BTN_H) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.update(width, height, true);
    }

    private void activate(int index) {
        TransitionManager tm = TransitionManager.getInstance();
        switch (index) {
            case 0 -> {
                GameSession.getInstance().reset();
                tm.goTo(ScreenType.HERO_SELECT);
            }
            case 1 -> tm.goTo(ScreenType.SHOP);
            case 2 -> tm.goTo(ScreenType.SETTINGS);
            case 3 -> {
                // Exit: no transition needed; the whoosh still gives audio feedback.
                AudioManager.getInstance().playSFX("transition_whoosh");
                Gdx.app.exit();
            }
        }
    }

    @Override
    public void hide() {
        AudioManager.getInstance().stopMusic();
    }

    @Override
    public void dispose() {
        batch.dispose();
        titleFont.dispose();
        buttonFont.dispose();
        pixel.dispose();
    }
}