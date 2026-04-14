package com.ashenthrone.observer;

import com.ashenthrone.characters.AbstractCharacter;

/**
 * Immutable event payload passed to {@link EventListener} subscribers.
 *
 * Use the static factory methods to create events — each method documents
 * which getter fields are populated for that event type.
 *
 * Fields not relevant to a given event type return {@code null} / 0.
 */
public final class GameEvent {

    private final EventType type;

    // DAMAGE_DEALT
    private final AbstractCharacter source;
    private final AbstractCharacter target;
    private final int amount;

    // CHARACTER_DIED
    private final AbstractCharacter character;

    // ITEM_USED
    private final Object item;

    // BATTLE_END
    private final String result;

    private GameEvent(EventType type,
                      AbstractCharacter source,
                      AbstractCharacter target,
                      int amount,
                      AbstractCharacter character,
                      Object item,
                      String result) {
        this.type      = type;
        this.source    = source;
        this.target    = target;
        this.amount    = amount;
        this.character = character;
        this.item      = item;
        this.result    = result;
    }

    // ---- Static factories ----

    /**
     * @param source  the attacker
     * @param target  the character that received damage
     * @param amount  raw damage value passed to {@code takeDamage()}
     */
    public static GameEvent damageDealt(AbstractCharacter source,
                                        AbstractCharacter target,
                                        int amount) {
        return new GameEvent(EventType.DAMAGE_DEALT, source, target, amount,
                null, null, null);
    }

    /** @param character the character whose HP just reached zero */
    public static GameEvent characterDied(AbstractCharacter character) {
        return new GameEvent(EventType.CHARACTER_DIED, null, null, 0,
                character, null, null);
    }

    /**
     * @param item   the item consumed (typed to Object until AT-014 introduces Item)
     * @param target the character the item was applied to
     */
    public static GameEvent itemUsed(Object item, AbstractCharacter target) {
        return new GameEvent(EventType.ITEM_USED, null, target, 0,
                null, item, null);
    }

    /** @param result "VICTORY" or "DEFEAT" */
    public static GameEvent battleEnd(String result) {
        return new GameEvent(EventType.BATTLE_END, null, null, 0,
                null, null, result);
    }

    // ---- Getters ----

    public EventType getType()              { return type; }

    /** Populated for DAMAGE_DEALT. */
    public AbstractCharacter getSource()    { return source; }

    /** Populated for DAMAGE_DEALT and ITEM_USED. */
    public AbstractCharacter getTarget()    { return target; }

    /** Populated for DAMAGE_DEALT. */
    public int getAmount()                  { return amount; }

    /** Populated for CHARACTER_DIED. */
    public AbstractCharacter getCharacter() { return character; }

    /** Populated for ITEM_USED. */
    public Object getItem()                 { return item; }

    /** Populated for BATTLE_END: "VICTORY" or "DEFEAT". */
    public String getResult()               { return result; }
}