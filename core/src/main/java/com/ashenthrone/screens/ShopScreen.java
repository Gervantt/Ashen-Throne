package com.ashenthrone.screens;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.core.AshenThroneGame;
import com.ashenthrone.core.GameSession;
import com.ashenthrone.equipment.CursedRing;
import com.ashenthrone.equipment.FireAmulet;
import com.ashenthrone.equipment.ShadowBlade;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Shop screen (AT-020).
 *
 * Reachable from the main menu. Displays the player's current gold and the
 * hero's effective stats (already wrapped with previously-purchased
 * Decorators), plus three items for sale:
 *   FireAmulet  — +5 attack,             50 gold
 *   CursedRing  — +8 defense, -2 attack, 40 gold
 *   ShadowBlade — +10 attack,            80 gold
 *   HealthPotion — heals 35% max HP,      25 gold
 *
 * Buying an item subtracts its cost, appends the item id to
 * {@link GameSession#equipItem(String)}, and plays {@code purchase_sound}.
 * Items can be re-bought (the spec doesn't forbid it; a Decorator stack
 * naturally allows multiple copies). Insufficient gold disables the buy
 * action with no SFX.
 */
public class ShopScreen extends BaseScreen {

    private static final int SCREEN_W = 1280;
    private static final int SCREEN_H = 720;

    public static final String ITEM_FIRE_AMULET  = "FireAmulet";
    public static final String ITEM_CURSED_RING  = "CursedRing";
    public static final String ITEM_SHADOW_BLADE = "ShadowBlade";
    public static final String ITEM_HEALTH_POTION = "HealthPotion";

    private static final ItemDef[] ITEMS = {
            new ItemDef(ITEM_FIRE_AMULET,  "Fire Amulet",  "+5 Attack",            50,
                    new Color(0.85f, 0.40f, 0.25f, 1f), false),
            new ItemDef(ITEM_CURSED_RING,  "Cursed Ring",  "+8 Defense, -2 Attack", 40,
                    new Color(0.55f, 0.35f, 0.65f, 1f), false),
            new ItemDef(ITEM_SHADOW_BLADE, "Shadow Blade", "+10 Attack",            80,
                    new Color(0.40f, 0.40f, 0.55f, 1f), false),
            new ItemDef(ITEM_HEALTH_POTION, "Health Potion", "Heal 35% max HP",     25,
                    new Color(0.35f, 0.75f, 0.55f, 1f), true),
    };

    // ---- Layout ----
    private static final float CARD_W = 280f;
    private static final float CARD_H = 370f;
    private static final float CARD_GAP = 20f;
    private static final float CARDS_TOTAL_W = ITEMS.length * CARD_W + (ITEMS.length - 1) * CARD_GAP;
    private static final float CARDS_X = (SCREEN_W - CARDS_TOTAL_W) / 2f;
    private static final float CARDS_Y = 180f;

    private static final float BUY_W = CARD_W - 60f;
    private static final float BUY_H = 50f;

    private static final float BACK_W = 160f;
    private static final float BACK_H = 44f;

    private SpriteBatch batch;
    private BitmapFont  titleFont;
    private BitmapFont  itemFont;
    private BitmapFont  bodyFont;
    private Texture     pixel;
    private Texture     background;          // backgrounds/main_menu.png
    private Texture[]   itemIcons;           // images/items/item_<id>.png per ITEMS entry
    private GlyphLayout layout;
    private Viewport    viewport;
    private final Vector3 touchTmp = new Vector3();

    private int hovered = 0;

    public ShopScreen(AshenThroneGame game) {
        super(game);
    }

    @Override
    public void show() {
        batch     = new SpriteBatch();
        titleFont = new BitmapFont();
        itemFont  = new BitmapFont();
        bodyFont  = new BitmapFont();
        layout    = new GlyphLayout();
        viewport  = new FitViewport(SCREEN_W, SCREEN_H);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        titleFont.getData().setScale(2.4f);
        itemFont.getData().setScale(1.6f);
        bodyFont.getData().setScale(1.2f);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        background = tryLoad("backgrounds/main_menu.png");
        itemIcons  = new Texture[ITEMS.length];
        for (int i = 0; i < ITEMS.length; i++) {
            itemIcons[i] = tryLoad("images/items/item_" + toSnakeCase(ITEMS[i].id) + ".png");
        }

        // Main theme persists across menu/shop/settings/hero-select/tower
        // (idempotent — won't restart if already playing).
        AudioManager.getInstance().playMusic("main_theme");

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
                    hovered = (hovered - 1 + ITEMS.length) % ITEMS.length;
                    return true;
                }
                if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
                    hovered = (hovered + 1) % ITEMS.length;
                    return true;
                }
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    buy(hovered);
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    back();
                    return true;
                }
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                int idx = cardAt(screenX, screenY);
                if (idx >= 0) hovered = idx;
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                int idx = buyButtonAt(screenX, screenY);
                if (idx >= 0) {
                    hovered = idx;
                    buy(idx);
                    return true;
                }
                idx = cardAt(screenX, screenY);
                if (idx >= 0) {
                    hovered = idx;
                    return true;
                }
                if (backButtonHit(screenX, screenY)) {
                    back();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.06f, 0.05f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        if (background != null) {
            batch.setColor(Color.WHITE);
            batch.draw(background, 0, 0, SCREEN_W, SCREEN_H);
        }

        titleFont.setColor(new Color(0.85f, 0.7f, 0.3f, 1f));
        layout.setText(titleFont, "MERCHANT");
        titleFont.draw(batch, layout, (SCREEN_W - layout.width) / 2f, SCREEN_H - 30f);

        drawHeroSummary();
        drawCards();
        drawBackButton();

        batch.end();
    }

    /** Top-left summary: gold + current effective stats from the wrapped hero. */
    private void drawHeroSummary() {
        GameSession s = GameSession.getInstance();

        // Gold pill.
        bodyFont.setColor(new Color(0.95f, 0.85f, 0.3f, 1f));
        bodyFont.draw(batch, "Gold: " + s.getGold(), 60f, SCREEN_H - 90f);

        AbstractCharacter equipped = EquipmentApplier.apply(
                (AbstractCharacter) s.getHero(), s.getEquippedItems());

        bodyFont.setColor(new Color(0.85f, 0.80f, 0.70f, 1f));
        if (equipped == null) {
            bodyFont.draw(batch, "No hero selected.", 60f, SCREEN_H - 130f);
            return;
        }
        bodyFont.draw(batch, "Hero: "    + equipped.getName(),    60f, SCREEN_H - 130f);
        bodyFont.draw(batch, "HP "       + equipped.getHp() + "/" + equipped.getMaxHp(),
                60f, SCREEN_H - 160f);
        bodyFont.draw(batch, "Attack: "  + equipped.getAttack(),  60f, SCREEN_H - 190f);
        bodyFont.draw(batch, "Defense: " + equipped.getDefense(), 60f, SCREEN_H - 220f);
        bodyFont.draw(batch, "Speed: "   + equipped.getSpeed(),   60f, SCREEN_H - 250f);

        // Equipped list — short comma-separated tail to confirm purchases.
        if (!s.getEquippedItems().isEmpty()) {
            bodyFont.setColor(new Color(0.7f, 0.85f, 0.95f, 1f));
            bodyFont.draw(batch, "Equipped: " + String.join(", ", s.getEquippedItems()),
                    60f, SCREEN_H - 290f);
        }

        int potionCount = s.getConsumableCount(ITEM_HEALTH_POTION);
        bodyFont.setColor(new Color(0.70f, 0.95f, 0.75f, 1f));
        bodyFont.draw(batch, "Potions: " + potionCount, 60f, SCREEN_H - 320f);
    }

    private void drawCards() {
        int gold = GameSession.getInstance().getGold();
        for (int i = 0; i < ITEMS.length; i++) {
            ItemDef item = ITEMS[i];
            float x = cardX(i);
            float y = CARDS_Y;
            boolean hot = (i == hovered);
            boolean affordable = gold >= item.cost;

            // Frame.
            Color border = hot ? new Color(0.95f, 0.75f, 0.35f, 1f)
                               : new Color(0.45f, 0.35f, 0.20f, 1f);
            batch.setColor(border);
            batch.draw(pixel, x - 3, y - 3, CARD_W + 6, CARD_H + 6);

            // Body.
            batch.setColor(new Color(0.13f, 0.10f, 0.14f, 1f));
            batch.draw(pixel, x, y, CARD_W, CARD_H);
            batch.setColor(Color.WHITE);

            // Icon — uses items/item_<id>.png if present, falls back to tinted block.
            float iconW = 180f;
            float iconH = 180f;
            float iconX = x + (CARD_W - iconW) / 2f;
            float iconY = y + CARD_H - iconH - 50f;
            batch.setColor(new Color(0.25f, 0.20f, 0.15f, 1f));
            batch.draw(pixel, iconX - 3, iconY - 3, iconW + 6, iconH + 6);
            if (itemIcons[i] != null) {
                batch.setColor(Color.WHITE);
                batch.draw(itemIcons[i], iconX, iconY, iconW, iconH);
            } else {
                batch.setColor(item.tint);
                batch.draw(pixel, iconX, iconY, iconW, iconH);
                batch.setColor(Color.WHITE);
            }

            // Title + effect.
            itemFont.setColor(new Color(1f, 0.95f, 0.75f, 1f));
            layout.setText(itemFont, item.name);
            itemFont.draw(batch, layout, x + (CARD_W - layout.width) / 2f, iconY - 20f);

            bodyFont.setColor(new Color(0.85f, 0.80f, 0.70f, 1f));
            layout.setText(bodyFont, item.effect);
            bodyFont.draw(batch, layout, x + (CARD_W - layout.width) / 2f, iconY - 50f);

            // Price line.
            bodyFont.setColor(affordable ? new Color(0.95f, 0.85f, 0.3f, 1f)
                                         : new Color(0.7f, 0.40f, 0.40f, 1f));
            String priceText = item.cost + " gold";
            layout.setText(bodyFont, priceText);
            bodyFont.draw(batch, layout, x + (CARD_W - layout.width) / 2f, iconY - 80f);

            // Buy button.
            float[] r = buyRect(i);
            Color bbg = !affordable ? new Color(0.18f, 0.10f, 0.10f, 1f)
                       : (hot       ? new Color(0.30f, 0.20f, 0.10f, 1f)
                                    : new Color(0.16f, 0.13f, 0.18f, 1f));
            Color bborder = !affordable ? new Color(0.40f, 0.20f, 0.20f, 1f)
                          : (hot         ? new Color(0.95f, 0.75f, 0.35f, 1f)
                                         : new Color(0.45f, 0.35f, 0.20f, 1f));
            batch.setColor(bborder);
            batch.draw(pixel, r[0] - 2, r[1] - 2, r[2] + 4, r[3] + 4);
            batch.setColor(bbg);
            batch.draw(pixel, r[0], r[1], r[2], r[3]);
            batch.setColor(Color.WHITE);

            bodyFont.setColor(!affordable ? new Color(0.5f, 0.4f, 0.4f, 1f)
                                          : new Color(1f, 0.95f, 0.7f, 1f));
            String label = affordable ? "Buy" : "Need more gold";
            layout.setText(bodyFont, label);
            bodyFont.draw(batch, layout,
                    r[0] + (r[2] - layout.width) / 2f,
                    r[1] + (r[3] + layout.height) / 2f);
        }
    }

    private void drawBackButton() {
        float[] r = backRect();
        boolean hot = backButtonHit(Gdx.input.getX(), Gdx.input.getY());
        Color bg     = hot ? new Color(0.30f, 0.20f, 0.10f, 1f) : new Color(0.12f, 0.10f, 0.14f, 1f);
        Color border = hot ? new Color(0.95f, 0.75f, 0.35f, 1f) : new Color(0.45f, 0.35f, 0.20f, 1f);
        batch.setColor(border);
        batch.draw(pixel, r[0] - 2, r[1] - 2, r[2] + 4, r[3] + 4);
        batch.setColor(bg);
        batch.draw(pixel, r[0], r[1], r[2], r[3]);
        batch.setColor(Color.WHITE);

        bodyFont.setColor(hot ? new Color(1f, 0.95f, 0.7f, 1f) : Color.LIGHT_GRAY);
        layout.setText(bodyFont, "Back");
        bodyFont.draw(batch, layout,
                r[0] + (r[2] - layout.width) / 2f,
                r[1] + (r[3] + layout.height) / 2f);
    }

    // ---- Hit tests ----

    private static float cardX(int i) {
        return CARDS_X + i * (CARD_W + CARD_GAP);
    }

    private float[] buyRect(int i) {
        float x = cardX(i) + (CARD_W - BUY_W) / 2f;
        float y = CARDS_Y + 30f;
        return new float[] { x, y, BUY_W, BUY_H };
    }

    private float[] backRect() {
        return new float[] { 40f, 30f, BACK_W, BACK_H };
    }

    private int cardAt(int screenX, int screenY) {
        touchTmp.set(screenX, screenY, 0);
        viewport.unproject(touchTmp);
        for (int i = 0; i < ITEMS.length; i++) {
            float x = cardX(i);
            if (touchTmp.x >= x && touchTmp.x <= x + CARD_W
                    && touchTmp.y >= CARDS_Y && touchTmp.y <= CARDS_Y + CARD_H) {
                return i;
            }
        }
        return -1;
    }

    private int buyButtonAt(int screenX, int screenY) {
        touchTmp.set(screenX, screenY, 0);
        viewport.unproject(touchTmp);
        for (int i = 0; i < ITEMS.length; i++) {
            float[] r = buyRect(i);
            if (touchTmp.x >= r[0] && touchTmp.x <= r[0] + r[2]
                    && touchTmp.y >= r[1] && touchTmp.y <= r[1] + r[3]) {
                return i;
            }
        }
        return -1;
    }

    private boolean backButtonHit(int screenX, int screenY) {
        touchTmp.set(screenX, screenY, 0);
        viewport.unproject(touchTmp);
        float[] r = backRect();
        return touchTmp.x >= r[0] && touchTmp.x <= r[0] + r[2]
                && touchTmp.y >= r[1] && touchTmp.y <= r[1] + r[3];
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.update(width, height, true);
    }

    // ---- Actions ----

    private void buy(int index) {
        ItemDef item = ITEMS[index];
        GameSession s = GameSession.getInstance();
        if (!s.spendGold(item.cost)) {
            // Insufficient gold — silent reject. The card visibly shows
            // "Need more gold" so the player already has feedback.
            return;
        }
        if (item.consumable) {
            s.addConsumable(item.id, 1);
        } else {
            s.equipItem(item.id);
        }
        AudioManager.getInstance().playSFX("purchase_sound");
    }

    private void back() {
        com.ashenthrone.transition.TransitionManager.getInstance()
                .goTo(com.ashenthrone.transition.ScreenType.MAIN_MENU);
    }

    @Override
    public void dispose() {
        batch.dispose();
        titleFont.dispose();
        itemFont.dispose();
        bodyFont.dispose();
        pixel.dispose();
        if (background != null) background.dispose();
        if (itemIcons != null) {
            for (Texture t : itemIcons) if (t != null) t.dispose();
        }
    }

    private static Texture tryLoad(String path) {
        try {
            com.badlogic.gdx.files.FileHandle fh = Gdx.files.internal(path);
            if (fh.exists()) return new Texture(fh);
        } catch (Exception e) {
            Gdx.app.log("ShopScreen", "Failed to load '" + path + "': " + e.getMessage());
        }
        return null;
    }

    /** "FireAmulet" → "fire_amulet". */
    private static String toSnakeCase(String camel) {
        if (camel == null || camel.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c) && i > 0) sb.append('_');
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    // ---- Internals ----

    private static final class ItemDef {
        final String id;
        final String name;
        final String effect;
        final int cost;
        final Color tint;
        final boolean consumable;

        ItemDef(String id, String name, String effect, int cost, Color tint, boolean consumable) {
            this.id = id;
            this.name = name;
            this.effect = effect;
            this.cost = cost;
            this.tint = tint;
            this.consumable = consumable;
        }
    }

    /**
     * Wraps a base character in the Decorator chain implied by a list of item
     * ids. Used by ShopScreen to preview stats and by RealmSelectScreen to
     * apply equipment when entering battle. Lives here because the shop owns
     * the item-id vocabulary; pulling it into a separate class would just
     * duplicate the switch.
     */
    public static final class EquipmentApplier {
        private EquipmentApplier() {}

        public static AbstractCharacter apply(AbstractCharacter base, java.util.List<String> itemIds) {
            if (base == null) return null;
            AbstractCharacter c = base;
            for (String id : itemIds) {
                c = switch (id) {
                    case ITEM_FIRE_AMULET  -> new FireAmulet(c);
                    case ITEM_CURSED_RING  -> new CursedRing(c);
                    case ITEM_SHADOW_BLADE -> new ShadowBlade(c);
                    default -> c; // Unknown id — skip rather than crash on stale save data.
                };
            }
            return c;
        }
    }
}
