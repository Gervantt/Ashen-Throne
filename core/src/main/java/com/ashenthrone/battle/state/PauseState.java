package com.ashenthrone.battle.state;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.battle.ActionType;
import com.ashenthrone.core.GameSession;
import com.ashenthrone.input.BattleInputAdapter;
import com.ashenthrone.screens.BattleScreen;
import com.ashenthrone.transition.ScreenType;
import com.ashenthrone.transition.TransitionManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

public class PauseState implements BattleState, BattleInputAdapter.ActionListener, Disposable {

    private static final int   SCREEN_W = 1280;
    private static final int   SCREEN_H = 720;
    private static final float BTN_W    = 320f;
    private static final float BTN_H    = 60f;
    private static final float BTN_GAP  = 24f;

    private static final String[] LABELS = { "Continue", "Exit to Menu" };
    private static final int IDX_CONTINUE = 0;
    private static final int IDX_EXIT     = 1;

    private final BattleScreen screen;
    private final BattleState  previous;

    private BitmapFont  titleFont;
    private BitmapFont  buttonFont;
    private GlyphLayout layout;
    private Texture     pixel;
    private int         hovered = IDX_CONTINUE;

    public PauseState(BattleScreen screen, BattleState previous) {
        this.screen   = screen;
        this.previous = previous;
        this.titleFont  = new BitmapFont();
        this.buttonFont = new BitmapFont();
        this.layout     = new GlyphLayout();
        this.titleFont.getData().setScale(3f);
        this.buttonFont.getData().setScale(1.6f);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        this.pixel = new Texture(pm);
        pm.dispose();

        screen.getInputAdapter().setListener(this);
    }

    @Override public void handleInput() {}

    @Override public void update(float delta) {}

    @Override public void render(SpriteBatch batch) {}

    @Override
    public void renderOverlay(SpriteBatch batch) {

        batch.setColor(0f, 0f, 0f, 0.65f);
        batch.draw(pixel, 0, 0, SCREEN_W, SCREEN_H);
        batch.setColor(Color.WHITE);

        titleFont.setColor(new Color(0.95f, 0.85f, 0.4f, 1f));
        layout.setText(titleFont, "PAUSED");
        titleFont.draw(batch, layout,
                (SCREEN_W - layout.width) / 2f,
                SCREEN_H / 2f + 140f);

        float totalH = LABELS.length * BTN_H + (LABELS.length - 1) * BTN_GAP;
        float firstY = (SCREEN_H + totalH) / 2f - BTN_H - 30f;
        for (int i = 0; i < LABELS.length; i++) {
            float bx = (SCREEN_W - BTN_W) / 2f;
            float by = firstY - i * (BTN_H + BTN_GAP);
            drawButton(batch, LABELS[i], bx, by, i == hovered);
        }
    }

    private void drawButton(SpriteBatch batch, String label, float x, float y, boolean isHovered) {
        Color border = isHovered ? new Color(0.95f, 0.75f, 0.35f, 1f)
                                 : new Color(0.45f, 0.35f, 0.20f, 1f);
        Color bg     = isHovered ? new Color(0.30f, 0.20f, 0.10f, 1f)
                                 : new Color(0.12f, 0.10f, 0.14f, 1f);

        batch.setColor(border);
        batch.draw(pixel, x - 2, y - 2, BTN_W + 4, BTN_H + 4);
        batch.setColor(bg);
        batch.draw(pixel, x, y, BTN_W, BTN_H);
        batch.setColor(Color.WHITE);

        buttonFont.setColor(isHovered ? new Color(1f, 0.95f, 0.7f, 1f) : Color.LIGHT_GRAY);
        layout.setText(buttonFont, label);
        buttonFont.draw(batch, layout,
                x + (BTN_W - layout.width) / 2f,
                y + (BTN_H + layout.height) / 2f);
    }

    @Override public void onActionSelected(ActionType type) {}
    @Override public void onTargetSelected(int enemyIndex)  {}

    @Override
    public void onConfirm() {
        AudioManager.getInstance().playSFX("transition_whoosh");
        if (hovered == IDX_CONTINUE) resume();
        else                         exitToMenu();
    }

    @Override
    public void onCancel() {
        resume();
    }

    @Override
    public void onPause() {
        resume();
    }

    private void resume() {

        screen.setState(previous);
        if (previous instanceof BattleInputAdapter.ActionListener prevListener) {
            screen.getInputAdapter().setListener(prevListener);
        }
        dispose();
    }

    private void exitToMenu() {
        GameSession.getInstance().abandonActiveRun();
        TransitionManager.getInstance().goTo(ScreenType.MAIN_MENU);
        dispose();
    }

    @Override
    public void dispose() {
        if (titleFont != null)  { titleFont.dispose();  titleFont = null;  }
        if (buttonFont != null) { buttonFont.dispose(); buttonFont = null; }
        if (pixel != null)      { pixel.dispose();      pixel = null;      }
    }

    @Override
    public void onNavigate(int dy) {
        hovered = ((hovered + dy) % LABELS.length + LABELS.length) % LABELS.length;
    }

    public int getHovered() { return hovered; }
}
