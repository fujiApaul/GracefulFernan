package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.audio.Sound;


public class GameMenuFernan implements Screen {

    private final FernansGrace game;
    private Stage stage;
    private Texture background;
    private BitmapFont defaultFont;
    private BitmapFont hoverFont;
    private Label descriptionLabel;
    private Sound clickSound;

    public GameMenuFernan(final FernansGrace game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        background = new Texture(Gdx.files.internal("ui/gamemenubg2.png"));
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Image bgImage = new Image(new TextureRegionDrawable(new TextureRegion(background)));
        bgImage.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.addActor(bgImage);

        defaultFont = new BitmapFont(Gdx.files.internal("ui/smalligator_yellow.fnt"));
        hoverFont = new BitmapFont(Gdx.files.internal("ui/smalligator_gradient2.fnt"));

        clickSound = Gdx.audio.newSound(Gdx.files.internal("Click.mp3"));

        Drawable transparentDrawable = new TextureRegionDrawable(new TextureRegion(new Texture("ui/transparent.png")));

        TextButton.TextButtonStyle defaultStyle = new TextButton.TextButtonStyle();
        defaultStyle.up = transparentDrawable;
        defaultStyle.down = transparentDrawable;
        defaultStyle.over = transparentDrawable;
        defaultStyle.font = defaultFont;

        TextButton.TextButtonStyle hoverStyle = new TextButton.TextButtonStyle();
        hoverStyle.up = transparentDrawable;
        hoverStyle.down = transparentDrawable;
        hoverStyle.over = transparentDrawable;
        hoverStyle.font = hoverFont;

        String[] descriptions = {
            "Battle against various creatures, enemies, and other players.",
            "View and manage your battle decks.",
            "Visit the in-game store to buy items.",
            "Change game settings and preferences.",
            "Return to the main menu."
        };

        String[] buttonTexts = {"Play", "Decks", "Store", "Settings", "Main Menu"};

        Table table = new Table();
        table.setFillParent(true);
        table.left().padLeft(50);
        stage.addActor(table);

        descriptionLabel = new Label("", new Label.LabelStyle(hoverFont, Color.YELLOW));
        descriptionLabel.setWrap(true);
        descriptionLabel.setWidth(800);
        descriptionLabel.setPosition(400, 450);
        stage.addActor(descriptionLabel);

        for (int i = 0; i < buttonTexts.length; i++) {
            final int index = i;
            final TextButton button = new TextButton(buttonTexts[i], defaultStyle);
            button.getLabel().setFontScale(1.5f);

            final Label rowDescription = new Label("", new Label.LabelStyle(hoverFont, Color.YELLOW));
            rowDescription.setWrap(true);
            rowDescription.setWidth(600);
            rowDescription.setAlignment(Align.left);

            button.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    button.setStyle(hoverStyle);
                    rowDescription.setText(descriptions[index]);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    button.setStyle(defaultStyle);
                    rowDescription.setText("");
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    switch (index) {
                        case 0:
                            System.out.println("Load game clicked");
                            clickSound.play();
                            game.setScreen(new ModeSelectionFernan(game));
                            break;
                        case 1:
                            System.out.println("Load game clicked");
                            clickSound.play();
                            game.setScreen(new DeckBuilderScreen(game));
                            break;
                        case 2:
                            clickSound.play();
                            game.setScreen(new StoreScreenFernan(game));
                            break;
                        case 3:
                            clickSound.play();
                            game.setScreen(new SettingsFernan(game)); // Go back to the main menu
                            break;
                        case 4:
                            clickSound.play();
                            game.setScreen(new MainFernan(game));
                            game.isInGame = false;
                            break;
                    }
                }
            });

            float extraPadLeft = (index == 1 || index == 2) ? 40f : 0f;

            table.add(button).padBottom(20).padLeft(extraPadLeft).left();
            table.add(rowDescription).padLeft(30 + extraPadLeft).width(600).left().row();
        }
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
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
        stage.dispose();
        background.dispose();
        defaultFont.dispose();
        hoverFont.dispose();
    }
}
