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

        // Image Buttons (Back and Sort)
        ImageButton backButton = new ImageButton(new TextureRegionDrawable(
            new TextureRegion(new Texture("ui/backicon.png"))));
        backButton.setSize(50, 50);  // Set button size
        backButton.getImage().setScaling(Scaling.fit);  // Scale icon properly
        backButton.getImageCell().size(50, 50);  // Ensure image inside button fits
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

        // Header Table for Back and Sort buttons
        Table header = new Table();
        header.pad(10).top();

        header.add(backButton).size(50, 50).padRight(10).left();
        Label titleLabel = new Label("CARD SELECTION", skin);
        titleLabel.setColor(Color.WHITE);
        header.add(titleLabel).left().expandX();
        header.add(sortButton).size(50, 50).padLeft(10).right();

        // Card table
        cardTable = new Table().top().left();
        rebuildCardTable();

        // Scrollable Area for Cards
        ScrollPane scrollPane = new ScrollPane(cardTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setScrollbarsOnTop(true);

        root.add(header).expandX().fillX().top().row();
        root.add(scrollPane).expand().fill().pad(10).row();

        // Load BitmapFonts for SELECT Button
        BitmapFont whiteFont = new BitmapFont(Gdx.files.internal("ui/smalligator_white.fnt"));
        BitmapFont yellowFont = new BitmapFont(Gdx.files.internal("ui/smalligator_yellow.fnt"));

        // Create a Label for the SELECT button
        Label.LabelStyle selectButtonStyle = new Label.LabelStyle();
        selectButtonStyle.font = whiteFont;  // Use white font for default state

        final Label selectButton = new Label("SELECT", selectButtonStyle);
        selectButton.setColor(Color.WHITE);  // Set the text color to white initially

        // Add a ClickListener to handle button clicks
        selectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedCards.size() > 0) {
                    System.out.println("Selected Deck:");
                    for (CardSystem.Card c : selectedCards)
                        System.out.println("- " + c.getName());
                    game.setScreen(new GameMap1Screen(game));  // Navigate to game map
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                // Change to yellow font on hover
                selectButtonStyle.font = yellowFont;
                selectButton.setStyle(selectButtonStyle);  // Apply style change
                selectButton.setColor(Color.YELLOW);  // Change text color to yellow
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                // Change back to white font when not hovering
                selectButtonStyle.font = whiteFont;
                selectButton.setStyle(selectButtonStyle);  // Apply style change
                selectButton.setColor(Color.WHITE);  // Change text color back to white
            }
        });

        // Add SELECT button to the root table
        root.add(selectButton).padTop(20).colspan(3);
    }





    private void rebuildCardTable() {
        cardTable.clear();
        cardContainers.clear();

        int columns = 4;  // Number of columns set to 4
        int i = 0;

        for (CardSystem.Card card : allCards) {
            VerticalGroup cardGroup = new VerticalGroup();
            cardGroup.space(5);
            cardGroup.pad(5);
            cardGroup.setTouchable(Touchable.enabled);

            // Load card texture
            Texture texture = new Texture(Gdx.files.internal(card.getImagePath()));
            Image cardImage = new Image(texture);
            cardImage.setScaling(Scaling.fit);

            // Set adjusted card size (suitable for 4 per row)
            Container<Image> imageContainer = new Container<>(cardImage);
            imageContainer.size(190, 280);  // Adjusted size for 4 cards per row

            cardGroup.addActor(imageContainer);

            // Removed the name label as requested
            // Label nameLabel = new Label(card.getName(), skin);
            // cardGroup.addActor(nameLabel);

            Container<VerticalGroup> container = new Container<>(cardGroup);
            container.setBackground(skin.newDrawable("white", Color.DARK_GRAY));
            container.width(190).height(280).pad(10);  // Adjusted width/height and padding

            cardContainers.put(card, container);

            cardGroup.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (selectedCards.contains(card)) {
                        selectedCards.remove(card);
                        container.setBackground(skin.newDrawable("white", Color.DARK_GRAY));
                    } else if (selectedCards.size() < 10) {
                        selectedCards.add(card);
                        container.setBackground(skin.newDrawable("white", Color.YELLOW));  // Highlight entire card
                    }
                }
            });

            // Add each card to the table
            cardTable.add(container).pad(10);  // Add padding between cards

            i++;
            if (i % columns == 0) cardTable.row();  // Ensure proper row break after every 4 cards
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

    private void insertionSort(List<CardSystem.Card> cards, Comparator<CardSystem.Card> comparator) {
        for (int i = 1; i < cards.size(); i++) {
            CardSystem.Card key = cards.get(i);
            int j = i - 1;
            while (j >= 0 && comparator.compare(cards.get(j), key) > 0) {
                cards.set(j + 1, cards.get(j));
                j--;
            }
            cards.set(j + 1, key);
        }
    }

    private void bubbleSort(List<CardSystem.Card> cards, Comparator<CardSystem.Card> comparator) {
        for (int i = 0; i < cards.size() - 1; i++) {
            for (int j = 0; j < cards.size() - i - 1; j++) {
                if (comparator.compare(cards.get(j), cards.get(j + 1)) > 0) {
                    Collections.swap(cards, j, j + 1);
                }
            }
        }
    }

}
