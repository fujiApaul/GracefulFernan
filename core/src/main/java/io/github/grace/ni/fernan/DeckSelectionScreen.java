package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.List;

public class DeckSelectionScreen implements Screen {
    public interface ReturnCallback { void goBack(); }

    private final FernansGrace game;
    private final SaveProfile profile;
    private final List<Deck> decks;
    private final ReturnCallback onBack;
    private int focusedIndex = 0;

    private Stage stage;
    private Skin skin;
    private BitmapFont titleFont, normalFont, hoverFont;
    private Texture backgroundTex;
    private Image backgroundImage, deckImage;
    private Label deckNameLabel;

    public DeckSelectionScreen(FernansGrace game,
                               SaveProfile profile,
                               ReturnCallback onBack) {
        this.game    = game;
        this.profile = profile;
        this.decks   = profile.decks;
        this.onBack  = onBack;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin       = new Skin(Gdx.files.internal("ui/uiskin.json"));
        titleFont  = new BitmapFont(Gdx.files.internal("ui/smalligator_white.fnt"));
        normalFont = new BitmapFont(Gdx.files.internal("ui/smalligator_white.fnt"));
        hoverFont  = new BitmapFont(Gdx.files.internal("ui/smalligator_gradient2.fnt"));

        backgroundTex   = new Texture(Gdx.files.internal("BG2B.png"));
        backgroundImage = new Image(new TextureRegionDrawable(new TextureRegion(backgroundTex)));
        backgroundImage.setFillParent(true);

        buildUI();
        updateFocusedDeck();
    }

    private void buildUI() {
        stage.clear();
        stage.addActor(backgroundImage);

        deckImage = new Image();
        deckImage.setScaling(Scaling.fit);
        deckImage.setSize(200, 280);

        deckNameLabel = new Label("", new Label.LabelStyle(normalFont, null));
        deckNameLabel.setAlignment(Align.center);
        deckNameLabel.setFontScale(1f);

        Texture transTex = new Texture(Gdx.files.internal("ui/transparent.png"));
        Drawable up = new TextureRegionDrawable(new TextureRegion(transTex));
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up   = up;
        btnStyle.down = up;
        btnStyle.over = up;
        btnStyle.font = normalFont;
        TextButton.TextButtonStyle btnHover = new TextButton.TextButtonStyle(btnStyle);
        btnHover.font = hoverFont;

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Top row: back, title, spacer
        ImageButton backBtn = new ImageButton(new TextureRegionDrawable(
            new TextureRegion(new Texture("ui/backicon.png"))));
        backBtn.setSize(30,30);
        backBtn.addListener(new ClickListener(){
            @Override public void clicked(InputEvent e, float x, float y) {
                onBack.goBack();
            }
        });
        Label title = new Label("FERNAN'S GRACE", new Label.LabelStyle(titleFont, null));
        title.setFontScale(2f);
        Label spacer = new Label("", new Label.LabelStyle(normalFont, null));

        root.top().padTop(5);
        root.add(backBtn).size(60,60).padLeft(10).left();
        root.add(title).expandX().center();
        root.add(spacer).padRight(50).right();
        root.row();

        // Carousel
        Texture deckBgTex = new Texture(Gdx.files.internal("ui/deck_background.png"));
        Image deckBg = new Image(new TextureRegion(deckBgTex));
        deckBg.setSize(300,300);
        Stack deckStack = new Stack();
        deckStack.setSize(240,300);
        deckStack.add(deckBg);
        Table inner = new Table();
        inner.add(deckImage).size(200,210).padTop(5).row();
        inner.add(deckNameLabel);
        deckStack.add(inner);

        Texture arrowL = new Texture(Gdx.files.internal("ui/arrow_left.png"));
        Texture arrowR = new Texture(Gdx.files.internal("ui/arrow_right.png"));
        ImageButton leftArrow = new ImageButton(new TextureRegionDrawable(new TextureRegion(arrowL)));
        ImageButton rightArrow= new ImageButton(new TextureRegionDrawable(new TextureRegion(arrowR)));
        leftArrow.setSize(24,24);
        rightArrow.setSize(24,24);
        leftArrow.addListener(new ClickListener(){
            @Override public void clicked(InputEvent e, float x, float y) {
                focusedIndex = (focusedIndex + decks.size() - 1) % decks.size();
                updateFocusedDeck();
            }
        });
        rightArrow.addListener(new ClickListener(){
            @Override public void clicked(InputEvent e, float x, float y) {
                focusedIndex = (focusedIndex + 1) % decks.size();
                updateFocusedDeck();
            }
        });

        Table carousel = new Table();
        carousel.center();
        carousel.add(leftArrow).size(24,24).pad(5);
        carousel.add(deckStack).pad(5);
        carousel.add(rightArrow).size(24,24).pad(5);
        root.add(carousel).colspan(3).center().size(300,300).row();

        // Create New Deck
        TextButton createDeck = new TextButton("Create New Deck", btnStyle);
        createDeck.addListener(new ClickListener(){
            @Override public void enter(InputEvent e, float x, float y, int p, Actor from) {
                createDeck.setStyle(btnHover);
            }
            @Override public void exit(InputEvent e, float x, float y, int p, Actor to) {
                createDeck.setStyle(btnStyle);
            }
            @Override public void clicked(InputEvent e, float x, float y) {
                if (decks.size() >= 3) {
                    new Dialog("Limit Reached", skin)
                        .text("You can only have up to 3 decks.")
                        .button("OK").show(stage);
                } else {
                    TextField tf = new TextField("", skin);
                    tf.setMessageText("Deck name");
                    Dialog d = new Dialog("New Deck", skin) {
                        @Override protected void result(Object obj) {
                            if (Boolean.TRUE.equals(obj)) {
                                String name = tf.getText().trim();
                                if (name.isEmpty()) name = "Deck " + (decks.size()+1);
                                Deck newDeck = new Deck(name, new ArrayList<>());
                                decks.add(newDeck);
                                focusedIndex = decks.size()-1;
                                updateFocusedDeck();
                                SaveManager.saveProfile(profile);
                            }
                        }
                    };
                    d.getContentTable().add(tf).width(300).row();
                    d.button("Create", true).button("Cancel", false);
                    d.show(stage);
                }
            }
        });
        root.add(createDeck).colspan(3).center().size(20,40).row();

        // Buttons: Make Active, Customize
        TextButton makeActive = new TextButton("Make Deck Active", btnStyle);
        makeActive.addListener(new ClickListener(){
            @Override public void enter(InputEvent e, float x, float y, int p, Actor from) {
                makeActive.setStyle(btnHover);
            }
            @Override public void exit(InputEvent e, float x, float y, int p, Actor to) {
                makeActive.setStyle(btnStyle);
            }
            @Override public void clicked(InputEvent e, float x, float y) {
                decks.get(focusedIndex).setActive(true);
                SaveManager.saveProfile(profile);
            }
        });
        TextButton customize = new TextButton("Customize Deck", btnStyle);
        customize.addListener(new ClickListener(){
            @Override public void enter(InputEvent e, float x, float y, int p, Actor from) {
                customize.setStyle(btnHover);
            }
            @Override public void exit(InputEvent e, float x, float y, int p, Actor to) {
                customize.setStyle(btnStyle);
            }
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new DeckCustomizationScreen(
                    game,
                    profile,
                    decks.get(focusedIndex),
                    () -> game.setScreen(DeckSelectionScreen.this)
                ));
            }
        });
        root.add(makeActive).colspan(3).center().size(20,40).row();
        root.add(customize).colspan(3).center().size(20,40).row();
    }

    private void updateFocusedDeck() {
        Deck d = decks.get(focusedIndex);
        deckImage.setDrawable(new TextureRegionDrawable(
            new TextureRegion(new Texture(d.getCoverImagePath()))
        ));
        deckNameLabel.setText(d.getName());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }
    @Override public void render(float delta) {
        ScreenUtils.clear(0,0,0,1);
        stage.act(delta);
        stage.draw();
    }
    @Override public void resize(int w,int h) { stage.getViewport().update(w,h,true); }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}
    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
        titleFont.dispose();
        normalFont.dispose();
        hoverFont.dispose();
        backgroundTex.dispose();
    }

    // simple Deck model
    public static class Deck {
        private String name;
        private List<CardSystem.Card> cards;
        private boolean active;
        public Deck() { this.name=""; this.cards=new ArrayList<>(); }
        public Deck(String name, List<CardSystem.Card> cards) {
            this.name  = name;
            this.cards = cards;
        }
        public String getName() { return name; }
        public void setName(String n) { name = n; }
        public List<CardSystem.Card> getCards() { return cards; }
        public void setCards(List<CardSystem.Card> c) { cards = c; }
        public String getCoverImagePath() {
            return cards.isEmpty()
                ? "ui/card_slot_empty.png"
                : cards.get(0).getImagePath();
        }
        public void setActive(boolean a) { active = a; }
        public boolean isActive() { return active; }
    }
}
