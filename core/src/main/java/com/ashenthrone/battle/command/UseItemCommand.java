package com.ashenthrone.battle.command;

import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.observer.EventManager;
import com.ashenthrone.observer.GameEvent;

/**
 * Placeholder "use item" battle action (AT-007).
 *
 * <p>Consumable items are not modelled yet — purchased equipment lives in
 * {@link com.ashenthrone.core.GameSession#getEquippedItems()} as permanent
 * Decorators rather than per-turn consumables. {@link #execute} therefore
 * only publishes {@code ITEM_USED} so the battle log stays consistent;
 * {@link #undo} is a true no-op.
 */
public class UseItemCommand implements BattleCommand {

    private final AbstractCharacter user;

    public UseItemCommand(AbstractCharacter user) {
        this.user = user;
    }

    @Override
    public void execute() {
        EventManager.getInstance().publish(GameEvent.itemUsed(null, user));
    }

    @Override
    public void undo() {
        // No state was mutated — nothing to reverse.
    }
}
