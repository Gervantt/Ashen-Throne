package com.ashenthrone.core;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.prototype.EnemyRegistry;
import com.ashenthrone.screens.MainMenuScreen;
import com.ashenthrone.screens.SettingsScreen;
import com.ashenthrone.transition.TransitionManager;
import com.badlogic.gdx.Game;

import java.util.List;

public class AshenThroneGame extends Game {

    private static AshenThroneGame instance;

    private AshenThroneGame() {}

    public static AshenThroneGame getInstance() {
        if (instance == null) {
            instance = new AshenThroneGame();
        }
        return instance;
    }


    @Override
    public void create() {
        GameSession.getInstance().reset();
        SettingsScreen.applySavedDisplayMode();
        TransitionManager.getInstance().init(this);

        setScreen(new MainMenuScreen(this));
    }


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
