package com.ashenthrone.ui;

import com.ashenthrone.characters.AbstractCharacter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CharacterSprite extends UIComponent {

    private static final float ACTION_DURATION = 0.45f;

    private final AbstractCharacter character;
    private final Color   tint;
    private final Texture texture;
    private final int     frameCount;

    private boolean animating;
    private float   elapsed;

    public CharacterSprite(AbstractCharacter character, Color tint,
                           float x, float y, float width, float height) {
        this(character, tint, null, 1, x, y, width, height);
    }

    public CharacterSprite(AbstractCharacter character, Color tint, Texture texture,
                           float x, float y, float width, float height) {
        this(character, tint, texture, 1, x, y, width, height);
    }


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
        if (character.getHp() <= 0) return;

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

    private int currentFrame() {
        if (!animating || frameCount <= 1) return 0;
        int idx = (int) (elapsed / (ACTION_DURATION / frameCount));
        if (idx >= frameCount) idx = frameCount - 1;
        return idx;
    }
}
