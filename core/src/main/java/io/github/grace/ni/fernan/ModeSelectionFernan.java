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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ModeSelectionFernan implements Screen {
    private final FernansGrace game;
    private final SaveProfile profile;
    private Stage stage;
    private Image background;
    private Array<GameMode> gameModes;

    private Table[] cards = new Table[3]; // Array to hold the card tables
    private Table mainTable;

    private Label hudLabel;
    private Label descriptionLabel;

    private int selectedCardIndex = -1; // -1 means no card is currently selected

    public ModeSelectionFernan(final FernansGrace game, SaveProfile profile) {
        this.game = game;
        this.profile = profile;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        BitmapFont font1 = new BitmapFont(Gdx.files.internal("ui/smalligator_yellow.fnt"));
        BitmapFont font2 = new BitmapFont(Gdx.files.internal("ui/smalligator_white.fnt"));
        // Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json")); // skin is unused

        background = new Image(new Texture("Bg1.png"));
        background.setFillParent(true);
        stage.addActor(background);

        gameModes = new Array<>();
        gameModes.add(new GameMode("Classic", "Fight against various enemies to get the Ultimate God.", new Texture("BG2.png")));
        gameModes.add(new GameMode("Versus", "Challenge other players.", new Texture("BG2.png")));
        gameModes.add(new GameMode("Coming Soon", "Ma'am, next week na po please.", new Texture("BG2.png")));

        mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        for (int i = 0; i < 3; i++) {
            cards[i] = createCard(i, font2); // Store the created card Table
        }

        layoutUI(font1);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font2;
        buttonStyle.fontColor = Color.WHITE;

        final TextButton backButton = new TextButton("BACK", buttonStyle);
        backButton.getLabel().setFontScale(1f);

        backButton.addListener(new ClickListener() {
            @Override public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                backButton.getLabel().setStyle(new Label.LabelStyle(font1, Color.WHITE));
            }
            @Override public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                backButton.getLabel().setStyle(new Label.LabelStyle(font2, Color.WHITE));
            }
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameMenuFernan(game, profile));
            }
        });

        Table backTable = new Table();
        backTable.setFillParent(true);
        backTable.top().left().pad(20);
        backTable.add(backButton);
        stage.addActor(backTable);
    }

    private Table createCard(int index, BitmapFont font) {
        final int cardIndex = index; // To be used inside the listener

        // 'cardActor' is the Table representing the card visually
        final Table cardActor = new Table();
        cardActor.setBackground(new TextureRegionDrawable(new TextureRegion(gameModes.get(index).texture)));
        cardActor.setTouchable(Touchable.enabled);

        Label title = new Label(gameModes.get(index).title, new Label.LabelStyle(font, Color.WHITE));
        title.setAlignment(Align.center);
        cardActor.add(title).expand().bottom().padBottom(20);

        // Initial state: No tint (Color.WHITE) and semi-transparent
        cardActor.setColor(Color.WHITE); // Default tint (no color effect on texture)
        cardActor.getColor().a = 0.5f;   // Default alpha for non-selected, non-hovered

        cardActor.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                // Hovering over this card (cardActor)
                cardActor.setColor(Color.YELLOW); // Tint yellow on hover
                if (cardIndex == selectedCardIndex) {
                    cardActor.getColor().a = 1f; // Selected and hovered: opaque yellow
                } else {
                    cardActor.getColor().a = 0.7f; // Not selected but hovered: brighter semi-transparent yellow
                }
                descriptionLabel.setText(gameModes.get(cardIndex).description);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                // Mouse is exiting this card (cardActor)
                if (cardIndex == selectedCardIndex) {
                    // This card is the currently selected one, so it should remain opaque yellow
                    cardActor.setColor(Color.YELLOW);
                    cardActor.getColor().a = 1f;
                } else {
                    // This card is not selected, revert to default appearance (no tint, default alpha)
                    cardActor.setColor(Color.WHITE); // Reset tint to neutral
                    cardActor.getColor().a = 0.5f;   // Reset to default alpha
                }

                // Update description label to reflect the currently selected card or default text
                if (selectedCardIndex != -1) {
                    descriptionLabel.setText(gameModes.get(selectedCardIndex).description);
                } else {
                    descriptionLabel.setText("Hover over a mode to see details");
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Clicking on this card (cardActor)
                if (selectedCardIndex != cardIndex) { // If this card was not already selected
                    // 1. If another card was selected, reset its appearance
                    if (selectedCardIndex != -1) {
                        cards[selectedCardIndex].setColor(Color.WHITE); // Reset old selected to no tint
                        cards[selectedCardIndex].getColor().a = 0.5f;   // Reset old selected to default alpha
                    }

                    // 2. Update the selected index
                    selectedCardIndex = cardIndex;

                    // 3. Set this clicked card to selected appearance (opaque yellow)
                    cardActor.setColor(Color.YELLOW);
                    cardActor.getColor().a = 1f;
                    descriptionLabel.setText(gameModes.get(selectedCardIndex).description);

                } else { // This card was already selected (effectively a "confirm" click)
                    // Ensure its appearance is correct (opaque yellow) before navigating
                    cardActor.setColor(Color.YELLOW);
                    cardActor.getColor().a = 1f;

                    // Proceed based on the selected game mode
                    switch (gameModes.get(cardIndex).title) {
                        case "Classic":
                            game.setScreen(new ConvergingMapScreen(game, profile));
                            break;
                        case "Versus":
                            game.setScreen(new ConvergingMapScreen(game, profile)); // Or your Versus screen
                            break;
                        case "Coming Soon":
                            // Do nothing or show a message
                            break;
                    }
                }
            }
        });

        return cardActor;
    }

    private void layoutUI(BitmapFont font) {
        mainTable.clear();

        Label titleLabel = new Label("Select Game Mode", new Label.LabelStyle(font, Color.WHITE));
        titleLabel.setFontScale(1.5f);
        titleLabel.setAlignment(Align.center);
        mainTable.add(titleLabel).colspan(3).padTop(40).padBottom(20).row();

        Table cardRow = new Table();
        for (Table c : cards) { // cards[] now holds the Table actors
            cardRow.add(c).width(250).height(350).pad(10);
        }
        mainTable.add(cardRow).colspan(3).padBottom(30).row();

        descriptionLabel = new Label("Hover over a mode to see details",
            new Label.LabelStyle(font, Color.LIGHT_GRAY)); // Using font1 as per titleLabel
        descriptionLabel.setFontScale(0.9f);
        descriptionLabel.setAlignment(Align.center);
        mainTable.add(descriptionLabel).colspan(3).padBottom(10).row();

        // Assuming hudLabel uses font1 as well for consistency, or font2 if intended
        hudLabel = new Label("18,007 Gold | 420 Crystals | Level 52",
            new Label.LabelStyle(font, Color.GOLD)); // Using font1 from layoutUI parameter
        hudLabel.setFontScale(0.8f);
        hudLabel.setAlignment(Align.center);
        mainTable.add(hudLabel).colspan(3).padTop(5);
    }

    @Override public void show() {}

    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override public void dispose() {
        stage.dispose();
        // The background image actor will be disposed by the stage if it's still added.
        // If you manually remove it elsewhere, you might need to dispose its texture.
        // background.remove(); // This removes from stage, doesn't dispose texture.
        if (background.getDrawable() instanceof TextureRegionDrawable) {
            TextureRegionDrawable drawable = (TextureRegionDrawable) background.getDrawable();
            if (drawable != null && drawable.getRegion() != null) {
                // drawable.getRegion().getTexture().dispose(); // Be careful if this texture is shared
            }
        }


        for (GameMode gm : gameModes) {
            if (gm.texture != null) {
                gm.texture.dispose();
            }
        }
        // Fonts are typically disposed by the game or asset manager if loaded globally,
        // or here if loaded and used only by this screen.
    }

    static class GameMode {
        String title, description;
        Texture texture;
        GameMode(String t, String d, Texture tx) {
            title = t;
            description = d;
            texture = tx;
        }
    }
}
