package com.ashenthrone.battle.command;

/**
 * Command pattern interface for all battle actions (AT-007).
 *
 * Every player action (attack, defend, skill, item) is wrapped in a
 * BattleCommand before execution. Commands are pushed onto the history
 * stack held by BattleScreen (TODO: AT-010 — move stack to BattleEngine),
 * enabling undo during the player's turn.
 *
 * Rule: execute() is called exactly once before undo() may be called.
 * Implementations must record any state needed for reversal inside execute().
 */
public interface BattleCommand {

    /** Perform the action and record any state needed to reverse it. */
    void execute();

    /** Reverse the effect of execute(). Called at most once per execute(). */
    void undo();
}