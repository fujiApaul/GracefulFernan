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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DeckCustomizationScreen implements Screen {
    private static final int MAX_CARDS = 20;

    public interface ReturnCallback { void goBack(); }

    private final FernansGrace game;
    private final SaveProfile profile;
    private final DeckSelectionScreen.Deck deck;
    private final ReturnCallback onBack;

    private Stage stage;
    private Texture texBg;
    private Label nameLabel, countLabel;
    private Container<Actor> nameContainer;
    private Table cardsTable;
    private BitmapFont fontNormal, fontHover, fontBlackAdder;
    private com.badlogic.gdx.scenes.scene2d.ui.Skin skin;

    public DeckCustomizationScreen(FernansGrace game,
                                   SaveProfile profile,
                                   DeckSelectionScreen.Deck deck,
                                   ReturnCallback onBack) {
        this.game    = game;
        this.profile = profile;
        this.deck    = deck;
        this.onBack  = onBack;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin           = new com.badlogic.gdx.scenes.scene2d.ui.Skin(Gdx.files.internal("ui/uiskin.json"));
        fontNormal     = new BitmapFont(Gdx.files.internal("ui/smalligator_white.fnt"));
        fontHover      = new BitmapFont(Gdx.files.internal("ui/smalligator_gradient2.fnt"));
        fontBlackAdder = new BitmapFont(Gdx.files.internal("ui/black_adder.fnt"));
        texBg          = new Texture(Gdx.files.internal("ui/deck_customization_bg.png"));

        buildUI();
    }

    private void buildUI() {
        stage.clear();

        Image bg = new Image(new TextureRegionDrawable(new TextureRegion(texBg)));
        bg.setFillParent(true);
        stage.addActor(bg);

        Table root = new Table(skin);
        root.setFillParent(true);
        root.top().pad(10);
        stage.addActor(root);

        // Back
        ImageButton back = new ImageButton(new TextureRegionDrawable(
            new TextureRegion(new Texture("ui/backicon.png"))));
        back.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                onBack.goBack();
            }
        });
        root.add(back).size(50,50).padRight(10);

        // Name
        nameLabel = new Label(deck.getName(),
            new Label.LabelStyle(fontBlackAdder, Color.BLACK));
        nameContainer = new Container<>(nameLabel);
        nameContainer.background(new TextureRegionDrawable(
            new TextureRegion(new Texture("ui/textfield_bg.png"))));
        nameContainer.pad(10).width(400).height(40);
        root.add(nameContainer).padRight(10);

        // Count
        countLabel = new Label(deck.getCards().size() + "/" + MAX_CARDS,
            new Label.LabelStyle(fontNormal, Color.BLACK));
        Container<Label> countContainer = new Container<>(countLabel);
        countContainer.background(new TextureRegionDrawable(
            new TextureRegion(new Texture("ui/counter_bg.png"))));
        countContainer.pad(5).size(90,50);
        root.add(countContainer).padRight(10);

        // Trash
        ImageButton trash = new ImageButton(new TextureRegionDrawable(
            new TextureRegion(new Texture("ui/trash_button.png"))));
        trash.addListener(new ClickListener(){
            @Override public void clicked(InputEvent e, float x, float y) {
                deck.getCards().clear();
                refreshCards();
                SaveManager.saveProfile(profile);
            }
        });
        root.add(trash).size(50,50).padRight(10);

        // Save (commit name)
        ImageButton save = new ImageButton(new TextureRegionDrawable(
            new TextureRegion(new Texture("ui/save_button.png"))));
        save.addListener(new ClickListener(){
            @Override public void clicked(InputEvent e, float x, float y) {
                Actor a = nameContainer.getActor();
                if (a instanceof TextField) {
                    String newName = ((TextField)a).getText();
                    deck.setName(newName);
                    nameLabel.setText(newName);
                    nameContainer.setActor(nameLabel);
                    SaveManager.saveProfile(profile);
                }
            }
        });
        root.add(save).size(50,50).padRight(10);

        // Rename
        ImageButton rename = new ImageButton(new TextureRegionDrawable(
            new TextureRegion(new Texture("ui/rename_button.png"))));
        rename.addListener(new ClickListener(){
            @Override public void clicked(InputEvent e, float x, float y) {
                TextField.TextFieldStyle base = skin.get(TextField.TextFieldStyle.class);
                TextField.TextFieldStyle tfStyle = new TextField.TextFieldStyle(base);
                tfStyle.font      = fontBlackAdder;
                tfStyle.fontColor = Color.BLACK;
                tfStyle.background = new TextureRegionDrawable(
                    new TextureRegion(new Texture("ui/textfield_bg.png"))
                );
                TextField tf = new TextField(deck.getName(), skin);
                tf.setStyle(tfStyle);
                tf.setMessageText("Enter deck name");
                nameContainer.setActor(tf);
                stage.setKeyboardFocus(tf);
                tf.setTextFieldListener((field, c) -> {
                    if (c == '\n') {
                        String nm = field.getText();
                        deck.setName(nm);
                        nameLabel.setText(nm);
                        nameContainer.setActor(nameLabel);
                        SaveManager.saveProfile(profile);
                    }
                });
            }
        });
        root.add(rename).size(50,50).padRight(10);

        // Sort
        ImageButton sort = new ImageButton(new TextureRegionDrawable(
            new TextureRegion(new Texture("ui/sorticon.png"))));
        sort.addListener(new ClickListener(){
            @Override public void clicked(InputEvent e, float x, float y) {
                showSortDialog();
            }
        });
        root.add(sort).size(50,50).row();

        // Card list
        cardsTable = new Table(skin);
        refreshCards();
        ScrollPane scroll = new ScrollPane(cardsTable, skin);
        scroll.setFadeScrollBars(false);

        root.add(scroll)
            .expand().fill()
            .colspan(7)
            .padTop(60).padBottom(20).row();
    }


    private void refreshCards() {
        cardsTable.clear();
        List<CardSystem.Card> list = deck.getCards();
        int perRow=5, count=0;
        for (CardSystem.Card c: list) {
            Image img = new Image(new TextureRegionDrawable(new TextureRegion(
                new Texture(c.getImagePath())
            )));
            Table cell = new Table(skin);
            cell.add(img).size(150,220).pad(5);
            cell.addListener(new ClickListener(){
                @Override public void clicked(InputEvent e, float x, float y) {
                    list.remove(c);
                    countLabel.setText(list.size() + "/" + MAX_CARDS);
                    refreshCards();
                    SaveManager.saveProfile(profile);
                }
            });
            cardsTable.add(cell);
            if (++count % perRow == 0) cardsTable.row();
        }
        Texture emptyTex = new Texture(Gdx.files.internal("ui/card_slot_empty.png"));
        while (count++ < MAX_CARDS) {
            Image empty = new Image(new TextureRegionDrawable(new TextureRegion(emptyTex)));
            Table cell = new Table(skin);
            cell.add(empty).size(150,220).pad(5);
            cell.addListener(new ClickListener(){
                @Override public void clicked(InputEvent e, float x, float y) {
                    if (list.size() < MAX_CARDS) {
                        game.setScreen(new DeckBuilderScreen(
                            game,
                            profile,
                            deck,
                            () -> game.setScreen(DeckCustomizationScreen.this)
                        ));
                    }
                }
                private void onBack() { buildUI(); }
            });
            cardsTable.add(cell);
            if (count % perRow == 0) cardsTable.row();
        }
        countLabel.setText(list.size() + "/" + MAX_CARDS);
    }

    private void showSortDialog() {
        Dialog dialog = new Dialog("Sort Deck", skin);

        // same three selects as in DeckBuilderScreen
        final SelectBox<String> fieldSelect = new SelectBox<>(skin);
        fieldSelect.setItems("HP", "Name", "Pantheon", "Card Type");

        final SelectBox<String> methodSelect = new SelectBox<>(skin);
        methodSelect.setItems("Bubble Sort", "Insertion Sort", "Merge Sort");

        final SelectBox<String> orderSelect = new SelectBox<>(skin);
        orderSelect.setItems("Ascending", "Descending");

        final SelectBox<String> pantheonValue = new SelectBox<>(skin);
        pantheonValue.setItems("GREEK", "ROMAN");
        pantheonValue.setVisible(false);

        final SelectBox<String> typeValue = new SelectBox<>(skin);
        typeValue.setItems("GOD", "DIVINE", "ITEM", "ARTIFACT");
        typeValue.setVisible(false);

        fieldSelect.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                pantheonValue.setVisible("Pantheon".equals(fieldSelect.getSelected()));
                typeValue.setVisible   ("Card Type".equals(fieldSelect.getSelected()));
            }
        });

        TextButton apply = new TextButton("Apply Sort", skin);
        apply.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                String field = fieldSelect.getSelected();
                String method = methodSelect.getSelected();
                boolean asc = orderSelect.getSelected().equals("Ascending");
                List<CardSystem.Card> list = deck.getCards();

                // filter isn't needed here – just sort the existing list:
                Comparator<CardSystem.Card> cmp;
                switch(field) {
                    case "HP":        cmp = Comparator.comparingInt(CardSystem.Card::getHealth); break;
                    case "Name":      cmp = Comparator.comparing(CardSystem.Card::getName, String.CASE_INSENSITIVE_ORDER); break;
                    case "Pantheon":  cmp = Comparator.comparing(c -> c.getPantheon().name(), String.CASE_INSENSITIVE_ORDER); break;
                    case "Card Type": cmp = Comparator.comparing(c -> c.getType().name(), String.CASE_INSENSITIVE_ORDER); break;
                    default: throw new IllegalArgumentException("Unknown field");
                }
                if (!asc) cmp = cmp.reversed();

                switch(method) {
                    case "Bubble Sort":    bubbleSort(list, cmp); break;
                    case "Insertion Sort": insertionSort(list, cmp); break;
                    case "Merge Sort":     list.sort(cmp); break;
                    default: throw new IllegalArgumentException("Unknown method");
                }

                dialog.hide();
                refreshCards();  // repaint with the newly‐ordered deck
            }
        });

        TextButton clear = new TextButton("Cancel", skin);
        clear.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });

        Table t = new Table(skin);
        t.add(new Label("Field:", skin)).left();
        t.add(fieldSelect).row();
        t.add(new Label("Method:", skin)).left();
        t.add(methodSelect).row();
        t.add(new Label("Order:", skin)).left();
        t.add(orderSelect).row();
        t.add(pantheonValue).padTop(5).row();
        t.add(typeValue).row();
        t.add(apply).padTop(10).padRight(10);
        t.add(clear).padTop(10);

        dialog.getContentTable().add(t);
        dialog.show(stage);
    }

    // Copy the builder’s sort helpers:
    private <T> void bubbleSort(List<T> list, Comparator<T> cmp) {
        for (int i = 0; i < list.size()-1; i++) {
            for (int j = 0; j < list.size()-i-1; j++) {
                if (cmp.compare(list.get(j), list.get(j+1)) > 0) {
                    Collections.swap(list, j, j+1);
                }
            }
        }
    }
    private <T> void insertionSort(List<T> list, Comparator<T> cmp) {
        for (int i = 1; i < list.size(); i++) {
            T key = list.get(i);
            int j = i - 1;
            while (j >= 0 && cmp.compare(list.get(j), key) > 0) {
                list.set(j+1, list.get(j));
                j--;
            }
            list.set(j+1, key);
        }
    }


    @Override
    public void show() {
        // restore input
        Gdx.input.setInputProcessor(stage);
        // update name & count in case they changed
        nameLabel.setText(deck.getName());
        countLabel.setText(deck.getCards().size() + "/" + MAX_CARDS);
        // re-draw the card grid
        refreshCards();
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
        fontNormal.dispose();
        fontHover.dispose();
        fontBlackAdder.dispose();
        texBg.dispose();
    }
}
