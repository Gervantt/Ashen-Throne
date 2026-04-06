package com.ashenthrone.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.ashenthrone.core.AshenThroneGame;

/**
 * Desktop launcher — the entry point that starts the libGDX application.
 *
 * libGDX note: Lwjgl3Application is the class that actually creates the
 * window and starts the game loop. You pass it your Game instance and a
 * configuration object. Once constructed, it runs the loop internally
 * (calling render() ~60 times/sec) until the window is closed.
 */
public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Ashen Throne");
        config.setWindowedMode(1280, 720);
        config.setResizable(false);
        config.useVsync(true);
        config.setForegroundFPS(60);

        // This constructor blocks until the game window is closed.
        new Lwjgl3Application(AshenThroneGame.getInstance(), config);
    }
}
