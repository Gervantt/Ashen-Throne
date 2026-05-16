package com.ashenthrone.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

public class AudioManager {

    private static AudioManager instance;

    private final Map<String, String> musicPaths = new HashMap<>();
    private final Map<String, String> sfxPaths   = new HashMap<>();

    private final Map<String, Music> musicCache = new HashMap<>();
    private final Map<String, Sound> sfxCache   = new HashMap<>();

    private final java.util.Set<String> nonLoopingMusic = new java.util.HashSet<>();

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

        musicPaths.put("main_theme",    "audio/music/main_theme.mp3");
        musicPaths.put("battle_theme",  "audio/music/battle_theme.mp3");
        musicPaths.put("boss_theme",    "audio/music/boss_theme.ogg");

        musicPaths.put("victory_sting", "audio/music/victory_sting.wav");
        musicPaths.put("defeat_sting",  "audio/music/defeat_sting.mp3");
        nonLoopingMusic.add("victory_sting");
        nonLoopingMusic.add("defeat_sting");

        sfxPaths.put("sword_hit",         "audio/sfx/sword_hit.mp3");
        sfxPaths.put("fireball",          "audio/sfx/fireball.mp3");
        sfxPaths.put("heal_cast",         "audio/sfx/heal_cast.mp3");
        sfxPaths.put("enemy_death",       "audio/sfx/enemy_death.mp3");
        sfxPaths.put("hero_hurt",         "audio/sfx/hero_hurt.mp3");
        sfxPaths.put("transition_whoosh", "audio/sfx/transition_whoosh.mp3");
        sfxPaths.put("purchase_sound",    "audio/sfx/purchase_sound.mp3");
    }

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

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusic    = null;
        currentMusicKey = null;
    }

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