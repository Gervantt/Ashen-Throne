package com.ashenthrone.characters;

/**
 * The player's character. Overrides chooseAction() to wait for player input
 * rather than acting autonomously.
 */
public class Hero extends AbstractCharacter {

    Hero() {
        // Package-private — use HeroBuilder to construct.
    }

    /**
     * For the hero, "choosing an action" means waiting for the player to pick
     * from the UI. The actual selection happens in PlayerTurnState (AT-006),
     * which will set the chosen BattleCommand on this hero. So this method
     * is intentionally a no-op: the state machine drives input, not the
     * character itself.
     */
    @Override
    protected void chooseAction() {
        // Player input is handled externally by PlayerTurnState.
    }
}
