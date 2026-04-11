package com.ashenthrone.battle.command;

import com.ashenthrone.characters.AbstractCharacter;

/**
 * Sets the character into a defending stance for this turn.
 *
 * While defending, AbstractCharacter.takeDamage() halves incoming damage.
 * The previous defending state is recorded so undo() can restore it exactly
 * (e.g. if the character was already defending when this command ran).
 *
 * Note: AbstractCharacter.beginTurn() resets the defending flag at the start
 * of each turn, so undo is only meaningful within the same turn.
 */
public class DefendCommand implements BattleCommand {

    private final AbstractCharacter character;
    private boolean previousDefending;

    public DefendCommand(AbstractCharacter character) {
        this.character = character;
    }

    @Override
    public void execute() {
        previousDefending = character.isDefending();
        character.setDefending(true);
    }

    @Override
    public void undo() {
        character.setDefending(previousDefending);
    }
}