package com.ashenthrone.screens;

import com.ashenthrone.battle.BattleEngine;
import com.ashenthrone.battle.command.BattleCommand;
import com.ashenthrone.battle.state.BattleState;
import com.ashenthrone.battle.state.PlayerTurnState;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.Hero;
import com.ashenthrone.core.AshenThroneGame;
import com.ashenthrone.input.BattleInputAdapter;
import com.ashenthrone.observer.EventManager;
import com.ashenthrone.observer.EventType;
import com.ashenthrone.observer.listeners.AudioListener;
import com.ashenthrone.observer.listeners.BattleLogListener;
import com.ashenthrone.observer.listeners.HealthBarListener;
import com.ashenthrone.observer.listeners.VictoryChecker;
import com.ashenthrone.ui.ActionMenu;
import com.ashenthrone.ui.BattleLog;
import com.ashenthrone.ui.HealthBar;
import com.ashenthrone.ui.Panel;
import com.ashenthrone.ui.UIComponent;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

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
 * Construction:
 *   new BattleScreen(AshenThroneGame.getInstance(), hero, enemies)
 * Transition in:
 *   AshenThroneGame.getInstance().setScreen(battleScreen)   // AT-013
 */
public class BattleScreen implements Screen {

    private final AshenThroneGame game;
    private final BattleEngine    engine;

    private BattleState currentState;
    private SpriteBatch batch;

    // AT-012: single adapter instance — registered with Gdx.input for the lifetime of this screen.
    private final BattleInputAdapter inputAdapter = new BattleInputAdapter();

    // AT-009: observer listeners — kept as fields so other systems can query their state.
    private final BattleLogListener battleLog      = new BattleLogListener();
    private final VictoryChecker    victoryChecker = new VictoryChecker();

    public BattleScreen(AshenThroneGame game, Hero hero, List<Enemy> enemies) {
        if (game == null)    throw new IllegalArgumentException("game must not be null");
        if (hero == null)    throw new IllegalArgumentException("hero must not be null");
        if (enemies == null) throw new IllegalArgumentException("enemies must not be null");
        this.game   = game;
        this.engine = new BattleEngine();
        engine.startBattle(hero, enemies); // AT-010: engine owns hero + enemies
    }

    // ---- Screen lifecycle ----

    @Override
    public void show() {
        batch = new SpriteBatch();

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

        // AT-012: register the adapter once; states swap the listener via setListener().
        inputAdapter.setEnemyCount(engine.getEnemies().size());
        Gdx.input.setInputProcessor(inputAdapter);

        currentState = new PlayerTurnState(this);
    }

    /**
     * Constructs the battle HUD as a Composite UIComponent tree (AT-011).
     *
     * Root (transparent Panel 1280×720)
     *   ├── HealthBar — hero               (top-left)
     *   ├── HealthBar — enemy 0..n         (top-right, spaced horizontally)
     *   ├── BattleLog                      (bottom-left)
     *   └── ActionMenu (4 ActionButtons)   (bottom-center)
     */
    private void buildHud() {
        battleHud = new Panel(0, 0, 1280, 720);

        // Hero health bar
        battleHud.addChild(new HealthBar(engine.getHero(), 50, 650, 200, 20));

        // Enemy health bars — spaced horizontally from the right side
        List<Enemy> enemies = engine.getEnemies();
        for (int i = 0; i < enemies.size(); i++) {
            battleHud.addChild(new HealthBar(enemies.get(i), 850 + i * 150f, 650, 140, 20));
        }

        // Battle log (bottom-left)
        battleHud.addChild(new BattleLog(battleLog, 20, 20, 360, 110));

        // Action menu (bottom-center)
        actionMenu = new ActionMenu(440, 10, 400, 80);
        battleHud.addChild(actionMenu);
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
        // TODO: AT-015 — update viewport/camera on resize
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
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

    public Hero        getHero()    { return engine.getHero(); }
    public List<Enemy> getEnemies() { return engine.getEnemies(); }
    public AshenThroneGame getGame()  { return game; }

    /** Exposes the engine so states can call executeEnemyTurns(), isOver(), getResult(). */
    public BattleEngine getBattleEngine() { return engine; }

    // AT-009: expose observers so UI (AT-011) and screen flow (AT-013) can read them.
    public BattleLogListener getBattleLog()      { return battleLog; }
    public VictoryChecker    getVictoryChecker() { return victoryChecker; }

    // AT-012: expose adapter so states can register as listener in their constructor.
    public BattleInputAdapter getInputAdapter() { return inputAdapter; }
}
