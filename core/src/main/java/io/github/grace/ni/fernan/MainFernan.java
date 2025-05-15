package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.github.grace.ni.fernan.NewGameScreen;
import io.github.grace.ni.fernan.LoadGameScreen;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;


public class MainFernan implements Screen {

    final FernansGrace game;

    private Texture background;
    private Stage stage;
    private Skin skin;
    private BitmapFont customFont, buttonFont, yellowFont, whiteFont;
    private TextButton.TextButtonStyle customButtonStyle;
    private TextButton button1, button2, button3;
    private Table table;
    private boolean isMainMenu2 = false;

    private Sound clickSound;

    public MainFernan(final FernansGrace game) {
        this.game = game;
        background = new Texture("BG1.png");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Fonts
        customFont = new BitmapFont(Gdx.files.internal("ui/Aligator4.fnt"));
        customFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        buttonFont = new BitmapFont(Gdx.files.internal("ui/Aligator2.fnt"));
        buttonFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        yellowFont = new BitmapFont(Gdx.files.internal("ui/smalligator_yellow.fnt"));
        yellowFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        whiteFont = new BitmapFont(Gdx.files.internal("ui/smalligator_gradient2.fnt"));
        whiteFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Load click sound
        clickSound = Gdx.audio.newSound(Gdx.files.internal("Click.mp3")); // Make sure this file exists

        // Button style
        customButtonStyle = new TextButton.TextButtonStyle();
        Drawable transparentDrawable = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("ui/transparent.png"))));
        customButtonStyle.up = transparentDrawable;
        customButtonStyle.down = transparentDrawable;
        customButtonStyle.over = transparentDrawable;
        customButtonStyle.font = yellowFont;

        // Table setup
        table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        // Initial button render
        updateMenuButtons();
    }

    private void updateMenuButtons() {
        table.clear();

        // Title
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = customFont;
        titleStyle.fontColor = Color.WHITE;
        Label titleLabel = new Label("Fernan's Grace", titleStyle);
        titleLabel.setFontScale(2.5f);
        titleLabel.setAlignment(Align.center);

        // Buttons
        if (!isMainMenu2) {
            button1 = new TextButton("Start", customButtonStyle);
            button2 = new TextButton("Settings", customButtonStyle);
            button3 = new TextButton("Exit", customButtonStyle);
        } else {
            button1 = new TextButton("New Game", customButtonStyle);
            button2 = new TextButton("Load Game", customButtonStyle);
            button3 = new TextButton("Back", customButtonStyle);
        }

        button1.getLabel().setFontScale(1.7f);
        button2.getLabel().setFontScale(1.7f);
        button3.getLabel().setFontScale(1.7f);

        setButtonListeners();

        table.add(titleLabel).padBottom(40).row();
        table.add(button1).width(200).height(50).padBottom(20).row();
        table.add(button2).width(200).height(50).padBottom(20).row();
        table.add(button3).width(200).height(50).row();
    }

    private void setButtonListeners() {
        // Button 1
        button1.addListener(new ClickListener() {
            @Override public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                changeFont(button1, whiteFont);
            }

            @Override public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                changeFont(button1, yellowFont);
            }

            @Override public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                if (!isMainMenu2) {
                    isMainMenu2 = true;
                    updateMenuButtons();
                } else {
                    System.out.println("New Game clicked");
                    game.setScreen(new NewGameScreen(game));
                    game.isInGame = true;
                }
            }
        });

        // Button 2
        button2.addListener(new ClickListener() {
            @Override public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                changeFont(button2, whiteFont);
            }

            @Override public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                changeFont(button2, yellowFont);
            }

            @Override public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                if (!isMainMenu2) {
                    game.setScreen(new SettingsFernan(game));
                } else {
                    System.out.println("Load Game clicked");
                    game.setScreen(new LoadGameScreen(game));
                    // handle load
                }
            }
        });

        // Button 3
        button3.addListener(new ClickListener() {
            @Override public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                changeFont(button3, whiteFont);
            }

            @Override public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                changeFont(button3, yellowFont);
            }

            @Override public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                if (!isMainMenu2) {
                    Gdx.app.exit();
                } else {
                    isMainMenu2 = false;
                    updateMenuButtons();
                }
            }
        });
    }

    private void changeFont(TextButton button, BitmapFont font) {
        Label.LabelStyle style = new Label.LabelStyle(button.getLabel().getStyle());
        style.font = font;
        button.getLabel().setStyle(style);
    }

    @Override public void show() {}

    @Override public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);

        game.batch.begin();
        game.batch.draw(background, 0, 0, game.viewport.getWorldWidth(), game.viewport.getWorldHeight());
        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        game.viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override public void dispose() {
        background.dispose();
        stage.dispose();
        skin.dispose();
        customFont.dispose();
        buttonFont.dispose();
        yellowFont.dispose();
        whiteFont.dispose();
        clickSound.dispose();
    }
}
