package io.github.grace.ni.fernan;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class PackOpenScreenFernan implements Screen {
    private final FernansGrace game;
    private Stage stage;

    public PackOpenScreenFernan(FernansGrace game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());

        // Background image (copied from PackScreenFernan)
        Texture bgTexture = new Texture(Gdx.files.internal("Bg2B.PNG"));
        bgTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Image bg = new Image(bgTexture);
        bg.setFillParent(true);
        stage.addActor(bg);

        // Add animation, reward reveal logic here
    }

    @Override public void show() {}
    @Override public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }
    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        stage.dispose();
    }
}
