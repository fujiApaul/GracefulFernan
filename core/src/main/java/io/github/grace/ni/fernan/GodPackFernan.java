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

public class GodPackFernan implements Screen {
    final FernansGrace game;
    private Stage stage;
    private Image background;
    private Array<StoreItem> storeItems;
    private Table itemCard;
    private Table mainTable;

    private BitmapFont yellowFont;
    private BitmapFont whiteFont;

    public GodPackFernan(final FernansGrace game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        yellowFont = new BitmapFont(Gdx.files.internal("ui/smalligator_yellow.fnt"));
        yellowFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        whiteFont = new BitmapFont(Gdx.files.internal("ui/smalligator_gradient2.fnt"));
        whiteFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Background
        background = new Image(new Texture("Bg2B.PNG"));
        background.setFillParent(true);
        stage.addActor(background);

        // Store Item
        storeItems = new Array<>();
        storeItems.add(new StoreItem(
            "GOD / DIVINE CARD PACK",
            "\nThis pack contains a god or divine card with chances:\n\n" +
                "15% chance for a God Card\n" +
                "85% chance for a Divine Card",
            "Packs owned: 13",
            new Texture("BG1.png")
        ));

        mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        itemCard = createItemCard(0);
        layoutUI();

        createBackButton();
    }

    private Table createItemCard(int index) {
        StoreItem item = storeItems.get(index);

        Table cardContainer = new Table();
        cardContainer.defaults().padBottom(10);
        cardContainer.setTouchable(Touchable.disabled);

        Image image = new Image(item.texture);
        image.setColor(Color.WHITE);

        Table textTable = new Table();
        textTable.top().pad(10);

        Label name = new Label(item.name, new Label.LabelStyle(yellowFont, Color.WHITE));
        name.setFontScale(0.8f);
        name.setWrap(true);
        name.setAlignment(Align.center);

        textTable.add(name).width(280).padBottom(10).row();

        Stack stack = new Stack();
        stack.add(image);
        stack.add(textTable);

        cardContainer.add(stack).width(300).height(400).row();

        return cardContainer;
    }

    private void layoutUI() {
        mainTable.clear();

        StoreItem item = storeItems.get(0);

        // LEFT: Description + Buttons
        Table leftSideTable = new Table();
        leftSideTable.top().left().pad(40);

        Label infoLabel = new Label(item.infoText, new Label.LabelStyle(yellowFont, Color.YELLOW));
        infoLabel.setFontScale(0.6f);
        infoLabel.setWrap(true);
        infoLabel.setAlignment(Align.left);

        Label ownedLabel = new Label(item.ownedText, new Label.LabelStyle(yellowFont, Color.YELLOW));
        ownedLabel.setFontScale(0.6f);
        ownedLabel.setWrap(true);
        ownedLabel.setAlignment(Align.left);

        leftSideTable.add(infoLabel).width(300).padBottom(10).left().row();
        leftSideTable.add(ownedLabel).width(250).padBottom(40).left().row();

        // Buttons
        Table buttonTable = new Table();

        TextButton.TextButtonStyle yellowButtonStyle = new TextButton.TextButtonStyle();
        yellowButtonStyle.up = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("ui/transparent.png"))));
        yellowButtonStyle.down = yellowButtonStyle.up;
        yellowButtonStyle.over = yellowButtonStyle.up;
        yellowButtonStyle.font = yellowFont;

        TextButton open1 = new TextButton("Open 1", yellowButtonStyle);
        TextButton open10 = new TextButton("Open 10", yellowButtonStyle);
        TextButton buy1 = new TextButton("Buy 1 pack", yellowButtonStyle);
        TextButton buy10 = new TextButton("Buy 10 packs", yellowButtonStyle);

        // Add hover effects to each button
        open1.addListener(createHoverListener(open1));
        open10.addListener(createHoverListener(open10));
        buy1.addListener(createHoverListener(buy1));
        buy10.addListener(createHoverListener(buy10));

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

        leftSideTable.add(buttonTable).top().left().row();

        Table layoutRow = new Table();
        layoutRow.add(leftSideTable).expandX().left().pad(10);
        layoutRow.add(itemCard).right().pad(10);

        mainTable.add(layoutRow).expand().fill().row();
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

        backButton.addListener(createHoverListener(backButton));

        backButton.addListener(new ClickListener() {
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

    // Shared hover logic for all buttons
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
        String infoText;
        String ownedText;
        Texture texture;

        public StoreItem(String name, String infoText, String ownedText, Texture texture) {
            this.name = name;
            this.infoText = infoText;
            this.ownedText = ownedText;
            this.texture = texture;
        }
    }
}
