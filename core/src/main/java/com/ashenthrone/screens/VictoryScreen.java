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
 * Shown after the player defeats all enemies in an encounter (AT-013).
 *
 * Displays the total gold accumulated so far and prompts the player
 * to continue to the next encounter. The hero reference is threaded
 * forward so HP and equipment carry over between encounters.
 *
 * Gold was already credited by VictoryState.update() before this screen
 * was created, so GameSession.getGold() reflects the post-battle total.
 */
public class VictoryScreen implements Screen {

    private static final int SCREEN_W = 1280;
    private static final int SCREEN_H = 720;

    private final AshenThroneGame game;
    private final Hero            hero;

    private SpriteBatch batch;
    private BitmapFont  titleFont;
    private BitmapFont  infoFont;
    private GlyphLayout layout;

    public VictoryScreen(AshenThroneGame game, Hero hero) {
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
                    nextEncounter();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.1f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        titleFont.setColor(new Color(0.3f, 0.9f, 0.3f, 1f));
        layout.setText(titleFont, "VICTORY!");
        titleFont.draw(batch, layout,
                (SCREEN_W - layout.width) / 2f,
                SCREEN_H / 2f + 80f);

        infoFont.setColor(new Color(0.9f, 0.75f, 0.1f, 1f));
        String goldText = "Gold: " + GameSession.getInstance().getGold();
        layout.setText(infoFont, goldText);
        infoFont.draw(batch, layout,
                (SCREEN_W - layout.width) / 2f,
                SCREEN_H / 2f);

        infoFont.setColor(Color.LIGHT_GRAY);
        layout.setText(infoFont, "Press ENTER for next encounter");
        infoFont.draw(batch, layout,
                (SCREEN_W - layout.width) / 2f,
                SCREEN_H / 2f - 50f);

        batch.end();
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

    private void nextEncounter() {
        int idx = GameSession.getInstance().getCurrentEncounterIndex();
        game.setScreen(new BattleScreen(game, hero, AshenThroneGame.spawnEnemies(idx)));
    }
}
