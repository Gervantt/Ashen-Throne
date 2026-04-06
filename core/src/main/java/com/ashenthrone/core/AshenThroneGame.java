package com.ashenthrone.core;

import com.badlogic.gdx.Game;

/**
 * The root application class.
 *
 * libGDX note: Game is a convenience class that implements ApplicationListener
 * and adds screen management. The framework calls create() once at startup,
 * then calls render() every frame. Game.render() delegates to the current
 * Screen's render() method, so you control what's on screen by calling
 * setScreen(). You don't need to manage the game loop yourself.
 */
public class AshenThroneGame extends Game {

    private static AshenThroneGame instance;

    private AshenThroneGame() {}

    public static AshenThroneGame getInstance() {
        if (instance == null) {
            instance = new AshenThroneGame();
        }
        return instance;
    }

    /**
     * Called once when the application starts (after the OpenGL context is ready).
     * This is where you load assets, create your first screen, etc.
     */
    @Override
    public void create() {
        GameSession.getInstance().reset();
        // TODO: setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        // TODO: dispose AudioManager, AssetManager, etc.
    }
}
