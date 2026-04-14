package com.ashenthrone.observer;

/**
 * All game events that can be published through {@link EventManager}.
 *
 * AT-009 defines four events; extend this enum as new systems are added.
 */
public enum EventType {

    /** A character dealt damage to another. Carries source, target, and amount. */
    DAMAGE_DEALT,

    /** A character's HP reached zero. Carries the character that died. */
    CHARACTER_DIED,

    /** A player consumed an item. Carries the item and its target. */
    ITEM_USED,

    /** The battle has concluded. Carries the result string ("VICTORY" or "DEFEAT"). */
    BATTLE_END
}