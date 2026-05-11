package com.ashenthrone.screens;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.battle.WaveIterator;
import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.Hero;
import com.ashenthrone.core.AshenThroneGame;
import com.ashenthrone.core.GameSession;
import com.ashenthrone.factory.AbyssRealmFactory;
import com.ashenthrone.factory.AshenThroneRealmFactory;
import com.ashenthrone.factory.CursedForestFactory;
import com.ashenthrone.factory.RealmFactory;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Realm tower / selection screen (AT-019).
 *
 * Tower-style vertical layout with three sections, top to bottom:
 *   1. The Abyss        — 2 waves
 *   2. Cursed Forest    — 2 waves
 *   3. Ashen Throne     — 1 wave (Hollow King)
 *
 * Each section is a full-width tile showing the realm name, wave summary, and
 * a status badge. Selecting a section sets the current realm on {@link GameSession},
 * builds wave 1 via the matching
 * {@link RealmFactory}, and launches a {@link BattleScreen}. Multi-wave
 * progression and reward screens are wired in AT-024.
 *
 * A progress bar at the bottom reads "X/3 Realms Conquered".
 */
public class RealmSelectScreen extends BaseScreen {

    private static final int SCREEN_W = 1280;
    private static final int SCREEN_H = 720;

    // Realm keys also used by GameSession.completedRealms.
    public static final String KEY_ABYSS  = "abyss";
    public static final String KEY_FOREST = "forest";
    public static final String KEY_THRONE = "throne";

    private static final RealmDef[] REALMS = {
            new RealmDef(KEY_THRONE, "Ashen Throne",
                    "Wave 1: The Hollow King",
                    new Color(0.55f, 0.20f, 0.20f, 1f)),
            new RealmDef(KEY_FOREST, "Cursed Forest",
                    "Wave 1: 3 Hollow Wolves    Wave 2: 1 Treant + 2 Wolves",
                    new Color(0.25f, 0.45f, 0.30f, 1f)),
            new RealmDef(KEY_ABYSS, "The Abyss",
                    "Wave 1: 2 Stonewarden    Wave 2: 1 Emberclaw + 2 Stonewarden",
                    new Color(0.25f, 0.30f, 0.55f, 1f)),
    };

    // ---- Layout constants ----
    private static final float SECTION_W = 880f;
    private static final float SECTION_H = 150f;
    private static final float SECTION_GAP = 24f;
    private static final float SECTION_X = (SCREEN_W - SECTION_W) / 2f;
    private static final float SECTIONS_BOTTOM_Y = 130f;

    private static final float BACK_W = 160f;
    private static final float BACK_H = 44f;
    private static final float PROGRESS_BAR_W = 600f;
    private static final float PROGRESS_BAR_H = 18f;

    private SpriteBatch batch;
    private BitmapFont  titleFont;
    private BitmapFont  realmFont;
    private BitmapFont  bodyFont;
    private Texture     pixel;
    private GlyphLayout layout;
    private Viewport    viewport;
    private final Vector3 touchTmp = new Vector3();

    private int hovered = 0;

    public RealmSelectScreen(AshenThroneGame game) {
        super(game);
    }

    @Override
    public void show() {
        batch     = new SpriteBatch();
        titleFont = new BitmapFont();
        realmFont = new BitmapFont();
        bodyFont  = new BitmapFont();
        layout    = new GlyphLayout();
        viewport  = new FitViewport(SCREEN_W, SCREEN_H);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        titleFont.getData().setScale(2.4f);
        realmFont.getData().setScale(1.8f);
        bodyFont.getData().setScale(1.2f);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        // Main theme persists across menu/shop/settings/hero-select/tower
        // (idempotent — won't restart if already playing).
        AudioManager.getInstance().playMusic("main_theme");

        // Default the cursor to the highest-tier unfinished realm.
        hovered = defaultHoverIndex();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
                    hovered = (hovered - 1 + REALMS.length) % REALMS.length;
                    return true;
                }
                if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
                    hovered = (hovered + 1) % REALMS.length;
                    return true;
                }
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    activate(hovered);
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    back();
                    return true;
                }
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                int idx = sectionAt(screenX, screenY);
                if (idx >= 0) hovered = idx;
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                int idx = sectionAt(screenX, screenY);
                if (idx >= 0) {
                    hovered = idx;
                    activate(idx);
                    return true;
                }
                if (backButtonHit(screenX, screenY)) {
                    back();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.04f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        titleFont.setColor(new Color(0.85f, 0.7f, 0.3f, 1f));
        layout.setText(titleFont, "REALM TOWER");
        titleFont.draw(batch, layout, (SCREEN_W - layout.width) / 2f, SCREEN_H - 30f);

        drawSections();
        drawProgressBar();
        drawBackButton();

        batch.end();
    }

    private void drawSections() {
        for (int i = 0; i < REALMS.length; i++) {
            float y = sectionY(i);
            RealmDef r = REALMS[i];
            boolean cleared  = GameSession.getInstance().hasCompletedRealm(r.key);
            boolean hot      = (i == hovered);

            // Border tint communicates state at a glance.
            Color border;
            if (hot)          border = new Color(0.95f, 0.75f, 0.35f, 1f);
            else if (cleared) border = new Color(0.45f, 0.65f, 0.40f, 1f);
            else              border = new Color(0.55f, 0.40f, 0.20f, 1f);

            batch.setColor(border);
            batch.draw(pixel, SECTION_X - 3, y - 3, SECTION_W + 6, SECTION_H + 6);

            batch.setColor(r.tint);
            batch.draw(pixel, SECTION_X, y, SECTION_W, SECTION_H);
            batch.setColor(Color.WHITE);

            // Realm name.
            realmFont.setColor(new Color(1f, 0.95f, 0.75f, 1f));
            realmFont.draw(batch, r.name, SECTION_X + 30f, y + SECTION_H - 30f);

            // Wave summary.
            bodyFont.setColor(new Color(0.92f, 0.88f, 0.78f, 1f));
            bodyFont.draw(batch, r.waveSummary, SECTION_X + 30f, y + SECTION_H - 75f);

            // Status badge: CLEARED / READY.
            String status;
            Color statusColor;
            if (cleared) {
                status = "CLEARED";
                statusColor = new Color(0.6f, 0.95f, 0.6f, 1f);
            } else {
                status = "READY";
                statusColor = new Color(1f, 0.95f, 0.7f, 1f);
            }
            bodyFont.setColor(statusColor);
            layout.setText(bodyFont, status);
            bodyFont.draw(batch, layout,
                    SECTION_X + SECTION_W - layout.width - 30f,
                    y + SECTION_H - 30f);
        }
    }

    private void drawProgressBar() {
        int cleared = 0;
        for (RealmDef r : REALMS) {
            if (GameSession.getInstance().hasCompletedRealm(r.key)) cleared++;
        }
        float bx = (SCREEN_W - PROGRESS_BAR_W) / 2f;
        float by = 60f;

        // Frame.
        batch.setColor(new Color(0.40f, 0.30f, 0.18f, 1f));
        batch.draw(pixel, bx - 3, by - 3, PROGRESS_BAR_W + 6, PROGRESS_BAR_H + 6);
        // Track.
        batch.setColor(new Color(0.10f, 0.08f, 0.10f, 1f));
        batch.draw(pixel, bx, by, PROGRESS_BAR_W, PROGRESS_BAR_H);
        // Fill.
        float fillW = PROGRESS_BAR_W * cleared / (float) REALMS.length;
        batch.setColor(new Color(0.85f, 0.7f, 0.3f, 1f));
        batch.draw(pixel, bx, by, fillW, PROGRESS_BAR_H);
        batch.setColor(Color.WHITE);

        bodyFont.setColor(new Color(0.92f, 0.85f, 0.65f, 1f));
        String label = cleared + "/" + REALMS.length + " Realms Conquered";
        layout.setText(bodyFont, label);
        bodyFont.draw(batch, layout, (SCREEN_W - layout.width) / 2f, by + PROGRESS_BAR_H + 26f);
    }

    private void drawBackButton() {
        float[] r = backRect();
        boolean hot = backButtonHit(Gdx.input.getX(), Gdx.input.getY());
        Color bg     = hot ? new Color(0.30f, 0.20f, 0.10f, 1f) : new Color(0.12f, 0.10f, 0.14f, 1f);
        Color border = hot ? new Color(0.95f, 0.75f, 0.35f, 1f) : new Color(0.45f, 0.35f, 0.20f, 1f);
        batch.setColor(border);
        batch.draw(pixel, r[0] - 2, r[1] - 2, r[2] + 4, r[3] + 4);
        batch.setColor(bg);
        batch.draw(pixel, r[0], r[1], r[2], r[3]);
        batch.setColor(Color.WHITE);
        bodyFont.setColor(hot ? new Color(1f, 0.95f, 0.7f, 1f) : Color.LIGHT_GRAY);
        layout.setText(bodyFont, "Back");
        bodyFont.draw(batch, layout,
                r[0] + (r[2] - layout.width) / 2f,
                r[1] + (r[3] + layout.height) / 2f);
    }

    // ---- Hit testing ----

    /** Returns the y-coordinate of section i (i==0 is the top tile). */
    private float sectionY(int i) {
        // Sections stack top-to-bottom but we lay them out from a top anchor.
        float topY = SECTIONS_BOTTOM_Y + REALMS.length * SECTION_H + (REALMS.length - 1) * SECTION_GAP;
        return topY - (i + 1) * SECTION_H - i * SECTION_GAP;
    }

    private int sectionAt(int screenX, int screenY) {
        touchTmp.set(screenX, screenY, 0);
        viewport.unproject(touchTmp);
        float vx = touchTmp.x;
        float vy = touchTmp.y;
        for (int i = 0; i < REALMS.length; i++) {
            float y = sectionY(i);
            if (vx >= SECTION_X && vx <= SECTION_X + SECTION_W
                    && vy >= y && vy <= y + SECTION_H) {
                return i;
            }
        }
        return -1;
    }

    private float[] backRect() {
        return new float[] { 40f, 30f, BACK_W, BACK_H };
    }

    private boolean backButtonHit(int screenX, int screenY) {
        touchTmp.set(screenX, screenY, 0);
        viewport.unproject(touchTmp);
        float[] r = backRect();
        return touchTmp.x >= r[0] && touchTmp.x <= r[0] + r[2]
                && touchTmp.y >= r[1] && touchTmp.y <= r[1] + r[3];
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.update(width, height, true);
    }

    // ---- Actions ----

    private void activate(int index) {
        RealmDef r = REALMS[index];
        GameSession session = GameSession.getInstance();
        session.setCurrentRealm(r.key);
        session.setCurrentWaveInRealm(0);

        Hero hero = (Hero) session.getHero();
        if (hero == null) {
            TransitionManager.getInstance().goTo(ScreenType.HERO_SELECT);
            return;
        }
        hero.setHp(hero.getMaxHp());

        AbstractCharacter equipped = ShopScreen.EquipmentApplier.apply(
                hero, session.getEquippedItems());

        // AT-026: spin up the realm's WaveIterator and pull wave 1 from it.
        WaveIterator iterator = factoryFor(r.key).createWaveIterator();
        session.setWaveIterator(iterator);
        List<Enemy> wave = iterator.next();
        session.setCurrentWaveInRealm(iterator.getCurrentWaveNumber() - 1);
        TransitionManager.getInstance().goToBattle(equipped, wave);
    }

    private void back() {
        TransitionManager.getInstance().goTo(ScreenType.MAIN_MENU);
    }

    /** Total waves in a realm (AT-024 flow control). */
    public static int totalWaves(String realmKey) {
        return switch (realmKey) {
            case KEY_ABYSS, KEY_FOREST -> 2;
            case KEY_THRONE            -> 1;
            default -> throw new IllegalArgumentException("Unknown realm: " + realmKey);
        };
    }

    /**
     * Wave compositions per AT-019 spec. Wave 0 is wave 1 in spec terms.
     * AT-026 (WaveIterator) will replace this once the iterator pattern lands.
     */
    public static List<Enemy> buildWave(String realmKey, int waveIndex) {
        RealmFactory factory = factoryFor(realmKey);
        List<Enemy> enemies = new ArrayList<>();
        switch (realmKey) {
            case KEY_ABYSS -> {
                if (waveIndex == 0) {
                    enemies.add(factory.createMinion());
                    enemies.add(factory.createMinion());
                } else {
                    enemies.add(factory.createElite());
                    enemies.add(factory.createMinion());
                    enemies.add(factory.createMinion());
                }
            }
            case KEY_FOREST -> {
                if (waveIndex == 0) {
                    enemies.add(factory.createMinion());
                    enemies.add(factory.createMinion());
                    enemies.add(factory.createMinion());
                } else {
                    enemies.add(factory.createElite());
                    enemies.add(factory.createMinion());
                    enemies.add(factory.createMinion());
                }
            }
            case KEY_THRONE -> enemies.add(factory.createBoss());
            default -> throw new IllegalArgumentException("Unknown realm: " + realmKey);
        }
        return enemies;
    }

    private static RealmFactory factoryFor(String realmKey) {
        return switch (realmKey) {
            case KEY_ABYSS  -> new AbyssRealmFactory();
            case KEY_FOREST -> new CursedForestFactory();
            case KEY_THRONE -> new AshenThroneRealmFactory();
            default -> throw new IllegalArgumentException("Unknown realm: " + realmKey);
        };
    }

    private static int defaultHoverIndex() {
        // Highest-tier (top-most) unfinished realm.
        for (int i = 0; i < REALMS.length; i++) {
            if (!GameSession.getInstance().hasCompletedRealm(REALMS[i].key)) {
                return i;
            }
        }
        return REALMS.length - 1;
    }

    @Override
    public void dispose() {
        batch.dispose();
        titleFont.dispose();
        realmFont.dispose();
        bodyFont.dispose();
        pixel.dispose();
    }

    private static final class RealmDef {
        final String key;
        final String name;
        final String waveSummary;
        final Color  tint;

        RealmDef(String key, String name, String waveSummary, Color tint) {
            this.key = key;
            this.name = name;
            this.waveSummary = waveSummary;
            this.tint = tint;
        }
    }
}
