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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.audio.Sound;
import io.github.grace.ni.fernan.PackOpenScreenFernan.PackType;

public class PackScreenFernan implements Screen {
    final FernansGrace game;
    private final SaveProfile profile;
    private final Stage stage;
    private final BitmapFont yellowFont;
    private final BitmapFont whiteFont;
    private final PackScreenItemDetails itemDetails; // Use the new details class
    private final Skin skin;
    private Sound clickSound;
    private final Screen previousScreen; // For back navigation

    private Label ownedLabel;
    private Label currencyLabel;

    // New class to hold details for PackScreenFernan
    public static class PackScreenItemDetails {
        public final String name;
        public final String infoText;
        public final Texture texture; // Pass the texture directly
        public final PackOpenScreenFernan.PackType packType; // Type of pack
        public int currentPacksOwnedFromProfile; // How many the player actually owns

        public PackScreenItemDetails(String name, String infoText, Texture texture, PackType packType, int currentPacksOwnedFromProfile) {
            this.name = name;
            this.infoText = infoText;
            this.texture = texture;
            this.packType = packType;
            this.currentPacksOwnedFromProfile = currentPacksOwnedFromProfile;
        }
    }


    public PackScreenFernan(FernansGrace game, SaveProfile profile, PackScreenItemDetails itemDetails, Screen previousScreen) {
        this.game = game;
        this.profile = profile;
        this.itemDetails = itemDetails;
        this.previousScreen = previousScreen;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        this.clickSound = Gdx.audio.newSound(Gdx.files.internal("Click.mp3"));

        yellowFont = new BitmapFont(Gdx.files.internal("ui/smalligator_yellow.fnt"));
        yellowFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        whiteFont = new BitmapFont(Gdx.files.internal("ui/smalligator_gradient2.fnt"));
        whiteFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Image background = new Image(new Texture("Bg2B.PNG"));
        background.setFillParent(true);
        stage.addActor(background);

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        Table leftSideTable = createLeftPanel();
        Table rightCard = createItemCardVisuals(); // Uses itemDetails

        Table layoutRow = new Table();
        layoutRow.add(leftSideTable).expandX().left().pad(10);
        layoutRow.add(rightCard).right().pad(10);
        mainTable.add(layoutRow).expand().fill().row();

        createBackButton();
        updateOwnedLabelAndCurrency();
    }

    private Table createItemCardVisuals() {
        Table cardContainer = new Table();
        cardContainer.defaults().padBottom(10);
        cardContainer.setTouchable(Touchable.disabled);

        Image image = new Image(itemDetails.texture); // Use texture from itemDetails
        image.setColor(Color.WHITE);

        Label name = new Label(itemDetails.name, new Label.LabelStyle(yellowFont, Color.WHITE));
        name.setFontScale(0.8f);
        name.setWrap(true);
        name.setAlignment(Align.center);

        Table textTable = new Table();
        textTable.top().pad(10);
        textTable.add(name).width(280).padBottom(10).row();

        Stack stack = new Stack();
        stack.add(image);
        stack.add(textTable);

        cardContainer.add(stack).width(300).height(400).row();
        return cardContainer;
    }

    private Table createLeftPanel() {
        Table leftSideTable = new Table().top().left().pad(40);

        Label infoLabel = new Label(itemDetails.infoText, new Label.LabelStyle(yellowFont, Color.YELLOW));
        infoLabel.setFontScale(0.6f);
        infoLabel.setWrap(true);
        infoLabel.setAlignment(Align.left);

        // Initialize labels, will be updated by updateOwnedLabelAndCurrency
        ownedLabel = new Label("Packs Owned: 0", new Label.LabelStyle(yellowFont, Color.YELLOW));
        ownedLabel.setFontScale(0.6f);
        ownedLabel.setWrap(true);
        ownedLabel.setAlignment(Align.left);

        currencyLabel = new Label("Coins: 0", new Label.LabelStyle(yellowFont, Color.GOLD));
        currencyLabel.setFontScale(0.7f);
        currencyLabel.setWrap(true);
        currencyLabel.setAlignment(Align.left);

        leftSideTable.add(infoLabel).width(300).padBottom(10).left().row();
        leftSideTable.add(ownedLabel).width(250).padBottom(10).left().row();
        leftSideTable.add(currencyLabel).width(250).padBottom(20).left().row();

        Table buttonTable = createButtons();
        leftSideTable.add(buttonTable).top().left().row();

        return leftSideTable;
    }

    private void updateOwnedLabelAndCurrency() {
        if (profile == null) return;
        int currentOwnedPacks = 0;
        switch (itemDetails.packType) {
            case GOD_DIVINE:    currentOwnedPacks = profile.godDivinePacks; break;
            case DIVINE:        currentOwnedPacks = profile.divinePacks;    break;
            case ARTIFACT_ITEM: currentOwnedPacks = profile.artifactItemPacks; break;
            default: Gdx.app.error("PackScreen", "Unknown pack type in updateOwnedLabel: " + itemDetails.packType); break;
        }
        if (ownedLabel != null) {
            ownedLabel.setText("Packs Owned: " + currentOwnedPacks);
        }
        if (currencyLabel != null) {
            currencyLabel.setText("Coins: " + profile.gachaCurrency);
        }
    }


    private Table createButtons() {
        Table buttonTable = new Table();

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        Drawable transparent = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("ui/transparent.png"))));
        style.up = style.down = style.over = transparent;
        style.font = yellowFont;

        TextButton open1 = new TextButton("Open 1", style);
        TextButton open10 = new TextButton("Open 10", style);
        TextButton buy1 = new TextButton("Buy 1 pack", style);
        TextButton buy10 = new TextButton("Buy 10 packs", style);

        open1.addListener(createHoverListener(open1));
        open10.addListener(createHoverListener(open10));
        buy1.addListener(createHoverListener(buy1));
        buy10.addListener(createHoverListener(buy10));

        open1.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                if (profile != null) {
                    boolean canOpen = false;
                    switch (itemDetails.packType) {
                        case GOD_DIVINE:    if (profile.godDivinePacks > 0) { profile.useGodDivinePack(); canOpen = true; } break;
                        case DIVINE:        if (profile.divinePacks > 0) { profile.useDivinePack(); canOpen = true; } break;
                        case ARTIFACT_ITEM: if (profile.artifactItemPacks > 0) { profile.useArtifactItemPack(); canOpen = true; } break;
                    }
                    if (canOpen) {
                        SaveManager.saveProfile(profile);
                        updateOwnedLabelAndCurrency();
                        game.setScreen(new PackOpenScreenFernan(game, profile, 1, itemDetails.packType, PackScreenFernan.this));
                    } else {
                        showDialog("No Packs", "You do not have any " + itemDetails.name + " to open.");
                    }
                }
            }
        });

        open10.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                if (profile != null) {
                    int packsAvailable = 0;
                    switch (itemDetails.packType) {
                        case GOD_DIVINE:    packsAvailable = profile.godDivinePacks; break;
                        case DIVINE:        packsAvailable = profile.divinePacks;    break;
                        case ARTIFACT_ITEM: packsAvailable = profile.artifactItemPacks; break;
                    }
                    int packsToOpen = Math.min(packsAvailable, 10);
                    if (packsToOpen > 0) {
                        for(int i=0; i<packsToOpen; i++){ // Decrement for each pack opened
                            switch (itemDetails.packType) {
                                case GOD_DIVINE:    profile.useGodDivinePack(); break;
                                case DIVINE:        profile.useDivinePack();    break;
                                case ARTIFACT_ITEM: profile.useArtifactItemPack(); break;
                            }
                        }
                        SaveManager.saveProfile(profile);
                        updateOwnedLabelAndCurrency();
                        game.setScreen(new PackOpenScreenFernan(game, profile, packsToOpen, itemDetails.packType, PackScreenFernan.this));
                    } else {
                        showDialog("No Packs", "You do not have enough " + itemDetails.name + " to open.");
                    }
                }
            }
        });

        buy1.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                int packCost = 15;
                if (profile != null && profile.gachaCurrency >= packCost) {
                    profile.gachaCurrency -= packCost;
                    switch (itemDetails.packType) {
                        case GOD_DIVINE:    profile.addGodDivinePack(1); break;
                        case DIVINE:        profile.addDivinePack(1);    break;
                        case ARTIFACT_ITEM: profile.addArtifactItemPack(1); break;
                        default: Gdx.app.error("PackScreen", "Cannot buy unknown pack type: " + itemDetails.packType); return;
                    }
                    SaveManager.saveProfile(profile);
                    updateOwnedLabelAndCurrency();
                } else {
                    showDialog("Not Enough Coins", "You do not have enough coins to purchase this pack.");
                }
            }
        });

        buy10.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                int packCost = 150;
                if (profile != null && profile.gachaCurrency >= packCost) {
                    profile.gachaCurrency -= packCost;
                    switch (itemDetails.packType) {
                        case GOD_DIVINE:    profile.addGodDivinePack(10); break;
                        case DIVINE:        profile.addDivinePack(10);    break;
                        case ARTIFACT_ITEM: profile.addArtifactItemPack(10); break;
                        default: Gdx.app.error("PackScreen", "Cannot buy unknown pack type: " + itemDetails.packType); return;
                    }
                    SaveManager.saveProfile(profile);
                    updateOwnedLabelAndCurrency();
                } else {
                    showDialog("Not Enough Coins", "You do not have enough coins to purchase these packs.");
                }
            }
        });

        Label coinLabel1 = new Label("15 coins", new Label.LabelStyle(whiteFont, Color.YELLOW));
        coinLabel1.setFontScale(0.7f);
        Label coinLabel10 = new Label("150 coins", new Label.LabelStyle(whiteFont, Color.YELLOW));
        coinLabel10.setFontScale(0.7f);

        buttonTable.add(open1).width(100).height(50).padBottom(15).padRight(10);
        buttonTable.add(open10).width(100).height(50).padBottom(15).row();
        buttonTable.add(buy1).width(150).height(30).padBottom(5).padRight(25);
        buttonTable.add(buy10).width(150).height(30).padBottom(5).row();
        buttonTable.add(coinLabel1).padTop(0).padRight(15);
        buttonTable.add(coinLabel10).padTop(0).row();

        return buttonTable;
    }


    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(title, skin);
        dialog.text(message);
        dialog.button("OK");
        dialog.show(stage);
    }

    private void createBackButton() {
        TextButton.TextButtonStyle backButtonStyle = new TextButton.TextButtonStyle();
        Drawable transparent = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("ui/transparent.png"))));
        backButtonStyle.up = backButtonStyle.down = backButtonStyle.over = transparent;
        backButtonStyle.font = yellowFont;

        TextButton backButton = new TextButton("Back", backButtonStyle);
        backButton.getLabel().setFontScale(1.5f);
        backButton.addListener(createHoverListener(backButton));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                if (previousScreen != null) {
                    game.setScreen(previousScreen);
                } else {
                    // Fallback if previousScreen is null for some reason
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

    private ClickListener createHoverListener(final TextButton button) {
        return new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                button.getLabel().setStyle(new Label.LabelStyle(whiteFont, Color.WHITE));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.getLabel().setStyle(new Label.LabelStyle(yellowFont, Color.YELLOW));
            }
        };
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(stage);
        updateOwnedLabelAndCurrency(); // Refresh counts when screen is shown
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
        // itemDetails.texture is passed in, assume its lifecycle is managed elsewhere (e.g., StoreScreenFernan)
        // Do not dispose itemDetails.texture here unless PackScreenFernan uniquely owns it.
        if (yellowFont != null) yellowFont.dispose();
        if (whiteFont != null) whiteFont.dispose();
        if (skin != null) skin.dispose();
        if (clickSound != null) clickSound.dispose();
    }
}
