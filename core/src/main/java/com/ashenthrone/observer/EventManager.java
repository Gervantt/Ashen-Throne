package com.ashenthrone.observer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton event bus for the Observer pattern (AT-009).
 *
 * Usage:
 * <pre>
 *   // Subscribe
 *   EventManager.getInstance().subscribe(EventType.DAMAGE_DEALT, myListener);
 *
 *   // Publish
 *   EventManager.getInstance().publish(GameEvent.damageDealt(attacker, target, 15));
 *
 *   // Clear all subscriptions (call at the start of each new battle)
 *   EventManager.getInstance().clearAll();
 * </pre>
 *
 * All operations execute synchronously on the game thread.
 * No thread-safety needed — libGDX uses a single-threaded game loop.
 */
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

    // ---- Public API ----

    /**
     * Registers a listener for the given event type.
     * The same listener may be registered multiple times and will be called
     * that many times per publish — avoid duplicate registration.
     */
    public void subscribe(EventType type, EventListener listener) {
        if (type == null)     throw new IllegalArgumentException("EventType must not be null");
        if (listener == null) throw new IllegalArgumentException("EventListener must not be null");
        listeners.get(type).add(listener);
    }

    /**
     * Removes a previously registered listener.
     * No-op if the listener was not registered.
     */
    public void unsubscribe(EventType type, EventListener listener) {
        if (type == null || listener == null) return;
        listeners.get(type).remove(listener);
    }

    /**
     * Fires an event, calling every subscriber registered for its type
     * in registration order.
     */
    public void publish(GameEvent event) {
        if (event == null) return;
        List<EventListener> registered = listeners.get(event.getType());
        // Iterate a snapshot to allow listeners to subscribe/unsubscribe during dispatch.
        for (EventListener listener : new ArrayList<>(registered)) {
            listener.onEvent(event);
        }
    }

    /**
     * Removes all subscriptions for all event types.
     * Call this at the start of each new battle so stale listeners from a
     * previous BattleScreen instance do not accumulate.
     */
    public void clearAll() {
        for (List<EventListener> list : listeners.values()) {
            list.clear();
        }
    }
}