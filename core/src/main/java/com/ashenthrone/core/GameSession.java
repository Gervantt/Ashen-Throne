package com.ashenthrone.core;

import com.ashenthrone.battle.WaveIterator;
import com.ashenthrone.characters.AbstractCharacter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameSession {

    private static GameSession instance;


    private AbstractCharacter hero;
    private int gold;
    private int currentEncounterIndex;


    private final List<Object> inventory;

    private final List<String> equippedItems = new ArrayList<>();


    private final Map<String, Integer> consumables = new HashMap<>();


    private int lastGoldGained;

    private final Set<String> completedRealms = new HashSet<>();
    private String currentRealm;
    private int currentWaveInRealm;

    private WaveIterator waveIterator;

    private GameSession() {
        inventory = new ArrayList<>();
    }

    public static GameSession getInstance() {
        if (instance == null) {
            instance = new GameSession();
        }
        return instance;
    }

    public void reset() {
        hero = null;
        gold = 0;
        currentEncounterIndex = 0;
        inventory.clear();
        completedRealms.clear();
        currentRealm = null;
        currentWaveInRealm = 0;
        equippedItems.clear();
        consumables.clear();
        lastGoldGained = 0;
        waveIterator = null;
    }

    public void abandonActiveRun() {
        if (hero != null && !hero.isAlive()) {
            hero.setHp(hero.getMaxHp());
        }
        currentRealm = null;
        currentWaveInRealm = 0;
        waveIterator = null;
    }

    public AbstractCharacter getHero() { return hero; }
    public void setHero(AbstractCharacter hero) { this.hero = hero; }

    public int getGold() { return gold; }
    public void addGold(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Gold amount must be non-negative, got: " + amount);

        this.gold = (int) Math.min((long) gold + amount, Integer.MAX_VALUE);
    }

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

    public Map<String, Integer> getConsumables() { return Collections.unmodifiableMap(consumables); }
    public int getConsumableCount(String itemId) {
        if (itemId == null) return 0;
        return consumables.getOrDefault(itemId, 0);
    }
    public void addConsumable(String itemId, int count) {
        if (itemId == null) throw new IllegalArgumentException("itemId must not be null");
        if (count < 0) throw new IllegalArgumentException("count must be non-negative, got: " + count);
        if (count == 0) return;
        consumables.merge(itemId, count, Integer::sum);
    }
    public boolean consumeConsumable(String itemId) {
        if (itemId == null) throw new IllegalArgumentException("itemId must not be null");
        int count = consumables.getOrDefault(itemId, 0);
        if (count <= 0) return false;
        if (count == 1) consumables.remove(itemId);
        else consumables.put(itemId, count - 1);
        return true;
    }

    public int getLastGoldGained() { return lastGoldGained; }
    public void setLastGoldGained(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Gold amount must be non-negative, got: " + amount);
        lastGoldGained = amount;
    }

    public int getCurrentEncounterIndex() { return currentEncounterIndex; }
    public void advanceEncounter() { this.currentEncounterIndex++; }

    public List<Object> getInventory() { return Collections.unmodifiableList(inventory); }

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

    public WaveIterator getWaveIterator() { return waveIterator; }
    public void setWaveIterator(WaveIterator iterator) { this.waveIterator = iterator; }
}
