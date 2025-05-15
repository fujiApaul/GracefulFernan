package io.github.grace.ni.fernan;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

import java.util.Collections;
import java.util.List;

public class PackOpenScreenFernan implements Screen {

    private final FernansGrace game;
    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    private Image backgroundImage;
    private List<CardSystem.Card> allCards;
    private BitmapFont yellowFont;
    private BitmapFont whiteFont;
    private Sound clickSound;

    public PackOpenScreenFernan(FernansGrace game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        this.allCards = CardSystem.loadCardsFromJson();

        this.yellowFont = new BitmapFont(Gdx.files.internal("ui/smalligator_yellow.fnt"));
        this.whiteFont = new BitmapFont(Gdx.files.internal("ui/smalligator_gradient2.fnt"));
        this.clickSound = Gdx.audio.newSound(Gdx.files.internal("Click.mp3"));

        buildUI();
    }

    private void buildUI() {
        stage.clear();

        // Background
        backgroundTexture = new Texture(Gdx.files.internal("Bg2B.PNG"));
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Shuffle and pick 2 random cards
        Collections.shuffle(allCards);
        CardSystem.Card card1 = allCards.get(0);
        CardSystem.Card card2 = allCards.get(1);

        Table cardRow = new Table();
        addCardToTable(card1, cardRow);
        addCardToTable(card2, cardRow);

        root.add(cardRow).center();

        // Back Button
        TextButton.TextButtonStyle backButtonStyle = new TextButton.TextButtonStyle();
        Drawable transparentDrawable = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("ui/transparent.png"))));
        backButtonStyle.up = transparentDrawable;
        backButtonStyle.down = transparentDrawable;
        backButtonStyle.over = transparentDrawable;
        backButtonStyle.font = yellowFont;

        TextButton backButton = new TextButton("Back", backButtonStyle);
        backButton.getLabel().setFontScale(1.5f);

        backButton.addListener(new ClickListener() {
            @Override public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                backButton.getLabel().setStyle(new Label.LabelStyle(whiteFont, Color.WHITE));
            }

            @Override public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                backButton.getLabel().setStyle(new Label.LabelStyle(yellowFont, Color.YELLOW));
            }

            @Override public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.setScreen(new StoreScreenFernan(game));
            }
        });

        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top().left().padTop(10).padLeft(10);
        topTable.add(backButton).width(100).height(40);
        stage.addActor(topTable);
    }

    private void addCardToTable(CardSystem.Card card, Table table) {
        Texture cardTexture = new Texture(Gdx.files.internal(card.getImagePath()));
        Image cardImage = new Image(cardTexture);
        cardImage.setScaling(Scaling.fit);
        cardImage.setSize(200, 300);

        table.add(cardImage).size(200, 300).pad(20);
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
        skin.dispose();
        backgroundTexture.dispose();
        yellowFont.dispose();
        whiteFont.dispose();
        clickSound.dispose();
    }
}
