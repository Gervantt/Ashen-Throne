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

/**
 * Mediator singleton that owns every screen transition (AT-025).
 *
 * <p>Screens never call {@code game.setScreen()} directly — they delegate to
 * {@link #goTo(ScreenType)} (and typed overloads for parameterised screens),
 * keeping every screen oblivious of every other screen.
 *
 * <p>Each transition runs the following sequence:
 * <ol>
 *   <li>Play {@code transition_whoosh} SFX.</li>
 *   <li>Fade to black via {@link FadeOverlay#fadeOut} (AT-022).</li>
 *   <li>At the opaque midpoint: stop current music, instantiate the target
 *       screen, call {@code game.setScreen()} (which triggers the previous
 *       screen's {@code hide()} and the new screen's {@code show()} — the
 *       new screen starts its own music there).</li>
 *   <li>Fade back in.</li>
 * </ol>
 *
 * <p>While a transition is in flight further {@code goTo} calls are ignored
 * to prevent re-entrancy. The mediator does <em>not</em> own a
 * {@link ShapeRenderer} of its own; it delegates rendering to its bundled
 * {@link FadeOverlay}, which is rendered on top of the active screen by
 * {@link AshenThroneGame#render()}.
 */
public class TransitionManager implements Disposable {

    /** Default duration of each half of the fade transition. */
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

    /** Wires the manager to the running game. Called once from create(). */
    public void init(AshenThroneGame game) {
        this.game = game;
    }

    /** Advance the fade animation. Drive from {@link AshenThroneGame#render()}. */
    public void update(float delta) {
        overlay.update(delta);
    }

    /** Render the fade overlay on top of the current screen. */
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

    // ---- Routing ----

    /**
     * Navigate to a parameter-less screen. Throws {@link IllegalArgumentException}
     * for screen types that need constructor arguments — use the typed
     * overloads ({@link #goToBattle}, {@link #goToVictory}, {@link #goToDefeat}).
     */
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

    // ---- Internal pipeline ----

    private void swap(ScreenFactory factory) {
        if (game == null) {
            // Not initialised yet — failing silently leaves the player stuck,
            // so surface the bug loudly.
            throw new IllegalStateException("TransitionManager.init(game) not called");
        }
        if (transitioning) return;
        transitioning = true;

        AudioManager.getInstance().playSFX("transition_whoosh");
        overlay.fadeOut(DEFAULT_FADE_DURATION, () -> {
            // Midpoint — screen swap happens under cover.
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
