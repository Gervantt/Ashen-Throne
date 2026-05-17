package com.ashenthrone.observer.listeners;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.characters.Hero;
import com.ashenthrone.observer.EventListener;
import com.ashenthrone.observer.GameEvent;

public class AudioListener implements EventListener {

    @Override
    public void onEvent(GameEvent event) {
        AudioManager audio = AudioManager.getInstance();
        switch (event.getType()) {
            case DAMAGE_DEALT -> {
                AbstractCharacter target = event.getTarget();
                if (target instanceof Hero) {
                    audio.playSFX("hero_hurt");
                }
            }
            case CHARACTER_DIED -> {
                AbstractCharacter c = event.getCharacter();
                if (!(c instanceof Hero)) {
                    audio.playSFX("enemy_death");
                }
            }
            case BATTLE_END -> {
                if ("VICTORY".equals(event.getResult())) {
                    audio.playMusic("victory_sting");
                } else if ("DEFEAT".equals(event.getResult())) {
                    audio.playMusic("defeat_sting");
                }
            }
            default -> {}
        }
    }
}
