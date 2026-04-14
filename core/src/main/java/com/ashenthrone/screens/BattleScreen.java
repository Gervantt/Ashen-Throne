package com.ashenthrone.screens;

import com.ashenthrone.battle.command.BattleCommand;
import com.ashenthrone.battle.state.BattleState;
import com.ashenthrone.battle.state.PlayerTurnState;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.Hero;
import com.ashenthrone.core.AshenThroneGame;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;

import com.ashenthrone.observer.EventManager;
import com.ashenthrone.observer.EventType;
import com.ashenthrone.observer.listeners.AudioListener;
import com.ashenthrone.observer.listeners.BattleLogListener;
import com.ashenthrone.observer.listeners.HealthBarListener;
import com.ashenthrone.observer.listeners.VictoryChecker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * The battle screen hosts the battle loop and owns the State Machine (AT-006).
 *
 * It holds the current {@link BattleState} and delegates every frame's
 * handleInput / update / render calls to it. States transition by calling
 * {@link #setState(BattleState)}.
 *
 * BattleScreen intentionally knows nothing about battle logic — it is a
 * coordinator, not an actor. Once AT-010 is done, BattleScreen will talk to
 * BattleEngine instead of holding hero and enemies directly.
 *
 * Construction:
 *   new BattleScreen(AshenThroneGame.getInstance(), hero, enemies)
 * Transition in:
 *   AshenThroneGame.getInstance().setScreen(battleScreen)   // AT-013
 */
public class BattleScreen implements Screen {

    private final AshenThroneGame game;
    private final Hero hero;
    private final List<Enemy> enemies;

    private BattleState currentState;
    private SpriteBatch batch;

    // AT-007: command history for undo. TODO: AT-010 — move to BattleEngine.
    private final Deque<BattleCommand> commandHistory = new ArrayDeque<>();

    // AT-009: observer listeners — kept as fields so other systems can query their state.
    private final BattleLogListener battleLog     = new BattleLogListener();
    private final VictoryChecker    victoryChecker = new VictoryChecker();

    public BattleScreen(AshenThroneGame game, Hero hero, List<Enemy> enemies) {
        if (game == null)    throw new IllegalArgumentException("game must not be null");
        if (hero == null)    throw new IllegalArgumentException("hero must not be null");
        if (enemies == null) throw new IllegalArgumentException("enemies must not be null");
        this.game    = game;
        this.hero    = hero;
        this.enemies = new ArrayList<>(enemies); // defensive copy — caller cannot mutate our list
    }

    // ---- Screen lifecycle ----

    @Override
    public void show() {
        batch = new SpriteBatch();
        commandHistory.clear();

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

        currentState = new PlayerTurnState(this);
    }

    /**
     * Called every frame by libGDX.
     * Clears the screen, then lets the current state handle input, update, and render.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        currentState.handleInput();
        currentState.update(delta);
        currentState.render(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // TODO: AT-015 — update viewport/camera on resize
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
    }

    // ---- State machine ----

    /** Transitions to a new state immediately (takes effect next frame). */
    public void setState(BattleState state) {
        this.currentState = state;
    }

    // ---- Command history (AT-007) ----

    /** Executes a command and pushes it onto the undo history. */
    public void executeCommand(BattleCommand command) {
        command.execute();
        commandHistory.push(command);
    }

    /** Undoes the most recent command. No-op if history is empty. */
    public void undoLastCommand() {
        if (!commandHistory.isEmpty()) {
            commandHistory.pop().undo();
        }
    }

    /** True if there is at least one command that can be undone. */
    public boolean canUndo() {
        return !commandHistory.isEmpty();
    }

    // ---- Accessors for states ----

    public Hero getHero() { return hero; }
    public List<Enemy> getEnemies() { return Collections.unmodifiableList(enemies); }
    public AshenThroneGame getGame() { return game; }

    // AT-009: expose observers so UI (AT-011) and screen flow (AT-013) can read them.
    public BattleLogListener getBattleLog()      { return battleLog; }
    public VictoryChecker    getVictoryChecker() { return victoryChecker; }
}