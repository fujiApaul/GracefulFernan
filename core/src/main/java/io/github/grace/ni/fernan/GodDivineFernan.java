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

public class GodDivineFernan implements Screen {
    final FernansGrace game;
    private Stage stage;
    private Image background;
    private Array<StoreItem> storeItems;
    private Table itemCard;
    private Table mainTable;

    private BitmapFont yellowFont;
    private BitmapFont whiteFont;

    public GodDivineFernan(final FernansGrace game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        yellowFont = new BitmapFont(Gdx.files.internal("ui/smalligator_yellow.fnt"));
        yellowFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        whiteFont = new BitmapFont(Gdx.files.internal("ui/smalligator_gradient2.fnt"));
        whiteFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Background
        background = new Image(new Texture("Bg2B.PNG"));
        background.setFillParent(true);
        stage.addActor(background);

        // Store Item (only one item for now)
        storeItems = new Array<>();
        storeItems.add(new StoreItem("GOD/DIVINE PACK" new Texture("BG1.png")));

        // Main layout table
        mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // Create item card (only one)
        itemCard = createItemCard(0);

        layoutUI();

        // Back button
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
                game.setScreen(new StoreScreenFernan(game));
            }
        });

        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top().left().padTop(10).padLeft(10);
        topTable.add(backButton).width(100).height(40);
        stage.addActor(topTable);
    }

    private Table createItemCard(int index) {
        StoreItem item = storeItems.get(index);

        Table cardContainer = new Table();
        cardContainer.defaults().padBottom(10);
        cardContainer.setTouchable(Touchable.disabled); // No interaction

        Image image = new Image(item.texture);
        image.setColor(Color.WHITE);
        image.getColor().a = 1.0f; // Fully opaque

        Table textTable = new Table();
        textTable.top().pad(10);

        Label name = new Label(item.name, new Label.LabelStyle(yellowFont, Color.WHITE));
        name.setFontScale(0.8f);
        name.setWrap(true);
        name.setAlignment(Align.center);

        Label desc = new Label(item.description, new Label.LabelStyle(yellowFont, Color.WHITE));
        desc.setFontScale(0.7f);
        desc.setWrap(true);
        desc.setAlignment(Align.center);

        textTable.add(name).width(280).padBottom(10).row();
        textTable.add(desc).width(280).padTop(20).row();

        Stack stack = new Stack();
        stack.add(image);
        stack.add(textTable);

        cardContainer.add(stack).width(250).height(350).row();

        return cardContainer;
    }

    private void layoutUI() {
        mainTable.clear();

        // Create a description label
        Label descriptionLabel = new Label("This is the description of the GOD/DIVINE PACK.", new Label.LabelStyle(whiteFont, Color.WHITE));
        descriptionLabel.setFontScale(0.8f);
        descriptionLabel.setWrap(true); // To wrap text if it's too long
        descriptionLabel.setAlignment(Align.topLeft); // Align text to the left

        // Create a table to hold the description on the left side
        Table leftDescriptionTable = new Table();
        leftDescriptionTable.top().left(); // Align to the top left
        leftDescriptionTable.add(descriptionLabel).expandX().fillX().pad(10).row(); // Add the label and ensure it's aligned

        // Now add the leftDescriptionTable to the mainTable
        Table cardRow = new Table();
        cardRow.left(); // Align the card to the left
        cardRow.add(leftDescriptionTable).pad(20).expandX().fillX(); // Add the description table first (left)

        // Add the item card to the right side
        cardRow.add(itemCard).pad(20).expandX().right(); // Then add the item card (right aligned)

        mainTable.add(cardRow).expand().fill().row();
    }

    @Override public void show() {}
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
        background.remove();
        for (StoreItem item : storeItems) {
            item.texture.dispose();
        }
    }

    static class StoreItem {
        String name;
        String description;
        Texture texture;

        public StoreItem(String name, String description, Texture texture) {
            this.name = name;
            this.description = description;
            this.texture = texture;
        }
    }
}
