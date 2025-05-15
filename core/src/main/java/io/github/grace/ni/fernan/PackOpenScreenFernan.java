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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PackOpenScreenFernan implements Screen {

    public enum PackType {
        GOD_DIVINE,
        DIVINE,
        ARTIFACT_ITEM,
        GENERIC // A default or for packs whose type isn't specified by caller
    }

    private final FernansGrace game;
    private final SaveProfile profile;
    private final int numberOfCardsToDisplay;
    private final PackType packTypeToOpen;
    private final Screen previousScreen; // To know where to go back to

    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    private Image backgroundImage;
    private List<CardSystem.Card> allCardsFromSystem;
    private BitmapFont yellowFont;
    private BitmapFont whiteFont;
    private Sound clickSound;
    private Label packTypeLabel;

    // Main constructor
    public PackOpenScreenFernan(FernansGrace game, SaveProfile profile, int numberOfCardsToDisplay, PackType packTypeToOpen, Screen previousScreen) {
        this.game = game;
        this.profile = profile;
        this.numberOfCardsToDisplay = numberOfCardsToDisplay;
        this.packTypeToOpen = packTypeToOpen;
        this.previousScreen = previousScreen; // Store the previous screen

        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        this.allCardsFromSystem = CardSystem.loadCardsFromJson();

        this.yellowFont = new BitmapFont(Gdx.files.internal("ui/smalligator_yellow.fnt"));
        this.whiteFont = new BitmapFont(Gdx.files.internal("ui/smalligator_gradient2.fnt"));
        this.clickSound = Gdx.audio.newSound(Gdx.files.internal("Click.mp3"));

        buildUI();
    }

    // Overloaded constructor that defaults previousScreen to StoreScreenFernan (if profile is available)
    // This might be used by older code or simple store pack openings.
    public PackOpenScreenFernan(FernansGrace game, SaveProfile profile, int numberOfCardsToDisplay, PackType packTypeToOpen) {
        this(game, profile, numberOfCardsToDisplay, packTypeToOpen, new StoreScreenFernan(game, profile));
        Gdx.app.log("PackOpenScreenFernan", "Warning: previousScreen not specified, defaulting to StoreScreenFernan for pack type: " + packTypeToOpen);
    }

    // Constructor for when pack type is also not specified (e.g. from simple store call)
    // THIS IS THE ONE THAT WAS LIKELY CALLED BY PackScreenFernan from store if it didn't pass a type
    public PackOpenScreenFernan(FernansGrace game, SaveProfile profile, int numberOfCardsToDisplay, Screen previousScreenContext) {
        this(game, profile, numberOfCardsToDisplay, PackType.GENERIC, previousScreenContext);
        Gdx.app.log("PackOpenScreenFernan", "Warning: PackType not specified, defaulting to GENERIC. previousScreen: " + previousScreenContext.getClass().getSimpleName());
    }


    private void buildUI() {
        stage.clear();

        backgroundTexture = new Texture(Gdx.files.internal("Bg2B.PNG"));
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        String packNameDisplay = "Opening Pack";
        switch (packTypeToOpen) {
            case GOD_DIVINE: packNameDisplay = "Opening God/Divine Pack"; break;
            case DIVINE: packNameDisplay = "Opening Divine Pack"; break;
            case ARTIFACT_ITEM: packNameDisplay = "Opening Artifact/Item Pack"; break;
            case GENERIC: packNameDisplay = "Opening Generic Pack"; break;
        }
        packTypeLabel = new Label(packNameDisplay, new Label.LabelStyle(whiteFont, Color.YELLOW));
        packTypeLabel.setFontScale(1.2f);
        packTypeLabel.setAlignment(Align.center);

        Table cardDisplayTable = new Table();
        int cardsPerRow = Math.min(numberOfCardsToDisplay, 5);

        if (allCardsFromSystem.isEmpty()) {
            Label errorLabel = new Label("Error: No cards available in the game system.", new Label.LabelStyle(whiteFont, Color.RED));
            cardDisplayTable.add(errorLabel).colspan(cardsPerRow);
            System.err.println("PackOpenScreenFernan: allCardsFromSystem is empty.");
        } else {
            List<CardSystem.Card> openedCards = generateCardsForPack();

            for (int i = 0; i < openedCards.size(); i++) {
                CardSystem.Card newCard = openedCards.get(i);
                if (profile != null) { // Ensure profile is not null before adding
                    profile.addToCollection(newCard);
                } else {
                    Gdx.app.error("PackOpenScreen", "Profile is null, cannot add card to collection.");
                }
                addCardToTable(newCard, cardDisplayTable);
                if ((i + 1) % cardsPerRow == 0 && i < openedCards.size() - 1) {
                    cardDisplayTable.row();
                }
            }
            if (profile != null) { // Ensure profile is not null before saving
                SaveManager.saveProfile(profile);
                System.out.println("Opened " + openedCards.size() + " cards from " + packTypeToOpen + " pack and saved to profile.");
            }
        }

        root.add(packTypeLabel).padBottom(20).row();
        root.add(cardDisplayTable).center().row();

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
                if (previousScreen != null) {
                    game.setScreen(previousScreen);
                } else {
                    // Fallback if previousScreen is somehow null, go to a safe default like Store or GameMenu
                    Gdx.app.log("PackOpenScreenFernan", "Previous screen was null, returning to StoreScreenFernan as default.");
                    game.setScreen(new StoreScreenFernan(game, profile));
                }
            }
        });

        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top().left().padTop(10).padLeft(10);
        topTable.add(backButton).width(100).height(40);
        stage.addActor(topTable);
    }

    private List<CardSystem.Card> generateCardsForPack() {
        List<CardSystem.Card> cardsInPack = new ArrayList<>();
        if (allCardsFromSystem.isEmpty()) return cardsInPack;

        for (int i = 0; i < numberOfCardsToDisplay; i++) {
            CardSystem.Card chosenCard = null;
            List<CardSystem.Card> potentialCards;

            switch (packTypeToOpen) {
                case GOD_DIVINE:
                    boolean isGod = MathUtils.random() < 0.15f;
                    if (isGod) {
                        potentialCards = allCardsFromSystem.stream()
                            .filter(c -> c.getType() == CardSystem.CardType.GOD)
                            .collect(Collectors.toList());
                        if (potentialCards.isEmpty()) {
                            potentialCards = allCardsFromSystem.stream()
                                .filter(c -> c.getType() == CardSystem.CardType.DIVINE)
                                .collect(Collectors.toList());
                        }
                    } else {
                        potentialCards = allCardsFromSystem.stream()
                            .filter(c -> c.getType() == CardSystem.CardType.DIVINE)
                            .collect(Collectors.toList());
                    }
                    if (potentialCards.isEmpty() && !allCardsFromSystem.isEmpty()) {
                        potentialCards = new ArrayList<>(allCardsFromSystem);
                        Gdx.app.log("PackOpenScreen", "Warning: Could not find enough GOD/DIVINE cards, using random from all cards for God/Divine pack.");
                    }
                    break;

                case DIVINE:
                    potentialCards = allCardsFromSystem.stream()
                        .filter(c -> c.getType() == CardSystem.CardType.DIVINE)
                        .collect(Collectors.toList());
                    if (potentialCards.isEmpty() && !allCardsFromSystem.isEmpty()) {
                        potentialCards = new ArrayList<>(allCardsFromSystem);
                        Gdx.app.log("PackOpenScreen", "Warning: Could not find any DIVINE cards, using random from all cards for Divine pack.");
                    }
                    break;

                case ARTIFACT_ITEM:
                    boolean isArtifact = MathUtils.random() < 0.50f;
                    if (isArtifact) {
                        potentialCards = allCardsFromSystem.stream()
                            .filter(c -> c.getType() == CardSystem.CardType.ARTIFACT)
                            .collect(Collectors.toList());
                        if (potentialCards.isEmpty()) {
                            potentialCards = allCardsFromSystem.stream()
                                .filter(c -> c.getType() == CardSystem.CardType.ITEM)
                                .collect(Collectors.toList());
                        }
                    } else {
                        potentialCards = allCardsFromSystem.stream()
                            .filter(c -> c.getType() == CardSystem.CardType.ITEM)
                            .collect(Collectors.toList());
                    }
                    if (potentialCards.isEmpty() && !allCardsFromSystem.isEmpty()) {
                        potentialCards = new ArrayList<>(allCardsFromSystem);
                        Gdx.app.log("PackOpenScreen", "Warning: Could not find enough ARTIFACT/ITEM cards, using random from all cards for Artifact/Item pack.");
                    }
                    break;
                case GENERIC: // Fallthrough for generic or unspecified packs
                default:
                    potentialCards = new ArrayList<>(allCardsFromSystem); // Use all cards
                    Gdx.app.log("PackOpenScreen", "Opening GENERIC pack type. Using all available cards.");
                    break;
            }

            if (potentialCards != null && !potentialCards.isEmpty()) {
                Collections.shuffle(potentialCards); // Shuffle the filtered list before picking
                chosenCard = potentialCards.get(0); // Get the first one after shuffle
            } else if (!allCardsFromSystem.isEmpty()) {
                Gdx.app.log("PackOpenScreen", "Fallback - No specific cards for pack type " + packTypeToOpen + " or pool was empty. Picking random card from all available.");
                List<CardSystem.Card> fallbackPool = new ArrayList<>(allCardsFromSystem);
                Collections.shuffle(fallbackPool);
                if(!fallbackPool.isEmpty()){
                    chosenCard = fallbackPool.get(0);
                }
            }

            if (chosenCard != null) {
                cardsInPack.add(new CardSystem.Card(chosenCard)); // Add a copy
            }
        }
        return cardsInPack;
    }


    private void addCardToTable(CardSystem.Card card, Table table) {
        Texture cardTexture = null;
        String imagePath = card.getImagePath();
        if (imagePath == null || imagePath.trim().isEmpty()) {
            Gdx.app.error("PackOpenScreen", "Card " + card.getName() + " has null or empty imagePath. Using placeholder.");
            imagePath = "ui/card_slot_empty.png"; // Fallback to a default placeholder
        }

        try {
            cardTexture = new Texture(Gdx.files.internal(imagePath));
        } catch (Exception e) {
            Gdx.app.error("PackOpenScreen", "Error loading texture for card: " + card.getName() + " at path " + imagePath + " - " + e.getMessage());
            try {
                cardTexture = new Texture(Gdx.files.internal("ui/card_slot_empty.png")); // Ensure this placeholder exists
            } catch (Exception e2) {
                Gdx.app.error("PackOpenScreen", "CRITICAL: Failed to load even the placeholder card texture: " + e2.getMessage());
                // Cannot create an image if texture is null, so we might skip adding this card image
                return;
            }
        }

        Image cardImage = new Image(cardTexture);
        cardImage.setScaling(Scaling.fit);
        float cardWidth = 150f;
        float cardHeight = cardWidth * 1.4f;
        table.add(cardImage).size(cardWidth, cardHeight).pad(10);
    }

    @Override public void show() {}

    @Override public void render(float delta) {
        ScreenUtils.clear(0,0,0,1);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
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
        if (skin != null) skin.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (yellowFont != null) yellowFont.dispose();
        if (whiteFont != null) whiteFont.dispose();
        if (clickSound != null) clickSound.dispose();
    }
}
