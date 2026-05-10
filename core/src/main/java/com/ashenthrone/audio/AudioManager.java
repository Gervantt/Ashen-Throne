package com.ashenthrone.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton audio facade (AT-021).
 *
 * Maps logical keys (e.g. {@code "main_theme"}, {@code "sword_hit"}) to file
 * paths and plays them through libGDX. Missing or unloadable assets degrade
 * gracefully: a one-time warning is logged and subsequent calls become no-ops,
 * so the game still runs in development before audio assets are committed.
 *
 * <p>Music keys (looped, exclusive — only one plays at a time):
 * {@code main_theme}, {@code battle_theme}, {@code boss_theme},
 * {@code victory_sting}, {@code defeat_sting}.
 *
 * <p>SFX keys (one-shot): {@code sword_hit}, {@code fireball},
 * {@code heal_cast}, {@code enemy_death}, {@code hero_hurt},
 * {@code transition_whoosh}, {@code purchase_sound}.
 *
 * <p>Stings ({@code victory_sting}, {@code defeat_sting}) are registered as
 * non-looping music for one-shot fanfares, per AT-021 spec.
 */
public class AudioManager {

    private static AudioManager instance;

    private final Map<String, String> musicPaths = new HashMap<>();
    private final Map<String, String> sfxPaths   = new HashMap<>();

    private final Map<String, Music> musicCache = new HashMap<>();
    private final Map<String, Sound> sfxCache   = new HashMap<>();

    /** Keys that should not loop (one-shot stings). */
    private final java.util.Set<String> nonLoopingMusic = new java.util.HashSet<>();

    /** Keys that previously failed to load — never retried. */
    private final java.util.Set<String> failed = new java.util.HashSet<>();

    private Music  currentMusic;
    private String currentMusicKey;

    private float musicVolume = 1f;
    private float sfxVolume   = 1f;

    private AudioManager() {
        registerDefaults();
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    private void registerDefaults() {
        // Music (looped).
        musicPaths.put("main_theme",    "audio/music/main_theme.ogg");
        musicPaths.put("battle_theme",  "audio/music/battle_theme.ogg");
        musicPaths.put("boss_theme",    "audio/music/boss_theme.ogg");

        // Stings — registered as music but played non-looped.
        musicPaths.put("victory_sting", "audio/music/victory_sting.ogg");
        musicPaths.put("defeat_sting",  "audio/music/defeat_sting.ogg");
        nonLoopingMusic.add("victory_sting");
        nonLoopingMusic.add("defeat_sting");

        // SFX (one-shot).
        sfxPaths.put("sword_hit",         "audio/sfx/sword_hit.wav");
        sfxPaths.put("fireball",          "audio/sfx/fireball.wav");
        sfxPaths.put("heal_cast",         "audio/sfx/heal_cast.wav");
        sfxPaths.put("enemy_death",       "audio/sfx/enemy_death.wav");
        sfxPaths.put("hero_hurt",         "audio/sfx/hero_hurt.wav");
        sfxPaths.put("transition_whoosh", "audio/sfx/transition_whoosh.wav");
        sfxPaths.put("purchase_sound",    "audio/sfx/purchase_sound.wav");
    }

    /**
     * Start the track registered under {@code key}. If a different track is
     * already playing it is stopped first; calling with the currently playing
     * key is a no-op so screens can call this on every {@code show()} safely.
     */
    public void playMusic(String key) {
        if (key == null) return;
        if (key.equals(currentMusicKey) && currentMusic != null && currentMusic.isPlaying()) {
            return;
        }
        Music next = loadMusic(key);
        stopMusic();
        if (next == null) return;
        next.setLooping(!nonLoopingMusic.contains(key));
        next.setVolume(musicVolume);
        next.play();
        currentMusic    = next;
        currentMusicKey = key;
    }

    /** Stop whatever music is currently playing. */
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusic    = null;
        currentMusicKey = null;
    }

    /** Fire a one-shot sound effect registered under {@code key}. */
    public void playSFX(String key) {
        if (key == null) return;
        Sound s = loadSound(key);
        if (s == null) return;
        s.play(sfxVolume);
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = clamp(volume);
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }

    public void setSFXVolume(float volume) {
        this.sfxVolume = clamp(volume);
    }

    public float getMusicVolume() { return musicVolume; }
    public float getSFXVolume()   { return sfxVolume;   }

    /** Free all cached handles. Call from the game's {@code dispose()}. */
    public void dispose() {
        stopMusic();
        for (Music m : musicCache.values()) m.dispose();
        for (Sound s : sfxCache.values())   s.dispose();
        musicCache.clear();
        sfxCache.clear();
    }

    private Music loadMusic(String key) {
        if (failed.contains(key)) return null;
        Music cached = musicCache.get(key);
        if (cached != null) return cached;
        String path = musicPaths.get(key);
        if (path == null) {
            warnUnknown(key);
            failed.add(key);
            return null;
        }
        if (Gdx.audio == null || Gdx.files == null) return null;
        try {
            FileHandle fh = Gdx.files.internal(path);
            if (!fh.exists()) {
                warnMissing(key, path);
                failed.add(key);
                return null;
            }
            Music m = Gdx.audio.newMusic(fh);
            musicCache.put(key, m);
            return m;
        } catch (Exception e) {
            warnLoadError(key, path, e);
            failed.add(key);
            return null;
        }
    }

    private Sound loadSound(String key) {
        if (failed.contains(key)) return null;
        Sound cached = sfxCache.get(key);
        if (cached != null) return cached;
        String path = sfxPaths.get(key);
        if (path == null) {
            warnUnknown(key);
            failed.add(key);
            return null;
        }
        if (Gdx.audio == null || Gdx.files == null) return null;
        try {
            FileHandle fh = Gdx.files.internal(path);
            if (!fh.exists()) {
                warnMissing(key, path);
                failed.add(key);
                return null;
            }
            Sound s = Gdx.audio.newSound(fh);
            sfxCache.put(key, s);
            return s;
        } catch (Exception e) {
            warnLoadError(key, path, e);
            failed.add(key);
            return null;
        }
    }

    private static void warnUnknown(String key) {
        if (Gdx.app != null) Gdx.app.log("AudioManager", "Unknown audio key: " + key);
    }

    private static void warnMissing(String key, String path) {
        if (Gdx.app != null) Gdx.app.log("AudioManager", "Asset missing for '" + key + "': " + path + " (silenced)");
    }

    private static void warnLoadError(String key, String path, Exception e) {
        if (Gdx.app != null) Gdx.app.log("AudioManager", "Failed to load '" + key + "' (" + path + "): " + e.getMessage());
    }

    private static float clamp(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }
}