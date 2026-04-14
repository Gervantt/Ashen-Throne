package com.ashenthrone.observer.listeners;

import com.ashenthrone.observer.EventListener;
import com.ashenthrone.observer.GameEvent;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Appends human-readable lines to a scrolling battle log.
 *
 * Subscribes to all four event types. The log is capped at
 * {@value #MAX_LINES} entries; oldest entries are dropped first.
 *
 * AT-011 UIComponent (BattleLog) will read {@link #getLines()} to render.
 */
public class BattleLogListener implements EventListener {

    /** Maximum number of log lines retained (matches UI spec in AT-011). */
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
                yield targetName + " used an item.";
            }
            case BATTLE_END -> "Battle ended: " + event.getResult() + "!";
        };

        append(line);
    }

    /** Appends a line, evicting the oldest entry when the cap is reached. */
    private void append(String line) {
        if (lines.size() >= MAX_LINES) {
            lines.pollFirst();
        }
        lines.addLast(line);
    }

    /** Returns the current log lines in oldest-to-newest order (unmodifiable). */
    public List<String> getLines() {
        return Collections.unmodifiableList(lines.stream().toList());
    }

    /** Clears the log; call when starting a new battle. */
    public void clear() {
        lines.clear();
    }
}