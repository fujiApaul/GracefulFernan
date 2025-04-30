package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
    private BitmapFont customFont;
    private BitmapFont buttonFont;

    public MainFernan(final FernansGrace game){
        this.game = game;

        background = new Texture("BG1.png");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load custom font (replace with the correct path to your .fnt file)
        customFont = new BitmapFont(Gdx.files.internal("ui/Aligator3.fnt"));
        customFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        buttonFont = new BitmapFont(Gdx.files.internal("ui/Aligator2.fnt"));
        buttonFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Create a custom label style with the custom font
        Label.LabelStyle customLabelStyle = new Label.LabelStyle();
        customLabelStyle.font = customFont;
        customLabelStyle.fontColor = Color.WHITE;  // You can change the font color here

        // Create title label using custom style
        Label titleLabel = new Label("Fernan's Grace", customLabelStyle);
        titleLabel.setFontScale(2.5f);  // Adjust the title font size
        titleLabel.setAlignment(Align.center);

        // Create custom button style using the custom font
        TextButton.TextButtonStyle customButtonStyle = new TextButton.TextButtonStyle();
        customButtonStyle.up = skin.getDrawable("button-normal");
        customButtonStyle.down = skin.getDrawable("button-normal-pressed");
        customButtonStyle.over = skin.getDrawable("button-normal-over");
        customButtonStyle.font = buttonFont;  // Apply the custom font to the button
        customButtonStyle.fontColor = Color.WHITE;  // Set button text color


        // Create buttons with custom font
        TextButton startButton = new TextButton("Start", customButtonStyle);
        TextButton settingsButton = new TextButton("Settings", customButtonStyle);
        TextButton exitButton = new TextButton("Exit", customButtonStyle);
        startButton.getLabel().setFontScale(0.8f); // Scale X and Y


        // Add listeners to the buttons
        startButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Start clicked");
                // game.setScreen(new YourGameScreen(game)); // Uncomment and replace with the actual screen
            }
        });

        settingsButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Settings clicked");
                // Handle settings screen here
            }
        });

        exitButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();  // Exit the game
            }
        });

        // Create a table to arrange UI elements
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        // Add UI elements to the table
        table.add(titleLabel).padBottom(40).row();
        table.add(startButton).width(200).padBottom(20).row();
        table.add(settingsButton).width(200).padBottom(20).row();
        table.add(exitButton).width(200).row();

        // Add the table to the stage
        stage.addActor(table);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        // Clear the screen and draw the background
        ScreenUtils.clear(Color.BLACK);

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);

        game.batch.begin();
        game.batch.draw(background, 0, 0, game.viewport.getWorldWidth(), game.viewport.getWorldHeight());
        game.batch.end();

        // Draw the Scene2D UI elements (buttons, labels)
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
        customFont.dispose();  // Don't forget to dispose the font when done
    }
}
