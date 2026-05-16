package com.ashenthrone.transition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

public class FadeOverlay implements Disposable {

    public static final float DEFAULT_DURATION = 0.3f;

    private enum Phase { IDLE, FADING_IN, FADING_OUT, OPAQUE }

    private final ShapeRenderer renderer = new ShapeRenderer();

    private Phase    phase    = Phase.IDLE;
    private float    duration = DEFAULT_DURATION;
    private float    elapsed  = 0f;
    private float    alpha    = 0f;
    private Runnable onComplete;

    public void fadeIn(float duration, Runnable onComplete) {
        this.duration   = Math.max(0.0001f, duration);
        this.elapsed    = 0f;
        this.alpha      = 1f;
        this.phase      = Phase.FADING_IN;
        this.onComplete = onComplete;
    }

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
        } else {
            alpha = t;
            if (t >= 1f) {
                alpha = 1f;
                phase = Phase.OPAQUE;
                fireComplete();
            }
        }
    }

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