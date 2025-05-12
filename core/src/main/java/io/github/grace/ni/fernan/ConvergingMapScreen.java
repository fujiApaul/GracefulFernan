package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConvergingMapScreen implements Screen {
    enum NodeType { PLAYER, COMBAT, REWARD, REST, MINIBOSS, BOSS }

    static class Node {
        final NodeType type;
        final Vector2 pos;
        final List<Node> next = new ArrayList<>();

        Node(NodeType type, float x, float y) {
            this.type = type;
            this.pos = new Vector2(x, y);
        }
    }

    private final FernansGrace game;
    private final SaveProfile profile;
    private final List<DeckSelectionScreen.Deck> allYourDecks;

    private SpriteBatch   batch;
    private ShapeRenderer shape;
    private Viewport      viewport;
    private final Set<Node> visited = new HashSet<>();

    // map textures
    private Texture texBg, texPlayer, texCombat, texReward, texRest, texMiniBoss, texBoss;

    // map data
    private final List<Node> nodes = new ArrayList<>();
    private Node current;

    // popup UI
    private Stage      uiStage;
    private boolean    showingPopup = false;
    private Table      popupTable;
    private BitmapFont fontHeading, fontBody;
    private Texture    popupBgTex, startBtnTex;

    public ConvergingMapScreen(FernansGrace game, SaveProfile profile) {
        this.game    = game;
        this.profile = profile;
        batch        = new SpriteBatch();
        shape        = new ShapeRenderer();
        viewport     = new ScreenViewport();

        // load map assets
        texBg       = new Texture(Gdx.files.internal("ui/map_background2.png"));
        texPlayer   = new Texture(Gdx.files.internal("ui/player_x.png"));
        texCombat   = new Texture(Gdx.files.internal("ui/combat_icon.png"));
        texReward   = new Texture(Gdx.files.internal("ui/reward_icon.png"));
        texRest     = new Texture(Gdx.files.internal("ui/rest_temple.png"));
        texMiniBoss = new Texture(Gdx.files.internal("ui/miniboss_icon.png"));
        texBoss     = new Texture(Gdx.files.internal("ui/boss_skull.png"));

        // popup assets
        uiStage     = new Stage(new ScreenViewport(), batch);
        fontHeading = new BitmapFont(Gdx.files.internal("ui/black_adder_yellow.fnt"));
        fontBody    = new BitmapFont(Gdx.files.internal("ui/royal_hefana.fnt"));
        popupBgTex  = new Texture(Gdx.files.internal("ui/popupwindowbg.png"));
        startBtnTex = new Texture(Gdx.files.internal("ui/startbutton.png"));

        allYourDecks = profile.decks;

        defineMap();
        current = nodes.get(0);

        Gdx.input.setInputProcessor(uiStage);
    }

    public ConvergingMapScreen(FernansGrace game, SaveProfile profile, Node startNode) {
        this(game, profile);
        this.current = startNode;
    }

    private void defineMap() {
        Node n0  = new Node(NodeType.PLAYER,   150,  80);
        Node n1a = new Node(NodeType.COMBAT,   130, 210);
        Node n2a = new Node(NodeType.COMBAT,   200, 350);
        Node n3a = new Node(NodeType.REWARD,   320, 400);
        Node n4a = new Node(NodeType.MINIBOSS, 460, 440);
        Node n5a = new Node(NodeType.REST,     610, 460);

        Node n1b = new Node(NodeType.COMBAT,   340,  80);
        Node n2b = new Node(NodeType.REWARD,   480, 100);
        Node n3b = new Node(NodeType.MINIBOSS, 670, 140);
        Node n4b = new Node(NodeType.REWARD,   770, 270);

        Node nBoss = new Node(NodeType.BOSS,   800, 430);

        n0 .next.add(n1a); n1a.next.add(n2a); n2a.next.add(n3a);
        n3a.next.add(n4a); n4a.next.add(n5a); n5a.next.add(nBoss);
        n0 .next.add(n1b); n1b.next.add(n2b); n2b.next.add(n3b);
        n3b.next.add(n4b); n4b.next.add(nBoss);

        nodes.clear();
        Collections.addAll(nodes,
            n0,
            n1a, n2a, n3a, n4a, n5a,
            n1b, n2b, n3b, n4b,
            nBoss
        );
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(uiStage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0,0,0,1);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.draw(texBg, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        batch.end();

        batch.begin();
        for (Node n : nodes) {
            if (!visited.contains(n) && n != current) {
                Texture t = getTexture(n.type);
                float size = (n.type == NodeType.BOSS) ? 152f :
                    (n.type == NodeType.MINIBOSS) ? 100f : 60f;
                batch.draw(t, n.pos.x - size/2, n.pos.y - size/2, size, size);
            }
        }
        float markerSize = 96f;
        batch.draw(texPlayer,
            current.pos.x - markerSize/2,
            current.pos.y - markerSize/2,
            markerSize, markerSize);
        batch.end();

        if (Gdx.input.justTouched() && !showingPopup) {
            Vector2 touch = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touch);
            for (Node nxt : current.next) {
                if (nxt.pos.dst(touch) < 32) {
                    showPopup(nxt);
                    break;
                }
            }
        }

        if (showingPopup) {
            uiStage.act(delta);
            uiStage.draw();
        }
    }

    private void showPopup(Node node) {
        showingPopup = true;
        popupTable = new Table();
        popupTable.setBackground(new TextureRegionDrawable(new TextureRegion(popupBgTex)));
        popupTable.setSize(700, 350);
        float vw = uiStage.getViewport().getWorldWidth(), vh = uiStage.getViewport().getWorldHeight();
        popupTable.setPosition((vw - 700)/2, (vh - 350)/2);

        Label.LabelStyle h = new Label.LabelStyle(fontHeading, Color.YELLOW);
        Label.LabelStyle b = new Label.LabelStyle(fontBody,    Color.WHITE);

        Label title = new Label("Enemy Name", h);
        title.setAlignment(Align.center);
        title.getStyle().font.getData().setScale(1.3f);

        Label rewards = new Label("Rewards:", h);
        rewards.getStyle().font.getData().setScale(1.1f);

        String detail;
        switch (node.type) {
            case COMBAT: case MINIBOSS:
                detail = "- 1x Card Pack\n- XP Bonus\n\nBattle to claim rewards."; break;
            case REWARD:
                detail = "- Treasure chest!\n- You may open it now,\n  or come back later."; break;
            default:
                detail = "- Rest here to adjust your deck\n- Heal up to full HP."; break;
        }
        Label body = new Label(detail, b);
        body.setWrap(true);
        body.getStyle().font.getData().setScale(0.6f);

        popupTable.clear();
        popupTable.pad(20);
        popupTable.defaults().growX().pad(5);
        popupTable.add(title).padLeft(40).colspan(2).center().row();
        popupTable.add(rewards).padLeft(40).colspan(2).left().padBottom(8).row();
        popupTable.add(body).padLeft(60).width(500).colspan(2).row();

        if (node.type == NodeType.REWARD) {
            if (node.type == NodeType.REWARD) {
                // Open Later: advance immediately, stay on map
                ImageButton openLater = new ImageButton(new TextureRegionDrawable(
                    new TextureRegion(new Texture(Gdx.files.internal("ui/openlaterbutton.png")))));
                openLater.addListener(new ClickListener() {
                    @Override public void clicked(InputEvent e, float x, float y) {
                        hidePopup();
                        moveTo(node);
                    }
                });

                // Open Now: go to the reward‐detail screen
                ImageButton openNow = new ImageButton(new TextureRegionDrawable(
                    new TextureRegion(new Texture(Gdx.files.internal("ui/opennowbutton.png")))));
                openNow.addListener(new ClickListener() {
                    @Override public void clicked(InputEvent e, float x, float y) {
                        hidePopup();
                        game.setScreen(new RewardDetailScreen(game, ConvergingMapScreen.this, node));
                    }
                });

                // Group them centered
                Table buttonRow = new Table();
                buttonRow.defaults().pad(10);
                buttonRow.add(openLater).size(220, 80);
                buttonRow.add(openNow).  size(220, 80);

                popupTable.add(buttonRow)
                    .colspan(2)
                    .center()
                    .padTop(20)
                    .row();
            }
        } else if (node.type == NodeType.REST) {
            ImageButton viewDecks = new ImageButton(new TextureRegionDrawable(
                new TextureRegion(new Texture(Gdx.files.internal("ui/viewdecksbutton.png")))));
            ImageButton proceed   = new ImageButton(new TextureRegionDrawable(
                new TextureRegion(new Texture(Gdx.files.internal("ui/proceedbutton.png")))));

            viewDecks.addListener(new ClickListener(){
                @Override public void clicked(InputEvent e, float x, float y) {
                    hidePopup();
                    game.setScreen(new DeckSelectionScreen(
                        game,
                        profile,
                        () -> game.setScreen(ConvergingMapScreen.this)
                    ));
                }
            });
            proceed.addListener(new ClickListener(){
                @Override public void clicked(InputEvent e, float x, float y) {
                    hidePopup();
                    moveTo(node);
                }
            });

            Table row = new Table();
            row.defaults().pad(10);
            row.add(viewDecks).size(220,80);
            row.add(proceed).size(220,80);
            popupTable.add(row).colspan(2).center().padTop(20).row();
        } else {
            ImageButton start = new ImageButton(new TextureRegionDrawable(new TextureRegion(startBtnTex)));
            start.addListener(new ClickListener(){
                @Override public void clicked(InputEvent e, float x, float y) {
                    showingPopup = false;
                    uiStage.clear();
                    game.setScreen(new BattleScreen(game, ConvergingMapScreen.this, node));
                }
            });
            popupTable.add(start).size(180,60).colspan(2).center().padTop(20);
        }

        uiStage.clear();
        uiStage.addActor(popupTable);
    }

    private Texture getTexture(NodeType t) {
        switch (t) {
            case PLAYER:   return texPlayer;
            case COMBAT:   return texCombat;
            case REWARD:   return texReward;
            case REST:     return texRest;
            case MINIBOSS: return texMiniBoss;
            case BOSS:     return texBoss;
        }
        return texCombat;
    }

    public void hidePopup() {
        showingPopup = false;
        uiStage.clear();
        Gdx.input.setInputProcessor(uiStage);
    }

    public void moveTo(Node nxt) {
        visited.add(current);
        current = nxt;
    }

    @Override public void resize(int w, int h)    { viewport.update(w, h, true); }
    @Override public void pause()                  {}
    @Override public void resume()                 {}
    @Override public void hide()                   {}
    @Override
    public void dispose() {
        batch.dispose();
        shape.dispose();
        texBg.dispose();
        texPlayer.dispose();
        texCombat.dispose();
        texReward.dispose();
        texRest.dispose();
        texMiniBoss.dispose();
        texBoss.dispose();
        popupBgTex.dispose();
        startBtnTex.dispose();
        fontHeading.dispose();
        fontBody.dispose();
    }
}
