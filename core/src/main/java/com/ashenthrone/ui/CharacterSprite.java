package com.ashenthrone.ui;

import com.ashenthrone.characters.AbstractCharacter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Placeholder character portrait for the battle scene (AT-015).
 *
 * Renders a flat colored rectangle at a fixed slot. AT-018 / AT-019 / AT-021
 * will swap this for the actual hero / enemy sheet textures. The character
 * reference is held so the sprite can disappear when the character dies.
 */
public class CharacterSprite extends UIComponent {

    private final AbstractCharacter character;
    private final Color tint;

    public CharacterSprite(AbstractCharacter character, Color tint,
                           float x, float y, float width, float height) {
        this.character = character;
        this.tint      = tint;
        this.x = x;
        this.y = y;
        this.width  = width;
        this.height = height;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!visible) return;
        if (character.getHp() <= 0) return; // dead characters vanish until AT-021 adds death anim.

        batch.setColor(tint);
        batch.draw(pixel(), x, y, width, height);
        batch.setColor(Color.WHITE);
        // TODO: AT-018 / AT-019 / AT-021 — draw the character sprite sheet here.
    }
}