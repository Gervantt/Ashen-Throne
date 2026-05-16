package com.ashenthrone.screens;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.core.AshenThroneGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Settings screen (AT-017).
 *
 * Two sections:
 *   1. Display — toggle between fullscreen and windowed (1280×720).
 *   2. Controls — read-only list of in-game key bindings.
 *
 * Display preference is persisted via libGDX {@link Preferences} under the
 * key {@code "fullscreen"}; the chosen mode is applied immediately and on
 * subsequent launches by reading the same pref at startup (callers that
 * boot the application can use {@link #applySavedDisplayMode()}).
 */
public class SettingsScreen extends BaseScreen {

    private static final int SCREEN_W = 1280;
    private static final int SCREEN_H = 720;

    private static final String PREFS_NAME = "ashen-throne";
    private static final String PREF_FULLSCREEN = "fullscreen";

    private static final String[][] KEY_BINDINGS = {
            { "1 / 2 / 3 / 4", "Select action" },
            { "Arrows / Click", "Select target" },
            { "Enter / Space",  "Confirm" },
            { "Escape",         "Cancel / Pause" },
            { "U",              "Undo last action" },
    };

    private SpriteBatch batch;
    private BitmapFont  titleFont;
    private BitmapFont  bodyFont;
    private Texture     pixel;
    private GlyphLayout layout;
    private Viewport    viewport;
    private final Vector3 touchTmp = new Vector3();

    private boolean fullscreen;

    // Three interactive buttons: Toggle Display, Apply, Back.
    private static final int BTN_TOGGLE = 0;
    private static final int BTN_BACK   = 1;
    private int hovered = BTN_TOGGLE;
    // Константы разметки (Ширина увеличена до 760px, чтобы плашки клавиш не теснились)
    private final float panelW = 760f;
    private final float panelX = (SCREEN_W - panelW) / 2f;

    // Точная цветовая палитра из целевого дизайна (приглушенное золото и глубокий темный)
    private final Color goldTheme       = new Color(0.78f, 0.61f, 0.34f, 1f);
    private final Color panelBg         = new Color(0.07f, 0.06f, 0.06f, 0.95f);
    private final Color panelBorder     = new Color(0.28f, 0.24f, 0.18f, 1f);
    private final Color textLight       = new Color(0.85f, 0.85f, 0.85f, 1f);

    public SettingsScreen(AshenThroneGame game) {
        super(game);
    }

    /**
     * Reads the persisted display mode and applies it. Safe to call before
     * the first frame; should be invoked from {@code AshenThroneGame.create}
     * once asset/window setup is done.
     */
    public static void applySavedDisplayMode() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        boolean fs = prefs.getBoolean(PREF_FULLSCREEN, false);
        applyDisplayMode(fs);
    }

    private static void applyDisplayMode(boolean fullscreen) {
        if (fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(SCREEN_W, SCREEN_H);
        }
    }

    @Override
    public void show() {
        batch      = new SpriteBatch();
        titleFont  = new BitmapFont();
        bodyFont   = new BitmapFont();
        layout     = new GlyphLayout();
        viewport   = new FitViewport(SCREEN_W, SCREEN_H);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        titleFont.getData().setScale(2.4f);
        bodyFont.getData().setScale(1.3f);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        // Main theme persists across menu/shop/settings/hero-select/tower
        // (idempotent — won't restart if already playing).
        AudioManager.getInstance().playMusic("main_theme");

        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        fullscreen = prefs.getBoolean(PREF_FULLSCREEN, false);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    activate(BTN_BACK);
                    return true;
                }
                if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
                    hovered = (hovered - 1 + 2) % 2;
                    return true;
                }
                if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
                    hovered = (hovered + 1) % 2;
                    return true;
                }
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    activate(hovered);
                    return true;
                }
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                int idx = buttonAt(screenX, screenY);
                if (idx >= 0) hovered = idx;
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                int idx = buttonAt(screenX, screenY);
                if (idx >= 0) {
                    hovered = idx;
                    activate(idx);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.04f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        // 1. Заголовок "SETTINGS" с декоративными линиями по бокам
        titleFont.setColor(goldTheme);
        layout.setText(titleFont, "SETTINGS");
        float titleX = (SCREEN_W - layout.width) / 2f;
        float titleY = SCREEN_H - 60f;
        titleFont.draw(batch, layout, titleX, titleY);

        // Линии слева и справа от заголовка
        batch.setColor(panelBorder);
        batch.draw(pixel, titleX - 140f, titleY - 12f, 110f, 1f);
        batch.draw(pixel, titleX + layout.width + 30f, titleY - 12f, 110f, 1f);

        float currentY = titleY - 60f;

        // 2. Панель DISPLAY (Высота увеличена до 140px для лучшего распределения)
        float displayH = 140f;
        currentY -= displayH;

        // Фон и рамка панели Display
        batch.setColor(panelBg);
        batch.draw(pixel, panelX, currentY, panelW, displayH);
        batch.setColor(panelBorder);
        batch.draw(pixel, panelX, currentY, panelW, 1.5f);
        batch.draw(pixel, panelX, currentY + displayH - 1.5f, panelW, 1.5f);
        batch.draw(pixel, panelX, currentY, 1.5f, displayH);
        batch.draw(pixel, panelX + panelW - 1.5f, currentY, 1.5f, displayH);

        // Заголовок DISPLAY внутри панели и горизонтальная линия от него
        bodyFont.setColor(goldTheme);
        bodyFont.draw(batch, "DISPLAY", panelX + 30f, currentY + displayH - 24f);
        batch.setColor(panelBorder);
        batch.draw(pixel, panelX + 110f, currentY + displayH - 32f, panelW - 140f, 1f);

        // Текстовое описание режима
        bodyFont.setColor(Color.GRAY);
        bodyFont.draw(batch, "Window Mode", panelX + 30f, currentY + 75f);
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, fullscreen ? "Fullscreen" : "Windowed • 1280 × 720", panelX + 30f, currentY + 45f);

        // Кнопка TOGGLE MODE
        drawButton("TOGGLE MODE", panelX + panelW - 220f - 30f, currentY + 42f, 220f, 44f, hovered == BTN_TOGGLE);

        // 3. Панель CONTROLS (Высота 300px под просторную таблицу)
        currentY -= 24f;
        float controlsH = 300f;
        currentY -= controlsH;

        // Фон и рамка панели Controls
        batch.setColor(panelBg);
        batch.draw(pixel, panelX, currentY, panelW, controlsH);
        batch.setColor(panelBorder);
        batch.draw(pixel, panelX, currentY, panelW, 1.5f);
        batch.draw(pixel, panelX, currentY + controlsH - 1.5f, panelW, 1.5f);
        batch.draw(pixel, panelX, currentY, 1.5f, controlsH);
        batch.draw(pixel, panelX + panelW - 1.5f, currentY, 1.5f, controlsH);

        // Заголовок CONTROLS внутри панели и горизонтальная линия от него
        bodyFont.setColor(goldTheme);
        bodyFont.draw(batch, "CONTROLS", panelX + 30f, currentY + controlsH - 24f);
        batch.setColor(panelBorder);
        batch.draw(pixel, panelX + 130f, currentY + controlsH - 32f, panelW - 160f, 1f);

        // Геометрия таблицы управления
        float startTableY = currentY + controlsH - 80f;
        float centerDividerX = panelX + (panelW / 2f) - 30f; // Центральная ось разделителя

        for (int i = 0; i < KEY_BINDINGS.length; i++) {
            float rowY = startTableY - (i * 42f);

            // Рендеринг рамки вокруг горячей клавиши (Key Badge)
            layout.setText(bodyFont, KEY_BINDINGS[i][0]);
            float badgeW = layout.width + 24f;
            float badgeH = 30f;
            float badgeX = centerDividerX - badgeW - 20f;
            float badgeY = rowY - 10f;

            // Темная подложка плашки и её рамка
            batch.setColor(0.11f, 0.09f, 0.08f, 0.7f);
            batch.draw(pixel, badgeX, badgeY, badgeW, badgeH);
            batch.setColor(panelBorder);
            batch.draw(pixel, badgeX, badgeY, badgeW, 1f);
            batch.draw(pixel, badgeX, badgeY + badgeH - 1f, badgeW, 1f);
            batch.draw(pixel, badgeX, badgeY, 1f, badgeH);
            batch.draw(pixel, badgeX + badgeW - 1f, badgeY, 1f, badgeH);

            // Текст внутри плашки
            bodyFont.setColor(goldTheme);
            bodyFont.draw(batch, layout, badgeX + 12f, badgeY + badgeH - 8f);

            // Текст действия в правой колонке
            bodyFont.setColor(textLight);
            bodyFont.draw(batch, KEY_BINDINGS[i][1], centerDividerX + 20f, rowY + 12f);

            // Тонкие горизонтальные разделители строк
            if (i < KEY_BINDINGS.length - 1) {
                batch.setColor(0.14f, 0.12f, 0.12f, 1f);
                batch.draw(pixel, panelX + 30f, rowY - 22f, panelW - 60f, 1f);
            }
        }

        // Вертикальная разделительная линия по центру таблицы
        batch.setColor(panelBorder);
        batch.draw(pixel, centerDividerX, currentY + 25f, 1f, controlsH - 110f);

        // 4. Кнопка BACK (Внизу справа с аккуратной стрелкой)
        drawButton("←  BACK", panelX + panelW - 160f, currentY - 70f, 160f, 44f, hovered == BTN_BACK);

        batch.end();
    }

    private void drawButton(String label, float x, float y, float w, float h, boolean isHovered) {
        // Меняем цвета в зависимости от наведения мыши
        Color bg     = isHovered ? new Color(0.35f, 0.25f, 0.12f, 1f) : new Color(0.13f, 0.11f, 0.12f, 1f);
        Color border = isHovered ? goldTheme : new Color(0.35f, 0.28f, 0.18f, 1f);

        // Заливка кнопки
        batch.setColor(bg);
        batch.draw(pixel, x, y, w, h);

        // Рамка кнопки
        batch.setColor(border);
        batch.draw(pixel, x, y, w, 1.5f);
        batch.draw(pixel, x, y + h - 1.5f, w, 1.5f);
        batch.draw(pixel, x, y, 1.5f, h);
        batch.draw(pixel, x + w - 1.5f, y, 1.5f, h);
        batch.setColor(Color.WHITE);

        bodyFont.setColor(isHovered ? Color.WHITE : Color.LIGHT_GRAY);
        layout.setText(bodyFont, label);
        bodyFont.draw(batch, layout, x + (w - layout.width) / 2f, y + (h + layout.height) / 2f);
    }

    private int buttonAt(int screenX, int screenY) {
        touchTmp.set(screenX, screenY, 0);
        viewport.unproject(touchTmp);
        float vx = touchTmp.x;
        float vy = touchTmp.y;

        // Новые математические Y-координаты интерактивных кнопок
        float toggleY = SCREEN_H - 60f - 60f - 140f + 42f;
        float backY = SCREEN_H - 60f - 60f - 140f - 24f - 300f - 70f;

        if (inRect(vx, vy, panelX + panelW - 220f - 30f, toggleY, 220f, 44f)) return BTN_TOGGLE;
        if (inRect(vx, vy, panelX + panelW - 160f, backY, 160f, 44f)) return BTN_BACK;
        return -1;
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.update(width, height, true);
    }

    private static boolean inRect(float px, float py, float x, float y, float w, float h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    private void activate(int index) {
        switch (index) {
            case BTN_TOGGLE -> {
                AudioManager.getInstance().playSFX("transition_whoosh");
                fullscreen = !fullscreen;
                Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
                prefs.putBoolean(PREF_FULLSCREEN, fullscreen);
                prefs.flush();
                applyDisplayMode(fullscreen);
            }
            case BTN_BACK -> com.ashenthrone.transition.TransitionManager.getInstance()
                    .goTo(com.ashenthrone.transition.ScreenType.MAIN_MENU);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        titleFont.dispose();
        bodyFont.dispose();
        pixel.dispose();
    }
}