package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameMap1Screen implements Screen {

    final FernansGrace game;

    private Stage stage;
    private Texture combatTex, challengeTex, rewardTex, reposeTex, bossTex, playerTex, popupBgTex, dashedLineTex;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture mapBgTex;


    private enum NodeType { COMBAT, CHALLENGE, REWARD, REPOSE, BOSS, PLAYER }

    private static class Node {
        int x, y;
        NodeType type;
        boolean active;

        public Node(int x, int y, NodeType type) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.active = true;
        }

        public Vector2 getPosition() {
            return new Vector2(x, y);
        }
    }

    private List<Node> nodes = new ArrayList<>();
    private Node playerNode;

    public GameMap1Screen(final FernansGrace game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        mapBgTex = new Texture(Gdx.files.internal("ui/mapbg.png"));
        font = new BitmapFont();
        font.getData().setScale(2f);

        // Load Textures
        combatTex = new Texture(Gdx.files.internal("ui/combatnode.png"));
        challengeTex = new Texture(Gdx.files.internal("ui/challengenode.png"));
        rewardTex = new Texture(Gdx.files.internal("ui/rewardnode.png"));
        reposeTex = new Texture(Gdx.files.internal("ui/reposenode.png"));
        bossTex = new Texture(Gdx.files.internal("ui/bossnode.png"));
        playerTex = new Texture(Gdx.files.internal("ui/playermarker.png")); // e.g. yellow X
        popupBgTex = new Texture(Gdx.files.internal("ui/popupwindowbg.png"));
        dashedLineTex = new Texture(Gdx.files.internal("ui/dashed_line.png")); // if used


        createMapNodes();
        setupInput();
    }

    private void createMapNodes() {
        nodes.add(new Node(100, 100, NodeType.PLAYER)); // Starting point
        nodes.add(new Node(200, 200, NodeType.COMBAT));
        nodes.add(new Node(300, 300, NodeType.CHALLENGE));
        nodes.add(new Node(400, 400, NodeType.REPOSE));
        nodes.add(new Node(500, 500, NodeType.REWARD));
        nodes.add(new Node(600, 600, NodeType.BOSS));

        playerNode = nodes.get(0); // Yellow X start
    }

    private void setupInput() {
        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                for (Node node : nodes) {
                    if (node != playerNode && node.active &&
                        playerNode.getPosition().dst(node.getPosition()) <= 150) {

                        showPopupForNode(node);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void showPopupForNode(Node node) {
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        Image bg = new Image(new TextureRegionDrawable(new TextureRegion(popupBgTex)));
        bg.setSize(600, 300);

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.YELLOW);
        Label label = new Label("Enemy Name", labelStyle);

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.BLACK;
        btnStyle.up = new TextureRegionDrawable(new TextureRegion(new Texture("ui/popupwindowbg.png")));

        TextButton actionButton;

        switch (node.type) {
            case COMBAT:
            case CHALLENGE:
            case BOSS:
                actionButton = new TextButton("Start", btnStyle);
                break;
            case REWARD:
                actionButton = new TextButton("Open Now / Open Later", btnStyle);
                break;
            case REPOSE:
                actionButton = new TextButton("View Decks / Proceed", btnStyle);
                break;
            default:
                actionButton = new TextButton("OK", btnStyle);
        }

        actionButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Simulate player moving after "winning"
                playerNode = node;
                nodes.removeIf(n -> n == node); // Clear visited node
                stage.clear(); // Remove popup
                setupInput();  // Re-enable click listener
            }
        });

        table.add(label).padBottom(40).row();
        table.add(actionButton).padTop(40);

        Stack stack = new Stack();
        stack.setFillParent(true);
        stack.add(bg);
        stack.add(table);
        stage.addActor(stack);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        batch.begin();

        batch.draw(mapBgTex, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Draw lines
        for (Node a : nodes) {
            for (Node b : nodes) {
                if (a != b && a.getPosition().dst(b.getPosition()) <= 150) {
                    drawDashedLine(a.getPosition(), b.getPosition());
                }
            }
        }

        // Draw nodes
        for (Node node : nodes) {
            Texture tex = null;
            switch (node.type) {
                case COMBAT:
                    tex = combatTex;
                    break;
                case CHALLENGE:
                    tex = challengeTex;
                    break;
                case REWARD:
                    tex = rewardTex;
                    break;
                case REPOSE:
                    tex = reposeTex;
                    break;
                case BOSS:
                    tex = bossTex;
                    break;
                case PLAYER:
                    tex = playerTex;
                    break;
            }
            batch.draw(tex, node.x - 32, node.y - 32, 64, 64);
        }

        batch.end();
        stage.act(delta);
        stage.draw();
    }

    private void drawDashedLine(Vector2 start, Vector2 end) {
        float length = start.dst(end);
        Vector2 dir = end.cpy().sub(start).nor();
        Vector2 pos = new Vector2(start);
        float dashLength = 20f;

        for (float traveled = 0; traveled < length; traveled += dashLength * 2) {
            batch.draw(dashedLineTex, pos.x, pos.y, dashLength, 5);
            pos.add(dir.scl(dashLength * 2));
        }
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        combatTex.dispose();
        challengeTex.dispose();
        rewardTex.dispose();
        reposeTex.dispose();
        bossTex.dispose();
        playerTex.dispose();
        popupBgTex.dispose();
        dashedLineTex.dispose();
        font.dispose();
        mapBgTex.dispose();
    }
}
