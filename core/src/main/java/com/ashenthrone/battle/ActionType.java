package com.ashenthrone.battle;

/**
 * The four actions a player can select during their turn.
 * Referenced by PlayerTurnState and, once implemented, by BattleCommand (AT-007).
 */
public enum ActionType {
    ATTACK,
    DEFEND,
    SKILL,
    ITEM
}