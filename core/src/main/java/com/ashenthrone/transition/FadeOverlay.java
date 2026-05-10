package com.ashenthrone.transition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

/**
 * Full-screen black fade overlay (AT-022).
 *
 * <p>A {@link ShapeRenderer} draws a black quad over the entire window and
 * animates its alpha. Used by {@link com.ashenthrone.transition.TransitionManager}
 * (AT-025) to bracket screen swaps with fade-out / fade-in.
 *
 * <p>State machine:
 * <pre>
 *   IDLE  ── fadeOut() ──→ FADING_OUT  ── alpha→1 ──→ OPAQUE  (onComplete fires)
 *   OPAQUE ── fadeIn()  ──→ FADING_IN   ── alpha→0 ──→ IDLE    (onComplete fires)
 * </pre>
 *
 * <p>Default duration {@value #DEFAULT_DURATION} seconds. Calling
 * {@link #fadeIn} or {@link #fadeOut} mid-animation overrides the current one
 * (the previous {@code onComplete} is dropped).
 *
 * <p>Render contract: {@link #render()} owns its own GL state — it is safe to
 * call between {@code SpriteBatch.end()} and the next frame; do <b>not</b>
 * call it inside an active {@code SpriteBatch.begin()/end()} block.
 */
public class FadeOverlay implements Disposable {

    public static final float DEFAULT_DURATION = 0.3f;

    private enum Phase { IDLE, FADING_IN, FADING_OUT, OPAQUE }

    private final ShapeRenderer renderer = new ShapeRenderer();

    private Phase    phase    = Phase.IDLE;
    private float    duration = DEFAULT_DURATION;
    private float    elapsed  = 0f;
    private float    alpha    = 0f;
    private Runnable onComplete;

    /** Fade from opaque black to clear over {@code duration}s; reveals the screen. */
    public void fadeIn(float duration, Runnable onComplete) {
        this.duration   = Math.max(0.0001f, duration);
        this.elapsed    = 0f;
        this.alpha      = 1f;
        this.phase      = Phase.FADING_IN;
        this.onComplete = onComplete;
    }

    /** Fade from clear to opaque black over {@code duration}s; covers the screen. */
    public void fadeOut(float duration, Runnable onComplete) {
        this.duration   = Math.max(0.0001f, duration);
        this.elapsed    = 0f;
        this.alpha      = 0f;
        this.phase      = Phase.FADING_OUT;
        this.onComplete = onComplete;
    }

    public void update(float delta) {
        if (phase != Phase.FADING_IN && phase != Phase.FADING_OUT) return;

        elapsed += delta;
        float t = Math.min(1f, elapsed / duration);

        if (phase == Phase.FADING_IN) {
            alpha = 1f - t;
            if (t >= 1f) {
                alpha = 0f;
                phase = Phase.IDLE;
                fireComplete();
            }
        } else { // FADING_OUT
            alpha = t;
            if (t >= 1f) {
                alpha = 1f;
                phase = Phase.OPAQUE;
                fireComplete();
            }
        }
    }

    /** Draws the black quad at the current alpha. No-op when fully transparent and idle. */
    public void render() {
        if (alpha <= 0f) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(0f, 0f, 0f, alpha);
        renderer.rect(0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        renderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public boolean isAnimating() {
        return phase == Phase.FADING_IN || phase == Phase.FADING_OUT;
    }

    public boolean isOpaque() {
        return phase == Phase.OPAQUE;
    }

    public float getAlpha() {
        return alpha;
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }

    private void fireComplete() {
        Runnable r = onComplete;
        onComplete = null;
        if (r != null) r.run();
    }
}