package com.ashenthrone.observer.listeners;

import com.ashenthrone.observer.EventListener;
import com.ashenthrone.observer.EventType;
import com.ashenthrone.observer.GameEvent;

public class HealthBarListener implements EventListener {

    @Override
    public void onEvent(GameEvent event) {
        EventType t = event.getType();
        if (t != EventType.DAMAGE_DEALT && t != EventType.CHARACTER_DIED) return;

    }
}
