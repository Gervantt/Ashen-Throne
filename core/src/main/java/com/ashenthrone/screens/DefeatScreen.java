package com.ashenthrone.screens;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.battle.WaveIterator;
import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.Hero;
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

import java.util.List;

public class DefeatScreen extends BaseScreen {

    private static final int SCREEN_W = 1280;
    private static final int SCREEN_H = 720;

    private static final float BTN_W   = 320f;
    private static final float BTN_H   = 60f;
    private static final float BTN_GAP = 24f;

    private static final String[] LABELS = { "Retry", "Main Menu" };

    private final AbstractCharacter hero;

    private SpriteBatch batch;
    private BitmapFont  titleFont;
    private BitmapFont  infoFont;
    private GlyphLayout layout;
    private Texture     pixel;
    private Viewport    viewport;
    private final Vector3 touchTmp = new Vector3();

    private int hovered = 0;

    public DefeatScreen(AshenThroneGame game, AbstractCharacter hero) {
        super(game);
        this.hero = hero;
    }

    @Override
    public void show() {
        batch     = new SpriteBatch();
        titleFont = new BitmapFont();
        infoFont  = new BitmapFont();
        layout    = new GlyphLayout();
        viewport  = new FitViewport(SCREEN_W, SCREEN_H);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        titleFont.getData().setScale(3f);
        infoFont.getData().setScale(1.4f);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        AudioManager.getInstance().playMusic("defeat_sting");

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
                    activate(1);
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
        Gdx.gl.glClearColor(0.1f, 0.03f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        titleFont.setColor(new Color(0.85f, 0.15f, 0.15f, 1f));
        layout.setText(titleFont, "DEFEAT");
        titleFont.draw(batch, layout,
                (SCREEN_W - layout.width) / 2f,
                SCREEN_H / 2f + 160f);

        drawButtons();

        batch.end();
    }

    private void drawButtons() {
        float totalH = LABELS.length * BTN_H + (LABELS.length - 1) * BTN_GAP;
        float firstY = (SCREEN_H + totalH) / 2f - BTN_H - 40f;
        for (int i = 0; i < LABELS.length; i++) {
            float bx = (SCREEN_W - BTN_W) / 2f;
            float by = firstY - i * (BTN_H + BTN_GAP);
            drawButton(LABELS[i], bx, by, i == hovered);
        }
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

    private int buttonAt(int screenX, int screenY) {
        touchTmp.set(screenX, screenY, 0);
        viewport.unproject(touchTmp);
        float vx = touchTmp.x;
        float vy = touchTmp.y;
        float totalH = LABELS.length * BTN_H + (LABELS.length - 1) * BTN_GAP;
        float firstY = (SCREEN_H + totalH) / 2f - BTN_H - 40f;
        float bx = (SCREEN_W - BTN_W) / 2f;
        for (int i = 0; i < LABELS.length; i++) {
            float by = firstY - i * (BTN_H + BTN_GAP);
            if (vx >= bx && vx <= bx + BTN_W && vy >= by && vy <= by + BTN_H) {
                return i;
            }
        }
        return -1;
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
        batch.dispose();
        titleFont.dispose();
        infoFont.dispose();
        if (pixel != null) pixel.dispose();
    }

    private void activate(int index) {
        if (index == 0) retry();
        else            mainMenu();
    }

    private void retry() {
        GameSession session = GameSession.getInstance();
        WaveIterator iterator = session.getWaveIterator();
        Hero baseHero = (Hero) session.getHero();

        if (iterator == null || iterator.getCurrentWaveNumber() == 0 || baseHero == null) {
            mainMenu();
            return;
        }

        baseHero.setHp(baseHero.getMaxHp());
        if (hero != null) hero.setHp(hero.getMaxHp());

        AbstractCharacter equipped = ShopScreen.EquipmentApplier.apply(
                baseHero, session.getEquippedItems());

        List<Enemy> wave = iterator.currentWave();
        TransitionManager.getInstance().goToBattle(equipped, wave);
    }

    private void mainMenu() {

        GameSession session = GameSession.getInstance();
        session.abandonActiveRun();
        TransitionManager.getInstance().goTo(ScreenType.MAIN_MENU);
    }
}
