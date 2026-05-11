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

/**
 * The battle screen hosts the battle loop and owns the State Machine (AT-006).
 *
 * It holds the current {@link BattleState} and delegates every frame's
 * handleInput / update / render calls to it. States transition by calling
 * {@link #setState(BattleState)}.
 *
 * All battle logic is now delegated to {@link BattleEngine} (AT-010).
 * BattleScreen is a pure coordinator — it routes input and rendering to the
 * current state, and routes commands and queries to the engine.
 *
 * Input is translated from raw libGDX events to game-level callbacks by
 * {@link BattleInputAdapter} (AT-012). States register as listeners via
 * {@link BattleInputAdapter#setListener} in their constructors.
 *
 * The battle HUD (AT-011) is a Composite UIComponent tree rendered every frame
 * after the current state so it always appears on top.
 *
 * Construction:
 *   new BattleScreen(AshenThroneGame.getInstance(), hero, enemies)
 * Transition in:
 *   AshenThroneGame.getInstance().setScreen(battleScreen)   // AT-013
 */
public class BattleScreen extends BaseScreen {

    // AT-015: layout constants for the static battle scene.
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

    // AT-012: single adapter instance — registered with Gdx.input for the lifetime of this screen.
    private final BattleInputAdapter inputAdapter = new BattleInputAdapter();

    // AT-009: observer listeners — kept as fields so other systems can query their state.
    private final VictoryChecker    victoryChecker = new VictoryChecker();

    // AT-011: root HUD panel and direct reference to the action menu for state access.
    private Panel      battleHud;
    private ActionMenu actionMenu;
    private FloatingCombatText floatingText;

    // AT-018/AT-019: sprite textures loaded for this battle, owned by the
    // screen so they are disposed alongside it. Null when art is missing.
    private final java.util.List<Texture> battleTextures = new java.util.ArrayList<>();

    /** Hero sprite — kept as a field so action-driven animation can be triggered. */
    private CharacterSprite heroSprite;

    /** Enemy sprites in the same order as {@code engine.getEnemies()}. */
    private final java.util.List<CharacterSprite> enemySprites = new java.util.ArrayList<>();

    /**
     * Transient HUD components owned by the current state (e.g. the attack
     * timing bar). Updated and rendered after the main HUD so they sit on top.
     */
    private final java.util.List<UIComponent> overlays = new java.util.ArrayList<>();

    public BattleScreen(AshenThroneGame game, AbstractCharacter hero, List<Enemy> enemies) {
        super(game);
        if (game == null)    throw new IllegalArgumentException("game must not be null");
        if (hero == null)    throw new IllegalArgumentException("hero must not be null");
        if (enemies == null) throw new IllegalArgumentException("enemies must not be null");
        this.engine = new BattleEngine();
        engine.startBattle(hero, enemies); // AT-010: engine owns hero + enemies
    }

    // ---- Screen lifecycle ----

    @Override
    public void show() {
        batch    = new SpriteBatch();
        viewport = new FitViewport(SCREEN_W, SCREEN_H);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        // AT-009: reset and re-register observers for this battle.
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

        // AT-011: build the Composite HUD tree.
        buildHud();

        em.subscribe(EventType.DAMAGE_DEALT, floatingText);
        em.subscribe(EventType.ITEM_USED, floatingText);

        // AT-012: register the adapter once; states swap the listener via setListener().
        inputAdapter.setEnemyCount(engine.getEnemies().size());
        inputAdapter.setEnemyHitTester(this::enemyAtScreenPos);
        Gdx.input.setInputProcessor(inputAdapter);

        currentState = new PlayerTurnState(this);

        // AT-021: choose music based on whether the wave includes the final boss.
        AudioManager.getInstance().playMusic(hasBoss() ? "boss_theme" : "battle_theme");
    }

    /** True if any enemy in this wave is The Hollow King — switches to boss theme. */
    private boolean hasBoss() {
        for (Enemy e : engine.getEnemies()) {
            if (e != null && "HollowKing".equals(e.getType())) return true;
        }
        return false;
    }

    /**
     * Constructs the battle HUD as a Composite UIComponent tree (AT-011, AT-015).
     *
     * Layout (1280×720):
     *   - Background (fullscreen, behind everything)
     *   - Hero sprite + HealthBar at left, ~20% from edge
     *   - Enemy sprites + HealthBars at right, evenly spaced
     *   - Turn indicator centered at the top
     *   - Battle log at bottom-left
     *   - Action menu at bottom-center
     *
     * Sprites are placeholder rectangles until AT-018/019/021 supply textures.
     */
    private void buildHud() {
        battleHud = new Panel(0, 0, SCREEN_W, SCREEN_H);

        // Realm-specific background texture (AT-019/AT-021). Null path → flat fill.
        battleHud.addChild(new BattleBackground(realmBackgroundPath(), 0, 0, SCREEN_W, SCREEN_H));

        // Hero — left, 20% from edge. Sprite sheet has 4 horizontal frames:
        // idle = frame 0, attack/skill cycles 0→3 over ~0.45s.
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

        // Enemies — right half, evenly spaced. Area widened so larger sprites
        // (200×260) don't overlap when there are three enemies on screen.
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
            // Frame count is sheet-specific (see frameCountFor): some enemies
            // have 4 frames, others up to 11. Idle renders frame 0; attack
            // cycles 0→N-1 over ACTION_DURATION.
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

        // Turn indicator at top center.
        float tiW = 280f, tiH = 36f;
        battleHud.addChild(new TurnIndicator(this::turnLabel,
                (SCREEN_W - tiW) / 2f, SCREEN_H - tiH - 16f, tiW, tiH));

        // Action menu — bottom-center.
        actionMenu = new ActionMenu(440, 10, 400, 80);
        battleHud.addChild(actionMenu);
        battleHud.addChild(floatingText);
    }

    /** Maps the current state class to a human-readable turn label. */
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

    /**
     * Called every frame by libGDX.
     * Clears the screen, then lets the current state handle input, update, and render,
     * followed by the HUD composite tree which always renders on top.
     */
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
        // AT-011: render the HUD tree after state content so it appears on top.
        battleHud.update(delta);
        battleHud.render(batch);
        // State-owned overlays (e.g. the attack timing bar) sit above the HUD.
        for (UIComponent o : overlays) {
            o.update(delta);
            o.render(batch);
        }
        // AT-023: pause menu and other modal overlays render last, on top of HUD.
        currentState.renderOverlay(batch);
        batch.end();
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

    // ---- State machine ----

    /** Map the active realm in {@link GameSession} to its factory's background path. */
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

    /**
     * Loads a sprite texture if the file exists; returns null otherwise so
     * {@link CharacterSprite} can degrade to a colored rectangle. The texture
     * is registered for disposal alongside this screen.
     */
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

    /**
     * Returns the horizontal frame count for a sprite sheet. Most sheets use
     * square frames, in which case {@code width / height} gives the count.
     * A few enemy sheets have non-square frames and need explicit entries.
     */
    private static int frameCountFor(Texture tex) {
        if (tex == null) return 1;
        int w = tex.getWidth();
        int h = tex.getHeight();
        // Explicit overrides for sheets where frames are not square.
        if (w == 2640 && h == 192) return 11; // hollow_king (240×192)
        if (w == 320  && h == 160) return 4;  // hollow_wolf  (80×160)
        if (w == 1008 && h == 80)  return 7;  // treant       (144×80)
        // Default: assume square frames.
        if (h > 0 && w % h == 0) return w / h;
        return 1;
    }

    /** "HollowKing" → "hollow_king", "Wraith" → "wraith". */
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

    /**
     * Maps a window-space click to an enemy index using the same slot layout
     * as {@link #buildHud()}. Returns -1 if the click missed every sprite.
     * Wired into {@link BattleInputAdapter#setEnemyHitTester} (AT-012/AT-015).
     */
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

    /** Transitions to a new state immediately (takes effect next frame). */
    public void setState(BattleState state) {
        this.currentState = state;
    }

    /** Current state — used by states that suspend themselves (AT-023 pause). */
    public BattleState getCurrentState() {
        return currentState;
    }

    // ---- Command delegation (AT-007 + AT-010) ----

    /** Executes a command through the engine and records it for undo. */
    public void executeCommand(BattleCommand command) {
        engine.executePlayerAction(command);
    }

    /** Adds a transient overlay component drawn above the HUD. */
    public void addOverlay(UIComponent overlay) {
        if (overlay != null && !overlays.contains(overlay)) overlays.add(overlay);
    }

    /** Removes a previously-added overlay; safe to call if it isn't present. */
    public void removeOverlay(UIComponent overlay) {
        overlays.remove(overlay);
    }

    /** Trigger the hero's 4-frame action animation. No-op if no sprite sheet was loaded. */
    public void playHeroActionAnimation() {
        if (heroSprite != null) heroSprite.playActionAnimation();
    }

    /**
     * Trigger the 4-frame attack animation on every still-alive enemy.
     * Called by {@link com.ashenthrone.battle.state.EnemyTurnState} after the
     * engine has run all enemy actions, so the swing visuals overlap the
     * {@link com.ashenthrone.battle.state.AnimationState} dwell time.
     */
    public void playEnemyActionAnimations() {
        List<Enemy> enemies = engine.getEnemies();
        for (int i = 0; i < enemySprites.size() && i < enemies.size(); i++) {
            if (enemies.get(i).isAlive()) {
                enemySprites.get(i).playActionAnimation();
            }
        }
    }

    /** Undoes the most recent command. No-op if history is empty. */
    public void undoLastCommand() {
        engine.undoLastCommand();
    }

    /** True if there is at least one command that can be undone. */
    public boolean canUndo() {
        return engine.canUndo();
    }

    // ---- Accessors for states ----

    public AbstractCharacter getHero() { return engine.getHero(); }
    public List<Enemy> getEnemies() { return engine.getEnemies(); }
    public AshenThroneGame getGame()  { return game; }

    /** Exposes the engine so states can call executeEnemyTurns(), isOver(), getResult(). */
    public BattleEngine getBattleEngine() { return engine; }

    // AT-009: expose observers so screen flow (AT-013) can read them.
    public VictoryChecker    getVictoryChecker() { return victoryChecker; }

    // AT-011: expose action menu so PlayerTurnState can update the selected button.
    public ActionMenu getActionMenu() { return actionMenu; }

    // AT-012: expose adapter so states can register as listener in their constructor.
    public BattleInputAdapter getInputAdapter() { return inputAdapter; }
}
