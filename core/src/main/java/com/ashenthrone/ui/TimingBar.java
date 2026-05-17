package com.ashenthrone.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TimingBar extends UIComponent {

    public enum Result {
        CRITICAL(2.0f, "CRITICAL!"),
        GREAT   (1.0f, "GREAT"),
        WEAK    (0.5f, "WEAK"),
        MISS    (0.0f, "MISS");

        public final float multiplier;
        public final String label;
        Result(float multiplier, String label) {
            this.multiplier = multiplier;
            this.label = label;
        }
    }

    private static final float CRIT_HALF_WIDTH  = 0.02f;
    private static final float GREAT_HALF_WIDTH = 0.10f;
    private static final float WEAK_HALF_WIDTH  = 0.25f;

    private static final float CURSOR_SPEED = 1.0f;
    private static final float CURSOR_W = 4f;

    private static final Color BG_FRAME = new Color(0.08f, 0.08f, 0.10f, 0.95f);
    private static final Color BG_MISS  = new Color(0.35f, 0.35f, 0.40f, 1f);
    private static final Color C_WEAK   = new Color(0.85f, 0.75f, 0.20f, 1f);
    private static final Color C_GREAT  = new Color(0.25f, 0.75f, 0.30f, 1f);
    private static final Color C_CRIT   = new Color(0.85f, 0.20f, 0.20f, 1f);
    private static final Color CURSOR   = new Color(1f, 1f, 1f, 1f);

    private final GlyphLayout layout = new GlyphLayout();

    private float cursorPos = 0f;
    private float direction = 1f;
    private Result stoppedAt;

    public TimingBar(float x, float y, float width, float height) {
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
    }

    public Result stop() {
        if (stoppedAt == null) {
            stoppedAt = resultAt(cursorPos);
        }
        return stoppedAt;
    }

    public boolean isStopped() {
        return stoppedAt != null;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (stoppedAt != null) return;
        cursorPos += direction * CURSOR_SPEED * delta;
        if (cursorPos >= 1f) {
            cursorPos = 1f;
            direction = -1f;
        } else if (cursorPos <= 0f) {
            cursorPos = 0f;
            direction = 1f;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!visible) return;

        batch.setColor(BG_FRAME);
        float pad = 3f;
        batch.draw(pixel(), x - pad, y - pad, width + pad * 2, height + pad * 2);
        batch.setColor(BG_MISS);
        batch.draw(pixel(), x, y, width, height);

        drawZone(batch, WEAK_HALF_WIDTH,  C_WEAK);
        drawZone(batch, GREAT_HALF_WIDTH, C_GREAT);
        drawZone(batch, CRIT_HALF_WIDTH,  C_CRIT);

        float cursorX = x + cursorPos * width - CURSOR_W / 2f;
        batch.setColor(CURSOR);
        batch.draw(pixel(), cursorX, y - 4f, CURSOR_W, height + 8f);
        batch.setColor(Color.WHITE);

        String text = stoppedAt != null ? stoppedAt.label : "Press SPACE / ENTER";
        font().setColor(stoppedAt != null ? labelColor(stoppedAt) : Color.WHITE);
        layout.setText(font(), text);
        float textX = x + (width - layout.width) / 2f;
        float textY = y + height + 22f;
        font().draw(batch, layout, textX, textY);
        font().setColor(Color.WHITE);
    }

    private void drawZone(SpriteBatch batch, float halfWidth, Color color) {
        float zoneW = width * halfWidth * 2f;
        float zoneX = x + width / 2f - zoneW / 2f;
        batch.setColor(color);
        batch.draw(pixel(), zoneX, y, zoneW, height);
    }

    private static Result resultAt(float pos) {
        float d = Math.abs(pos - 0.5f);
        if (d < CRIT_HALF_WIDTH)  return Result.CRITICAL;
        if (d < GREAT_HALF_WIDTH) return Result.GREAT;
        if (d < WEAK_HALF_WIDTH)  return Result.WEAK;
        return Result.MISS;
    }

    private static Color labelColor(Result r) {
        return switch (r) {
            case CRITICAL -> C_CRIT;
            case GREAT    -> C_GREAT;
            case WEAK     -> C_WEAK;
            case MISS     -> BG_MISS;
        };
    }
}
