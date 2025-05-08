package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.*;
import java.util.List;

public class DeckBuilderScreen implements Screen {

    private final FernansGrace game;
    private Stage stage;
    private Skin skin;
    private List<CardSystem.Card> allCards;
    private List<CardSystem.Card> selectedCards = new ArrayList<>();
    private Texture backgroundTexture;
    private Image backgroundImage;
    private Map<CardSystem.Card, Container<VerticalGroup>> cardContainers = new HashMap<>();
    private Table cardTable;

    public DeckBuilderScreen(FernansGrace game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        allCards = CardSystem.loadCardsFromJson();
        buildUI();
    }

    private void buildUI() {
        stage.clear();

        backgroundTexture = new Texture(Gdx.files.internal("ui/deckbuilderscreenbg.png"));
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

// Inside buildUI() method

        ImageButton backButton = new ImageButton(new TextureRegionDrawable(
            new TextureRegion(new Texture("ui/backicon.png"))));
        backButton.setSize(50, 50); // Set button size
        backButton.getImage().setScaling(Scaling.fit); // Scale icon properly
        backButton.getImageCell().size(50, 50); // Ensure image inside button fits
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameMenuScreen(game));
            }
        });

        ImageButton sortButton = new ImageButton(new TextureRegionDrawable(
            new TextureRegion(new Texture("ui/sorticon.png"))));
        sortButton.setSize(50, 50);
        sortButton.getImage().setScaling(Scaling.fit);
        sortButton.getImageCell().size(50, 50);
        sortButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                allCards.sort(Comparator.comparing(CardSystem.Card::getName));
                rebuildCardTable();
            }
        });

        Table header = new Table();
        header.pad(10).top();

        header.add(backButton).size(50, 50).padRight(10).left();

        BitmapFont headerFont = new BitmapFont(Gdx.files.internal("ui/smalligator_white.fnt"));

        Label.LabelStyle headerStyle = new Label.LabelStyle();
        headerStyle.font = headerFont;

        Label titleLabel = new Label("CARD SELECTION", headerStyle);

        header.add(titleLabel).left().expandX();

        header.add(sortButton).size(50, 50).padLeft(10).right();



        cardTable = new Table().top().left();
        rebuildCardTable();

        ScrollPane scrollPane = new ScrollPane(cardTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setScrollbarsOnTop(true);

        root.add(header).expandX().fillX().top().row();
        root.add(scrollPane).expand().fill().pad(10).row();

        TextButton continueButton = new TextButton("Start Game", skin);
        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedCards.size() > 0) {
                    System.out.println("Selected Deck:");
                    for (CardSystem.Card c : selectedCards)
                        System.out.println("- " + c.getName());
                    game.setScreen(new GameMap1Screen(game));
                }
            }
        });

        root.add(continueButton).padTop(20).colspan(3);
    }

    private void rebuildCardTable() {
        cardTable.clear();
        cardContainers.clear();

        int columns = 3;
        int i = 0;

        for (CardSystem.Card card : allCards) {
            VerticalGroup cardGroup = new VerticalGroup();
            cardGroup.space(5);
            cardGroup.pad(5);
            cardGroup.setTouchable(Touchable.enabled);

            Texture texture = new Texture(Gdx.files.internal(card.getImagePath()));
            Image cardImage = new Image(texture);
            cardImage.setScaling(Scaling.fit);

            Container<Image> imageContainer = new Container<>(cardImage);
            imageContainer.size(150, 210);
            cardImage.setSize(50, 70);

            Label nameLabel = new Label(card.getName(), skin);
            nameLabel.setAlignment(1);

            cardGroup.addActor(imageContainer);
            cardGroup.addActor(nameLabel);

            Container<VerticalGroup> container = new Container<>(cardGroup);
            container.setBackground(skin.newDrawable("white", Color.DARK_GRAY));
            container.width(150).height(120).pad(5);

            cardContainers.put(card, container);

            cardGroup.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (selectedCards.contains(card)) {
                        selectedCards.remove(card);
                        container.setBackground(skin.newDrawable("white", Color.DARK_GRAY));
                    } else if (selectedCards.size() < 10) {
                        selectedCards.add(card);
                        container.setBackground(skin.newDrawable("white", Color.YELLOW));
                    }
                }
            });

            cardTable.add(container);

            i++;
            if (i % columns == 0) cardTable.row();
        }
    }

    @Override public void show() {}
    @Override public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
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
    }
}
