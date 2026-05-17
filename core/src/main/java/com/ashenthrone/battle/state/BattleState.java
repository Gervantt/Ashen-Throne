package com.ashenthrone.battle.state;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface BattleState {


    void handleInput();


    void update(float delta);


    void render(SpriteBatch batch);

    default void renderOverlay(SpriteBatch batch) {}
}
