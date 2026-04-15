package com.ashenthrone.observer;

/**
 * Observer contract for the event system.
 *
 * Implementations are registered via
 * {@link EventManager#subscribe(EventType, EventListener)}.
 * Being a functional interface allows lambda registration:
 * <pre>
 *   EventManager.getInstance().subscribe(EventType.DAMAGE_DEALT,
 *       e -> log.append(e.getSource().getName() + " attacked!"));
 * </pre>
 */
@FunctionalInterface
public interface EventListener {
    void onEvent(GameEvent event);
}