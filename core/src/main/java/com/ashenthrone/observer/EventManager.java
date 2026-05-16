package com.ashenthrone.observer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class EventManager {

    private static EventManager instance;

    private final Map<EventType, List<EventListener>> listeners;

    private EventManager() {
        listeners = new EnumMap<>(EventType.class);
        for (EventType type : EventType.values()) {
            listeners.put(type, new ArrayList<>());
        }
    }

    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }


    public void subscribe(EventType type, EventListener listener) {
        if (type == null)     throw new IllegalArgumentException("EventType must not be null");
        if (listener == null) throw new IllegalArgumentException("EventListener must not be null");
        listeners.get(type).add(listener);
    }

    public void unsubscribe(EventType type, EventListener listener) {
        if (type == null || listener == null) return;
        listeners.get(type).remove(listener);
    }


    public void publish(GameEvent event) {
        if (event == null) return;
        List<EventListener> registered = listeners.get(event.getType());

        for (EventListener listener : new ArrayList<>(registered)) {
            listener.onEvent(event);
        }
    }


    public void clearAll() {
        for (List<EventListener> list : listeners.values()) {
            list.clear();
        }
    }
}