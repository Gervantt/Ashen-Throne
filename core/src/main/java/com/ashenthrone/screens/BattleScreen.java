package com.ashenthrone.screens;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.battle.BattleEngine;
import com.ashenthrone.battle.command.BattleCommand;
import com.ashenthrone.battle.state.BattleState;
import com.ashenthrone.battle.state.PlayerTurnState;
import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.core.AshenThroneGame;
import com.ashenthrone.input.BattleInputAdapter;
import com.ashenthrone.observer.EventManager;
import com.ashenthrone.observer.EventType;
import com.ashenthrone.observer.listeners.AudioListener;
import com.ashenthrone.observer.listeners.BattleLogListener;
import com.ashenthrone.observer.listeners.HealthBarListener;
import com.ashenthrone.observer.listeners.VictoryChecker;
import com.ashenthrone.ui.ActionMenu;
import com.ashenthrone.ui.BattleBackground;
import com.ashenthrone.ui.BattleLog;
import com.ashenthrone.ui.CharacterSprite;
import com.ashenthrone.ui.HealthBar;
import com.ashenthrone.ui.Panel;
import com.ashenthrone.ui.TurnIndicator;
import com.ashenthrone.ui.UIComponent;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
    private static final int   SCREEN_W = 1280;
    private static final int   SCREEN_H = 720;
    private static final float SPRITE_W = 120f;
    private static final float SPRITE_H = 160f;
    private static final float SPRITE_Y = 200f;
    private static final float HP_BAR_H = 16f;

    private final BattleEngine    engine;

    private BattleState currentState;
    private SpriteBatch batch;
    private Viewport    viewport;

    // AT-012: single adapter instance — registered with Gdx.input for the lifetime of this screen.
    private final BattleInputAdapter inputAdapter = new BattleInputAdapter();

    // AT-009: observer listeners — kept as fields so other systems can query their state.
    private final BattleLogListener battleLog      = new BattleLogListener();
    private final VictoryChecker    victoryChecker = new VictoryChecker();

    // AT-011: root HUD panel and direct reference to the action menu for state access.
    private Panel      battleHud;
    private ActionMenu actionMenu;

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

        battleLog.clear();
        victoryChecker.reset();

        em.subscribe(EventType.DAMAGE_DEALT,   new HealthBarListener());
        em.subscribe(EventType.CHARACTER_DIED, new HealthBarListener());

        AudioListener audio = new AudioListener();
        em.subscribe(EventType.DAMAGE_DEALT,   audio);
        em.subscribe(EventType.CHARACTER_DIED, audio);
        em.subscribe(EventType.BATTLE_END,     audio);

        em.subscribe(EventType.DAMAGE_DEALT,   battleLog);
        em.subscribe(EventType.CHARACTER_DIED, battleLog);
        em.subscribe(EventType.ITEM_USED,      battleLog);
        em.subscribe(EventType.BATTLE_END,     battleLog);

        em.subscribe(EventType.CHARACTER_DIED, victoryChecker);
        em.subscribe(EventType.BATTLE_END,     victoryChecker);

        // AT-011: build the Composite HUD tree.
        buildHud();

        // AT-012: register the adapter once; states swap the listener via setListener().
        inputAdapter.setEnemyCount(engine.getEnemies().size());
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

        battleHud.addChild(new BattleBackground(0, 0, SCREEN_W, SCREEN_H));

        // Hero — left, 20% from edge.
        float heroSpriteX = SCREEN_W * 0.20f - SPRITE_W / 2f;
        battleHud.addChild(new CharacterSprite(engine.getHero(),
                new Color(0.35f, 0.55f, 0.85f, 1f),
                heroSpriteX, SPRITE_Y, SPRITE_W, SPRITE_H));
        battleHud.addChild(new HealthBar(engine.getHero(),
                heroSpriteX - 20f, SPRITE_Y + SPRITE_H + 20f,
                SPRITE_W + 40f, HP_BAR_H));

        // Enemies — right half, evenly spaced.
        List<Enemy> enemies = engine.getEnemies();
        int n = enemies.size();
        float enemyAreaLeft  = SCREEN_W * 0.55f;
        float enemyAreaRight = SCREEN_W - 80f;
        float enemyAreaW     = enemyAreaRight - enemyAreaLeft;
        float slotW = n > 0 ? enemyAreaW / n : 0f;
        for (int i = 0; i < n; i++) {
            float slotCenter = enemyAreaLeft + slotW * (i + 0.5f);
            float ex = slotCenter - SPRITE_W / 2f;
            battleHud.addChild(new CharacterSprite(enemies.get(i),
                    new Color(0.75f, 0.30f, 0.30f, 1f),
                    ex, SPRITE_Y, SPRITE_W, SPRITE_H));
            battleHud.addChild(new HealthBar(enemies.get(i),
                    ex - 10f, SPRITE_Y + SPRITE_H + 20f,
                    SPRITE_W + 20f, HP_BAR_H));
        }

        // Turn indicator at top center.
        float tiW = 280f, tiH = 36f;
        battleHud.addChild(new TurnIndicator(this::turnLabel,
                (SCREEN_W - tiW) / 2f, SCREEN_H - tiH - 16f, tiW, tiH));

        // Battle log — bottom-left.
        battleHud.addChild(new BattleLog(battleLog, 20, 20, 360, 110));

        // Action menu — bottom-center.
        actionMenu = new ActionMenu(440, 10, 400, 80);
        battleHud.addChild(actionMenu);
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
        UIComponent.disposeShared();
        Gdx.input.setInputProcessor(null);
    }

    // ---- State machine ----

    /** Transitions to a new state immediately (takes effect next frame). */
    public void setState(BattleState state) {
        this.currentState = state;
    }

    // ---- Command delegation (AT-007 + AT-010) ----

    /** Executes a command through the engine and records it for undo. */
    public void executeCommand(BattleCommand command) {
        engine.executePlayerAction(command);
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

    // AT-009: expose observers so UI (AT-011) and screen flow (AT-013) can read them.
    public BattleLogListener getBattleLog()      { return battleLog; }
    public VictoryChecker    getVictoryChecker() { return victoryChecker; }

    // AT-011: expose action menu so PlayerTurnState can update the selected button.
    public ActionMenu getActionMenu() { return actionMenu; }

    // AT-012: expose adapter so states can register as listener in their constructor.
    public BattleInputAdapter getInputAdapter() { return inputAdapter; }
}
