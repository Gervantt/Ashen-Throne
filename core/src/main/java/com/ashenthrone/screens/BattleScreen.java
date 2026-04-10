package com.ashenthrone.screens;

import com.ashenthrone.battle.state.BattleState;
import com.ashenthrone.battle.state.PlayerTurnState;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.Hero;
import com.ashenthrone.core.AshenThroneGame;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;

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

    public BattleScreen(AshenThroneGame game, Hero hero, List<Enemy> enemies) {
        this.game = game;
        this.hero = hero;
        this.enemies = enemies;
    }

    // ---- Screen lifecycle ----

    @Override
    public void show() {
        batch = new SpriteBatch();
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

    // ---- Accessors for states ----

    public Hero getHero() { return hero; }
    public List<Enemy> getEnemies() { return enemies; }
    public AshenThroneGame getGame() { return game; }
}