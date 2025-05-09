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
    final FernansGrace game;
    private Stage stage;
    private Image background;
    private Array<GameMode> gameModes;

    private Table[] cards = new Table[3];
    private Table mainTable;

    private Label hudLabel;
    private Label descriptionLabel;

    private int selectedCardIndex = -1;

    public ModeSelectionFernan(final FernansGrace game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        BitmapFont font1 = new BitmapFont(Gdx.files.internal("ui/smalligator_yellow.fnt")); // Hover font
        BitmapFont font2 = new BitmapFont(Gdx.files.internal("ui/smalligator_white.fnt"));  // Default font
        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // === Background ===
        background = new Image(new Texture("Bg1.png"));
        background.setFillParent(true);
        stage.addActor(background);

        // === Game Modes ===
        gameModes = new Array<>();
        gameModes.add(new GameMode("Classic", "Fight against various enemies to get the Ultimate God.", new Texture("BG2.png")));
        gameModes.add(new GameMode("Versus", "Challenge other players.", new Texture("BG2.png")));
        gameModes.add(new GameMode("Coming Soon", "Ma'am, next week na po please.", new Texture("BG2.png")));

        // === Main Table ===
        mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        for (int i = 0; i < 3; i++) {
            cards[i] = createCard(i, font2);
        }

        layoutUI(font1);

        // === Back Button ===
        final TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font2;
        buttonStyle.fontColor = Color.WHITE;

        final TextButton backButton = new TextButton("BACK", buttonStyle);
        backButton.getLabel().setFontScale(1f);

        backButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                backButton.getLabel().setStyle(new Label.LabelStyle(font1, Color.WHITE));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                backButton.getLabel().setStyle(new Label.LabelStyle(font2, Color.WHITE));
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Back button clicked.");
                game.setScreen(new GameMenuFernan(game));
            }
        });

        Table backTable = new Table();
        backTable.setFillParent(true);
        backTable.top().left().pad(20);
        backTable.add(backButton);
        stage.addActor(backTable);
    }

    private Table createCard(int index, BitmapFont font1) {
        final int cardIndex = index;

        Table card = new Table();
        card.setBackground(new TextureRegionDrawable(new TextureRegion(gameModes.get(index).texture)));
        card.setTouchable(Touchable.enabled);

        Label title = new Label(gameModes.get(index).title, new Label.LabelStyle(font1, Color.WHITE));
        title.setAlignment(Align.center);
        card.add(title).expand().bottom().padBottom(20);
        card.getColor().a = 0.5f;

        card.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                card.setColor(Color.YELLOW);
                card.getColor().a = (cardIndex == selectedCardIndex) ? 1f : 0.7f;
                descriptionLabel.setText(gameModes.get(cardIndex).description);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (cardIndex == selectedCardIndex) {
                    card.setColor(Color.WHITE);
                    card.getColor().a = 1f;
                    descriptionLabel.setText(gameModes.get(cardIndex).description);
                } else {
                    card.setColor(Color.WHITE);
                    card.getColor().a = 0.5f;
                    descriptionLabel.setText("Hover over a mode to see details");
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedCardIndex != cardIndex) {
                    for (int j = 0; j < 3; j++) {
                        if (j != cardIndex) {
                            cards[j].setColor(Color.WHITE);
                            cards[j].getColor().a = 0.5f;
                        }
                    }
                    card.setColor(Color.WHITE);
                    card.getColor().a = 1f;
                    selectedCardIndex = cardIndex;
                    descriptionLabel.setText(gameModes.get(cardIndex).description);
                } else {
                    System.out.println("Selected mode: " + gameModes.get(cardIndex).title);

                    switch (gameModes.get(cardIndex).title)
                    {
                        case "Classic":
                            game.setScreen(new MapNavigationFernan(game));
                            break;

                        case "Versus":
                            game.setScreen(new MapNavigationFernan(game));
                            break;

                        case "Coming Soon":
                            game.setScreen(new MapNavigationFernan(game));
                            break;
                    }
                }
            }
        });

        return card;
    }

    private void layoutUI(BitmapFont font1) {
        mainTable.clear();

        Label titleLabel = new Label("Select Game Mode", new Label.LabelStyle(font1, Color.WHITE));
        titleLabel.setFontScale(1.5f);
        titleLabel.setAlignment(Align.center);

        mainTable.add(titleLabel).colspan(3).padTop(40).padBottom(20).row();

        Table cardRow = new Table();
        for (int i = 0; i < 3; i++) {
            cardRow.add(cards[i]).width(250).height(350).pad(10);
        }

        mainTable.add(cardRow).colspan(3).padBottom(30).row();

        descriptionLabel = new Label("Hover over a mode to see details", new Label.LabelStyle(font1, Color.LIGHT_GRAY));
        descriptionLabel.setFontScale(0.9f);
        descriptionLabel.setAlignment(Align.center);
        mainTable.add(descriptionLabel).colspan(3).padBottom(10).row();

        hudLabel = new Label("18,007 Gold | 420 Crystals | Level 52", new Label.LabelStyle(font1, Color.GOLD));
        hudLabel.setFontScale(0.8f);
        hudLabel.setAlignment(Align.center);
        mainTable.add(hudLabel).colspan(3).padTop(5);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        background.remove();
        for (GameMode gm : gameModes) {
            gm.texture.dispose();
        }
    }

    static class GameMode {
        String title;
        String description;
        Texture texture;

        public GameMode(String title, String description, Texture texture) {
            this.title = title;
            this.description = description;
            this.texture = texture;
        }
    }
}
