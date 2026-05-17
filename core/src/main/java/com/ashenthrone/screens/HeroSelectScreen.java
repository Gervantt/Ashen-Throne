package com.ashenthrone.screens;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.characters.Hero;
import com.ashenthrone.characters.HeroBuilder;
import com.ashenthrone.strategy.AttackStrategy;
import com.ashenthrone.strategy.MagicAttack;
import com.ashenthrone.strategy.PhysicalAttack;
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

public class HeroSelectScreen extends BaseScreen {

    private static final int SCREEN_W = 1280;
    private static final int SCREEN_H = 720;

    private static final HeroDef[] HEROES = {
            new HeroDef("Kael",     120, 18, 12, 10, "Warrior",
                    new Color(0.75f, 0.30f, 0.25f, 1f),
                    "A disciplined swordsman with balanced might and guard.\n" +
                    "Reliable in any encounter, master of the steel blade."),
            new HeroDef("Malachar",  90, 25,  6, 11, "Dark Mage",
                    new Color(0.55f, 0.25f, 0.65f, 1f),
                    "Necromancer of the cursed wastes. Devastating arcane\n" +
                    "power at the cost of frail defenses."),
            new HeroDef("Thane",    160, 12, 18,  7, "Guardian",
                    new Color(0.40f, 0.45f, 0.55f, 1f),
                    "Stoic shield-bearer of unmatched endurance. Slow to\n" +
                    "strike, but nearly impossible to bring down."),
            new HeroDef("Vex",       80, 22,  8, 16, "Assassin",
                    new Color(0.30f, 0.55f, 0.40f, 1f),
                    "Fleet-footed killer who strikes from the shadows.\n" +
                    "Frail, but always the first to act."),
    };

    private static final float PORTRAIT_X = 60f;
    private static final float PORTRAIT_W = 110f;
    private static final float PORTRAIT_H = 110f;
    private static final float PORTRAIT_GAP = 38f;
    private static final float LABEL_DROP = 22f;

    private static final float PANEL_X = 230f;
    private static final float PANEL_Y = 80f;
    private static final float PANEL_W = SCREEN_W - PANEL_X - 60f;
    private static final float PANEL_H = SCREEN_H - PANEL_Y - 80f;

    private static final float PREVIEW_W = 360f;
    private static final float PREVIEW_H = 380f;

    private static final float CHOOSE_W = 240f;
    private static final float CHOOSE_H = 56f;
    private static final float BACK_W   = 160f;
    private static final float BACK_H   = 44f;

    private SpriteBatch batch;
    private BitmapFont  titleFont;
    private BitmapFont  nameFont;
    private BitmapFont  bodyFont;
    private Texture     pixel;
    private Texture[]   avatars;
    private GlyphLayout layout;
    private Viewport    viewport;
    private final Vector3 touchTmp = new Vector3();

    private int selected = 0;

    public HeroSelectScreen(AshenThroneGame game) {
        super(game);
    }

    @Override
    public void show() {
        batch     = new SpriteBatch();
        titleFont = new BitmapFont();
        nameFont  = new BitmapFont();
        bodyFont  = new BitmapFont();
        layout    = new GlyphLayout();
        viewport  = new FitViewport(SCREEN_W, SCREEN_H);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        titleFont.getData().setScale(2.4f);
        nameFont.getData().setScale(2.0f);
        bodyFont.getData().setScale(1.3f);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        AudioManager.getInstance().playMusic("main_theme");

        avatars = new Texture[HEROES.length];
        for (int i = 0; i < HEROES.length; i++) {
            String key = HEROES[i].name.toLowerCase();
            avatars[i] = tryLoad("images/heroes/hero_" + key + "_avatar.png");
        }

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
                    selected = (selected - 1 + HEROES.length) % HEROES.length;
                    return true;
                }
                if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
                    selected = (selected + 1) % HEROES.length;
                    return true;
                }
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    confirmHero();
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
                int idx = portraitAt(screenX, screenY);
                if (idx >= 0) selected = idx;
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                int idx = portraitAt(screenX, screenY);
                if (idx >= 0) {
                    selected = idx;
                    return true;
                }
                if (chooseButtonHit(screenX, screenY)) {
                    confirmHero();
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

    private static Texture tryLoad(String path) {
        try {
            com.badlogic.gdx.files.FileHandle fh = Gdx.files.internal(path);
            if (fh.exists()) return new Texture(fh);
        } catch (Exception e) {
            Gdx.app.log("HeroSelectScreen", "Failed to load '" + path + "': " + e.getMessage());
        }
        return null;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.06f, 0.04f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        titleFont.setColor(new Color(0.85f, 0.7f, 0.3f, 1f));
        layout.setText(titleFont, "CHOOSE YOUR HERO");
        titleFont.draw(batch, layout, (SCREEN_W - layout.width) / 2f, SCREEN_H - 30f);

        drawPortraits();
        drawPreviewPanel();
        drawChooseButton();
        drawBackButton();

        batch.end();
    }

    private void drawPortraits() {
        float firstY = portraitFirstY();
        for (int i = 0; i < HEROES.length; i++) {
            float py = firstY - i * (PORTRAIT_H + PORTRAIT_GAP);
            boolean isSel = (i == selected);

            Color border = isSel ? new Color(0.95f, 0.75f, 0.35f, 1f)
                                 : new Color(0.40f, 0.30f, 0.18f, 1f);
            batch.setColor(border);
            batch.draw(pixel, PORTRAIT_X - 3, py - 3, PORTRAIT_W + 6, PORTRAIT_H + 6);

            if (avatars[i] != null) {
                batch.setColor(Color.WHITE);
                batch.draw(avatars[i], PORTRAIT_X, py, PORTRAIT_W, PORTRAIT_H);
            } else {
                batch.setColor(HEROES[i].tint);
                batch.draw(pixel, PORTRAIT_X, py, PORTRAIT_W, PORTRAIT_H);
                batch.setColor(Color.WHITE);
            }

            bodyFont.setColor(isSel ? new Color(1f, 0.95f, 0.7f, 1f) : Color.LIGHT_GRAY);
            layout.setText(bodyFont, HEROES[i].name);
            bodyFont.draw(batch, layout,
                    PORTRAIT_X + (PORTRAIT_W - layout.width) / 2f,
                    py - LABEL_DROP);
        }
    }

    private void drawPreviewPanel() {

        batch.setColor(new Color(0.18f, 0.14f, 0.10f, 1f));
        batch.draw(pixel, PANEL_X - 2, PANEL_Y - 2, PANEL_W + 4, PANEL_H + 4);
        batch.setColor(new Color(0.10f, 0.08f, 0.12f, 1f));
        batch.draw(pixel, PANEL_X, PANEL_Y, PANEL_W, PANEL_H);
        batch.setColor(Color.WHITE);

        HeroDef h = HEROES[selected];

        float imgX = PANEL_X + 30f;
        float imgY = PANEL_Y + PANEL_H - PREVIEW_H - 30f;
        batch.setColor(new Color(0.25f, 0.20f, 0.15f, 1f));
        batch.draw(pixel, imgX - 3, imgY - 3, PREVIEW_W + 6, PREVIEW_H + 6);

        if (avatars[selected] != null) {
            batch.setColor(Color.WHITE);
            batch.draw(avatars[selected], imgX, imgY, PREVIEW_W, PREVIEW_H);
        } else {
            batch.setColor(h.tint);
            batch.draw(pixel, imgX, imgY, PREVIEW_W, PREVIEW_H);
            batch.setColor(Color.WHITE);
        }

        float infoX = imgX + PREVIEW_W + 40f;
        float infoY = PANEL_Y + PANEL_H - 50f;

        nameFont.setColor(new Color(0.95f, 0.85f, 0.4f, 1f));
        nameFont.draw(batch, h.name, infoX, infoY);

        bodyFont.setColor(new Color(0.75f, 0.65f, 0.45f, 1f));
        bodyFont.draw(batch, h.role, infoX, infoY - 36f);

        bodyFont.setColor(Color.LIGHT_GRAY);
        float statY = infoY - 80f;
        bodyFont.draw(batch, "HP",      infoX,         statY);
        bodyFont.draw(batch, String.valueOf(h.hp),      infoX + 140f, statY);
        bodyFont.draw(batch, "Attack",  infoX,         statY - 30f);
        bodyFont.draw(batch, String.valueOf(h.attack),  infoX + 140f, statY - 30f);
        bodyFont.draw(batch, "Defense", infoX,         statY - 60f);
        bodyFont.draw(batch, String.valueOf(h.defense), infoX + 140f, statY - 60f);
        bodyFont.draw(batch, "Speed",   infoX,         statY - 90f);
        bodyFont.draw(batch, String.valueOf(h.speed),   infoX + 140f, statY - 90f);

        bodyFont.setColor(new Color(0.85f, 0.80f, 0.70f, 1f));
        float descY = imgY - 20f;
        for (String line : h.description.split("\n")) {
            bodyFont.draw(batch, line, imgX, descY);
            descY -= 26f;
        }
    }

    private void drawChooseButton() {
        float[] r = chooseRect();
        boolean hot = pointerOverChoose();
        drawButton("Choose", r[0], r[1], r[2], r[3], hot);
    }

    private void drawBackButton() {
        float[] r = backRect();
        boolean hot = pointerOverBack();
        drawButton("Back", r[0], r[1], r[2], r[3], hot);
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

    private float portraitFirstY() {
        float total = HEROES.length * PORTRAIT_H + (HEROES.length - 1) * PORTRAIT_GAP;
        return (SCREEN_H + total) / 2f - PORTRAIT_H;
    }

    private int portraitAt(int screenX, int screenY) {
        touchTmp.set(screenX, screenY, 0);
        viewport.unproject(touchTmp);
        float vx = touchTmp.x;
        float vy = touchTmp.y;
        float firstY = portraitFirstY();
        for (int i = 0; i < HEROES.length; i++) {
            float py = firstY - i * (PORTRAIT_H + PORTRAIT_GAP);
            if (vx >= PORTRAIT_X && vx <= PORTRAIT_X + PORTRAIT_W
                    && vy >= py && vy <= py + PORTRAIT_H) {
                return i;
            }
        }
        return -1;
    }

    private float[] chooseRect() {
        float x = PANEL_X + PANEL_W - CHOOSE_W - 30f;
        float y = PANEL_Y + 30f;
        return new float[] { x, y, CHOOSE_W, CHOOSE_H };
    }

    private float[] backRect() {
        return new float[] { 40f, 30f, BACK_W, BACK_H };
    }

    private boolean chooseButtonHit(int screenX, int screenY) {
        touchTmp.set(screenX, screenY, 0);
        viewport.unproject(touchTmp);
        float[] r = chooseRect();
        return inRect(touchTmp.x, touchTmp.y, r[0], r[1], r[2], r[3]);
    }

    private boolean backButtonHit(int screenX, int screenY) {
        touchTmp.set(screenX, screenY, 0);
        viewport.unproject(touchTmp);
        float[] r = backRect();
        return inRect(touchTmp.x, touchTmp.y, r[0], r[1], r[2], r[3]);
    }

    private boolean pointerOverChoose() {
        return chooseButtonHit(Gdx.input.getX(), Gdx.input.getY());
    }

    private boolean pointerOverBack() {
        return backButtonHit(Gdx.input.getX(), Gdx.input.getY());
    }

    private static boolean inRect(float px, float py, float x, float y, float w, float h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.update(width, height, true);
    }

    private void confirmHero() {
        HeroDef d = HEROES[selected];
        Hero hero = new HeroBuilder()
                .name(d.name)
                .hp(d.hp)
                .attack(d.attack)
                .defense(d.defense)
                .speed(d.speed)
                .skill(defaultSkillFor(d.role))
                .build();
        GameSession.getInstance().setHero(hero);
        TransitionManager.getInstance().goTo(ScreenType.REALM_SELECT);
    }

    private static AttackStrategy defaultSkillFor(String role) {
        return switch (role) {
            case "Dark Mage" -> new MagicAttack();
            default          -> new PhysicalAttack();
        };
    }

    private void back() {
        TransitionManager.getInstance().goTo(ScreenType.MAIN_MENU);
    }

    @Override
    public void dispose() {
        batch.dispose();
        titleFont.dispose();
        nameFont.dispose();
        bodyFont.dispose();
        pixel.dispose();
        if (avatars != null) {
            for (Texture t : avatars) if (t != null) t.dispose();
        }
    }

    private static final class HeroDef {
        final String name;
        final int hp, attack, defense, speed;
        final String role;
        final Color tint;
        final String description;

        HeroDef(String name, int hp, int attack, int defense, int speed,
                String role, Color tint, String description) {
            this.name = name;
            this.hp = hp;
            this.attack = attack;
            this.defense = defense;
            this.speed = speed;
            this.role = role;
            this.tint = tint;
            this.description = description;
        }
    }
}