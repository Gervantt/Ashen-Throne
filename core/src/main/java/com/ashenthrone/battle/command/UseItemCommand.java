package com.ashenthrone.battle.command;

import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.core.GameSession;
import com.ashenthrone.observer.EventManager;
import com.ashenthrone.observer.GameEvent;
import com.ashenthrone.screens.ShopScreen;

public class UseItemCommand implements BattleCommand {

    private final AbstractCharacter user;
    private int previousHp;
    private boolean consumed;

    public UseItemCommand(AbstractCharacter user) {
        this.user = user;
    }

    @Override
    public void execute() {
        previousHp = user.getHp();
        int healedAmount = Math.max(1, Math.round(user.getMaxHp() * 0.35f));
        consumed = GameSession.getInstance().consumeConsumable(ShopScreen.ITEM_HEALTH_POTION);
        if (!consumed) return;

        user.heal(healedAmount);
        EventManager.getInstance().publish(GameEvent.itemUsed(ShopScreen.ITEM_HEALTH_POTION, user));
    }

    @Override
    public void undo() {
        if (!consumed) return;
        user.setHp(previousHp);
        GameSession.getInstance().addConsumable(ShopScreen.ITEM_HEALTH_POTION, 1);
    }

    public boolean wasConsumed() { return consumed; }
}
