package com.ashenthrone.transition;

/**
 * Identifies every screen the {@link TransitionManager} mediator can route to
 * (AT-025). Screens that require constructor parameters
 * ({@code BATTLE}, {@code VICTORY}, {@code DEFEAT}) are still listed here for
 * symmetry, but callers must use the dedicated typed methods on
 * {@link TransitionManager} so the parameters can be passed.
 */
public enum ScreenType {
    MAIN_MENU,
    HERO_SELECT,
    REALM_SELECT,
    BATTLE,
    VICTORY,
    DEFEAT,
    SHOP,
    SETTINGS,
    END_GAME
}
