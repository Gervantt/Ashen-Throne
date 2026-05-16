package com.ashenthrone.observer;

import com.ashenthrone.characters.AbstractCharacter;

public final class GameEvent {

    private final EventType type;

    private final AbstractCharacter source;
    private final AbstractCharacter target;
    private final int amount;

    private final AbstractCharacter character;

    private final Object item;

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


    public static GameEvent damageDealt(AbstractCharacter source,
                                        AbstractCharacter target,
                                        int amount) {
        return new GameEvent(EventType.DAMAGE_DEALT, source, target, amount,
                null, null, null);
    }

    public static GameEvent characterDied(AbstractCharacter character) {
        return new GameEvent(EventType.CHARACTER_DIED, null, null, 0,
                character, null, null);
    }


    public static GameEvent itemUsed(Object item, AbstractCharacter target) {
        return new GameEvent(EventType.ITEM_USED, null, target, 0,
                null, item, null);
    }

    public static GameEvent battleEnd(String result) {
        return new GameEvent(EventType.BATTLE_END, null, null, 0,
                null, null, result);
    }

    public EventType getType()              { return type; }

    public AbstractCharacter getSource()    { return source; }

    public AbstractCharacter getTarget()    { return target; }

    public int getAmount()                  { return amount; }

    public AbstractCharacter getCharacter() { return character; }

    public Object getItem()                 { return item; }

    public String getResult()               { return result; }
}