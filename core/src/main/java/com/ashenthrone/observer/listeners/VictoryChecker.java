package com.ashenthrone.observer.listeners;

import com.ashenthrone.characters.Enemy;
import com.ashenthrone.observer.EventListener;
import com.ashenthrone.observer.GameEvent;

public class VictoryChecker implements EventListener {

    private boolean heroDefeated;
    private int enemiesDefeated;
    private String battleResult;

    @Override
    public void onEvent(GameEvent event) {
        switch (event.getType()) {
            case CHARACTER_DIED -> {

                if (event.getCharacter() instanceof Enemy) {
                    enemiesDefeated++;
                } else {
                    heroDefeated = true;
                }
            }
            case BATTLE_END -> battleResult = event.getResult();
            default -> {  }
        }
    }

    public boolean isHeroDefeated()     { return heroDefeated; }

    public int getEnemiesDefeated()     { return enemiesDefeated; }


    public String getBattleResult()     { return battleResult; }

    public void reset() {
        heroDefeated    = false;
        enemiesDefeated = 0;
        battleResult    = null;
    }
}