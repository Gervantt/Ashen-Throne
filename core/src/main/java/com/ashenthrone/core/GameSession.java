package com.ashenthrone.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton that holds the state of the current run.
 * Accessed globally via GameSession.getInstance().
 */
public class GameSession {

    private static GameSession instance;

    // Will hold a Hero reference once the character model exists (AT-002).
    private Object hero; // TODO: change to Hero type
    private int gold;
    private int currentEncounterIndex;
    private final List<Object> inventory; // TODO: change to Item type

    private GameSession() {
        inventory = new ArrayList<>();
    }

    public static GameSession getInstance() {
        if (instance == null) {
            instance = new GameSession();
        }
        return instance;
    }

    /** Resets session to a fresh run state. */
    public void reset() {
        hero = null;
        gold = 0;
        currentEncounterIndex = 0;
        inventory.clear();
    }

    // ---- Getters & Setters ----

    public Object getHero() { return hero; }
    public void setHero(Object hero) { this.hero = hero; }

    public int getGold() { return gold; }
    public void addGold(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Gold amount must be non-negative, got: " + amount);
        this.gold += amount;
    }

    public int getCurrentEncounterIndex() { return currentEncounterIndex; }
    public void advanceEncounter() { this.currentEncounterIndex++; }

    public List<Object> getInventory() { return Collections.unmodifiableList(inventory); }
}
