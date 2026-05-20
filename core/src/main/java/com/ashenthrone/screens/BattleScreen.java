package com.ashenthrone.screens;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.battle.BattleEngine;
import com.ashenthrone.battle.command.BattleCommand;
import com.ashenthrone.battle.state.BattleState;
import com.ashenthrone.battle.state.PlayerTurnState;
import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.core.AshenThroneGame;
import com.ashenthrone.core.GameSession;
import com.ashenthrone.factory.AbyssRealmFactory;
import com.ashenthrone.factory.AshenThroneRealmFactory;
import com.ashenthrone.factory.CursedForestFactory;
import com.ashenthrone.factory.RealmFactory;
import com.ashenthrone.input.BattleInputAdapter;
import com.ashenthrone.observer.EventManager;
import com.ashenthrone.observer.EventType;
import com.ashenthrone.observer.listeners.AudioListener;
import com.ashenthrone.observer.listeners.HealthBarListener;
import com.ashenthrone.observer.listeners.VictoryChecker;
import com.ashenthrone.transition.TransitionManager;
import com.ashenthrone.ui.ActionMenu;
import com.ashenthrone.ui.BattleBackground;
import com.ashenthrone.ui.CharacterSprite;
import com.ashenthrone.ui.FloatingCombatText;
import com.ashenthrone.ui.HealthBar;
import com.ashenthrone.ui.Panel;
import com.ashenthrone.ui.TurnIndicator;
import com.ashenthrone.ui.UIComponent;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.List;

public class BattleScreen extends BaseScreen {

    public  static final int   SCREEN_W = 1280;
    public  static final int   SCREEN_H = 720;
    private static final float SPRITE_W = 190f;
    private static final float SPRITE_H = 240f;
    private static final float BOSS_SCALE = 2.45f;
    private static final float TREANT_WIDTH_SCALE = 1.28f;
    private static final float HOLLOW_WOLF_SCALE = 1.15f;
    private static final float SPRITE_Y = 96f;
    private static final float ABYSS_ENEMY_Y_OFFSET = -54f;
    private static final float ABYSS_HERO_Y_OFFSET = -76f;
    private static final float FOREST_HERO_Y_OFFSET = -74f;
    private static final float THRONE_HERO_Y_OFFSET = -54f;
    private static final float THRONE_BOSS_Y_OFFSET = -54f;
    private static final float HP_BAR_W = 140f;
    private static final float HP_BAR_H = 14f;
    private static final float HERO_CENTER_X = 255f;
    private static final float ENEMY_FORMATION_CENTER_X = 770f;
    private static final float ENEMY_SPACING = 160f;

    private final BattleEngine    engine;

    private BattleState currentState;
    private SpriteBatch batch;
    private Viewport    viewport;

    private final BattleInputAdapter inputAdapter = new BattleInputAdapter();

    private final VictoryChecker    victoryChecker = new VictoryChecker();

    private Panel      battleHud;
    private ActionMenu actionMenu;
    private FloatingCombatText floatingText;

    private final java.util.List<Texture> battleTextures = new java.util.ArrayList<>();

    private CharacterSprite heroSprite;

    private final java.util.List<CharacterSprite> enemySprites = new java.util.ArrayList<>();


    private final java.util.List<UIComponent> overlays = new java.util.ArrayList<>();

    public BattleScreen(AshenThroneGame game, AbstractCharacter hero, List<Enemy> enemies) {
        super(game);
        if (game == null)    throw new IllegalArgumentException("game must not be null");
        if (hero == null)    throw new IllegalArgumentException("hero must not be null");
        if (enemies == null) throw new IllegalArgumentException("enemies must not be null");
        this.engine = new BattleEngine();
        engine.startBattle(hero, enemies);
    }

    @Override
    public void show() {
        batch    = new SpriteBatch();
        viewport = new FitViewport(SCREEN_W, SCREEN_H);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        EventManager em = EventManager.getInstance();
        em.clearAll();

        victoryChecker.reset();

        em.subscribe(EventType.DAMAGE_DEALT,   new HealthBarListener());
        em.subscribe(EventType.CHARACTER_DIED, new HealthBarListener());

        AudioListener audio = new AudioListener();
        em.subscribe(EventType.DAMAGE_DEALT,   audio);
        em.subscribe(EventType.CHARACTER_DIED, audio);
        em.subscribe(EventType.BATTLE_END,     audio);

        em.subscribe(EventType.CHARACTER_DIED, victoryChecker);
        em.subscribe(EventType.BATTLE_END,     victoryChecker);

        buildHud();

        em.subscribe(EventType.DAMAGE_DEALT, floatingText);
        em.subscribe(EventType.ITEM_USED, floatingText);

        inputAdapter.setEnemyCount(engine.getEnemies().size());
        inputAdapter.setEnemyHitTester(this::enemyAtScreenPos);
        Gdx.input.setInputProcessor(inputAdapter);

        currentState = new PlayerTurnState(this);

        AudioManager.getInstance().playMusic(hasBoss() ? "boss_theme" : "battle_theme");
    }
    private boolean hasBoss() {
        for (Enemy e : engine.getEnemies()) {
            if (e != null && "HollowKing".equals(e.getType())) return true;
        }
        return false;
    }

    private void buildHud() {
        battleHud = new Panel(0, 0, SCREEN_W, SCREEN_H);

        battleHud.addChild(new BattleBackground(realmBackgroundPath(), 0, 0, SCREEN_W, SCREEN_H));
        float heroY = heroSpriteY();
        float heroSpriteX = HERO_CENTER_X - SPRITE_W / 2f;
        Texture heroTex = loadSprite("images/heroes/hero_"
                + engine.getHero().getName().toLowerCase() + ".png");
        heroSprite = new CharacterSprite(engine.getHero(),
                new Color(0.35f, 0.55f, 0.85f, 1f), heroTex, frameCountFor(heroTex),
                heroSpriteX, heroY, SPRITE_W, SPRITE_H);
        battleHud.addChild(heroSprite);
        battleHud.addChild(new HealthBar(engine.getHero(),
                HERO_CENTER_X - HP_BAR_W / 2f, heroY + SPRITE_H + 14f,
                HP_BAR_W, HP_BAR_H));

        floatingText = new FloatingCombatText();
        floatingText.register(engine.getHero(), HERO_CENTER_X, heroY + SPRITE_H);
        if (GameSession.getInstance().getHero() != engine.getHero()) {
            floatingText.register(GameSession.getInstance().getHero(), HERO_CENTER_X, heroY + SPRITE_H);
        }

        List<Enemy> enemies = engine.getEnemies();
        int n = enemies.size();
        enemySprites.clear();
        for (int i = 0; i < n; i++) {
            float slotCenter = enemyCenterX(i, n);
            Enemy enemy = enemies.get(i);
            float enemyW = enemySpriteW(enemy);
            float enemyH = enemySpriteH(enemy);
            float enemyY = enemySpriteY();
            float ex = slotCenter - enemyW / 2f;
            Texture enemyTex = loadSprite("images/enemies/enemy_"
                    + toSnakeCase(enemy.getType()) + ".png");

            CharacterSprite es = new CharacterSprite(enemy,
                    new Color(0.75f, 0.30f, 0.30f, 1f), enemyTex, frameCountFor(enemyTex),
                    ex, enemyY, enemyW, enemyH);
            enemySprites.add(es);
            battleHud.addChild(es);
            float barW = healthBarWidth(enemy);
            battleHud.addChild(new HealthBar(enemy,
                    slotCenter - barW / 2f, healthBarY(enemyY, enemyH),
                    barW, HP_BAR_H));
            floatingText.register(enemy, slotCenter, floatingTextHeadY(enemyY, enemyH));
        }

        float tiW = 280f, tiH = 36f;
        battleHud.addChild(new TurnIndicator(this::turnLabel,
                (SCREEN_W - tiW) / 2f, SCREEN_H - tiH - 16f, tiW, tiH));

        actionMenu = new ActionMenu(440, 10, 400, 80);
        battleHud.addChild(actionMenu);
        battleHud.addChild(floatingText);
    }

    private String turnLabel() {
        if (currentState == null) return "";
        String name = currentState.getClass().getSimpleName();
        return switch (name) {
            case "PlayerTurnState" -> "YOUR TURN";
            case "EnemyTurnState"  -> "ENEMY TURN";
            case "AnimationState"  -> "...";
            case "VictoryState"    -> "VICTORY";
            case "DefeatState"     -> "DEFEAT";
            default -> name;
        };
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        currentState.handleInput();
        currentState.update(delta);
        currentState.render(batch);

        battleHud.update(delta);
        battleHud.render(batch);

        for (UIComponent o : overlays) {
            o.update(delta);
            o.render(batch);
        }

        currentState.renderOverlay(batch);
        batch.end();

        TransitionManager.getInstance().update(delta);
        TransitionManager.getInstance().render();
    }

    @Override
    public void resize(int width, int height) {
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
        battleHud.dispose();
        for (UIComponent o : overlays) o.dispose();
        overlays.clear();
        for (Texture t : battleTextures) t.dispose();
        battleTextures.clear();
        UIComponent.disposeShared();
        Gdx.input.setInputProcessor(null);
    }

    private static String realmBackgroundPath() {
        String key = GameSession.getInstance().getCurrentRealm();
        if (key == null) return null;
        RealmFactory factory = switch (key) {
            case RealmSelectScreen.KEY_ABYSS  -> new AbyssRealmFactory();
            case RealmSelectScreen.KEY_FOREST -> new CursedForestFactory();
            case RealmSelectScreen.KEY_THRONE -> new AshenThroneRealmFactory();
            default -> null;
        };
        return factory != null ? factory.createBackground() : null;
    }

    private Texture loadSprite(String path) {
        try {
            com.badlogic.gdx.files.FileHandle fh = Gdx.files.internal(path);
            if (!fh.exists()) return null;
            Texture t = new Texture(fh);
            battleTextures.add(t);
            return t;
        } catch (Exception e) {
            Gdx.app.log("BattleScreen", "Failed to load '" + path + "': " + e.getMessage());
            return null;
        }
    }

    private static int frameCountFor(Texture tex) {
        if (tex == null) return 1;
        int w = tex.getWidth();
        int h = tex.getHeight();

        if (w == 2640 && h == 192) return 11;
        if (w == 320  && h == 160) return 4;
        if (w == 1008 && h == 80)  return 7;

        if (h > 0 && w % h == 0) return w / h;
        return 1;
    }

    private static String toSnakeCase(String camel) {
        if (camel == null || camel.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c) && i > 0) sb.append('_');
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    private int enemyAtScreenPos(int screenX, int screenY) {
        if (viewport == null) return -1;
        Vector3 v = new Vector3(screenX, screenY, 0f);
        viewport.unproject(v);

        List<Enemy> enemies = engine.getEnemies();
        int n = enemies.size();
        if (n == 0) return -1;

        for (int i = 0; i < n; i++) {
            Enemy enemy = enemies.get(i);
            float slotCenter = enemyCenterX(i, n);
            float enemyW = enemySpriteW(enemy);
            float enemyH = enemySpriteH(enemy);
            float enemyY = enemySpriteY();
            float ex = slotCenter - enemyW / 2f;
            if (v.x >= ex && v.x <= ex + enemyW
                    && v.y >= enemyY && v.y <= enemyY + enemyH) {
                return i;
            }
        }
        return -1;
    }

    private static float enemyCenterX(int index, int count) {
        if (count <= 1) return ENEMY_FORMATION_CENTER_X;
        float first = ENEMY_FORMATION_CENTER_X - ENEMY_SPACING * (count - 1) / 2f;
        return first + ENEMY_SPACING * index;
    }

    private static boolean isBoss(Enemy enemy) {
        return enemy != null && "HollowKing".equals(enemy.getType());
    }

    private static float enemySpriteW(Enemy enemy) {
        if (enemy != null && "HollowWolf".equals(enemy.getType())) {
            return SPRITE_W * HOLLOW_WOLF_SCALE;
        }
        if (enemy != null && "Treant".equals(enemy.getType())) {
            return SPRITE_W * TREANT_WIDTH_SCALE;
        }
        return isBoss(enemy) ? SPRITE_W * BOSS_SCALE : SPRITE_W;
    }

    private static float enemySpriteH(Enemy enemy) {
        if (enemy != null && "HollowWolf".equals(enemy.getType())) {
            return SPRITE_H * HOLLOW_WOLF_SCALE;
        }
        return isBoss(enemy) ? SPRITE_H * BOSS_SCALE : SPRITE_H;
    }

    private static float enemySpriteY() {
        String realm = GameSession.getInstance().getCurrentRealm();
        if (RealmSelectScreen.KEY_THRONE.equals(realm)) return SPRITE_Y + THRONE_BOSS_Y_OFFSET;
        if (RealmSelectScreen.KEY_ABYSS.equals(realm)) return SPRITE_Y + ABYSS_ENEMY_Y_OFFSET;
        return SPRITE_Y;
    }

    private static float heroSpriteY() {
        String realm = GameSession.getInstance().getCurrentRealm();
        if (RealmSelectScreen.KEY_ABYSS.equals(realm)) return SPRITE_Y + ABYSS_HERO_Y_OFFSET;
        if (RealmSelectScreen.KEY_FOREST.equals(realm)) return SPRITE_Y + FOREST_HERO_Y_OFFSET;
        if (RealmSelectScreen.KEY_THRONE.equals(realm)) return SPRITE_Y + THRONE_HERO_Y_OFFSET;
        return SPRITE_Y;
    }

    private static float healthBarWidth(Enemy enemy) {
        if (isBoss(enemy)) return HP_BAR_W * 2.60f;
        return HP_BAR_W;
    }

    private static float healthBarY(float spriteY, float spriteH) {
        return Math.min(spriteY + spriteH + 14f, SCREEN_H - 92f);
    }

    private static float floatingTextHeadY(float spriteY, float spriteH) {
        return Math.min(spriteY + spriteH, SCREEN_H - 116f);
    }

    public void setState(BattleState state) {
        this.currentState = state;
    }

    public BattleState getCurrentState() {
        return currentState;
    }

    public void executeCommand(BattleCommand command) {
        engine.executePlayerAction(command);
    }

    public void addOverlay(UIComponent overlay) {
        if (overlay != null && !overlays.contains(overlay)) overlays.add(overlay);
    }

    public void removeOverlay(UIComponent overlay) {
        overlays.remove(overlay);
    }

    public void playHeroActionAnimation() {
        if (heroSprite != null) heroSprite.playActionAnimation();
    }


    public void playEnemyActionAnimations() {
        List<Enemy> enemies = engine.getEnemies();
        for (int i = 0; i < enemySprites.size() && i < enemies.size(); i++) {
            if (enemies.get(i).isAlive()) {
                enemySprites.get(i).playActionAnimation();
            }
        }
    }

    public void undoLastCommand() {
        engine.undoLastCommand();
    }

    public boolean canUndo() {
        return engine.canUndo();
    }

    public AbstractCharacter getHero() { return engine.getHero(); }
    public List<Enemy> getEnemies() { return engine.getEnemies(); }
    public AshenThroneGame getGame()  { return game; }

    public BattleEngine getBattleEngine() { return engine; }

    public VictoryChecker    getVictoryChecker() { return victoryChecker; }

    public ActionMenu getActionMenu() { return actionMenu; }

    public BattleInputAdapter getInputAdapter() { return inputAdapter; }
}
