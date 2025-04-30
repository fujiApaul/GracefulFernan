package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class MainFernan implements Screen {

    final FernansGrace game;

    private Texture background;
    private Stage stage;
    private Skin skin;

    public MainFernan(final FernansGrace game){
        this.game = game;

        background = new Texture("BG1.png");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Label titleLabel = new Label("Fernan's Grace", skin);
        titleLabel.setFontScale(2f);
        titleLabel.setAlignment(Align.center);

        TextButton startButton = new TextButton("Start", skin);
        TextButton settingsButton = new TextButton("Settings", skin);
        TextButton exitButton = new TextButton("Exit", skin);

        startButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Start clicked");
                // game.setScreen(new YourGameScreen(game)); // implement this later
            }
        });

        settingsButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Settings clicked");
            }
        });

        exitButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // Create a table for layout
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        // Add UI elements to table
        table.add(titleLabel).padBottom(40).row();
        table.add(startButton).width(200).padBottom(20).row();
        table.add(settingsButton).width(200).padBottom(20).row();
        table.add(exitButton).width(200).row();

        stage.addActor(table);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        // Draw the background
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();
        game.batch.draw(background, 0, 0, game.viewport.getWorldWidth(), game.viewport.getWorldHeight());
        game.batch.end();

        // Draw Scene2D UI
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}

    @Override
    public void dispose() {
        background.dispose();
        stage.dispose();
        skin.dispose();
    }
}
