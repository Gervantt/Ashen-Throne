package com.ashenthrone.screens;

import com.ashenthrone.characters.Hero;
import com.ashenthrone.characters.HeroBuilder;
import com.ashenthrone.core.AshenThroneGame;
import com.ashenthrone.core.GameSession;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * First screen shown when the game starts (AT-013).
 *
 * Displays the game title and a "Press ENTER to Begin" prompt.
 * On confirm, creates a fresh Hero, spawns encounter-0 enemies, and
 * transitions to BattleScreen via AshenThroneGame.setScreen().
 */
public class MainMenuScreen implements Screen {

    private static final int SCREEN_W = 1280;
    private static final int SCREEN_H = 720;

    private final AshenThroneGame game;
    private SpriteBatch  batch;
    private BitmapFont   titleFont;
    private BitmapFont   promptFont;
    private Texture      pixel;
    private GlyphLayout  layout;

    public MainMenuScreen(AshenThroneGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch      = new SpriteBatch();
        titleFont  = new BitmapFont();
        promptFont = new BitmapFont();
        layout     = new GlyphLayout();

        titleFont.getData().setScale(4f);
        promptFont.getData().setScale(1.5f);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    startGame();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        titleFont.setColor(new Color(0.85f, 0.7f, 0.3f, 1f));
        layout.setText(titleFont, "ASHEN THRONE");
        titleFont.draw(batch, layout,
                (SCREEN_W - layout.width) / 2f,
                SCREEN_H / 2f + 60f);

        promptFont.setColor(Color.LIGHT_GRAY);
        layout.setText(promptFont, "Press ENTER to Begin");
        promptFont.draw(batch, layout,
                (SCREEN_W - layout.width) / 2f,
                SCREEN_H / 2f - 20f);

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
        promptFont.dispose();
        pixel.dispose();
    }

    // ---- Navigation ----

    private void startGame() {
        GameSession.getInstance().reset();
        Hero hero = new HeroBuilder()
                .name("Kael")
                .hp(120)
                .attack(18)
                .defense(12)
                .speed(10)
                .build();
        game.setScreen(new BattleScreen(game, hero, AshenThroneGame.spawnEnemies(0)));
    }
}
