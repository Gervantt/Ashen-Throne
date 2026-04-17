package com.ashenthrone.screens;

import com.ashenthrone.characters.Hero;
import com.ashenthrone.core.AshenThroneGame;
import com.ashenthrone.core.GameSession;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Shown after the hero is defeated (AT-013).
 *
 * Offers two options:
 *   ENTER/SPACE — retry the same encounter (hero HP restored to max)
 *   ESCAPE      — return to the main menu and reset the session
 */
public class DefeatScreen implements Screen {

    private static final int SCREEN_W = 1280;
    private static final int SCREEN_H = 720;

    private final AshenThroneGame game;
    private final Hero            hero;

    private SpriteBatch batch;
    private BitmapFont  titleFont;
    private BitmapFont  infoFont;
    private GlyphLayout layout;

    public DefeatScreen(AshenThroneGame game, Hero hero) {
        this.game = game;
        this.hero = hero;
    }

    @Override
    public void show() {
        batch     = new SpriteBatch();
        titleFont = new BitmapFont();
        infoFont  = new BitmapFont();
        layout    = new GlyphLayout();

        titleFont.getData().setScale(3f);
        infoFont.getData().setScale(1.4f);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    retry();
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    mainMenu();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.03f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        titleFont.setColor(new Color(0.85f, 0.15f, 0.15f, 1f));
        layout.setText(titleFont, "DEFEAT");
        titleFont.draw(batch, layout,
                (SCREEN_W - layout.width) / 2f,
                SCREEN_H / 2f + 80f);

        infoFont.setColor(Color.LIGHT_GRAY);
        drawCentered("ENTER — Retry encounter", SCREEN_H / 2f);
        drawCentered("ESC   — Main Menu",       SCREEN_H / 2f - 45f);

        batch.end();
    }

    private void drawCentered(String text, float y) {
        layout.setText(infoFont, text);
        infoFont.draw(batch, layout, (SCREEN_W - layout.width) / 2f, y);
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
        titleFont.dispose();
        infoFont.dispose();
    }

    // ---- Navigation ----

    private void retry() {
        hero.setHp(hero.getMaxHp());
        int idx = GameSession.getInstance().getCurrentEncounterIndex();
        game.setScreen(new BattleScreen(game, hero, AshenThroneGame.spawnEnemies(idx)));
    }

    private void mainMenu() {
        GameSession.getInstance().reset();
        game.setScreen(new MainMenuScreen(game));
    }
}
