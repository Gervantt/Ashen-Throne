package com.ashenthrone.screens;

import com.ashenthrone.battle.BattleEngine;
import com.ashenthrone.battle.command.BattleCommand;
import com.ashenthrone.battle.state.BattleState;
import com.ashenthrone.battle.state.PlayerTurnState;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.Hero;
import com.ashenthrone.core.AshenThroneGame;
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

    public BattleScreen(AshenThroneGame game, Hero hero, List<Enemy> enemies) {
        if (game == null)    throw new IllegalArgumentException("game must not be null");
        if (hero == null)    throw new IllegalArgumentException("hero must not be null");
        if (enemies == null) throw new IllegalArgumentException("enemies must not be null");
        this.game   = game;
        this.engine = new BattleEngine();
        engine.startBattle(hero, enemies);
    }

    // ---- Screen lifecycle ----

    @Override
    public void show() {
        batch        = new SpriteBatch();
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

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
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
}