package com.ashenthrone.observer;

@FunctionalInterface
public interface EventListener {
    void onEvent(GameEvent event);
}