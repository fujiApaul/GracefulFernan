package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.Align;
// import io.github.grace.ni.fernan.SaveManager; // Already in same package

public class NewGameScreen implements Screen {

    final FernansGrace game;
    private Texture background;
    private Stage stage;
    private BitmapFont titleFont; // Renamed for clarity
    private BitmapFont buttonFont; // Added for button text
    private Skin skin;
    private TextField nameField;
    private Label messageLabel;


    public NewGameScreen(final FernansGrace game) {
        this.game = game;

        background = new Texture("BG1.png"); // Assuming BG1.png is appropriate here

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        titleFont = new BitmapFont(Gdx.files.internal("ui/Aligator4.fnt")); // For title
        titleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        buttonFont = new BitmapFont(Gdx.files.internal("ui/Aligator2.fnt")); // For buttons/messages, adjust if needed
        buttonFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);


        skin = new Skin(Gdx.files.internal("ui/uiskin.json")); // uiskin is good for TextField

        Label.LabelStyle titleLabelStyle = new Label.LabelStyle(titleFont, Color.WHITE);
        Label.LabelStyle messageLabelStyle = new Label.LabelStyle(buttonFont, Color.WHITE); // Smaller font for messages

        Label titleLabel = new Label("Create New Game", titleLabelStyle);
        titleLabel.setFontScale(1.8f); // Adjusted scale
        titleLabel.setAlignment(Align.center);

        nameField = new TextField("", skin);
        nameField.setMessageText("Enter save name...");
        nameField.setMaxLength(20);
        nameField.setAlignment(Align.center);

        messageLabel = new Label("", messageLabelStyle);
        messageLabel.setFontScale(1.0f);
        messageLabel.setAlignment(Align.center);
        messageLabel.setColor(Color.RED); // For error messages

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = buttonFont; // Using buttonFont
        textButtonStyle.fontColor = Color.WHITE;
        textButtonStyle.overFontColor = Color.YELLOW;
        // textButtonStyle.downFontColor = Color.RED; // downFontColor might not be needed if using overFontColor

        TextButton createButton = new TextButton("Create Game", textButtonStyle);
        createButton.getLabel().setFontScale(1.2f);
        createButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String saveName = nameField.getText().trim();
                if (saveName.isEmpty()) {
                    messageLabel.setText("Save name cannot be empty!");
                } else if (SaveManager.profileExists(saveName)) { // Check if profile already exists
                    messageLabel.setText("'" + saveName + "' already exists!");
                }
                else {
                    messageLabel.setText(""); // Clear previous error messages
                    SaveProfile newProfile = new SaveProfile(saveName);
                    // You might want to initialize some default values in newProfile here
                    // E.g., newProfile.setPlayerLevel(1); newProfile.setStartDate(System.currentTimeMillis());
                    SaveManager.saveProfile(newProfile);

                    game.setCurrentSaveProfile(newProfile); // ****** THIS IS THE FIX ******
                    Gdx.app.log("NewGameScreen", "New profile created and set: " + newProfile.saveName);
                    game.setScreen(new GameMenuFernan(game, newProfile));
                }
            }
        });

        TextButton backButton = new TextButton("Back", textButtonStyle);
        backButton.getLabel().setFontScale(1.2f);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setCurrentSaveProfile(null); // Clear profile state when going back
                game.setScreen(new MainFernan(game));
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        table.add(titleLabel).padBottom(50).colspan(2).row();
        table.add(nameField).width(Gdx.graphics.getWidth() * 0.4f).height(50f).padBottom(20).colspan(2).row(); // Relative width
        table.add(createButton).padBottom(20).colspan(2).row();
        table.add(messageLabel).padBottom(30).colspan(2).row();
        table.add(backButton).colspan(2).row();

        stage.addActor(table);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        if (messageLabel != null) messageLabel.setText(""); // Clear message on show
        if (nameField != null) nameField.setText(""); // Clear name field on show
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK); // Or your desired background color

        // It's good practice to have viewport apply before drawing the background with spritebatch
        // if the background is meant to be managed by the game's main viewport.
        // If background is just a static image covering the screen, stage will handle its viewport.
        game.viewport.apply(); // Apply game's main viewport
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);

        game.batch.begin();
        // Ensure background is drawn respecting the main game viewport for consistency
        game.batch.draw(background, 0, 0, game.viewport.getWorldWidth(), game.viewport.getWorldHeight());
        game.batch.end();

        stage.getViewport().apply(); // Apply stage's viewport before drawing stage
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true); // Update game's main viewport
        stage.getViewport().update(width, height, true); // Update stage's viewport
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (background != null) background.dispose();
        if (stage != null) stage.dispose();
        if (titleFont != null) titleFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
        if (skin != null) skin.dispose();
    }
}
