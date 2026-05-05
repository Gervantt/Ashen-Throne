package com.ashenthrone.screens;

import com.ashenthrone.core.AshenThroneGame;
import com.badlogic.gdx.Screen;

/**
 * Common ancestor for every screen in the game (AT-013).
 *
 * Provides empty default implementations for the Screen lifecycle methods
 * so concrete screens only override what they actually need. All screens
 * hold a reference to the game singleton for navigation via setScreen()
 * (later routed through TransitionManager — AT-025).
 */
public abstract class BaseScreen implements Screen {

    protected final AshenThroneGame game;

    protected BaseScreen(AshenThroneGame game) {
        this.game = game;
    }

    @Override public void show() {}
    @Override public void render(float delta) {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}