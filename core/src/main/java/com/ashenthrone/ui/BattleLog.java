package com.ashenthrone.ui;

import com.ashenthrone.observer.listeners.BattleLogListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

/**
 * Scrolling battle log showing up to {@link BattleLogListener#MAX_LINES} lines (AT-011).
 *
 * Reads live log lines from the BattleLogListener each frame, so it always
 * reflects the most recent events without any extra wiring.
 *
 * Visual: semi-transparent dark background, white text lines spaced by font line height.
 */
public class BattleLog extends UIComponent {

    private static final Color BG_COLOR   = new Color(0f, 0f, 0f, 0.65f);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final float PADDING    = 6f;
    private static final float LINE_HEIGHT = 16f;

    private final BattleLogListener listener;

    public BattleLog(BattleLogListener listener, float x, float y, float width, float height) {
        this.listener = listener;
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!visible) return;

        // Semi-transparent background
        batch.setColor(BG_COLOR);
        batch.draw(pixel(), x, y, width, height);
        batch.setColor(Color.WHITE);

        // Log lines — oldest at top, newest at bottom
        List<String> lines = listener.getLines();
        font().setColor(TEXT_COLOR);
        for (int i = 0; i < lines.size(); i++) {
            float lineY = y + height - PADDING - LINE_HEIGHT * (i + 1);
            font().draw(batch, lines.get(i), x + PADDING, lineY);
        }
    }
}
