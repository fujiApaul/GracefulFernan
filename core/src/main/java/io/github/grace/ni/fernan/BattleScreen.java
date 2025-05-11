package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * A stub battle screen with Win/Lose buttons.
 * On Win or Lose, it returns to the same ConvergingMapScreen
 * so visited/current state is preserved.
 */
public class BattleScreen implements Screen {
    private final FernansGrace game;
    private final ConvergingMapScreen mapScreen;
    private final ConvergingMapScreen.Node targetNode;

    private Stage stage;
    private Skin  skin;
    private Table table;

    /**
     * @param game       your main game instance
     * @param mapScreen  the map screen to return to
     * @param targetNode the node you clicked on (so current has already been updated)
     */
    public BattleScreen(FernansGrace game,
                        ConvergingMapScreen mapScreen,
                        ConvergingMapScreen.Node targetNode) {
        this.game       = game;
        this.mapScreen  = mapScreen;
        this.targetNode = targetNode;

        // Set up a simple UI
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Win button
        TextButton winButton = new TextButton("Win", skin);
        winButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                mapScreen.hidePopup();          // dismiss the pop-up
                mapScreen.moveTo(targetNode);   // ← advance the marker *only on win*
                game.setScreen(mapScreen);      // go back to your map
            }
        });




        // Lose button
        TextButton loseButton = new TextButton("Lose", skin);
        loseButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                mapScreen.hidePopup();
                // no moveTo, so marker stays
                game.setScreen(mapScreen);
            }
        });


        // Layout the two buttons side by side
        table.add(winButton).pad(20).width(150).height(60);
        table.add(loseButton).pad(20).width(150).height(60);
    }

    @Override
    public void show() {
        // nothing special
    }

    @Override
    public void render(float delta) {
        // clear screen
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        // update & draw UI
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause()  { }
    @Override public void resume() { }
    @Override public void hide()   { }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
