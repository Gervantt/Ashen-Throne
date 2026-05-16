package com.ashenthrone.characters;

import com.ashenthrone.observer.EventManager;
import com.ashenthrone.observer.GameEvent;
import com.ashenthrone.strategy.AttackStrategy;

public abstract class AbstractCharacter {

    protected String name;
    protected int hp;
    protected int maxHp;
    protected int attack;
    protected int defense;
    protected int speed;
    protected boolean defending;

    protected AttackStrategy currentStrategy;

    protected AbstractCharacter() {}


    public final void takeTurn() {
        beginTurn();
        applyStatusEffects();
        chooseAction();
        executeAction();
        endTurn();
    }

    protected void beginTurn() {
        defending = false;
    }


    protected void applyStatusEffects() {

    }

    protected abstract void chooseAction();


    protected void executeAction() {

    }


    protected void endTurn() {

    }


    public void takeDamage(int amount) {
        takeDamage(null, amount);
    }


    public void takeDamage(AbstractCharacter source, int amount) {
        if (amount < 0) throw new IllegalArgumentException("Damage amount must be non-negative, got: " + amount);
        int effective = defending ? amount / 2 : amount;
        hp = Math.max(0, hp - effective);
        EventManager.getInstance().publish(GameEvent.damageDealt(source, this, effective));
        if (hp == 0) {
            EventManager.getInstance().publish(GameEvent.characterDied(this));
        }
    }

    public void heal(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Heal amount must be non-negative, got: " + amount);
        hp = Math.min(maxHp, hp + amount);
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed() { return speed; }
    public boolean isDefending() { return defending; }
    public AttackStrategy getCurrentStrategy() { return currentStrategy; }

    public void setCurrentStrategy(AttackStrategy strategy) {
        this.currentStrategy = strategy;
    }

    public void setHp(int hp) {
        if (hp < 0) throw new IllegalArgumentException("HP must be non-negative, got: " + hp);
        this.hp = hp;
    }

    public void setMaxHp(int maxHp) {
        if (maxHp <= 0) throw new IllegalArgumentException("Max HP must be positive, got: " + maxHp);
        this.maxHp = maxHp;
    }
    public void setDefending(boolean defending) { this.defending = defending; }

    @Override
    public String toString() {
        return name + " (HP: " + hp + "/" + maxHp + ")";
    }
}
