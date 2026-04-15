package com.ashenthrone.observer.listeners;

import com.ashenthrone.observer.EventListener;
import com.ashenthrone.observer.GameEvent;

/**
 * Triggers sound effects in response to combat events.
 *
 * Stub — full implementation deferred to AT-014 (Asset Loading & Audio Manager).
 * Once AudioManager exists, this listener should call the appropriate SFX:
 *   DAMAGE_DEALT     → attack-hit sound
 *   CHARACTER_DIED   → enemy-death or hero-hurt sound depending on type
 *   BATTLE_END       → victory fanfare or defeat sting
 *
 * Subscribes to: DAMAGE_DEALT, CHARACTER_DIED, BATTLE_END.
 */
public class AudioListener implements EventListener {

    @Override
    public void onEvent(GameEvent event) {
        switch (event.getType()) {
            case DAMAGE_DEALT   ->
                // TODO: AT-014 — AudioManager.getInstance().playSfx("attack_hit")
                    {}
            case CHARACTER_DIED ->
                // TODO: AT-014 — distinguish hero hurt vs enemy death SFX
                    {}
            case BATTLE_END     ->
                // TODO: AT-014 — play victory fanfare or defeat sting
                    {}
            default             -> {}
        }
    }
}