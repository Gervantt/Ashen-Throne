package com.ashenthrone.observer.listeners;

import com.ashenthrone.audio.AudioManager;
import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.characters.Hero;
import com.ashenthrone.observer.EventListener;
import com.ashenthrone.observer.GameEvent;

/**
 * Triggers sound effects in response to combat events (AT-021).
 *
 * <p>Routing:
 * <ul>
 *   <li>{@code DAMAGE_DEALT}   → {@code hero_hurt} when the target is the
 *       Hero. Attack-flavour SFX (sword_hit / fireball) are fired by the
 *       attacking strategy itself, since this event lacks strategy info.</li>
 *   <li>{@code CHARACTER_DIED} → {@code enemy_death} for non-Hero deaths;
 *       Hero death is handled by the {@code BATTLE_END} sting.</li>
 *   <li>{@code BATTLE_END}     → {@code victory_sting} or {@code defeat_sting}
 *       depending on result.</li>
 * </ul>
 */
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
