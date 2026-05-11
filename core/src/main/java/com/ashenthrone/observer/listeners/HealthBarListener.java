package com.ashenthrone.observer.listeners;

import com.ashenthrone.observer.EventListener;
import com.ashenthrone.observer.EventType;
import com.ashenthrone.observer.GameEvent;

/**
 * Receives {@code DAMAGE_DEALT} / {@code CHARACTER_DIED} events.
 *
 * <p>The on-screen {@link com.ashenthrone.ui.HealthBar} already reads its
 * character's HP live every frame, so no explicit refresh call is needed —
 * this listener is kept as a hook for future damage-popup / screen-shake
 * effects without forcing them on now.
 */
public class HealthBarListener implements EventListener {

    @Override
    public void onEvent(GameEvent event) {
        EventType t = event.getType();
        if (t != EventType.DAMAGE_DEALT && t != EventType.CHARACTER_DIED) return;
        // HealthBar polls the live character each frame; nothing to push.
    }
}
