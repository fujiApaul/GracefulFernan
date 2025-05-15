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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.audio.Sound;

public class StoreScreenFernan implements Screen {
    final FernansGrace game;
    private final SaveProfile profile;
    private Stage stage;
    private Image background;
    private Array<StoreItemDisplay> storeItemDisplays; // Changed from StoreItem to StoreItemDisplay
    private Table[] itemCards = new Table[3]; // Visual card elements
    private Table mainTable;

    private BitmapFont yellowFont;
    private BitmapFont whiteFont;
    private Skin skin;

    private Sound clickSound;

    // Represents the static information about a store offering
    static class StoreItemDisplay {
        String name; // e.g., "GOD/DIVINE PACK"
        String description; // Displayed on PackScreenFernan
        Texture texture; // Visual texture for the store item
        PackOpenScreenFernan.PackType packType; // Link to the actual pack type

        // packsOwned label will be part of the UI element, not this data object
        Label packsOwnedLabel; // Label to display packs owned, part of the UI card

        public StoreItemDisplay(String name, String description, Texture texture, PackOpenScreenFernan.PackType packType) {
            this.name = name;
            this.description = description;
            this.texture = texture;
            this.packType = packType;
        }
    }

    public StoreScreenFernan(final FernansGrace game, SaveProfile profile) {
        this.game = game;
        this.profile = profile;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        yellowFont = new BitmapFont(Gdx.files.internal("ui/smalligator_yellow.fnt"));
        yellowFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        whiteFont = new BitmapFont(Gdx.files.internal("ui/smalligator_gradient2.fnt"));
        whiteFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        clickSound = Gdx.audio.newSound(Gdx.files.internal("Click.mp3"));

        background = new Image(new Texture("Bg2B.PNG"));
        background.setFillParent(true);
        stage.addActor(background);

        storeItemDisplays = new Array<>();
        // Define the store offerings with their corresponding PackType
        storeItemDisplays.add(new StoreItemDisplay("GOD/DIVINE PACK",
            "\nThis pack contains a god or divine card with chances:\n\n" +
                "15% chance for a God Card\n" +
                "85% chance for a Divine Card",
            new Texture("cards/zeus.png"), PackOpenScreenFernan.PackType.GOD_DIVINE));

        storeItemDisplays.add(new StoreItemDisplay("DIVINE PACK", // Changed name for clarity
            "\nThis pack contains one divine card.\n\n" +
                "100% chance for a Divine Card",
            new Texture("cards/ananke.png"), PackOpenScreenFernan.PackType.DIVINE)); // Assuming Ananke is Divine for texture

        storeItemDisplays.add(new StoreItemDisplay("ARTIFACT/ITEM PACK",
            "\nThis pack contains an artifact or item card with chances:\n\n" +
                "50% chance for an Artifact Card\n" +
                "50% chance for an Item Card",
            new Texture("items/goldenapple.png"), PackOpenScreenFernan.PackType.ARTIFACT_ITEM));

        mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        for (int i = 0; i < storeItemDisplays.size && i < itemCards.length; i++) {
            itemCards[i] = createItemCardUI(i); // Renamed for clarity
        }

        layoutUI();
        createBackButton(); // Moved back button creation here
    }

    private void createBackButton() {
        TextButton.TextButtonStyle backButtonStyle = new TextButton.TextButtonStyle();
        Drawable transparentDrawable = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("ui/transparent.png"))));
        backButtonStyle.up = transparentDrawable;
        backButtonStyle.down = transparentDrawable;
        backButtonStyle.over = transparentDrawable;
        backButtonStyle.font = yellowFont;

        TextButton backButton = new TextButton("Back", backButtonStyle);
        backButton.getLabel().setFontScale(1.5f);

        backButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                backButton.getLabel().setStyle(new Label.LabelStyle(whiteFont, Color.WHITE));
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                backButton.getLabel().setStyle(new Label.LabelStyle(yellowFont, Color.YELLOW));
            }
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.setScreen(new GameMenuFernan(game, profile));
            }
        });
        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top().left().padTop(10).padLeft(10);
        topTable.add(backButton).width(100).height(40);
        stage.addActor(topTable);
    }


    private Table createItemCardUI(int index) {
        final StoreItemDisplay itemDisplay = storeItemDisplays.get(index);

        Table cardContainer = new Table();
        cardContainer.defaults().padBottom(10);
        cardContainer.setTouchable(Touchable.enabled);

        Image image = new Image(itemDisplay.texture);
        image.setColor(Color.WHITE);

        Table textTable = new Table();
        textTable.top().pad(10);

        Label nameLabel = new Label(itemDisplay.name, new Label.LabelStyle(yellowFont, Color.WHITE));
        nameLabel.setFontScale(0.8f);
        nameLabel.setWrap(true);
        nameLabel.setAlignment(Align.center);

        // Initialize the packsOwnedLabel for this item display
        itemDisplay.packsOwnedLabel = new Label("Packs Owned: 0", new Label.LabelStyle(yellowFont, Color.WHITE));
        itemDisplay.packsOwnedLabel.setFontScale(0.7f);
        itemDisplay.packsOwnedLabel.setWrap(true);
        itemDisplay.packsOwnedLabel.setAlignment(Align.center);
        updatePacksOwnedLabel(itemDisplay); // Update with actual count from profile

        textTable.add(nameLabel).width(280).padBottom(10).row();
        textTable.add(itemDisplay.packsOwnedLabel).width(280).padTop(20).row();

        Stack stack = new Stack();
        stack.add(image);
        stack.add(textTable);

        cardContainer.add(stack).width(250).height(350).row();

        cardContainer.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                // Pass necessary info to PackScreenFernan
                // The 'packsOwned' for MutableStoreItem should now be sourced from profile for display consistency
                // or just pass the packType and let PackScreenFernan handle fetching counts.
                int currentOwned = 0;
                if (profile != null) {
                    switch (itemDisplay.packType) {
                        case GOD_DIVINE:    currentOwned = profile.godDivinePacks; break;
                        case DIVINE:        currentOwned = profile.divinePacks;    break;
                        case ARTIFACT_ITEM: currentOwned = profile.artifactItemPacks; break;
                    }
                }

                game.setScreen(new PackScreenFernan(game, profile,
                    new PackScreenFernan.PackScreenItemDetails( // Use a dedicated class for details
                        itemDisplay.name,
                        itemDisplay.description,
                        itemDisplay.texture, // Pass texture, not path
                        itemDisplay.packType,
                        currentOwned // Pass the current owned count from profile
                    ),
                    StoreScreenFernan.this // Pass this screen as the previous screen
                ));
            }
        });
        return cardContainer;
    }

    private void updatePacksOwnedLabel(StoreItemDisplay itemDisplay) {
        if (itemDisplay == null || itemDisplay.packsOwnedLabel == null || profile == null) return;
        int count = 0;
        switch (itemDisplay.packType) {
            case GOD_DIVINE:    count = profile.godDivinePacks; break;
            case DIVINE:        count = profile.divinePacks;    break;
            case ARTIFACT_ITEM: count = profile.artifactItemPacks; break;
        }
        itemDisplay.packsOwnedLabel.setText("Packs Owned: " + count);
    }


    private void layoutUI() {
        mainTable.clear();
        Table cardRow = new Table();
        for (int i = 0; i < storeItemDisplays.size && i < itemCards.length; i++) {
            if (itemCards[i] != null) { // Ensure itemCard was created
                cardRow.add(itemCards[i]).pad(20);
            }
        }
        mainTable.add(cardRow).colspan(storeItemDisplays.size).padTop(60).padBottom(40).row();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        // When the screen is shown, refresh all item card pack counts
        for (int i = 0; i < storeItemDisplays.size && i < itemCards.length; i++) {
            if (itemCards[i] != null) { // Ensure itemCard was created
                updatePacksOwnedLabel(storeItemDisplays.get(i));
            }
        }
    }
    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
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
        if (background != null && background.getDrawable() != null && background.getDrawable() instanceof TextureRegionDrawable) {
            TextureRegionDrawable drawable = (TextureRegionDrawable) background.getDrawable();
            if (drawable.getRegion() != null && drawable.getRegion().getTexture() != null) {
                // drawable.getRegion().getTexture().dispose(); // Dispose only if uniquely owned
            }
        }
        for (StoreItemDisplay item : storeItemDisplays) {
            if (item.texture != null) {
                // item.texture.dispose(); // Dispose only if uniquely owned by this screen
            }
        }
        if (yellowFont != null) yellowFont.dispose();
        if (whiteFont != null) whiteFont.dispose();
        if (clickSound != null) clickSound.dispose();
        if (skin != null) skin.dispose();
    }
}
