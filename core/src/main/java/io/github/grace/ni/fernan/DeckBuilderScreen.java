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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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

    private String currentPantheonFilter = null;
    private String currentTypeFilter = null;

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

        ImageButton backButton = new ImageButton(new TextureRegionDrawable(
            new TextureRegion(new Texture("ui/backicon.png"))));
        backButton.setSize(50, 50);
        backButton.getImage().setScaling(Scaling.fit);
        backButton.getImageCell().size(50, 50);
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
                showSortDialog();
            }
        });

        Table header = new Table();
        header.pad(10).top();

        header.add(backButton).size(50, 50).padRight(10).left();
        Label titleLabel = new Label("CARD SELECTION", skin);
        titleLabel.setColor(Color.WHITE);
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

        BitmapFont whiteFont = new BitmapFont(Gdx.files.internal("ui/smalligator_white.fnt"));
        BitmapFont yellowFont = new BitmapFont(Gdx.files.internal("ui/smalligator_yellow.fnt"));

        Label.LabelStyle selectButtonStyle = new Label.LabelStyle();
        selectButtonStyle.font = whiteFont;

        final Label selectButton = new Label("SELECT", selectButtonStyle);
        selectButton.setColor(Color.WHITE);

        selectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedCards.size() > 0) {
                    System.out.println("Selected Deck:");
                    for (CardSystem.Card c : selectedCards)
                        System.out.println("- " + c.getName());
                    game.setScreen(new GameMap1Screen(game));
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                selectButtonStyle.font = yellowFont;
                selectButton.setStyle(selectButtonStyle);
                selectButton.setColor(Color.YELLOW);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                selectButtonStyle.font = whiteFont;
                selectButton.setStyle(selectButtonStyle);
                selectButton.setColor(Color.WHITE);
            }
        });

        root.add(selectButton).padTop(20).colspan(3);
    }

    private void rebuildCardTable() {
        cardTable.clear();
        cardContainers.clear();

        int columns = 4;
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
            imageContainer.size(190, 280);

            cardGroup.addActor(imageContainer);

            Container<VerticalGroup> container = new Container<>(cardGroup);
            container.setBackground(skin.newDrawable("white", Color.DARK_GRAY));
            container.width(190).height(280).pad(10);

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

            cardTable.add(container).pad(10);

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

    private void showSortDialog() {
        Dialog dialog = new Dialog("Sort Cards", skin);

        final SelectBox<String> fieldSelect = new SelectBox<>(skin);
        fieldSelect.setItems("HP", "Name", "Pantheon", "Card Type");

        final SelectBox<String> methodSelect = new SelectBox<>(skin);
        methodSelect.setItems("Bubble Sort", "Insertion Sort", "Merge Sort");

        final SelectBox<String> orderSelect = new SelectBox<>(skin);
        orderSelect.setItems("Ascending", "Descending");

        final SelectBox<String> pantheonValueSelect = new SelectBox<>(skin);
        pantheonValueSelect.setItems("GREEK", "ROMAN");
        pantheonValueSelect.setVisible(false);

        final SelectBox<String> typeValueSelect = new SelectBox<>(skin);
        typeValueSelect.setItems("GOD", "DIVINE", "ITEM", "ARTIFACT");
        typeValueSelect.setVisible(false);

        fieldSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selected = fieldSelect.getSelected();
                pantheonValueSelect.setVisible("Pantheon".equals(selected));
                typeValueSelect.setVisible("Card Type".equals(selected));
            }
        });

        TextButton applyBtn = new TextButton("Apply Sort", skin);
        applyBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String field = fieldSelect.getSelected();
                String method = methodSelect.getSelected();
                boolean ascending = orderSelect.getSelected().equals("Ascending");

                currentPantheonFilter = pantheonValueSelect.isVisible() ? pantheonValueSelect.getSelected() : null;
                currentTypeFilter = typeValueSelect.isVisible() ? typeValueSelect.getSelected() : null;

                sortCards(field, method, ascending);
                dialog.hide();
            }
        });

        TextButton clearBtn = new TextButton("Clear Filters", skin);
        clearBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                currentPantheonFilter = null;
                currentTypeFilter = null;
                allCards = CardSystem.loadCardsFromJson();
                rebuildCardTable();
                dialog.hide();
            }
        });

        Table table = new Table();
        table.add(new Label("Sort Field: ", skin)).left();
        table.add(fieldSelect).row();
        table.add(new Label("Sort Method: ", skin)).left();
        table.add(methodSelect).row();
        table.add(new Label("Order: ", skin)).left();
        table.add(orderSelect).row();

        table.add(new Label("Pantheon Value:", skin)).left();
        table.add(pantheonValueSelect).row();

        table.add(new Label("Card Type Value:", skin)).left();
        table.add(typeValueSelect).row();

        table.add(applyBtn).padTop(10);
        table.add(clearBtn).padTop(10);

        dialog.getContentTable().add(table);
        dialog.button("Cancel");
        dialog.show(stage);
    }

    private void sortCards(String field, String method, boolean ascending) {
        Comparator<CardSystem.Card> comparator;
        List<CardSystem.Card> filtered = new ArrayList<>(CardSystem.loadCardsFromJson());

        if (currentPantheonFilter != null) {
            filtered.removeIf(card -> !card.getPantheon().name().equalsIgnoreCase(currentPantheonFilter));
        }

        if (currentTypeFilter != null) {
            filtered.removeIf(card -> !card.getType().name().equalsIgnoreCase(currentTypeFilter));
        }

        switch (field) {
            case "HP":
                comparator = Comparator.comparingInt(CardSystem.Card::getHealth);
                break;
            case "Name":
                comparator = Comparator.comparing(CardSystem.Card::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Pantheon":
                comparator = Comparator.comparing(card -> card.getPantheon().name(), String.CASE_INSENSITIVE_ORDER);
                break;
            case "Card Type":
                comparator = Comparator.comparing(card -> card.getType().name(), String.CASE_INSENSITIVE_ORDER);
                break;
            default:
                throw new IllegalArgumentException("Unknown field: " + field);
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        switch (method) {
            case "Bubble Sort":
                bubbleSort(filtered, comparator);
                break;
            case "Insertion Sort":
                insertionSort(filtered, comparator);
                break;
            case "Merge Sort":
                filtered.sort(comparator);
                break;
            default:
                throw new IllegalArgumentException("Unknown sort method: " + method);
        }

        allCards = filtered;
        updateCardDisplay();
    }

    private void bubbleSort(List<CardSystem.Card> list, Comparator<CardSystem.Card> comparator) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (comparator.compare(list.get(j), list.get(j + 1)) > 0) {
                    Collections.swap(list, j, j + 1);
                }
            }
        }
    }

    private void insertionSort(List<CardSystem.Card> list, Comparator<CardSystem.Card> comparator) {
        for (int i = 1; i < list.size(); i++) {
            CardSystem.Card key = list.get(i);
            int j = i - 1;
            while (j >= 0 && comparator.compare(list.get(j), key) > 0) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }

    private void updateCardDisplay() {
        rebuildCardTable();
    }
}
