package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.Actor;
// It's good practice to keep imports tidy, though not a functional issue here
// import io.github.grace.ni.fernan.SaveManager;
// import io.github.grace.ni.fernan.SaveProfile;
import java.util.List;


public class LoadGameScreen implements Screen {

    final FernansGrace game;
    private Stage stage;
    private List<SaveProfile> profiles;
    // private Texture background; // This was declared but not initialized in dispose or constructor properly
    private int selectedSaveIndex = -1;

    private BitmapFont font;
    private BitmapFont buttonFont;
    private BitmapFont buttonFontHover;
    private Skin skin;
    private Texture selectionTexture; // For disposing the pixmap-generated texture

    public LoadGameScreen(final FernansGrace game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        profiles = SaveManager.listProfiles();


        Texture bgTexture = new Texture(Gdx.files.internal("ui/loadbg.png"));
        bgTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Image backgroundImage = new Image(new TextureRegionDrawable(new TextureRegion(bgTexture)));
        backgroundImage.setScaling(Scaling.fill);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage); // Add background first so it's behind other elements

        font = new BitmapFont(Gdx.files.internal("ui/black_adder.fnt"));
        buttonFont = new BitmapFont(Gdx.files.internal("ui/smalligator_white.fnt"));
        buttonFontHover = new BitmapFont(Gdx.files.internal("ui/smalligator_gradient2.fnt"));

        Table root = new Table();
        root.setFillParent(true);
        // Adjusted padding to ensure it's visually balanced
        root.padTop(150).padBottom(50).padLeft(50).padRight(50);
        stage.addActor(root);


        final TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle(); // Renamed to avoid conflict
        textButtonStyle.font = buttonFont;
        textButtonStyle.fontColor = Color.WHITE;
        final TextButton backButton = new TextButton("BACK", textButtonStyle);
        backButton.getLabel().setFontScale(1.2f); // Increased scale slightly for better visibility

        backButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                backButton.getLabel().setStyle(new Label.LabelStyle(buttonFontHover, Color.WHITE));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                backButton.getLabel().setStyle(new Label.LabelStyle(buttonFont, Color.WHITE));
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setCurrentSaveProfile(null); // Clear profile state when going back
                game.setScreen(new MainFernan(game));
            }
        });

        Table topBar = new Table();
        // Removed setFillParent, as it might conflict with root table's fillparent.
        // Position it absolutely or within the root table.
        // For simplicity, let's add it to the root table.
        // topBar.setFillParent(true);
        // topBar.top().left().pad(20);
        // topBar.add(backButton).left();
        // stage.addActor(topBar);


        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.8f, 0.8f, 1f, 0.2f); // Slightly bluish and more subtle transparency
        pixmap.fill();
        selectionTexture = new Texture(pixmap); // Store to dispose later
        Drawable selectionDrawable = new TextureRegionDrawable(new TextureRegion(selectionTexture));
        pixmap.dispose();

        Table scrollContent = new Table();
        scrollContent.top().left();


        if (profiles.isEmpty()) {
            Label noSavesLabel = new Label("No Savegames Found.", new Label.LabelStyle(font, Color.BLACK));
            noSavesLabel.setFontScale(1.5f);
            scrollContent.add(noSavesLabel).pad(20f).center();
        } else {
            for (int i = 0; i < profiles.size(); i++) {
                final int index = i;
                final SaveProfile p = profiles.get(i);
                final Table row = new Table(); // Make row final for use in listener
                row.pad(5).padBottom(10); // Added some padding inside the row
                row.setBackground(selectionDrawable); // Default background
                row.addListener(new ClickListener() {
                    @Override public void clicked(InputEvent e, float x, float y) {
                        selectedSaveIndex = index;
                        updateRowSelection(scrollContent); // Pass the container of rows
                    }
                });
                Label nameLabel = new Label(p.saveName, new Label.LabelStyle(font, Color.BLACK));
                nameLabel.setFontScale(1.5f);
                nameLabel.setAlignment(Align.left);
                row.add(nameLabel).expandX().left().padLeft(10f); // Added padding to label
                scrollContent.add(row).expandX().fillX().row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(scrollContent, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setOverscroll(false, false);

        // Add elements to root table for better layout management
        root.add(backButton).top().left().padBottom(20).row(); // Back button at top-left of content area
        root.add(scrollPane).height(Gdx.graphics.getHeight() * 0.4f).expandX().fillX().pad(10).row(); // Relative height

        final TextButton loadButton = new TextButton("LOAD SAVEPOINT", textButtonStyle);
        loadButton.getLabel().setFontScale(1.2f);

        loadButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                loadButton.getLabel().setStyle(new Label.LabelStyle(buttonFontHover, Color.WHITE));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                loadButton.getLabel().setStyle(new Label.LabelStyle(buttonFont, Color.WHITE));
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedSaveIndex >= 0 && selectedSaveIndex < profiles.size()) {
                    SaveProfile profileMeta = profiles.get(selectedSaveIndex);
                    SaveProfile loadedProfile = SaveManager.loadProfile(profileMeta.saveName);
                    if (loadedProfile != null) {
                        game.setCurrentSaveProfile(loadedProfile); // ****** THIS IS THE FIX ******
                        game.setScreen(new GameMenuFernan(game, loadedProfile));
                    } else {
                        // Optional: Show a dialog or message that loading failed
                        Gdx.app.error("LoadGameScreen", "Failed to load profile: " + profileMeta.saveName);
                        Dialog errorDialog = new Dialog("Error", skin, "default");
                        Label errorLabel = new Label("Could not load save: " + profileMeta.saveName, new Label.LabelStyle(buttonFont, Color.WHITE));
                        errorLabel.setWrap(true);
                        errorDialog.getContentTable().add(errorLabel).width(300f).pad(10f);
                        errorDialog.button("OK", true);
                        errorDialog.show(stage);
                    }
                }
            }
        });
        if (!profiles.isEmpty()) { // Only show load button if there are profiles
            root.add(loadButton).center().padTop(20);
        }
    }

    private void updateRowSelection(Table scrollContentTable) {
        // Iterate through the children of scrollContentTable, which are the 'row' Tables
        for (int i = 0; i < scrollContentTable.getChildren().size; i++) {
            Actor actor = scrollContentTable.getChildren().get(i);
            if (actor instanceof Table) { // Ensure it's one of our row tables
                Table rowTable = (Table) actor;
                // Assuming the order of children matches the 'profiles' list index
                if (i == selectedSaveIndex) {
                    // Highlight: Brighter or different background
                    rowTable.setColor(0.9f, 0.9f, 1f, 0.4f); // Light blue highlight
                } else {
                    // Normal: Default background transparency
                    rowTable.setColor(0.8f, 0.8f, 1f, 0.2f); // Default subtle background
                }
            }
        }
    }


    @Override public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK); // It's good practice to clear screen if background doesn't always cover
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        // if (background != null) background.dispose(); // Was not used as a field texture
        if (font != null) font.dispose();
        if (buttonFont != null) buttonFont.dispose();
        if (buttonFontHover != null) buttonFontHover.dispose();
        if (skin != null) skin.dispose();
        if (selectionTexture != null) selectionTexture.dispose(); // Dispose pixmap-generated texture
    }
}
