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
// import com.badlogic.gdx.scenes.scene2d.ui.TextButton; // Not directly used for popup buttons in this version
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

// Import PackType for reward node logic
import io.github.grace.ni.fernan.PackOpenScreenFernan.PackType;


public class ConvergingMapScreen implements Screen {
    enum NodeType { PLAYER, COMBAT, REWARD, REST, MINIBOSS, BOSS }

    static class Node {
        final String id;
        final NodeType type;
        final Vector2 pos;
        final List<Node> next = new ArrayList<>();

        Node(String id, NodeType type, float x, float y) {
            this.id = id;
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
    private final Set<String> visitedNodeIds = new HashSet<>();

    private Texture texBg, texPlayer, texCombat, texReward, texRest, texMiniBoss, texBoss;
    private final List<Node> nodes = new ArrayList<>();
    private Node current;

    private Stage      uiStage;
    private boolean    showingPopup = false;
    private Table      popupTable;
    private BitmapFont fontHeading, fontBody;
    private Texture    popupBgTex, startBtnTex, openNowBtnTex, openLaterBtnTex, viewDecksBtnTex, proceedBtnTex;


    public ConvergingMapScreen(FernansGrace game, SaveProfile profile) {
        this.game    = game;
        this.profile = profile;
        batch        = new SpriteBatch();
        shape        = new ShapeRenderer();
        viewport     = new ScreenViewport();

        texBg       = new Texture(Gdx.files.internal("ui/map_background2.png"));
        texPlayer   = new Texture(Gdx.files.internal("ui/player_x.png"));
        texCombat   = new Texture(Gdx.files.internal("ui/combat_icon.png"));
        texReward   = new Texture(Gdx.files.internal("ui/reward_icon.png"));
        texRest     = new Texture(Gdx.files.internal("ui/rest_temple.png"));
        texMiniBoss = new Texture(Gdx.files.internal("ui/miniboss_icon.png"));
        texBoss     = new Texture(Gdx.files.internal("ui/boss_skull.png"));

        uiStage     = new Stage(new ScreenViewport(), batch);
        fontHeading = new BitmapFont(Gdx.files.internal("ui/black_adder_yellow.fnt"));
        fontBody    = new BitmapFont(Gdx.files.internal("ui/royal_hefana.fnt"));
        popupBgTex  = new Texture(Gdx.files.internal("ui/popupwindowbg.png"));
        startBtnTex = new Texture(Gdx.files.internal("ui/startbutton.png"));
        openNowBtnTex = new Texture(Gdx.files.internal("ui/opennowbutton.png"));
        openLaterBtnTex = new Texture(Gdx.files.internal("ui/openlaterbutton.png"));
        viewDecksBtnTex = new Texture(Gdx.files.internal("ui/viewdecksbutton.png"));
        proceedBtnTex = new Texture(Gdx.files.internal("ui/proceedbutton.png"));

        allYourDecks = (profile != null && profile.decks != null) ? profile.decks : new ArrayList<>();

        defineMap();

        if (profile != null) {
            if (profile.currentMapId != null) {
                Node foundNode = findNodeById(profile.currentMapId);
                this.current = (foundNode != null) ? foundNode : nodes.get(0);
            } else {
                this.current = nodes.get(0);
            }
            if (this.current != null && this.current.type != NodeType.PLAYER) {
                visitedNodeIds.add(this.current.id);
            }
        } else {
            this.current = nodes.get(0);
        }
        Gdx.input.setInputProcessor(uiStage);
    }

    private Node findNodeById(String id) {
        for (Node node : nodes) {
            if (node.id.equals(id)) {
                return node;
            }
        }
        return null;
    }

    private void defineMap() {
        Node n0  = new Node("n0",  NodeType.PLAYER,   150,  80);
        Node n1a = new Node("n1a", NodeType.COMBAT,   130, 210);
        Node n2a = new Node("n2a", NodeType.COMBAT,   200, 350);
        Node n3a = new Node("n3a", NodeType.REWARD,   320, 400);
        Node n4a = new Node("n4a", NodeType.MINIBOSS, 460, 440);
        Node n5a = new Node("n5a", NodeType.REST,     610, 460);
        Node n1b = new Node("n1b", NodeType.COMBAT,   340,  80);
        Node n2b = new Node("n2b", NodeType.REWARD,   480, 100);
        Node n3b = new Node("n3b", NodeType.MINIBOSS, 670, 140);
        Node n4b = new Node("n4b", NodeType.REWARD,   770, 270);
        Node nBoss = new Node("nBoss", NodeType.BOSS, 800, 430);

        n0 .next.add(n1a); n1a.next.add(n2a); n2a.next.add(n3a);
        n3a.next.add(n4a); n4a.next.add(n5a); n5a.next.add(nBoss);
        n0 .next.add(n1b); n1b.next.add(n2b); n2b.next.add(n3b);
        n3b.next.add(n4b); n4b.next.add(nBoss);

        nodes.clear();
        Collections.addAll(nodes, n0, n1a, n2a, n3a, n4a, n5a, n1b, n2b, n3b, n4b, nBoss);
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

        shape.setProjectionMatrix(viewport.getCamera().combined);
        shape.begin(ShapeRenderer.ShapeType.Line);
        for (Node n : nodes) {
            for (Node nextNode : n.next) {
                if (visitedNodeIds.contains(n.id) && visitedNodeIds.contains(nextNode.id)) {
                    shape.setColor(Color.YELLOW);
                } else if (visitedNodeIds.contains(n.id) || n == current) {
                    shape.setColor(Color.LIGHT_GRAY);
                } else {
                    shape.setColor(Color.DARK_GRAY);
                }
                shape.line(n.pos.x, n.pos.y, nextNode.pos.x, nextNode.pos.y);
            }
        }
        shape.end();

        batch.begin();
        for (Node n : nodes) {
            if (n != current) {
                if (visitedNodeIds.contains(n.id) && n.type != NodeType.PLAYER) {
                    batch.setColor(0.5f, 0.5f, 0.5f, 0.7f);
                } else {
                    batch.setColor(Color.WHITE);
                }
                Texture t = getTexture(n.type);
                float size = (n.type == NodeType.BOSS) ? 152f : (n.type == NodeType.MINIBOSS) ? 100f : 60f;
                if (n.type != NodeType.PLAYER) {
                    batch.draw(t, n.pos.x - size/2, n.pos.y - size/2, size, size);
                }
                batch.setColor(Color.WHITE);
            }
        }
        if (current != null) {
            float markerSize = 96f;
            batch.draw(texPlayer, current.pos.x - markerSize/2, current.pos.y - markerSize/2, markerSize, markerSize);
        }
        batch.end();

        if (Gdx.input.justTouched() && !showingPopup && current != null) {
            Vector2 touch = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touch);
            for (Node nxt : current.next) {
                boolean canMoveTo = !visitedNodeIds.contains(nxt.id) || nxt.type == NodeType.REST;
                if (canMoveTo && nxt.pos.dst(touch) < 32) {
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

    private void showPopup(final Node node) {
        showingPopup = true;
        popupTable = new Table();
        popupTable.setBackground(new TextureRegionDrawable(new TextureRegion(popupBgTex)));
        popupTable.setSize(700, 350);
        float vw = uiStage.getViewport().getWorldWidth(), vh = uiStage.getViewport().getWorldHeight();
        popupTable.setPosition((vw - popupTable.getWidth())/2, (vh - popupTable.getHeight())/2);

        Label.LabelStyle hStyle = new Label.LabelStyle(fontHeading, Color.YELLOW);
        Label.LabelStyle bStyle = new Label.LabelStyle(fontBody,    Color.WHITE);

        String titleText = "Unknown Node";
        String rewardText = "";
        String detailText = "";

        switch (node.type) {
            case COMBAT:
                titleText = "Combat Encounter";
                rewardText = "- 1 Artifact/Item Pack\n- 20 Coins";
                detailText = "\nPrepare for battle!";
                break;
            case MINIBOSS:
                titleText = "Miniboss Battle!";
                rewardText = "- 1 Divine Pack\n- 30 Coins";
                detailText = "\nA powerful foe guards this path!";
                break;
            case BOSS:
                titleText = "Final Boss!";
                rewardText = "- 1 God/Divine Pack\n- 50 Coins";
                detailText = "\nThe ultimate challenge awaits!";
                break;
            case REWARD:
                titleText = "Treasure Chest";
                rewardText = "- 1 God/Divine Pack\n- 30 Coins";
                detailText = "\nYou found a treasure! Open it now or save the pack for later.";
                break;
            case REST:
                titleText = "Rest Shrine";
                rewardText = "- Peace and recuperation.";
                detailText = "- Rest here to adjust your deck.\n- Heal all active/bench cards to full HP.";
                break;
            default:
                titleText = "Unknown Encounter";
                rewardText = "- ???";
                detailText = "An unknown encounter awaits.";
                break;
        }

        Label title = new Label(titleText, hStyle);
        title.setAlignment(Align.center);
        title.getStyle().font.getData().setScale(1.3f);

        Label rewardsHeaderLabel = new Label("Rewards:", hStyle);
        rewardsHeaderLabel.getStyle().font.getData().setScale(1.1f);

        Label rewardsDetailLabel = new Label(rewardText, bStyle);
        rewardsDetailLabel.setWrap(true);
        rewardsDetailLabel.getStyle().font.getData().setScale(0.7f);

        Label body = new Label(detailText, bStyle);
        body.setWrap(true);
        body.getStyle().font.getData().setScale(0.7f);

        popupTable.clear();
        popupTable.pad(20);
        popupTable.defaults().padBottom(8);
        popupTable.add(title).colspan(2).center().row();

        if (!rewardText.isEmpty()){
            popupTable.add(rewardsHeaderLabel).colspan(2).left().padLeft(20).row();
            popupTable.add(rewardsDetailLabel).width(600f).colspan(2).left().padLeft(40).row();
        }
        popupTable.add(body).width(600f).colspan(2).left().padLeft(20).padTop(5).row();


        if (node.type == NodeType.REWARD) {
            ImageButton openLater = new ImageButton(new TextureRegionDrawable(new TextureRegion(openLaterBtnTex)));
            openLater.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    hidePopup();
                    if (profile != null) {
                        profile.gachaCurrency += 30;
                        profile.addGodDivinePack(1);
                        SaveManager.saveProfile(profile);
                        Gdx.app.log("MapScreen", "Reward Node: Added 30 coins and 1 God/Divine Pack (Save for Later). Profile Saved.");
                    }
                    moveTo(node); // Player moves onto the reward node
                }
            });

            ImageButton openNow = new ImageButton(new TextureRegionDrawable(new TextureRegion(openNowBtnTex)));
            openNow.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    hidePopup();
                    if (profile != null) {
                        profile.gachaCurrency += 30;
                        // Don't add to stored packs here as it's being opened immediately.
                        // PackOpenScreenFernan will handle adding the actual cards from the pack to collection.
                        SaveManager.saveProfile(profile); // Save coin update
                        Gdx.app.log("MapScreen", "Reward Node: Added 30 coins. Opening God/Divine Pack now.");
                    }
                    moveTo(node); // Move player to the node first
                    // Then go to pack opening screen, passing 'this' (ConvergingMapScreen) as previousScreen
                    game.setScreen(new PackOpenScreenFernan(game, profile, 1, PackOpenScreenFernan.PackType.GOD_DIVINE, ConvergingMapScreen.this));
                }
            });
            Table buttonRow = new Table();
            buttonRow.defaults().pad(10);
            buttonRow.add(openLater).size(220, 80);
            buttonRow.add(openNow).  size(220, 80);
            popupTable.add(buttonRow).colspan(2).center().padTop(15).row();

        } else if (node.type == NodeType.REST) {
            ImageButton viewDecks = new ImageButton(new TextureRegionDrawable(new TextureRegion(viewDecksBtnTex)));
            ImageButton proceed   = new ImageButton(new TextureRegionDrawable(new TextureRegion(proceedBtnTex)));

            viewDecks.addListener(new ClickListener(){
                @Override public void clicked(InputEvent e, float x, float y) {
                    hidePopup();
                    moveTo(node);
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
            popupTable.add(row).colspan(2).center().padTop(15).row();

        } else {
            ImageButton start = new ImageButton(new TextureRegionDrawable(new TextureRegion(startBtnTex)));
            start.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    hidePopup();
                    game.setScreen(new BattleScreen(game, ConvergingMapScreen.this, profile, node));
                }
            });
            popupTable.add(start).size(180,60).colspan(2).center().padTop(15);
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
        if (popupTable != null) {
            popupTable.remove();
            popupTable = null;
        }
    }

    public void moveTo(Node nxtNodeCompleted) {
        if (nxtNodeCompleted == null) return;
        this.current = nxtNodeCompleted;
        visitedNodeIds.add(this.current.id);

        if (profile != null) {
            profile.currentMapId = this.current.id;
            SaveManager.saveProfile(profile);
            Gdx.app.log("MapScreen", "Player moved to node: " + this.current.id + ". Profile saved.");
        } else {
            Gdx.app.log("MapScreen", "Player moved to node: " + this.current.id + ". Profile is null, not saving.");
        }
    }

    public Node getCurrentNode() {
        return this.current;
    }

    @Override public void resize(int w, int h)    {
        viewport.update(w, h, true);
        uiStage.getViewport().update(w,h,true);
    }
    @Override public void pause()                  {}
    @Override public void resume()                 {}
    @Override public void hide()                   {}
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shape != null) shape.dispose();
        if (texBg != null) texBg.dispose();
        if (texPlayer != null) texPlayer.dispose();
        if (texCombat != null) texCombat.dispose();
        if (texReward != null) texReward.dispose();
        if (texRest != null) texRest.dispose();
        if (texMiniBoss != null) texMiniBoss.dispose();
        if (texBoss != null) texBoss.dispose();

        if (uiStage != null) uiStage.dispose();
        if (fontHeading != null) fontHeading.dispose();
        if (fontBody != null) fontBody.dispose();
        if (popupBgTex != null) popupBgTex.dispose();
        if (startBtnTex != null) startBtnTex.dispose();
        if (openNowBtnTex != null) openNowBtnTex.dispose();
        if (openLaterBtnTex != null) openLaterBtnTex.dispose();
        if (viewDecksBtnTex != null) viewDecksBtnTex.dispose();
        if (proceedBtnTex != null) proceedBtnTex.dispose();
    }
}
