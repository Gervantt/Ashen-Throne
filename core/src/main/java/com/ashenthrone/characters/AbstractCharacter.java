package com.ashenthrone.characters;

/**
 * Base class for all characters (hero and enemies).
 * 
 * Uses the Template Method pattern: takeTurn() defines the fixed sequence,
 * subclasses override chooseAction() to provide player input or AI logic.
 */
public abstract class AbstractCharacter {

    protected String name;
    protected int hp;
    protected int maxHp;
    protected int attack;
    protected int defense;
    protected int speed;
    protected boolean defending;

    // Will be AttackStrategy once AT-008 is done.
    protected Object currentStrategy; // TODO: change to AttackStrategy

    protected AbstractCharacter() {}

    /**
     * Template Method — the turn skeleton is fixed; subclasses fill in chooseAction().
     * Final so nobody can break the sequence.
     */
    public final void takeTurn() {
        beginTurn();
        applyStatusEffects();
        chooseAction();
        executeAction();
        endTurn();
    }

    /** Reset per-turn flags (e.g. defending resets at the start of your next turn). */
    protected void beginTurn() {
        defending = false;
    }

    /** Apply poison, buffs, debuffs, etc. Placeholder for now. */
    protected void applyStatusEffects() {
        // TODO: integrate with StatusEffectProcessor (AT-010)
    }

    /** Subclass responsibility: decide what to do this turn. */
    protected abstract void chooseAction();

    /** Carry out whatever action was chosen. Placeholder until Command system (AT-007). */
    protected void executeAction() {
        // TODO: execute the chosen BattleCommand
    }

    protected void endTurn() {
        // TODO: notify observers (AT-009)
    }

    // ---- Combat helpers ----

    public void takeDamage(int amount) {
        int effective = defending ? amount / 2 : amount;
        hp = Math.max(0, hp - effective);
    }

    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    public boolean isAlive() {
        return hp > 0;
    }

    // ---- Getters (virtual so Decorator can override) ----

    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed() { return speed; }
    public boolean isDefending() { return defending; }

    // ---- Setters ----

    public void setHp(int hp) { this.hp = hp; }
    public void setDefending(boolean defending) { this.defending = defending; }

    @Override
    public String toString() {
        return name + " (HP: " + hp + "/" + maxHp + ")";
    }
}
