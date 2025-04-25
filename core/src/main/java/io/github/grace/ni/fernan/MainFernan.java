package io.github.grace.ni.fernan;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MainFernan extends Game {
    @Override
    public void create() {
        setScreen(new FirstScreen());
    }
}