package com.ashenthrone.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.function.Supplier;

/**
 * Top-center label that announces whose turn it is (AT-015).
 *
 * Reads its label live from a {@link Supplier} so it stays in sync with the
 * battle state machine without observers — the screen passes a lambda that
 * inspects {@code currentState}.
 */
public class TurnIndicator extends UIComponent {

    private static final Color BG_COLOR   = new Color(0f, 0f, 0f, 0.55f);
    private static final Color TEXT_COLOR = new Color(0.95f, 0.9f, 0.55f, 1f);

    private final Supplier<String> labelSupplier;
    private final GlyphLayout layout = new GlyphLayout();

    public TurnIndicator(Supplier<String> labelSupplier,
                         float x, float y, float width, float height) {
        this.labelSupplier = labelSupplier;
        this.x = x;
        this.y = y;
        this.width  = width;
        this.height = height;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!visible) return;

        batch.setColor(BG_COLOR);
        batch.draw(pixel(), x, y, width, height);
        batch.setColor(Color.WHITE);

        String text = labelSupplier.get();
        font().setColor(TEXT_COLOR);
        layout.setText(font(), text);
        float tx = x + (width  - layout.width)  / 2f;
        float ty = y + (height + layout.height) / 2f;
        font().draw(batch, layout, tx, ty);
    }
}