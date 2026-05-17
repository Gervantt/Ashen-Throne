package com.ashenthrone.transition;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.core.AshenThroneGame;
import com.ashenthrone.screens.BattleScreen;
import com.ashenthrone.screens.DefeatScreen;
import com.ashenthrone.screens.EndGameScreen;
import com.ashenthrone.screens.HeroSelectScreen;
import com.ashenthrone.screens.MainMenuScreen;
import com.ashenthrone.screens.RealmSelectScreen;
import com.ashenthrone.screens.SettingsScreen;
import com.ashenthrone.screens.ShopScreen;
import com.ashenthrone.screens.VictoryScreen;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

import java.util.List;

public class TransitionManager implements Disposable {

    public static final float DEFAULT_FADE_DURATION = FadeOverlay.DEFAULT_DURATION;

    private static TransitionManager instance;

    private AshenThroneGame game;
    private final FadeOverlay overlay = new FadeOverlay();

    private boolean transitioning;

    private TransitionManager() {}

    public static TransitionManager getInstance() {
        if (instance == null) {
            instance = new TransitionManager();
        }
        return instance;
    }

    public void init(AshenThroneGame game) {
        this.game = game;
    }

    public void update(float delta) {
        overlay.update(delta);
    }

    public void render() {
        overlay.render();
    }

    public boolean isTransitioning() {
        return transitioning;
    }

    @Override
    public void dispose() {
        overlay.dispose();
    }


    public void goTo(ScreenType target) {
        switch (target) {
            case MAIN_MENU     -> swap(() -> new MainMenuScreen(game));
            case HERO_SELECT   -> swap(() -> new HeroSelectScreen(game));
            case REALM_SELECT  -> swap(() -> new RealmSelectScreen(game));
            case SHOP          -> swap(() -> new ShopScreen(game));
            case SETTINGS      -> swap(() -> new SettingsScreen(game));
            case END_GAME      -> swap(() -> new EndGameScreen(game));
            case BATTLE, VICTORY, DEFEAT ->
                    throw new IllegalArgumentException(target
                            + " requires parameters — use the typed goTo* method");
        }
    }

    public void goToBattle(AbstractCharacter hero, List<Enemy> enemies) {
        swap(() -> new BattleScreen(game, hero, enemies));
    }

    public void goToVictory(AbstractCharacter hero, boolean realmComplete) {
        swap(() -> new VictoryScreen(game, hero, realmComplete));
    }

    public void goToDefeat(AbstractCharacter hero) {
        swap(() -> new DefeatScreen(game, hero));
    }

    private void swap(ScreenFactory factory) {
        if (game == null) {

            throw new IllegalStateException("TransitionManager.init(game) not called");
        }
        if (transitioning) return;
        transitioning = true;

        AudioManager.getInstance().playSFX("transition_whoosh");
        overlay.fadeOut(DEFAULT_FADE_DURATION, () -> {

            Screen next = factory.create();
            game.setScreen(next);
            overlay.fadeIn(DEFAULT_FADE_DURATION, () -> transitioning = false);
        });
    }

    @FunctionalInterface
    private interface ScreenFactory {
        Screen create();
    }
}
