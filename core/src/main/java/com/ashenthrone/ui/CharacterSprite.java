package com.ashenthrone.ui;

import com.ashenthrone.characters.AbstractCharacter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Battle-scene portrait for a single character (AT-015, AT-018/AT-019).
 *
 * <p>Supports horizontal sprite sheets ({@code frameCount > 1}). Idle state
 * renders frame 0 only; calling {@link #playActionAnimation()} cycles through
 * every frame over {@link #ACTION_DURATION} seconds, then returns to idle.
 *
 * <p>Falls back to a flat tinted rectangle when no texture was supplied.
 */
public class CharacterSprite extends UIComponent {

    /** Total animation duration for one full attack/skill cycle (seconds). */
    private static final float ACTION_DURATION = 0.45f;

    private final AbstractCharacter character;
    private final Color   tint;
    private final Texture texture;
    private final int     frameCount;

    private boolean animating;
    private float   elapsed;

    /** Colored-rectangle constructor — no texture, no animation. */
    public CharacterSprite(AbstractCharacter character, Color tint,
                           float x, float y, float width, float height) {
        this(character, tint, null, 1, x, y, width, height);
    }

    /** Texture-backed, single-frame constructor. */
    public CharacterSprite(AbstractCharacter character, Color tint, Texture texture,
                           float x, float y, float width, float height) {
        this(character, tint, texture, 1, x, y, width, height);
    }

    /**
     * Full constructor.
     *
     * @param frameCount horizontal frame count in the sprite sheet (≥ 1).
     *                   1 means the whole texture is a single still image.
     */
    public CharacterSprite(AbstractCharacter character, Color tint, Texture texture,
                           int frameCount,
                           float x, float y, float width, float height) {
        this.character = character;
        this.tint      = tint;
        this.texture   = texture;
        this.frameCount = Math.max(1, frameCount);
        this.x = x;
        this.y = y;
        this.width  = width;
        this.height = height;
    }

    /**
     * Kick off the action animation (cycles through all frames once).
     * Calling again mid-animation restarts from frame 0.
     */
    public void playActionAnimation() {
        if (frameCount <= 1 || texture == null) return;
        animating = true;
        elapsed   = 0f;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (!animating) return;
        elapsed += delta;
        if (elapsed >= ACTION_DURATION) {
            animating = false;
            elapsed   = 0f;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!visible) return;
        if (character.getHp() <= 0) return; // dead characters vanish.

        if (texture != null) {
            batch.setColor(Color.WHITE);
            int frame = currentFrame();
            int frameW = texture.getWidth() / frameCount;
            int srcX   = frame * frameW;
            batch.draw(texture,
                    x, y, width, height,
                    srcX, 0, frameW, texture.getHeight(),
                    false, false);
        } else {
            batch.setColor(tint);
            batch.draw(pixel(), x, y, width, height);
            batch.setColor(Color.WHITE);
        }
    }

    /** Which sheet column should be drawn this frame. */
    private int currentFrame() {
        if (!animating || frameCount <= 1) return 0;
        // Progress through frames 0 .. frameCount-1 evenly over ACTION_DURATION.
        int idx = (int) (elapsed / (ACTION_DURATION / frameCount));
        if (idx >= frameCount) idx = frameCount - 1;
        return idx;
    }
}
