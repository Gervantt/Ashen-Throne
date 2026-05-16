package com.ashenthrone.ui;

import com.ashenthrone.characters.AbstractCharacter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class HealthBar extends UIComponent {

    private static final Color BG_COLOR   = new Color(0.35f, 0.05f, 0.05f, 1f);
    private static final Color FILL_COLOR = new Color(0.15f, 0.75f, 0.15f, 1f);
    private static final float LABEL_OFFSET_Y = 14f;

    private final AbstractCharacter character;

    public HealthBar(AbstractCharacter character, float x, float y, float width, float height) {
        this.character = character;
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!visible) return;
        if (!character.isAlive()) return;

        int hp    = character.getHp();
        int maxHp = character.getMaxHp();
        float fillRatio = maxHp > 0 ? (float) hp / maxHp : 0f;

        batch.setColor(BG_COLOR);
        batch.draw(pixel(), x, y, width, height);

        batch.setColor(FILL_COLOR);
        batch.draw(pixel(), x, y, width * fillRatio, height);

        batch.setColor(Color.WHITE);

        font().setColor(Color.WHITE);
        font().draw(batch, character.getName() + "  " + hp + "/" + maxHp,
                x, y + height + LABEL_OFFSET_Y);
    }
}
