package com.ashenthrone.screens;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.Hero;
import com.ashenthrone.core.AshenThroneGame;
import com.ashenthrone.core.GameSession;
import com.ashenthrone.factory.AbyssRealmFactory;
import com.ashenthrone.factory.AshenThroneRealmFactory;
import com.ashenthrone.factory.CursedForestFactory;
import com.ashenthrone.factory.RealmFactory;
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
 *   1. The Abyss        — 2 waves, unlocked from start
 *   2. Cursed Forest    — 2 waves, locked until Abyss is cleared
 *   3. Ashen Throne     — 1 wave (Hollow King), locked until Forest is cleared
 *
 * Each section is a full-width tile showing the realm name, wave summary, and
 * a lock icon when not yet unlocked. Selecting an unlocked section sets the
 * current realm on {@link GameSession}, builds wave 1 via the matching
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
                    new Color(0.55f, 0.20f, 0.20f, 1f), KEY_FOREST),
            new RealmDef(KEY_FOREST, "Cursed Forest",
                    "Wave 1: 3 Hollow Wolves    Wave 2: 1 Treant + 2 Wolves",
                    new Color(0.25f, 0.45f, 0.30f, 1f), KEY_ABYSS),
            new RealmDef(KEY_ABYSS, "The Abyss",
                    "Wave 1: 2 Stonewarden    Wave 2: 1 Emberclaw + 2 Stonewarden",
                    new Color(0.25f, 0.30f, 0.55f, 1f), null),
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

        // Default the cursor to the highest-tier unlocked realm so the player's
        // first arrow press lands on the next thing they can play.
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
            boolean unlocked = isUnlocked(r);
            boolean cleared  = GameSession.getInstance().hasCompletedRealm(r.key);
            boolean hot      = (i == hovered);

            // Border tint communicates state at a glance.
            Color border;
            if (!unlocked)    border = new Color(0.28f, 0.24f, 0.22f, 1f);
            else if (hot)     border = new Color(0.95f, 0.75f, 0.35f, 1f);
            else if (cleared) border = new Color(0.45f, 0.65f, 0.40f, 1f);
            else              border = new Color(0.55f, 0.40f, 0.20f, 1f);

            batch.setColor(border);
            batch.draw(pixel, SECTION_X - 3, y - 3, SECTION_W + 6, SECTION_H + 6);

            // Body fill — darker when locked.
            Color body = unlocked ? r.tint : dim(r.tint, 0.35f);
            batch.setColor(body);
            batch.draw(pixel, SECTION_X, y, SECTION_W, SECTION_H);
            batch.setColor(Color.WHITE);

            // Realm name.
            realmFont.setColor(unlocked ? new Color(1f, 0.95f, 0.75f, 1f)
                                        : new Color(0.5f, 0.45f, 0.40f, 1f));
            realmFont.draw(batch, r.name, SECTION_X + 30f, y + SECTION_H - 30f);

            // Wave summary.
            bodyFont.setColor(unlocked ? new Color(0.92f, 0.88f, 0.78f, 1f)
                                       : new Color(0.45f, 0.40f, 0.35f, 1f));
            bodyFont.draw(batch, r.waveSummary, SECTION_X + 30f, y + SECTION_H - 75f);

            // Status badge: CLEARED / LOCKED / READY.
            String status;
            Color statusColor;
            if (!unlocked) {
                status = "LOCKED";
                statusColor = new Color(0.7f, 0.55f, 0.55f, 1f);
            } else if (cleared) {
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

            // Simple lock glyph for locked realms — a small dark square
            // overlay in the lower-right corner stands in for an icon.
            if (!unlocked) {
                float lx = SECTION_X + SECTION_W - 60f;
                float ly = y + 25f;
                batch.setColor(new Color(0.05f, 0.04f, 0.06f, 0.85f));
                batch.draw(pixel, lx, ly, 30f, 30f);
                batch.setColor(new Color(0.7f, 0.55f, 0.30f, 1f));
                batch.draw(pixel, lx + 6, ly + 16, 18f, 12f); // lock body
                batch.draw(pixel, lx + 10, ly + 22, 10f, 8f); // shackle
                batch.setColor(Color.WHITE);
            }
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
        if (!isUnlocked(r)) {
            // Locked sections silently ignore activation — there is no SFX nor
            // transition so the player gets no false signal of progress.
            return;
        }
        AudioManager.getInstance().playSFX("transition_whoosh");

        GameSession session = GameSession.getInstance();
        session.setCurrentRealm(r.key);
        session.setCurrentWaveInRealm(0);

        Hero hero = (Hero) session.getHero();
        if (hero == null) {
            // Defensive: should never happen since hero is chosen before the
            // tower screen, but bouncing back is safer than crashing.
            game.setScreen(new HeroSelectScreen(game));
            return;
        }

        // AT-020: apply purchased equipment as a Decorator chain so battle
        // stats reflect the shop loadout.
        AbstractCharacter equipped = ShopScreen.EquipmentApplier.apply(
                hero, session.getEquippedItems());

        List<Enemy> wave = buildWave(r.key, 0);
        // TODO: AT-024/AT-025 — route through TransitionManager. AT-026's
        // WaveIterator will replace the inline wave-composition logic.
        game.setScreen(new BattleScreen(game, equipped, wave));
    }

    private void back() {
        AudioManager.getInstance().playSFX("transition_whoosh");
        game.setScreen(new MainMenuScreen(game));
    }

    /**
     * Wave compositions per AT-019 spec. Wave 0 is wave 1 in spec terms.
     * AT-026 (WaveIterator) will replace this once the iterator pattern lands.
     */
    private static List<Enemy> buildWave(String realmKey, int waveIndex) {
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

    private static boolean isUnlocked(RealmDef r) {
        return r.requiresKey == null
                || GameSession.getInstance().hasCompletedRealm(r.requiresKey);
    }

    private static int defaultHoverIndex() {
        // Highest-tier (top-most) unlocked realm so a fresh arrow-down moves
        // toward already-cleared content rather than away from it.
        for (int i = 0; i < REALMS.length; i++) {
            if (isUnlocked(REALMS[i])
                    && !GameSession.getInstance().hasCompletedRealm(REALMS[i].key)) {
                return i;
            }
        }
        return REALMS.length - 1;
    }

    private static Color dim(Color c, float factor) {
        return new Color(c.r * factor, c.g * factor, c.b * factor, c.a);
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
        /** key of the realm that must be cleared to unlock this one; null for first realm. */
        final String requiresKey;

        RealmDef(String key, String name, String waveSummary, Color tint, String requiresKey) {
            this.key = key;
            this.name = name;
            this.waveSummary = waveSummary;
            this.tint = tint;
            this.requiresKey = requiresKey;
        }
    }
}
