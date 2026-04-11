package com.ashenthrone.battle.command;

import com.ashenthrone.characters.AbstractCharacter;

/**
 * Consumes an item from the player's inventory and applies its effect.
 *
 * Placeholder until the item/inventory system is implemented. Once items
 * exist, execute() should remove the item from GameSession.inventory and
 * apply its effect; undo() should return the item and reverse the effect.
 */
public class UseItemCommand implements BattleCommand {

    private final AbstractCharacter user;

    public UseItemCommand(AbstractCharacter user) {
        this.user = user;
    }

    @Override
    public void execute() {
        // TODO: consume item from GameSession.getInstance().getInventory()
        //       and apply its effect to user (or a target)
    }

    @Override
    public void undo() {
        // TODO: return item to GameSession.getInstance().getInventory()
        //       and reverse the item's effect on user (or target)
    }
}