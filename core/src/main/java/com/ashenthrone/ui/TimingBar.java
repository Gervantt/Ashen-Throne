package com.ashenthrone.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Horizontal timing bar shown when the player confirms an Attack.
 *
 * A cursor bounces left-right across the bar at a constant speed.
 * The player presses Confirm to stop it; where it lands determines the
 * damage multiplier:
 *
 * <pre>
 *   |x - 0.5| &lt; 0.02   → CRITICAL  (red,    2.0×)
 *   |x - 0.5| &lt; 0.10   → GREAT     (green,  1.0×)
 *   |x - 0.5| &lt; 0.25   → WEAK      (yellow, 0.5×)
 *   else               → MISS      (gray,   0×)
 * </pre>
 *
 * Zone widths are expressed as a half-width fraction of the bar
 * (so {@code 0.10} means a band from 40 % to 60 % of the bar).
 *
 * Owned by {@link com.ashenthrone.battle.state.TimingBarState}, which
 * adds it to the HUD while the player is aiming.
 */
public class TimingBar extends UIComponent {

    /** Outcome zones, ordered from best to worst. */
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

    /** Sweeps per second (full left↔right cycle). One sweep ≈ 1.0 s. */
    private static final float CURSOR_SPEED = 1.0f;
    /** Cursor width in pixels. */
    private static final float CURSOR_W = 4f;

    private static final Color BG_FRAME = new Color(0.08f, 0.08f, 0.10f, 0.95f);
    private static final Color BG_MISS  = new Color(0.35f, 0.35f, 0.40f, 1f);
    private static final Color C_WEAK   = new Color(0.85f, 0.75f, 0.20f, 1f);
    private static final Color C_GREAT  = new Color(0.25f, 0.75f, 0.30f, 1f);
    private static final Color C_CRIT   = new Color(0.85f, 0.20f, 0.20f, 1f);
    private static final Color CURSOR   = new Color(1f, 1f, 1f, 1f);

    private final GlyphLayout layout = new GlyphLayout();

    /** Cursor position as a fraction of the bar, in [0, 1]. */
    private float cursorPos = 0f;
    /** Sign of cursor velocity (+1 right, -1 left). */
    private float direction = 1f;
    /** When non-null, the bar has stopped and shows this outcome. */
    private Result stoppedAt;

    public TimingBar(float x, float y, float width, float height) {
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
    }

    /**
     * Stops the cursor and returns the resulting hit zone.
     * Subsequent calls return the same cached result.
     */
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

        // Frame / background (miss zone fills the whole bar by default).
        batch.setColor(BG_FRAME);
        float pad = 3f;
        batch.draw(pixel(), x - pad, y - pad, width + pad * 2, height + pad * 2);
        batch.setColor(BG_MISS);
        batch.draw(pixel(), x, y, width, height);

        // Zones, drawn widest first so the narrower ones overlay on top.
        drawZone(batch, WEAK_HALF_WIDTH,  C_WEAK);
        drawZone(batch, GREAT_HALF_WIDTH, C_GREAT);
        drawZone(batch, CRIT_HALF_WIDTH,  C_CRIT);

        // Cursor.
        float cursorX = x + cursorPos * width - CURSOR_W / 2f;
        batch.setColor(CURSOR);
        batch.draw(pixel(), cursorX, y - 4f, CURSOR_W, height + 8f);
        batch.setColor(Color.WHITE);

        // Hint / result label centered above the bar.
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
