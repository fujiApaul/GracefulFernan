package io.github.grace.ni.fernan;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class FernansGrace extends Game {

    public SpriteBatch batch;
    public FitViewport viewport;
    public boolean isInGame = false;

    @Override
    public void create() {

        viewport = new FitViewport(16, 9);
        batch = new SpriteBatch();

        this.setScreen(new MainFernan(this));
    }

    public void render(){
        super.render();
    }
}
