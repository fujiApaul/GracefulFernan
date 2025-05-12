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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.*;
import java.util.List;

public class DeckBuilderScreen implements Screen {
    public interface ReturnCallback { void goBack(); }

    private final FernansGrace game;
    private final SaveProfile profile;
    private final DeckSelectionScreen.Deck deck;
    private final ReturnCallback onBack;
    private static final int MAX_CARDS = 20;

    private Stage stage;
    private Skin  skin;
    private List<CardSystem.Card> allCards;
    private List<CardSystem.Card> selected;
    // after your `private List<CardSystem.Card> allCards;`
    private Set<String> selectedIds;
    private Table cardTable;
    private Texture bgTex;
    private Image   bgImg;

    private BitmapFont fontWhite, fontYellow, fontHover;

    public DeckBuilderScreen(FernansGrace game,
                             SaveProfile profile,
                             DeckSelectionScreen.Deck deck,
                             ReturnCallback onBack) {
        this.game    = game;
        this.profile = profile;
        this.deck    = deck;
        this.onBack  = onBack;

        // Pre‐seed selected with the deck’s current cards:
        this.selected = new ArrayList<>(deck.getCards());

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        // Only load the player’s owned cards:
        allCards = new ArrayList<>(profile.getOwnedCards());

        // seed selectedIds from the cards already in deck:
        selectedIds = new HashSet<>();
        for (CardSystem.Card c : deck.getCards()) {
            selectedIds.add(c.getName());
        }
// also seed your `selected` list if you still need it elsewhere:
        selected = new ArrayList<>(deck.getCards());


        bgTex = new Texture(Gdx.files.internal("ui/deckbuilderscreenbg.png"));
        bgImg = new Image(bgTex);

        fontWhite  = new BitmapFont(Gdx.files.internal("ui/smalligator_white.fnt"));
        fontYellow = new BitmapFont(Gdx.files.internal("ui/smalligator_yellow.fnt"));
        fontHover  = new BitmapFont(Gdx.files.internal("ui/smalligator_gradient2.fnt"));

        buildUI();
    }

    private void buildUI() {
        stage.clear();
        bgImg.setFillParent(true);
        stage.addActor(bgImg);

        Table root = new Table(skin);
        root.setFillParent(true);
        stage.addActor(root);

        // Back button
        ImageButton back = new ImageButton(new TextureRegionDrawable(
            new TextureRegion(new Texture("ui/backicon.png"))));
        back.setSize(50,50);
        back.addListener(new ClickListener(){
            @Override public void clicked(InputEvent e, float x, float y) {
                onBack.goBack();
            }
        });

        // Sort button
        ImageButton sort = new ImageButton(new TextureRegionDrawable(
            new TextureRegion(new Texture("ui/sorticon.png"))));
        sort.setSize(50,50);
        sort.addListener(new ClickListener(){
            @Override public void clicked(InputEvent e, float x, float y) {
                showSortDialog();
            }
        });

        Label title = new Label("CARD SELECTION",
            new Label.LabelStyle(fontWhite, Color.WHITE));

        Table header = new Table(skin);
        header.pad(10).top();
        header.add(back).size(50,50).padRight(10);
        header.add(title).expandX().left();
        header.add(sort).size(50,50);
        root.add(header).colspan(4).fillX().row();

        // Card grid
        cardTable = new Table(skin);
        refreshCards();
        ScrollPane sp = new ScrollPane(cardTable, skin);
        sp.setFadeScrollBars(false);
        root.add(sp).expand().fill().colspan(4).pad(10).row();

        // SELECT label with hover font
        final Label select = new Label("SELECT", new Label.LabelStyle(fontWhite, Color.WHITE));
        select.setAlignment(Align.center);
        select.addListener(new ClickListener(){
            @Override public void clicked(InputEvent e, float x, float y) {
                if (!selected.isEmpty()) {
                    deck.getCards().clear();
                    deck.getCards().addAll(selected);
                    SaveManager.saveProfile(profile);
                    onBack.goBack();
                }
            }
            @Override public void enter(InputEvent event, float x, float y, int pointer, Actor from) {
                select.setStyle(new Label.LabelStyle(fontHover, Color.WHITE));
            }
            @Override public void exit(InputEvent event, float x, float y, int pointer, Actor to) {
                select.setStyle(new Label.LabelStyle(fontWhite, Color.WHITE));
            }
        });
        root.add(select).colspan(4).padTop(20);
    }

    private void refreshCards() {
        cardTable.clear();
        int cols = 4, i = 0;

        for (CardSystem.Card c : allCards) {
            // create the image & container
            Image img = new Image(new TextureRegionDrawable(
                new TextureRegion(new Texture(c.getImagePath()))
            ));
            img.setScaling(Scaling.fit);

            // to this:
            Container<Image> cont = new Container<>(img);
            cont.size(190, 280);
            cont.pad(5);
            cont.setTouchable(Touchable.enabled);

            // mark already‐selected cards
            boolean inDeck = selectedIds.contains(c.getName());
            if (inDeck) {
                cont.background(skin.newDrawable("white", Color.YELLOW));
            } else {
                cont.background(skin.newDrawable("white", Color.DARK_GRAY));
            }


            // toggle on click
            cont.addListener(new ClickListener(){
                @Override public void clicked(InputEvent e, float x, float y) {
                    String id = c.getName();
                    if (selectedIds.remove(id)) {
                        // was selected, now removed
                        selected.removeIf(card -> card.getName().equals(id));
                        cont.background(skin.newDrawable("white", Color.DARK_GRAY));
                    } else if (selectedIds.size() < MAX_CARDS) {
                        selectedIds.add(id);
                        selected.add(c);
                        cont.background(skin.newDrawable("white", Color.YELLOW));
                    }
                }
            });


            cardTable.add(cont).pad(10);
            if (++i % cols == 0) cardTable.row();
        }
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(stage);
    }
    @Override public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        stage.draw();
    }
    @Override public void resize(int w, int h) { stage.getViewport().update(w,h,true); }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}
    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
        bgTex.dispose();
        fontWhite.dispose();
        fontYellow.dispose();
        fontHover.dispose();
    }

    /** Copy your existing showSortDialog/bubbleSort/insertionSort here… */
    public void showSortDialog() {
        Dialog dialog = new Dialog("Sort Cards", skin);

        // 1) Field selector
        final SelectBox<String> fieldSelect = new SelectBox<>(skin);
        fieldSelect.setItems("HP", "Name", "Pantheon", "Card Type");

        // 2) Method selector
        final SelectBox<String> methodSelect = new SelectBox<>(skin);
        methodSelect.setItems("Bubble Sort", "Insertion Sort", "Merge Sort");

        // 3) Order selector
        final SelectBox<String> orderSelect = new SelectBox<>(skin);
        orderSelect.setItems("Ascending", "Descending");

        // 4) Build table
        Table table = new Table(skin);
        table.add(new Label("Field:", skin)).left();
        table.add(fieldSelect).row();
        table.add(new Label("Method:", skin)).left();
        table.add(methodSelect).row();
        table.add(new Label("Order:", skin)).left();
        table.add(orderSelect).row();

        // 5) Apply button
        TextButton applyBtn = new TextButton("Apply Sort", skin);
        applyBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String field  = fieldSelect.getSelected();
                String method = methodSelect.getSelected();
                boolean asc   = orderSelect.getSelected().equals("Ascending");

                // choose comparator
                Comparator<CardSystem.Card> cmp;
                switch (field) {
                    case "HP":
                        cmp = Comparator.comparingInt(CardSystem.Card::getHealth);
                        break;
                    case "Name":
                        cmp = Comparator.comparing(CardSystem.Card::getName, String.CASE_INSENSITIVE_ORDER);
                        break;
                    case "Pantheon":
                        cmp = Comparator.comparing(c -> c.getPantheon().name(), String.CASE_INSENSITIVE_ORDER);
                        break;
                    case "Card Type":
                        cmp = Comparator.comparing(c -> c.getType().name(), String.CASE_INSENSITIVE_ORDER);
                        break;
                    default:
                        throw new IllegalStateException("Unknown field: " + field);
                }
                if (!asc) cmp = cmp.reversed();

                // sort
                switch (method) {
                    case "Bubble Sort":
                        bubbleSort(allCards, cmp);
                        break;
                    case "Insertion Sort":
                        insertionSort(allCards, cmp);
                        break;
                    case "Merge Sort":
                        allCards.sort(cmp);
                        break;
                    default:
                        throw new IllegalStateException("Unknown method: " + method);
                }

                dialog.hide();
                refreshCards();
            }
        });

        // 6) Cancel button
        TextButton cancelBtn = new TextButton("Cancel", skin);
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });

        // 7) Lay out buttons
        table.row().padTop(10);
        table.add(applyBtn).padRight(10);
        table.add(cancelBtn);

        dialog.getContentTable().add(table);
        dialog.show(stage);
    }
    private <T> void bubbleSort(List<T> list, Comparator<T> cmp) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 0; j < list.size() - i - 1; j++) {
                if (cmp.compare(list.get(j), list.get(j + 1)) > 0) {
                    Collections.swap(list, j, j + 1);
                }
            }
        }
    }

    private <T> void insertionSort(List<T> list, Comparator<T> cmp) {
        for (int i = 1; i < list.size(); i++) {
            T key = list.get(i);
            int j = i - 1;
            while (j >= 0 && cmp.compare(list.get(j), key) > 0) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }

}
