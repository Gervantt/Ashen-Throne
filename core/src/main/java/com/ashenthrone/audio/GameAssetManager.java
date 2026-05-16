package com.ashenthrone.audio;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

import java.util.List;

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

        }
    }

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
