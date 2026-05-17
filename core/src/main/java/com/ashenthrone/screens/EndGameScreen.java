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

public class EndGameScreen extends BaseScreen {

    private static final int SCREEN_W = 1280;
    private static final int SCREEN_H = 720;

    private static final float BTN_W = 320f;
    private static final float BTN_H = 60f;

    private SpriteBatch batch;
    private BitmapFont  titleFont;
    private BitmapFont  infoFont;
    private GlyphLayout layout;
    private Texture     pixel;
    private Viewport    viewport;
    private final Vector3 touchTmp = new Vector3();

    private boolean hovered = false;

    public EndGameScreen(AshenThroneGame game) {
        super(game);
    }

    @Override
    public void show() {
        batch     = new SpriteBatch();
        titleFont = new BitmapFont();
        infoFont  = new BitmapFont();
        layout    = new GlyphLayout();
        viewport  = new FitViewport(SCREEN_W, SCREEN_H);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        titleFont.getData().setScale(3.4f);
        infoFont.getData().setScale(1.4f);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        AudioManager.getInstance().playMusic("victory_sting");

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE
                        || keycode == Input.Keys.ESCAPE) {
                    activate();
                    return true;
                }
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                hovered = buttonHit(screenX, screenY);
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (buttonHit(screenX, screenY)) {
                    activate();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.06f, 0.04f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        titleFont.setColor(new Color(0.95f, 0.85f, 0.4f, 1f));
        layout.setText(titleFont, "THE THRONE IS YOURS");
        titleFont.draw(batch, layout,
                (SCREEN_W - layout.width) / 2f,
                SCREEN_H / 2f + 200f);

        infoFont.setColor(new Color(0.85f, 0.80f, 0.70f, 1f));
        drawCentered("The Hollow King has fallen.",                  SCREEN_H / 2f + 100f);
        drawCentered("From the ashes, a new age dawns.",             SCREEN_H / 2f + 60f);
        drawCentered("Gold amassed: " + GameSession.getInstance().getGold(), SCREEN_H / 2f + 0f);

        float[] r = buttonRect();
        drawButton("Main Menu", r[0], r[1], hovered);

        batch.end();
    }

    private void drawCentered(String text, float y) {
        layout.setText(infoFont, text);
        infoFont.draw(batch, layout, (SCREEN_W - layout.width) / 2f, y);
    }

    private void drawButton(String label, float x, float y, boolean isHovered) {
        Color border = isHovered ? new Color(0.95f, 0.75f, 0.35f, 1f)
                                 : new Color(0.45f, 0.35f, 0.20f, 1f);
        Color bg     = isHovered ? new Color(0.30f, 0.20f, 0.10f, 1f)
                                 : new Color(0.12f, 0.10f, 0.14f, 1f);
        batch.setColor(border);
        batch.draw(pixel, x - 2, y - 2, BTN_W + 4, BTN_H + 4);
        batch.setColor(bg);
        batch.draw(pixel, x, y, BTN_W, BTN_H);
        batch.setColor(Color.WHITE);

        infoFont.setColor(isHovered ? new Color(1f, 0.95f, 0.7f, 1f) : Color.LIGHT_GRAY);
        layout.setText(infoFont, label);
        infoFont.draw(batch, layout,
                x + (BTN_W - layout.width) / 2f,
                y + (BTN_H + layout.height) / 2f);
    }

    private float[] buttonRect() {
        float x = (SCREEN_W - BTN_W) / 2f;
        float y = SCREEN_H / 2f - 130f;
        return new float[] { x, y, BTN_W, BTN_H };
    }

    private boolean buttonHit(int screenX, int screenY) {
        touchTmp.set(screenX, screenY, 0);
        viewport.unproject(touchTmp);
        float[] r = buttonRect();
        return touchTmp.x >= r[0] && touchTmp.x <= r[0] + BTN_W
                && touchTmp.y >= r[1] && touchTmp.y <= r[1] + BTN_H;
    }

    @Override public void resize(int width, int height) {
        if (viewport != null) viewport.update(width, height, true);
    }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {
        AudioManager.getInstance().stopMusic();
    }

    @Override
    public void dispose() {
        if (batch != null)     batch.dispose();
        if (titleFont != null) titleFont.dispose();
        if (infoFont != null)  infoFont.dispose();
        if (pixel != null)     pixel.dispose();
    }

    private void activate() {

        GameSession.getInstance().reset();
        TransitionManager.getInstance().goTo(ScreenType.MAIN_MENU);
    }
}