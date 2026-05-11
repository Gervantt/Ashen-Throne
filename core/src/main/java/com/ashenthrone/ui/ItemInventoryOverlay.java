package com.ashenthrone.ui;

import com.ashenthrone.core.GameSession;
import com.ashenthrone.screens.ShopScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Small battle inventory panel shown when the player selects Item.
 */
public class ItemInventoryOverlay extends UIComponent {

    private final GlyphLayout layout = new GlyphLayout();
    private Texture potionIcon;

    public ItemInventoryOverlay(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        potionIcon = tryLoad("images/items/item_health_potion.png");
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!visible) return;

        batch.setColor(new Color(0.05f, 0.04f, 0.06f, 0.94f));
        batch.draw(pixel(), x, y, width, height);
        batch.setColor(new Color(0.85f, 0.65f, 0.25f, 1f));
        batch.draw(pixel(), x - 2f, y - 2f, width + 4f, 2f);
        batch.draw(pixel(), x - 2f, y + height, width + 4f, 2f);
        batch.draw(pixel(), x - 2f, y - 2f, 2f, height + 4f);
        batch.draw(pixel(), x + width, y - 2f, 2f, height + 4f);

        font().setColor(new Color(1f, 0.92f, 0.65f, 1f));
        layout.setText(font(), "Inventory");
        font().draw(batch, layout, x + 20f, y + height - 20f);

        int count = GameSession.getInstance().getConsumableCount(ShopScreen.ITEM_HEALTH_POTION);
        float itemX = x + 24f;
        float itemY = y + 24f;
        float itemH = 74f;

        batch.setColor(count > 0
                ? new Color(0.17f, 0.13f, 0.11f, 1f)
                : new Color(0.10f, 0.09f, 0.10f, 1f));
        batch.draw(pixel(), itemX, itemY, width - 48f, itemH);
        batch.setColor(count > 0
                ? new Color(0.95f, 0.75f, 0.35f, 1f)
                : new Color(0.35f, 0.30f, 0.28f, 1f));
        batch.draw(pixel(), itemX, itemY + itemH - 2f, width - 48f, 2f);

        float iconSize = 54f;
        float iconX = itemX + 10f;
        float iconY = itemY + 10f;
        if (potionIcon != null) {
            batch.setColor(count > 0 ? Color.WHITE : new Color(0.35f, 0.35f, 0.35f, 1f));
            batch.draw(potionIcon, iconX, iconY, iconSize, iconSize);
        } else {
            batch.setColor(new Color(0.35f, 0.75f, 0.55f, count > 0 ? 1f : 0.35f));
            batch.draw(pixel(), iconX, iconY, iconSize, iconSize);
        }

        font().setColor(count > 0 ? Color.WHITE : new Color(0.55f, 0.50f, 0.48f, 1f));
        String label = count > 0 ? "Health Potion x" + count : "No consumables";
        layout.setText(font(), label);
        font().draw(batch, layout, iconX + iconSize + 18f, itemY + 47f);

        if (count > 0) {
            font().setColor(new Color(0.70f, 0.95f, 0.75f, 1f));
            layout.setText(font(), "Heals 35% max HP");
            font().draw(batch, layout, iconX + iconSize + 18f, itemY + 24f);
        }

        batch.setColor(Color.WHITE);
    }

    @Override
    public void dispose() {
        if (potionIcon != null) {
            potionIcon.dispose();
            potionIcon = null;
        }
        super.dispose();
    }

    private static Texture tryLoad(String path) {
        try {
            com.badlogic.gdx.files.FileHandle fh = Gdx.files.internal(path);
            if (fh.exists()) return new Texture(fh);
        } catch (Exception e) {
            Gdx.app.log("ItemInventoryOverlay", "Failed to load '" + path + "': " + e.getMessage());
        }
        return null;
    }
}
