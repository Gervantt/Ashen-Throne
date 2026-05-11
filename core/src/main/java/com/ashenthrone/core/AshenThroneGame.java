package com.ashenthrone.core;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.prototype.EnemyRegistry;
import com.ashenthrone.screens.MainMenuScreen;
import com.ashenthrone.screens.SettingsScreen;
import com.ashenthrone.transition.TransitionManager;
import com.badlogic.gdx.Game;

import java.util.List;

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
     * Resets session state and launches the main menu (AT-013).
     */
    @Override
    public void create() {
        GameSession.getInstance().reset();
        SettingsScreen.applySavedDisplayMode(); // AT-017
        TransitionManager.getInstance().init(this); // AT-025
        // Initial screen does not transition (there's nothing to fade from).
        setScreen(new MainMenuScreen(this));
    }

    /**
     * Per-frame render: delegates to the active Screen via super.render(),
     * then drives + draws the fade overlay on top (AT-022 / AT-025).
     */
    @Override
    public void render() {
        super.render();
        TransitionManager tm = TransitionManager.getInstance();
        tm.update(com.badlogic.gdx.Gdx.graphics.getDeltaTime());
        tm.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        TransitionManager.getInstance().dispose();
        AudioManager.getInstance().dispose();
    }

    // ---- Encounter factory (AT-013) ----

    /**
     * Returns a fresh enemy list for the given encounter index.
     *
     * The pattern cycles every 4 encounters, escalating in difficulty:
     *   0 — single minion (ShadowCrawler)
     *   1 — minion + elite (ShadowCrawler + Wraith)
     *   2 — two heavies (HollowWolf + Treant)
     *   3 — boss (HollowKing)
     */
    public static List<Enemy> spawnEnemies(int encounterIndex) {
        EnemyRegistry reg = EnemyRegistry.getInstance();
        return switch (encounterIndex % 4) {
            case 0  -> List.of(reg.spawn("ShadowCrawler"));
            case 1  -> List.of(reg.spawn("ShadowCrawler"), reg.spawn("Wraith"));
            case 2  -> List.of(reg.spawn("HollowWolf"),    reg.spawn("Treant"));
            default -> List.of(reg.spawn("HollowKing"));
        };
    }
}
