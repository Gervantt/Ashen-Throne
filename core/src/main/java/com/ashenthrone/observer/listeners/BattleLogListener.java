package com.ashenthrone.observer.listeners;

import com.ashenthrone.observer.EventListener;
import com.ashenthrone.observer.GameEvent;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class BattleLogListener implements EventListener {

    public static final int MAX_LINES = 6;

    private final Deque<String> lines = new ArrayDeque<>();

    @Override
    public void onEvent(GameEvent event) {
        String line = switch (event.getType()) {
            case DAMAGE_DEALT -> {
                String sourceName = event.getSource() != null ? event.getSource().getName() : "Unknown";
                String targetName = event.getTarget() != null ? event.getTarget().getName() : "Unknown";
                yield sourceName + " dealt " + event.getAmount() + " damage to " + targetName + ".";
            }
            case CHARACTER_DIED -> {
                String name = event.getCharacter() != null ? event.getCharacter().getName() : "Unknown";
                yield name + " has been defeated!";
            }
            case ITEM_USED -> {
                String targetName = event.getTarget() != null ? event.getTarget().getName() : "Unknown";
                String itemName = event.getItem() != null ? displayName(event.getItem().toString()) : "an item";
                yield targetName + " used " + itemName + ".";
            }
            case BATTLE_END -> "Battle ended: " + event.getResult() + "!";
        };

        append(line);
    }

    private void append(String line) {
        if (lines.size() >= MAX_LINES) {
            lines.pollFirst();
        }
        lines.addLast(line);
    }

    public List<String> getLines() {
        return Collections.unmodifiableList(lines.stream().toList());
    }

    public void clear() {
        lines.clear();
    }

    private static String displayName(String id) {
        if ("HealthPotion".equals(id)) return "Health Potion";
        return id;
    }
}
