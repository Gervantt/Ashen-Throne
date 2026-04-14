package com.ashenthrone.observer.listeners;

import com.ashenthrone.observer.EventListener;
import com.ashenthrone.observer.GameEvent;

/**
 * Refreshes the health-bar UI component when damage is dealt.
 *
 * Stub — full implementation deferred to AT-011 (Composite UI) and
 * AT-015 (Battle Screen Layout). Once HealthBar UIComponents exist,
 * this listener should look up the bar for the damaged character and
 * call its update method.
 *
 * Subscribes to: DAMAGE_DEALT.
 */
public class HealthBarListener implements EventListener {

    @Override
    public void onEvent(GameEvent event) {
        if (event.getType() != com.ashenthrone.observer.EventType.DAMAGE_DEALT) return;
        // TODO: AT-011 — find the HealthBar for event.getTarget() and refresh it.
        // TODO: AT-015 — trigger damage number popup and screen shake.
    }
}