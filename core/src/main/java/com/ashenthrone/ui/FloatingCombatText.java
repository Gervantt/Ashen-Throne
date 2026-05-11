package com.ashenthrone.ui;

import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.observer.EventListener;
import com.ashenthrone.observer.GameEvent;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Floating damage / item feedback anchored to character positions.
 */
public class FloatingCombatText extends UIComponent implements EventListener {

    private static final float LIFETIME = 1.05f;
    private static final float RISE = 42f;

    private final Map<AbstractCharacter, Anchor> anchors = new HashMap<>();
    private final List<Entry> entries = new ArrayList<>();
    private final GlyphLayout layout = new GlyphLayout();

    public void register(AbstractCharacter character, float centerX, float headY) {
        if (character != null) anchors.put(character, new Anchor(centerX, headY));
    }

    @Override
    public void onEvent(GameEvent event) {
        if (event == null) return;
        switch (event.getType()) {
            case DAMAGE_DEALT -> add(event.getTarget(), "-" + event.getAmount(),
                    new Color(1f, 0.22f, 0.12f, 1f));
            case ITEM_USED -> add(event.getTarget(), "+HP",
                    new Color(0.45f, 1f, 0.55f, 1f));
            default -> { }
        }
    }

    @Override
    public void update(float delta) {
        Iterator<Entry> it = entries.iterator();
        while (it.hasNext()) {
            Entry e = it.next();
            e.age += delta;
            if (e.age >= LIFETIME) it.remove();
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!visible) return;
        for (Entry e : entries) {
            float progress = e.age / LIFETIME;
            float alpha = 1f - progress;
            font().getData().setScale(1.45f);
            font().setColor(e.color.r, e.color.g, e.color.b, alpha);
            layout.setText(font(), e.text);
            font().draw(batch, layout, e.x - layout.width / 2f, e.y + progress * RISE);
        }
        font().getData().setScale(1f);
        batch.setColor(Color.WHITE);
    }

    private void add(AbstractCharacter character, String text, Color color) {
        Anchor anchor = anchors.get(character);
        if (anchor == null) return;
        entries.add(new Entry(text, color, anchor.centerX, anchor.headY + 24f));
    }

    private record Anchor(float centerX, float headY) {}

    private static final class Entry {
        final String text;
        final Color color;
        final float x;
        final float y;
        float age;

        Entry(String text, Color color, float x, float y) {
            this.text = text;
            this.color = color;
            this.x = x;
            this.y = y;
        }
    }
}
