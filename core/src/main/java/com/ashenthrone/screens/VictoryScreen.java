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

public class VictoryScreen extends BaseScreen {

    private static final int SCREEN_W = 1280;
    private static final int SCREEN_H = 720;

    private static final float BTN_W   = 320f;
    private static final float BTN_H   = 60f;
    private static final float BTN_GAP = 24f;

    private final AbstractCharacter hero;
    private final boolean realmComplete;

    private SpriteBatch batch;
    private BitmapFont  titleFont;
    private BitmapFont  infoFont;
    private GlyphLayout layout;
    private Texture     pixel;
    private Viewport    viewport;
    private final Vector3 touchTmp = new Vector3();

    private String[] labels;
    private int hovered = 0;

    public VictoryScreen(AshenThroneGame game, AbstractCharacter hero, boolean realmComplete) {
        super(game);
        this.hero = hero;
        this.realmComplete = realmComplete;
    }

    public VictoryScreen(AshenThroneGame game, AbstractCharacter hero) {
        this(game, hero, false);
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

        if (realmComplete) {
            String key = GameSession.getInstance().getCurrentRealm();
            if (key != null) GameSession.getInstance().markRealmCompleted(key);
        }

        labels = new String[] {
                realmComplete ? "Continue" : "Next Wave",
                "Main Menu"
        };

        AudioManager.getInstance().playMusic("victory_sting");

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
                    hovered = (hovered - 1 + labels.length) % labels.length;
                    return true;
                }
                if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
                    hovered = (hovered + 1) % labels.length;
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
        Gdx.gl.glClearColor(0.05f, 0.1f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        titleFont.setColor(new Color(0.3f, 0.9f, 0.3f, 1f));
        String title = realmComplete ? "REALM CLEARED!" : "WAVE CLEARED!";
        layout.setText(titleFont, title);
        titleFont.draw(batch, layout,
                (SCREEN_W - layout.width) / 2f,
                SCREEN_H / 2f + 160f);

        infoFont.setColor(new Color(0.9f, 0.75f, 0.1f, 1f));
        int gained = GameSession.getInstance().getLastGoldGained();
        String goldText = gained > 0
                ? "+" + gained + " gold collected     Total: " + GameSession.getInstance().getGold()
                : "Gold: " + GameSession.getInstance().getGold();
        layout.setText(infoFont, goldText);
        infoFont.draw(batch, layout,
                (SCREEN_W - layout.width) / 2f,
                SCREEN_H / 2f + 100f);

        drawButtons();

        batch.end();
    }

    private void drawButtons() {
        float totalH = labels.length * BTN_H + (labels.length - 1) * BTN_GAP;
        float firstY = (SCREEN_H + totalH) / 2f - BTN_H - 40f;
        for (int i = 0; i < labels.length; i++) {
            float bx = (SCREEN_W - BTN_W) / 2f;
            float by = firstY - i * (BTN_H + BTN_GAP);
            drawButton(labels[i], bx, by, i == hovered);
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
        float totalH = labels.length * BTN_H + (labels.length - 1) * BTN_GAP;
        float firstY = (SCREEN_H + totalH) / 2f - BTN_H - 40f;
        float bx = (SCREEN_W - BTN_W) / 2f;
        for (int i = 0; i < labels.length; i++) {
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
        if (index == 0) {
            if (realmComplete) goToTower();
            else               goToNextWave();
        } else {
            goToMainMenu();
        }
    }

    private void goToNextWave() {
        GameSession session = GameSession.getInstance();
        WaveIterator iterator = session.getWaveIterator();
        Hero baseHero = (Hero) session.getHero();

        if (iterator == null || !iterator.hasNext() || baseHero == null) {
            TransitionManager.getInstance().goTo(ScreenType.REALM_SELECT);
            return;
        }
        List<Enemy> wave = iterator.next();
        int waveNumber = iterator.getCurrentWaveNumber();
        session.setCurrentWaveInRealm(waveNumber - 1);

        baseHero.setHp(baseHero.getMaxHp());
        applyWaveEscalation(wave, waveNumber - 1);

        AbstractCharacter equipped = ShopScreen.EquipmentApplier.apply(
                baseHero, session.getEquippedItems());
        TransitionManager.getInstance().goToBattle(equipped, wave);
    }

    private void applyWaveEscalation(List<Enemy> wave, int level) {
        if (level <= 0) return;
        for (Enemy enemy : wave) {
            if (enemy != null) enemy.applyWaveEscalation(level);
        }
    }

    private void goToTower() {
        GameSession session = GameSession.getInstance();
        session.setCurrentRealm(null);
        session.setCurrentWaveInRealm(0);
        session.setWaveIterator(null);
        TransitionManager.getInstance().goTo(ScreenType.REALM_SELECT);
    }

    private void goToMainMenu() {

        GameSession session = GameSession.getInstance();
        session.setCurrentRealm(null);
        session.setCurrentWaveInRealm(0);
        session.setWaveIterator(null);
        TransitionManager.getInstance().goTo(ScreenType.MAIN_MENU);
    }
}
