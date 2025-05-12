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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.grace.ni.fernan.SaveManager;
import io.github.grace.ni.fernan.SaveProfile;
import java.util.List;


public class LoadGameScreen implements Screen {

    final FernansGrace game;
    private Stage stage;
    private List<SaveProfile> profiles;
    private Texture background;
    private int selectedSaveIndex = -1;

    private BitmapFont font;
    private BitmapFont buttonFont;
    private BitmapFont buttonFontHover;
    private Skin skin;

    public LoadGameScreen(final FernansGrace game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        // pull all saved profiles
        profiles = SaveManager.listProfiles();


        Texture bgTexture = new Texture(Gdx.files.internal("ui/loadbg.png"));
        bgTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Image backgroundImage = new Image(new TextureRegionDrawable(new TextureRegion(bgTexture)));
        backgroundImage.setScaling(Scaling.fill);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        font = new BitmapFont(Gdx.files.internal("ui/black_adder.fnt"));
        buttonFont = new BitmapFont(Gdx.files.internal("ui/smalligator_white.fnt"));
        buttonFontHover = new BitmapFont(Gdx.files.internal("ui/smalligator_gradient2.fnt"));

        Table root = new Table();
        root.setFillParent(true);
        root.padTop(185).padBottom(10).padLeft(10).padRight(10);
        stage.addActor(root);


        final TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = buttonFont;
        buttonStyle.fontColor = Color.WHITE;
        final TextButton backButton = new TextButton("BACK", buttonStyle);
        backButton.getLabel().setFontScale(1f);

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
                // TODO: Replace with actual screen change
                game.setScreen(new MainFernan(game));
            }
        });

        Table topBar = new Table();
        topBar.setFillParent(true);
        topBar.top().left().pad(20);
        topBar.add(backButton).left();
        stage.addActor(topBar);


        // Semi-transparent selection background
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 0.3f);
        pixmap.fill();
        Texture selectionTexture = new Texture(pixmap);
        Drawable selectionDrawable = new TextureRegionDrawable(new TextureRegion(selectionTexture));
        pixmap.dispose();

        // Scrollable content (name + date rows)
        Table scrollContent = new Table();
        scrollContent.top().left();
        scrollContent.defaults().expandX().fillX();


        for (int i = 0; i < profiles.size(); i++) {
            final int index = i;
            final SaveProfile p = profiles.get(i);
            Table row = new Table();
            row.padBottom(10);
            row.setBackground(selectionDrawable);
            row.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    selectedSaveIndex = index;
                    updateRowSelection(scrollContent);
                }
            });
            Label nameLabel = new Label(p.saveName, new Label.LabelStyle(font, Color.BLACK));
            nameLabel.setFontScale(1.5f);
            nameLabel.setAlignment(Align.left);
            row.add(nameLabel).expandX().left();
            scrollContent.add(row).expandX().fillX().row();
        }

        ScrollPane scrollPane = new ScrollPane(scrollContent, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setOverscroll(false, false);

        root.add(scrollPane).height(240).expand().fill().pad(30).row();

        // Load Savepoint Button

        final TextButton loadButton = new TextButton("LOAD SAVEPOINT", buttonStyle);
        loadButton.getLabel().setFontScale(1f);

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
                if (selectedSaveIndex >= 0) {
                    SaveProfile toLoad = profiles.get(selectedSaveIndex);
                    SaveProfile loaded = SaveManager.loadProfile(toLoad.saveName);
                    if (loaded != null) {
                        game.setScreen(new GameMenuFernan(game, loaded));
                    }
                }
            }
        });

        root.add(loadButton).center();
    }

    private void updateRowSelection(Table scrollContent) {
        for (int i = 0; i < scrollContent.getChildren().size; i++) {
            Actor actor = scrollContent.getChildren().get(i);
            if (actor instanceof Table) {
                Table row = (Table) actor;
                if (i == selectedSaveIndex) {
                    row.setColor(1f, 1f, 1f, 0.5f); // highlight
                } else {
                    row.setColor(1f, 1f, 1f, 1f); // normal
                }
            }
        }
    }


    @Override public void show() {}

    @Override
    public void render(float delta) {
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
        stage.dispose();
        background.dispose();
        font.dispose();
        buttonFont.dispose();
        skin.dispose();
    }
}
