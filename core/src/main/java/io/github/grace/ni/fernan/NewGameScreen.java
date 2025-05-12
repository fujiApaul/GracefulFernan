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
import io.github.grace.ni.fernan.SaveManager;


public class NewGameScreen implements Screen {

    final FernansGrace game;
    private Texture background;
    private Stage stage;
    private BitmapFont customFont;
    private Skin skin;

    public NewGameScreen(final FernansGrace game) {
        this.game = game;

        background = new Texture("BG1.png");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        customFont = new BitmapFont(Gdx.files.internal("ui/Aligator4.fnt"));
        customFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = customFont;
        labelStyle.fontColor = Color.WHITE;

        // Title Label
        Label titleLabel = new Label("Create New Game", labelStyle);
        titleLabel.setFontScale(2.0f);
        titleLabel.setAlignment(Align.center);

        // Name Input Field
        final TextField nameField = new TextField("", skin);
        nameField.setMessageText("Enter name...");
        nameField.setMaxLength(20);
        nameField.setAlignment(Align.center); // Align the text in the center of the TextField

        // Message Label (confirmation or errors)
        final Label messageLabel = new Label("", labelStyle);
        messageLabel.setFontScale(0.7f);
        messageLabel.setAlignment(Align.center);

        // Button Style
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = null;
        buttonStyle.down = null;
        buttonStyle.over = null;
        buttonStyle.font = customFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.YELLOW;
        buttonStyle.downFontColor = Color.RED;

        // Create Game Button
        TextButton createButton = new TextButton("Create Game", buttonStyle);
        createButton.getLabel().setFontScale(1.2f);
        createButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String saveName = nameField.getText().trim();
                if (saveName.isEmpty()) {
                    messageLabel.setText("Please enter a save name");
                } else {
                    SaveProfile profile = new SaveProfile(saveName);
                    SaveManager.saveProfile(profile);
                    game.setScreen(new GameMenuFernan(game, profile));
                }
            }
        });


        // Back Button
        TextButton backButton = new TextButton("Back", buttonStyle);
        backButton.getLabel().setFontScale(1.2f);

        // Back button logic
        backButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainFernan(game));
            }
        });

        // Layout - Align everything in the center
        Table table = new Table();
        table.setFillParent(true);
        table.center(); // Ensure everything is centered

        table.add(titleLabel).padBottom(30).colspan(2).row();
        table.add(nameField).width(200).padBottom(20).colspan(2).row(); // Center the TextField by setting colspan to 2
        table.add(createButton).colspan(2).padBottom(10).row();
        table.add(messageLabel).colspan(2).padBottom(20).row();
        table.add(backButton).colspan(2).row();

        stage.addActor(table);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);

        game.batch.begin();
        game.batch.draw(background, 0, 0, game.viewport.getWorldWidth(), game.viewport.getWorldHeight());
        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        background.dispose();
        stage.dispose();
        customFont.dispose();
        skin.dispose();
    }
}
