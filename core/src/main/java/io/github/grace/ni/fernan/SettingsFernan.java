package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.audio.Sound;


public class SettingsFernan implements Screen {

    private final FernansGrace game;
    private Texture background;
    private Stage stage;
    private Skin skin;
    private BitmapFont font, whiteFont, yellowFont;
    private Sound clickSound;
    private TextButton musicToggleButton;

    public SettingsFernan(final FernansGrace game) {
        this.game = game;
        this.background = new Texture("BG2B.png");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        font = new BitmapFont(Gdx.files.internal("ui/Aligator2.fnt"));
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        whiteFont = new BitmapFont(Gdx.files.internal("ui/smalligator_gradient2.fnt"));
        whiteFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        yellowFont = new BitmapFont(Gdx.files.internal("ui/smalligator_yellow.fnt"));
        yellowFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        clickSound = Gdx.audio.newSound(Gdx.files.internal("Click.mp3"));


        Texture transparentTexture = new Texture(Gdx.files.internal("ui/transparent.png"));
        Drawable transparentDrawable = new TextureRegionDrawable(new TextureRegion(transparentTexture));

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = transparentDrawable;
        buttonStyle.down = transparentDrawable;
        buttonStyle.over = transparentDrawable;
        buttonStyle.font = yellowFont;

        musicToggleButton = new TextButton("Music: ON", buttonStyle);
        musicToggleButton.getLabel().setFontScale(1.7f);
        updateMusicButtonText();

        musicToggleButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                changeFont(musicToggleButton, whiteFont);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                changeFont(musicToggleButton, yellowFont);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (clickSound != null) clickSound.play();
                game.toggleMusic();
                updateMusicButtonText();
            }
        });

        TextButton backButton = new TextButton("Back", buttonStyle);
        backButton.getLabel().setFontScale(1.7f);

        backButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                changeFont(backButton, whiteFont);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                changeFont(backButton, yellowFont);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (clickSound != null) clickSound.play();
                if (!game.isInGame){
                    game.setScreen(new MainFernan(game));
                } else {
                    // Now this line should work as FernansGrace has the method
                    SaveProfile currentProfile = game.getCurrentSaveProfile();
                    if (currentProfile != null) {
                        game.setScreen(new GameMenuFernan(game, currentProfile));
                    } else {
                        // Fallback if profile is somehow null even if isInGame is true
                        Gdx.app.error("SettingsFernan", "isInGame is true, but no active profile found. Returning to Main Menu.");
                        game.setScreen(new MainFernan(game));
                    }
                }
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        Label settingsTitle = new Label("Settings", new Label.LabelStyle(font, Color.WHITE));
        settingsTitle.setFontScale(2.5f);
        table.add(settingsTitle).padBottom(50).row();

        table.add(musicToggleButton).width(300).height(60).padBottom(30).row();
        table.add(backButton).width(200).height(50).row();

        stage.addActor(table);
    }

    private void updateMusicButtonText() {
        if (musicToggleButton != null) {
            if (game.isMusicEnabled()) {
                musicToggleButton.setText("Music: ON");
            } else {
                musicToggleButton.setText("Music: OFF");
            }
        }
    }

    private void changeFont(TextButton button, BitmapFont newFont) {
        TextButton.TextButtonStyle newStyle = new TextButton.TextButtonStyle(button.getStyle());
        newStyle.font = newFont;
        button.setStyle(newStyle);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        updateMusicButtonText();
        game.ensureMusicState();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        if (game.viewport != null) { // Add null check for game.viewport
            game.viewport.apply();
            game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        } else {
            // Fallback or handle if viewport is null, though it should be initialized in FernansGrace.create()
            Gdx.app.error("SettingsFernan", "game.viewport is null in render method.");
            // Use stage's viewport as a fallback for drawing background if game.viewport is an issue
            stage.getViewport().apply();
            game.batch.setProjectionMatrix(stage.getViewport().getCamera().combined);
        }


        game.batch.begin();
        if (background != null && game.viewport != null) { // Ensure background and viewport are not null
            game.batch.draw(background, 0, 0, game.viewport.getWorldWidth(), game.viewport.getWorldHeight());
        } else if (background != null) { // Fallback if game.viewport was null
            game.batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        game.batch.end();

        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (game.viewport != null) game.viewport.update(width, height, true);
        if (stage.getViewport() != null) stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}
    @Override
    public void resume() {
        game.ensureMusicState();
    }
    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (background != null) background.dispose();
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (font != null) font.dispose();
        if (whiteFont != null) whiteFont.dispose();
        if (yellowFont != null) yellowFont.dispose();
        if (clickSound != null) clickSound.dispose();
    }
}
