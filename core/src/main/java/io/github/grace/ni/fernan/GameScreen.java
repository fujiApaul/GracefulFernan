package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen implements Screen {

    final FernansGrace game;
    private Texture background;
    private Stage stage;
    private Skin skin;
    private BitmapFont customFont;
    private BitmapFont buttonFont;

    public GameScreen(final FernansGrace game) {
        this.game = game;
        background = new Texture("BG1.png");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load fonts
        customFont = new BitmapFont(Gdx.files.internal("ui/Aligator4.fnt"));
        customFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        buttonFont = new BitmapFont(Gdx.files.internal("ui/Aligator2.fnt"));
        buttonFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        BitmapFont yellowFont = new BitmapFont(Gdx.files.internal("ui/smalligator_yellow.fnt"));
        yellowFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        BitmapFont whiteFont = new BitmapFont(Gdx.files.internal("ui/smalligator_gradient2.fnt"));
        whiteFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Title label
        Label.LabelStyle customLabelStyle = new Label.LabelStyle();
        customLabelStyle.font = customFont;
        customLabelStyle.fontColor = Color.WHITE;

        Label titleLabel = new Label("Fernan's Grace", customLabelStyle);
        titleLabel.setFontScale(2.5f);
        titleLabel.setAlignment(Align.center);

        // Button style
        Texture transparentTexture = new Texture(Gdx.files.internal("ui/transparent.png"));
        Drawable transparentDrawable = new TextureRegionDrawable(new TextureRegion(transparentTexture));

        TextButton.TextButtonStyle customButtonStyle = new TextButton.TextButtonStyle();
        customButtonStyle.up = transparentDrawable;
        customButtonStyle.down = transparentDrawable;
        customButtonStyle.over = transparentDrawable;
        customButtonStyle.font = yellowFont;

        // Buttons
        TextButton newGameButton = new TextButton("New Game", customButtonStyle);
        TextButton loadGameButton = new TextButton("Load Game", customButtonStyle);
        TextButton backButton = new TextButton("Back", customButtonStyle);

        newGameButton.getLabel().setFontScale(1.7f);
        loadGameButton.getLabel().setFontScale(1.7f);
        backButton.getLabel().setFontScale(1.7f);

        // Hover and click listeners for all buttons
        addHoverEffect(newGameButton, yellowFont, whiteFont);
        addHoverEffect(loadGameButton, yellowFont, whiteFont);
        addHoverEffect(backButton, yellowFont, whiteFont);

        // Button actions
        newGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            	game.setScreen(new GameMenuScreen(game));
            }
        });

        loadGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Load game clicked");
            }
        });

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainFernan(game));
            }
        });

        // Layout with table
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        table.add(titleLabel).padBottom(40).row();
        table.add(newGameButton).width(200).height(50).padBottom(20).row();
        table.add(loadGameButton).width(200).height(50).padBottom(20).row();
        table.add(backButton).width(200).height(50).row();

        stage.addActor(table);
    }

    private void addHoverEffect(final TextButton button, final BitmapFont yellowFont, final BitmapFont whiteFont) {
        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                Label.LabelStyle style = new Label.LabelStyle(button.getLabel().getStyle());
                style.font = whiteFont;
                button.getLabel().setStyle(style);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                Label.LabelStyle style = new Label.LabelStyle(button.getLabel().getStyle());
                style.font = yellowFont;
                button.getLabel().setStyle(style);
            }
        });
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
        customFont.dispose();
        buttonFont.dispose();
    }
}
