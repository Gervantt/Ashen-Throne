package com.ashenthrone.battle.command;

public interface BattleCommand {

    void execute();

    void undo();
}