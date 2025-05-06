package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameModeSelectionScreen implements Screen {
    final FernansGrace game;
    private Stage stage;
    private Array<GameMode> gameModes;
    private int currentIndex = 0;

    private Label titleLabel, descriptionLabel;
    private TextButton leftArrow, rightArrow, selectButton;
    private Table leftCard, centerCard, rightCard;
    private Table mainTable;

    public GameModeSelectionScreen(final FernansGrace game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        BitmapFont font = new BitmapFont(Gdx.files.internal("ui/smalligator_yellow.fnt"));
        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        gameModes = new Array<>();
        gameModes.add(new GameMode("Battle Mode", "Fight AI using your chosen deck.", new Texture("BG1.png")));
        gameModes.add(new GameMode("Player vs Player", "Challenge a friend to a duel.", new Texture("BG2.png")));
        gameModes.add(new GameMode("Coming Soon", "New game mode will be available soon.", new Texture("BG2.png")));

        // Create main table
        mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // Title label
        titleLabel = new Label(gameModes.get(currentIndex).title, new Label.LabelStyle(font, Color.WHITE));
        titleLabel.setFontScale(1.5f);
        titleLabel.setAlignment(Align.center);

        // Description label
        descriptionLabel = new Label(gameModes.get(currentIndex).description, new Label.LabelStyle(font, Color.LIGHT_GRAY));
        descriptionLabel.setFontScale(0.8f);
        descriptionLabel.setWrap(true);
        descriptionLabel.setAlignment(Align.center);

        // Create arrow buttons
        TextButton.TextButtonStyle arrowStyle = new TextButton.TextButtonStyle();
        arrowStyle.font = font;
        leftArrow = new TextButton("<", arrowStyle);
        rightArrow = new TextButton(">", arrowStyle);

        leftArrow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                animateSlide(-1);
            }
        });

        rightArrow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                animateSlide(1);
            }
        });

        // Create select button
        selectButton = new TextButton("Select", skin);
        selectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Handle game mode selection here
                if (currentIndex == 0) {
                    // Battle Mode selected
                } else if (currentIndex == 1) {
                    // PvP Mode selected
                }
            }
        });

        // Create cards
        leftCard = createCard((currentIndex - 1 + gameModes.size) % gameModes.size);
        centerCard = createCard(currentIndex);
        rightCard = createCard((currentIndex + 1) % gameModes.size);

        // Layout the main table
        mainTable.add(titleLabel).colspan(3).padBottom(20).row();
        mainTable.add(descriptionLabel).colspan(3).width(Gdx.graphics.getWidth() * 0.8f).padBottom(30).row();

        Table cardsRow = new Table();
        cardsRow.add(leftArrow).width(50).padRight(20);
        cardsRow.add(leftCard).width(250).height(350).padRight(10);
        cardsRow.add(centerCard).width(300).height(400).padLeft(10).padRight(10);
        cardsRow.add(rightCard).width(250).height(350).padLeft(10);
        cardsRow.add(rightArrow).width(50).padLeft(20);

        mainTable.add(cardsRow).colspan(3).padBottom(30).row();
        mainTable.add(selectButton).colspan(3).width(200).height(60);
    }

    private Table createCard(int index) {
        Table card = new Table();
        card.setBackground(new TextureRegionDrawable(new TextureRegion(gameModes.get(index).texture)));

        Label title = new Label(gameModes.get(index).title, new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        title.setAlignment(Align.center);

        card.add(title).growX().bottom().padBottom(20);
        return card;
    }

    private void updateCards() {
        titleLabel.setText(gameModes.get(currentIndex).title);
        descriptionLabel.setText(gameModes.get(currentIndex).description);

        // Update card contents
        leftCard.clear();
        centerCard.clear();
        rightCard.clear();

        leftCard = createCard((currentIndex - 1 + gameModes.size) % gameModes.size);
        centerCard = createCard(currentIndex);
        rightCard = createCard((currentIndex + 1) % gameModes.size);

        // Rebuild the layout
        mainTable.clear();
        mainTable.add(titleLabel).colspan(3).padBottom(20).row();
        mainTable.add(descriptionLabel).colspan(3).width(Gdx.graphics.getWidth() * 0.8f).padBottom(30).row();

        Table cardsRow = new Table();
        cardsRow.add(leftArrow).width(50).padRight(20);
        cardsRow.add(leftCard).width(250).height(350).padRight(10);
        cardsRow.add(centerCard).width(300).height(400).padLeft(10).padRight(10);
        cardsRow.add(rightCard).width(250).height(350).padLeft(10);
        cardsRow.add(rightArrow).width(50).padLeft(20);

        mainTable.add(cardsRow).colspan(3).padBottom(30).row();
        mainTable.add(selectButton).colspan(3).width(200).height(60);
    }

    private void animateSlide(final int direction) {
        currentIndex = (currentIndex + direction + gameModes.size) % gameModes.size;
        updateCards();
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
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
