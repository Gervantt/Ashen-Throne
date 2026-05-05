package com.ashenthrone.audio;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

import java.util.List;

/**
 * Thin wrapper around libGDX {@link AssetManager} (AT-014 skeleton).
 *
 * Provides a tiny façade with {@link #loadAll(List)} (queue any path; type is
 * inferred from extension) and {@link #loadingFinished()} (poll progress).
 * Concrete asset paths are registered in AT-021.
 */
public class GameAssetManager {

    private static GameAssetManager instance;

    private final AssetManager manager = new AssetManager();

    private GameAssetManager() {}

    public static GameAssetManager getInstance() {
        if (instance == null) {
            instance = new GameAssetManager();
        }
        return instance;
    }

    /**
     * Queue every path for asynchronous loading. Asset type is inferred from
     * file extension: .png/.jpg → Texture, .ogg/.mp3 (long) → Music,
     * .wav/.ogg (short SFX) → Sound. Long-form .ogg vs .wav distinction is
     * conventional — we treat .wav as Sound and .ogg/.mp3 as Music here;
     * AT-021 may override per-asset.
     */
    public void loadAll(List<String> paths) {
        for (String path : paths) {
            String lower = path.toLowerCase();
            if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                manager.load(path, Texture.class);
            } else if (lower.endsWith(".wav")) {
                manager.load(path, Sound.class);
            } else if (lower.endsWith(".ogg") || lower.endsWith(".mp3")) {
                manager.load(path, Music.class);
            }
            // unknown extensions are skipped silently — AT-021 owns the registry.
        }
    }

    /** Pump async loading and return true once every queued asset is ready. */
    public boolean loadingFinished() {
        return manager.update();
    }

    public float getProgress() {
        return manager.getProgress();
    }

    public <T> T get(String path, Class<T> type) {
        return manager.get(path, type);
    }

    public AssetManager raw() {
        return manager;
    }

    public void dispose() {
        manager.dispose();
    }
}
