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

public class StoreScreenFernan implements Screen {
    final FernansGrace game;
    private Stage stage;
    private Image background;
    private Array<StoreItem> storeItems;
    private Table[] itemCards = new Table[3];
    private Table mainTable;

    private BitmapFont yellowFont;
    private BitmapFont whiteFont;

    private int selectedItemIndex = -1;

    public StoreScreenFernan(final FernansGrace game) {
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

        // Store Items
        storeItems = new Array<>();
        storeItems.add(new StoreItem("GOD/DIVINE PACK", "\n\n\n\n\n\n\n Pack Owned", new Texture("BG1.png")));
        storeItems.add(new StoreItem("DIVINE/SUPPORT PACK", "\n\n\n\n\n\n\n Pack Owned", new Texture("BG1.png")));
        storeItems.add(new StoreItem("ARTIFACT/ITEM PACK", "\n\n\n\n\n\n\n Pack Owned", new Texture("BG1.png")));

        // Main layout table
        mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // Create item cards
        for (int i = 0; i < 3; i++) {
            itemCards[i] = createItemCard(i);
        }

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
            @Override public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                backButton.getLabel().setStyle(new Label.LabelStyle(whiteFont, Color.WHITE));
            }

            @Override public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                backButton.getLabel().setStyle(new Label.LabelStyle(yellowFont, Color.YELLOW));
            }

            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameMenuFernan(game));
            }
        });

        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top().left().padTop(10).padLeft(10);
        topTable.add(backButton).width(100).height(40);
        stage.addActor(topTable);
    }

    private Table createItemCard(int index) {
        final int itemIndex = index;
        StoreItem item = storeItems.get(index);

        Table cardContainer = new Table();
        cardContainer.defaults().padBottom(10);
        cardContainer.setTouchable(Touchable.enabled);

        Image image = new Image(item.texture);
        image.setColor(Color.WHITE);
        image.getColor().a = 0.5f;

        Table textTable = new Table();
        textTable.top().pad(10);

        // Larger font scale for item name and description
        Label name = new Label(item.name, new Label.LabelStyle(yellowFont, Color.WHITE));
        name.setFontScale(0.8f);  // Increased font scale
        name.setWrap(true);
        name.setAlignment(Align.center);

        Label desc = new Label(item.description, new Label.LabelStyle(yellowFont, Color.WHITE));
        desc.setFontScale(0.7f);  // Slightly larger description font scale
        desc.setWrap(true);
        desc.setAlignment(Align.center);

        // Adjusting widths to match the larger size
        textTable.add(name).width(280).padBottom(10).row();
        textTable.add(desc).width(280).padTop(20).row();

        Stack stack = new Stack();
        stack.add(image);
        stack.add(textTable);

        // Increased card dimensions (300x400)
        cardContainer.add(stack).width(250).height(350).row();

        cardContainer.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                image.setColor(Color.GREEN);
                image.getColor().a = (itemIndex == selectedItemIndex) ? 1f : 0.7f;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (itemIndex == selectedItemIndex) {
                    image.setColor(Color.WHITE);
                    image.getColor().a = 1f;
                } else {
                    image.setColor(Color.WHITE);
                    image.getColor().a = 0.5f;
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedItemIndex != itemIndex) {
                    for (int j = 0; j < 3; j++) {
                        Image otherImage = (Image) ((Stack) itemCards[j].getChildren().first()).getChildren().first();
                        otherImage.setColor(Color.WHITE);
                        otherImage.getColor().a = 0.5f;
                    }
                    image.setColor(Color.WHITE);
                    image.getColor().a = 1f;
                    selectedItemIndex = itemIndex;
                } else {
                    System.out.println("Purchased: " + item.name);
                }

                // Transition to different screens based on the item clicked
                switch (item.name) {
                    case "GOD/DIVINE PACK":
                        game.setScreen(new GodDivineFernan(game));  // Transition to GodDivineScreen
                        break;
                    case "DIVINE/SUPPORT PACK":
                        System.out.println("Load game clicked");  // Transition to DivineSupportScreen
                        break;
                    case "ARTIFACT/ITEM PACK":
                        System.out.println("Load game clicked");  // Transition to ArtifactItemScreen
                        break;
                    default:
                        // Handle unknown items if necessary
                        System.out.println("Unknown item: " + item.name);
                        break;
                }
            }
        });

        return cardContainer;
    }

    private void layoutUI() {
        mainTable.clear();

        Table cardRow = new Table();
        for (int i = 0; i < 3; i++) {
            cardRow.add(itemCards[i]).pad(20);
        }

        mainTable.add(cardRow).colspan(3).padTop(60).padBottom(40).row();
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
