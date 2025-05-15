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

        // MUSIC TOGGLE BUTTON - CENTER
        TextButton musicToggleButton = new TextButton(game.isMusicOn ? "Music: ON" : "Music: OFF", buttonStyle);
        musicToggleButton.getLabel().setFontScale(2f);

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
                clickSound.play();
                game.toggleMusic();
                musicToggleButton.setText(game.isMusicOn ? "Music: ON" : "Music: OFF");
            }
        });

        // BACK BUTTON - BOTTOM
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
                clickSound.play();
                if (!game.isInGame) {
                    game.setScreen(new MainFernan(game));
                } else {
                    game.setScreen(new GameMenuFernan(game));
                }
            }
        });

        // TABLES
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();

        Table bottomTable = new Table();
        bottomTable.setFillParent(true);
        bottomTable.bottom().pad(30);

        mainTable.add(musicToggleButton).width(250).height(70);
        bottomTable.add(backButton).width(150).height(50);

        stage.addActor(mainTable);
        stage.addActor(bottomTable);
    }

    private void changeFont(TextButton button, BitmapFont font) {
        Label.LabelStyle style = new Label.LabelStyle(button.getLabel().getStyle());
        style.font = font;
        button.getLabel().setStyle(style);
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
        font.dispose();
        whiteFont.dispose();
        yellowFont.dispose();
        clickSound.dispose();
    }
}
