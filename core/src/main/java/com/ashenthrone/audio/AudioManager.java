package com.ashenthrone.audio;

/**
 * Singleton audio facade (AT-014 skeleton).
 *
 * Empty method bodies for now — concrete asset registration, music/SFX
 * playback, and {@link com.ashenthrone.observer.EventManager} subscription
 * are wired in AT-021.
 *
 * Why a singleton: there is exactly one audio device per process, and every
 * screen / listener needs to reach it without threading the reference through
 * constructors.
 */
public class AudioManager {

    private static AudioManager instance;

    private float musicVolume = 1f;
    private float sfxVolume   = 1f;

    private AudioManager() {}

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    /** Start (and loop) the music track registered under {@code key}. TODO: AT-021. */
    public void playMusic(String key) {
        // TODO: AT-021 — resolve key via AssetRegistry, stop current track, play looped.
    }

    /** Stop whatever music is currently playing. TODO: AT-021. */
    public void stopMusic() {
        // TODO: AT-021 — stop current Music handle if any.
    }

    /** Fire a one-shot sound effect registered under {@code key}. TODO: AT-021. */
    public void playSFX(String key) {
        // TODO: AT-021 — resolve key via AssetRegistry and Sound.play(sfxVolume).
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = clamp(volume);
        // TODO: AT-021 — apply to currently playing Music handle.
    }

    public void setSFXVolume(float volume) {
        this.sfxVolume = clamp(volume);
    }

    public float getMusicVolume() { return musicVolume; }
    public float getSFXVolume()   { return sfxVolume;   }

    private static float clamp(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }
}