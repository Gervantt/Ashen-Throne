package com.ashenthrone.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // AT-020: equipped item identifiers, in order they should be applied as
    // Decorators around the hero. Item ids are the keys defined in ShopScreen.
    private final List<String> equippedItems = new ArrayList<>();

    // AT-019: realm progression. completedRealms holds keys ("abyss", "forest",
    // "throne") for realms the player has fully cleared. currentRealm is the
    // realm key the player is presently inside; currentWaveInRealm is its
    // 0-based wave index. Both are null/0 outside an active realm run.
    private final Set<String> completedRealms = new HashSet<>();
    private String currentRealm;
    private int currentWaveInRealm;

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
        completedRealms.clear();
        currentRealm = null;
        currentWaveInRealm = 0;
        equippedItems.clear();
    }

    // ---- Getters & Setters ----

    public Object getHero() { return hero; }
    public void setHero(Object hero) { this.hero = hero; }

    public int getGold() { return gold; }
    public void addGold(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Gold amount must be non-negative, got: " + amount);
        // Cap at Integer.MAX_VALUE to prevent silent overflow.
        this.gold = (int) Math.min((long) gold + amount, Integer.MAX_VALUE);
    }

    /** AT-020: subtract gold for a purchase. Returns false if insufficient. */
    public boolean spendGold(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Gold amount must be non-negative, got: " + amount);
        if (gold < amount) return false;
        gold -= amount;
        return true;
    }

    public List<String> getEquippedItems() { return Collections.unmodifiableList(equippedItems); }
    public boolean hasItem(String itemId) { return equippedItems.contains(itemId); }
    public void equipItem(String itemId) {
        if (itemId == null) throw new IllegalArgumentException("itemId must not be null");
        equippedItems.add(itemId);
    }

    public int getCurrentEncounterIndex() { return currentEncounterIndex; }
    public void advanceEncounter() { this.currentEncounterIndex++; }

    public List<Object> getInventory() { return Collections.unmodifiableList(inventory); }

    // ---- AT-019: realm progression ----

    public Set<String> getCompletedRealms() { return Collections.unmodifiableSet(completedRealms); }
    public boolean hasCompletedRealm(String realmKey) { return completedRealms.contains(realmKey); }
    public void markRealmCompleted(String realmKey) {
        if (realmKey == null) throw new IllegalArgumentException("realmKey must not be null");
        completedRealms.add(realmKey);
    }

    public String getCurrentRealm() { return currentRealm; }
    public void setCurrentRealm(String realmKey) { this.currentRealm = realmKey; }

    public int getCurrentWaveInRealm() { return currentWaveInRealm; }
    public void setCurrentWaveInRealm(int wave) { this.currentWaveInRealm = wave; }
    public void advanceWave() { this.currentWaveInRealm++; }
}
