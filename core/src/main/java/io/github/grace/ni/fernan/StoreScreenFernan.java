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
    private Stage stage;
    private Image background;
    private Array<StoreItem> storeItems;
    private Table[] itemCards = new Table[3];
    private Table mainTable;

    private BitmapFont yellowFont;
    private BitmapFont whiteFont;

    private int selectedItemIndex = -1;

    private Sound clickSound;

    public StoreScreenFernan(final FernansGrace game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        yellowFont = new BitmapFont(Gdx.files.internal("ui/smalligator_yellow.fnt"));
        yellowFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        whiteFont = new BitmapFont(Gdx.files.internal("ui/smalligator_gradient2.fnt"));
        whiteFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        clickSound = Gdx.audio.newSound(Gdx.files.internal("Click.mp3"));

        background = new Image(new Texture("Bg2B.PNG"));
        background.setFillParent(true);
        stage.addActor(background);

        storeItems = new Array<>();
        storeItems.add(new StoreItem("GOD/DIVINE PACK", new Texture("cards/zeus.png")));
        storeItems.add(new StoreItem("DIVINE/SUPPORT PACK", new Texture("BG1.png")));
        storeItems.add(new StoreItem("ARTIFACT/ITEM PACK", new Texture("items/goldenapple.png")));

        mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        for (int i = 0; i < 3; i++) {
            itemCards[i] = createItemCard(i);
        }

        layoutUI();

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
        image.getColor().a = 1f;

        Table textTable = new Table();
        textTable.top().pad(10);

        Label name = new Label(item.name, new Label.LabelStyle(yellowFont, Color.WHITE));
        name.setFontScale(0.8f);
        name.setWrap(true);
        name.setAlignment(Align.center);

        Label desc = new Label("Packs Owned: " + item.packsOwned, new Label.LabelStyle(yellowFont, Color.WHITE));
        desc.setFontScale(0.7f);
        desc.setWrap(true);
        desc.setAlignment(Align.center);

        textTable.add(name).width(280).padBottom(10).row();
        textTable.add(desc).width(280).padTop(20).row();

        Stack stack = new Stack();
        stack.add(image);
        stack.add(textTable);

        cardContainer.add(stack).width(250).height(350).row();

        cardContainer.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                image.setColor(Color.WHITE);
                image.getColor().a = 1f;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                image.setColor(Color.WHITE);
                image.getColor().a = 1f;
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedItemIndex != itemIndex) {
                    // Select new item, reset alpha for all images
                    for (int j = 0; j < 3; j++) {
                        Image otherImage = (Image) ((Stack) itemCards[j].getChildren().first()).getChildren().first();
                        otherImage.setColor(Color.WHITE);
                        otherImage.getColor().a = 1f;
                    }
                    selectedItemIndex = itemIndex;
                } else {
                    // Increment owned packs on second click on selected item
                    clickSound.play();
                    item.packsOwned++;
                    refreshItemCard(itemIndex);
                    System.out.println("Purchased: " + item.name);
                }

                // Switch to PackScreen passing current packsOwned count as String
                switch (item.name) {
                    case "GOD/DIVINE PACK":
                        game.setScreen(new PackScreenFernan(game,
                            new PackScreenFernan.MutableStoreItem(
                                "GOD / DIVINE CARD PACK",
                                "\nThis pack contains a god or divine card with chances:\n\n" +
                                    "15% chance for a God Card\n" +
                                    "85% chance for a Divine Card",
                                String.valueOf(item.packsOwned),
                                new Texture("cards/zeus.png")
                            )));
                        break;
                    case "DIVINE/SUPPORT PACK":
                        game.setScreen(new PackScreenFernan(game,
                            new PackScreenFernan.MutableStoreItem(
                                "DIVINE / SUPPORT CARD PACK",
                                "\nThis pack contains a divine or support card with chances:\n\n" +
                                    "15% chance for a God Card\n" +
                                    "85% chance for a Divine Card",
                                String.valueOf(item.packsOwned),
                                new Texture("BG1.png")
                            )));
                        break;
                    case "ARTIFACT/ITEM PACK":
                        game.setScreen(new PackScreenFernan(game,
                            new PackScreenFernan.MutableStoreItem(
                                "ARTIFACT / ITEM CARD PACK",
                                "\nThis pack contains an artifact or item card with chances:\n\n" +
                                    "15% chance for a God Card\n" +
                                    "85% chance for a Divine Card",
                                String.valueOf(item.packsOwned),
                                new Texture("items/goldenapple.png")
                            )));
                        break;
                    default:
                        System.out.println("Unknown item: " + item.name);
                        break;
                }
            }
        });

        return cardContainer;
    }

    private void refreshItemCard(int index) {
        Table card = itemCards[index];
        Stack stack = (Stack) card.getChildren().first();
        Table textTable = (Table) stack.getChildren().get(1);

        Label descLabel = (Label) textTable.getChildren().get(1);
        StoreItem item = storeItems.get(index);
        descLabel.setText("Packs Owned: " + item.packsOwned);
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
        Texture texture;
        int packsOwned;

        public StoreItem(String name, Texture texture) {
            this.name = name;
            this.texture = texture;
            this.packsOwned = 0;
        }
    }
}
