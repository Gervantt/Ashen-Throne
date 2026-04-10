package com.ashenthrone.characters;

/**
 * Abstract base for the Decorator pattern (AT-005).
 *
 * Wraps any AbstractCharacter and delegates all behaviour to it.
 * Subclasses (equipment items) override getAttack() and/or getDefense()
 * to layer stat bonuses without modifying the underlying character.
 *
 * Example usage:
 *   AbstractCharacter equipped = new FireAmulet(new CursedRing(baseHero));
 *   equipped.getAttack();  // baseHero.attack - 2 + 5
 *
 * Placed in the characters package so it can delegate the protected
 * chooseAction() method to the wrapped instance (same-package access).
 */
public abstract class CharacterDecorator extends AbstractCharacter {

    protected final AbstractCharacter wrapped;

    protected CharacterDecorator(AbstractCharacter wrapped) {
        if (wrapped == null) throw new IllegalArgumentException("Wrapped character must not be null");
        this.wrapped = wrapped;
    }

    // ---- Delegation — stat reads ----

    @Override
    public String getName() { return wrapped.getName(); }

    @Override
    public int getHp() { return wrapped.getHp(); }

    @Override
    public int getMaxHp() { return wrapped.getMaxHp(); }

    /** Base delegation — equipment subclasses override to add their bonus. */
    @Override
    public int getAttack() { return wrapped.getAttack(); }

    /** Base delegation — equipment subclasses override to add their bonus. */
    @Override
    public int getDefense() { return wrapped.getDefense(); }

    @Override
    public int getSpeed() { return wrapped.getSpeed(); }

    @Override
    public boolean isDefending() { return wrapped.isDefending(); }

    // ---- Delegation — combat helpers ----

    @Override
    public void takeDamage(int amount) { wrapped.takeDamage(amount); }

    @Override
    public void heal(int amount) { wrapped.heal(amount); }

    @Override
    public boolean isAlive() { return wrapped.isAlive(); }

    // ---- Delegation — setters ----

    @Override
    public void setHp(int hp) { wrapped.setHp(hp); }

    @Override
    public void setMaxHp(int maxHp) { wrapped.setMaxHp(maxHp); }

    @Override
    public void setDefending(boolean defending) { wrapped.setDefending(defending); }

    // ---- Delegation — turn behaviour ----

    /**
     * Delegates to the wrapped character's chooseAction().
     * Accessible here because CharacterDecorator is in the same package as AbstractCharacter.
     */
    @Override
    protected void chooseAction() {
        wrapped.chooseAction();
    }

    @Override
    public String toString() { return wrapped.toString(); }
}