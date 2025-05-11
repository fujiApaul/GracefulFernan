package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.Align;


/**
 * A dummy “reward details” screen.
 * Pressing Proceed returns you to the map and advances the marker.
 */
public class RewardDetailScreen implements Screen {
    private final FernansGrace game;
    private final ConvergingMapScreen mapScreen;
    private final ConvergingMapScreen.Node node;

    private Stage stage;
    private Skin  skin;
    private Table table;

    public RewardDetailScreen(FernansGrace game,
                              ConvergingMapScreen mapScreen,
                              ConvergingMapScreen.Node node) {
        this.game      = game;
        this.mapScreen = mapScreen;
        this.node      = node;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin  = new Skin(Gdx.files.internal("ui/uiskin.json"));
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Info label
        Label info = new Label("You open the treasure!\nEnjoy your reward.", skin);
        info.setWrap(true);
        info.setAlignment(Align.center);

        // Proceed button
        TextButton proceed = new TextButton("Proceed", skin);
        proceed.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                // advance the marker now that you’ve “opened” the reward
                mapScreen.moveTo(node);
                // return to the map
                game.setScreen(mapScreen);
            }
        });

        table.add(info).width(400).pad(20).row();
        table.add(proceed).width(200).height(60).pad(20);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
