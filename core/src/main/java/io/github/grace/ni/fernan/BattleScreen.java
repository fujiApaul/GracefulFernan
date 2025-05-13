package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.List;

public class BattleScreen implements Screen {
    private static final int STARTING_HAND = 5;
    private static final int MAX_ACTIVE_DEATHS = 5;
    private static final int MAX_FAITH = 10;
    private static final int MAX_BENCH_SLOTS = 3;

    private final FernansGrace game;
    private final ConvergingMapScreen mapScreen;
    private final SaveProfile profile;

    private List<CardSystem.Card> playerDeck;
    private List<CardSystem.Card> playerHand;
    private List<CardSystem.Card> playerBench;
    private List<CardSystem.Card> playerGraveyard;
    private CardSystem.Card playerActive;
    private int activeDeathCount = 0;
    private int playerFaith = 1;

    private CardSystem.Card enemyActive;
    private boolean playerTurn = true;

    private Stage stage;
    private Skin skin;
    private BitmapFont font;
    private Table root;
    private Table topHudTable;
    private ScrollPane handScroll;
    private Table handTable;
    private Container<Table> benchContainer;
    private Table benchTable;
    private Table graveTable;
    private Table logTable;
    private ScrollPane logScroll;
    private Label roundLabel, faithLabel, deathCountLabel, deckCountLabel;
    private TextButton endTurnBtn;
    private Stack enemyStack, activeStack;
    private Label enemyHpLabel, activeHpLabel;
    private int roundNumber = 1;

    public BattleScreen(FernansGrace game, ConvergingMapScreen mapScreen, SaveProfile profile) {
        this.game = game;
        this.mapScreen = mapScreen;
        this.profile = profile;

        DeckSelectionScreen.Deck activeDeck = profile.decks.stream()
            .filter(DeckSelectionScreen.Deck::isActive).findFirst().orElse(null);
        if (activeDeck == null || activeDeck.getCards().isEmpty())
            throw new IllegalStateException("No active deck selected or deck is empty!");

        playerDeck = new ArrayList<>(activeDeck.getCards());
        playerHand = new ArrayList<>();
        playerBench = new ArrayList<>();
        playerGraveyard = new ArrayList<>();
        playerActive = null;
        drawCards(STARTING_HAND);

        List<CardSystem.Card> all = CardSystem.loadCardsFromJson();
        enemyActive = all.get(MathUtils.random(all.size() - 1));

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        font = new BitmapFont(Gdx.files.internal("ui/smalligator_white.fnt"));

        buildUI();
    }

    private void buildUI() {
        stage.clear();
        root = new Table(skin);
        root.setFillParent(true);
        stage.addActor(root);

        // HUD
        topHudTable = new Table(skin);
        roundLabel = new Label("Round " + roundNumber, new Label.LabelStyle(font, Color.YELLOW));
        faithLabel = new Label("Faith: " + playerFaith, new Label.LabelStyle(font, Color.CYAN));
        deathCountLabel = new Label("Lives: " + (MAX_ACTIVE_DEATHS - activeDeathCount), new Label.LabelStyle(font, Color.RED));
        topHudTable.add(roundLabel).expandX().left();
        topHudTable.add(faithLabel).expandX().center();
        topHudTable.add(deathCountLabel).expandX().right();

        // Enemy slot
        enemyStack = createCardStack(enemyActive.getImagePath(), enemyActive.getHealth());
        enemyHpLabel = (Label)((Table)enemyStack.getChildren().get(1)).getChildren().first();
        // Player slot
        activeStack = createCardStack("ui/card_slot_empty.png", 0);
        activeHpLabel = (Label)((Table)activeStack.getChildren().get(1)).getChildren().first();
        activeStack.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (playerActive == null || !playerTurn) return;
                showSkillDialog();
            }
        });

        // Hand Scroll
        handTable = new Table(skin);
        handScroll = new ScrollPane(handTable, skin);
        handScroll.setFadeScrollBars(false);
        handScroll.setScrollingDisabled(false, true);
        handScroll.setSize(440, 160);

        // Bench Container (fixed size)
        benchTable = new Table(skin);
        benchContainer = new Container<>(benchTable);
        benchContainer.size(MAX_BENCH_SLOTS * 80 + (MAX_BENCH_SLOTS+1)*6, 112);

        // Graveyard
        graveTable = new Table(skin);

        // Log (between slots)
        logTable = new Table(skin);
        logScroll = new ScrollPane(logTable, skin);
        logScroll.setFadeScrollBars(false);
        logScroll.setScrollingDisabled(false, false);
        logScroll.setSize(200, 160);

        // Deck label & End button
        deckCountLabel = new Label("Deck: " + playerDeck.size(), new Label.LabelStyle(font, Color.WHITE));
        endTurnBtn = new TextButton("End Turn", skin);
        endTurnBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (playerActive == null) {
                    log("Cannot end turn: no active card.");
                } else {
                    log("Player ends turn.");
                    enemyTurn();
                }
            }
        });

        // Layout with fixed cell sizes
        root.top().pad(10);
        root.add(topHudTable).colspan(5).fillX().row();
        root.add(enemyStack).width(220).height(280).padRight(6);
        root.add(logScroll).width(200).height(160).padRight(6);
        root.add(benchContainer).padRight(20);
        root.add(activeStack).width(180).height(240).padRight(20);
        root.add(handScroll).padRight(6).row();
        root.add(graveTable).colspan(2).left().padTop(5);
        root.add(new Label("", skin)).expandX();
        root.add(deckCountLabel).right().padTop(5).colspan(2).row();
        root.add(endTurnBtn).colspan(5).center().padTop(10).row();

        refreshAll();
        log("Battle started.");
    }

    private Stack createCardStack(String imgPath, int hp) {
        Stack stack = new Stack();
        Image img = new Image(new Texture(imgPath));
        img.setScaling(Scaling.fit);
        Table overlay = new Table(skin);
        overlay.top();
        Label hpLabel = new Label(hp > 0 ? String.valueOf(hp) : "", new Label.LabelStyle(font, Color.WHITE));
        hpLabel.setFontScale(0.6f);
        overlay.add(hpLabel).padTop(2).row();
        stack.add(img);
        stack.add(overlay);
        return stack;
    }

    // Refresh methods
    private void refreshHandUI() {
        handTable.clear();
        for (CardSystem.Card c : playerHand) {
            ImageButton btn = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(c.getImagePath()))));
            btn.setTouchable(Touchable.enabled);
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) {
                    if (!playerTurn) return;
                    new Dialog("Play this card?", skin) {
                        protected void result(Object obj) {
                            if (Boolean.TRUE.equals(obj)) handleHandCard(c);
                        }
                    }.text("Play " + c.getName() + "?")
                        .button("Yes", true)
                        .button("No", false)
                        .show(stage);
                }
            });
            handTable.add(btn).size(80, 112).pad(3);
        }
        handTable.row();
    }

    private void refreshBenchUI() {
        benchTable.clear();
        Texture emptyTex = new Texture(Gdx.files.internal("ui/card_slot_empty.png"));
        for (int i = 0; i < MAX_BENCH_SLOTS; i++) {
            if (i < playerBench.size()) {
                CardSystem.Card c = playerBench.get(i);
                Stack s = createCardStack(c.getImagePath(), c.getHealth());
                benchTable.add(s).size(80,112).pad(3);
            } else {
                Image img = new Image(emptyTex);
                benchTable.add(img).size(80,112).pad(3);
            }
        }
        benchTable.row();
    }

    private void refreshGraveUI() {
        graveTable.clear();
        for (CardSystem.Card c : playerGraveyard) {
            Stack s = createCardStack(c.getImagePath(), 0);
            graveTable.add(s).size(50,70).pad(2);
        }
        graveTable.row();
    }

    private void updateActiveStack() {
        activeHpLabel.setText(playerActive != null ? String.valueOf(playerActive.getHealth()) : "");
        Image img = (Image)activeStack.getChildren().first();
        img.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(
            playerActive != null ? playerActive.getImagePath() : "ui/card_slot_empty.png"))));
    }

    private void updateEnemyStack() {
        enemyHpLabel.setText(String.valueOf(enemyActive.getHealth()));
        Image img = (Image)enemyStack.getChildren().first();
        img.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(enemyActive.getImagePath()))));
    }

    private void refreshAll() {
        refreshHandUI();
        refreshBenchUI();
        refreshGraveUI();
        updateActiveStack();
        updateEnemyStack();
        deckCountLabel.setText("Deck: " + playerDeck.size());
        deathCountLabel.setText("Lives: " + (MAX_ACTIVE_DEATHS - activeDeathCount));
        faithLabel.setText("Faith: " + playerFaith);
        roundLabel.setText("Round " + roundNumber);
    }

    private void handleHandCard(CardSystem.Card c) {
        switch (c.getType()) {
            case GOD:
            case DIVINE:
                if (playerActive == null) {
                    playerActive = c;
                    log("Player set active to " + c.getName());
                } else if (playerBench.size() < MAX_BENCH_SLOTS) {
                    playerBench.add(c);
                    log("Player benched " + c.getName());
                }
                playerHand.remove(c);
                break;
            case ITEM:
            case ARTIFACT:
                if (!c.getSkills().isEmpty() && playerActive != null) {
                    CardSystem.Skill skl = c.getSkills().get(0);
                    log("Player uses " + skl.getName());
                    int oldHp = enemyActive.getHealth();
                    skl.apply(playerActive, enemyActive);
                    log("Enemy HP: " + oldHp + " -> " + enemyActive.getHealth());
                    playerGraveyard.add(c);
                    playerHand.remove(c);
                }
                break;
        }
        refreshAll();
    }

    private void showSkillDialog() {
        List<CardSystem.Skill> skills = playerActive.getSkills();
        String[] names = new String[skills.size()];
        for (int i = 0; i < skills.size(); i++) {
            CardSystem.Skill sk = skills.get(i);
            names[i] = sk.getName() + " (" + sk.getCost() + ")";
        }
        SelectBox<String> box = new SelectBox<>(skin);
        box.setItems(names);
        Dialog d = new Dialog("Choose Skill", skin) {
            protected void result(Object obj) {
                if (!Boolean.TRUE.equals(obj)) return;
                int idx = box.getSelectedIndex();
                CardSystem.Skill sel = skills.get(idx);
                if ("Retreat".equals(sel.getName())) {
                    showRetreatDialog();
                } else if (playerFaith < sel.getCost()) {
                    log("Insufficient faith for " + sel.getName());
                } else {
                    log("Player uses " + sel.getName());
                    int oldHp = enemyActive.getHealth();
                    sel.apply(playerActive, enemyActive);
                    playerFaith -= sel.getCost();
                    log("Enemy HP: " + oldHp + " -> " + enemyActive.getHealth());
                    refreshAll();
                    checkEnemyDefeated();
                }
            }
        };
        d.getContentTable().add(box).row();
        d.button("Use", true);
        d.button("Cancel", false);
        d.show(stage);
    }

    private void showRetreatDialog() {
        if (playerBench.isEmpty()) {
            log("Cannot retreat: no bench slots available.");
            return;
        }
        String[] names = new String[playerBench.size()];
        for (int i = 0; i < playerBench.size(); i++) names[i] = playerBench.get(i).getName();
        SelectBox<String> box = new SelectBox<>(skin);
        box.setItems(names);
        Dialog d = new Dialog("Retreat", skin) {
            protected void result(Object obj) {
                if (Boolean.TRUE.equals(obj)) {
                    int idx = box.getSelectedIndex();
                    CardSystem.Card swap = playerBench.get(idx);
                    playerBench.set(idx, playerActive);
                    playerActive = swap;
                    log("Player retreated: swapped with " + swap.getName());
                    refreshAll();
                }
            }
        };
        d.getContentTable().add(box).row();
        d.button("Swap", true);
        d.button("Cancel", false);
        d.show(stage);
    }

    private void enemyTurn() {
        roundNumber++;
        log("-- Enemy turn --");
        if (playerActive != null) {
            CardSystem.Skill sk = enemyActive.getSkills().get(MathUtils.random(enemyActive.getSkills().size() - 1));
            log("Enemy uses " + sk.getName());
            int oldHp = playerActive.getHealth();
            sk.apply(enemyActive, playerActive);
            log("Active HP: " + oldHp + " -> " + playerActive.getHealth());
            checkPlayerDefeated();
        }
        if (playerFaith < MAX_FAITH) playerFaith++;
        drawCards(1);
        refreshAll();
        playerTurn = true;
    }

    private void checkEnemyDefeated() {
        if (enemyActive.getHealth() <= 0) {
            log("Enemy defeated!");
            new Dialog("Victory", skin) {
                protected void result(Object obj) {
                    game.setScreen(mapScreen);
                }
            }.text("You WIN!").button("OK", true).show(stage);
        }
    }

    private void checkPlayerDefeated() {
        if (playerActive != null && playerActive.getHealth() <= 0) {
            log("Active " + playerActive.getName() + " defeated.");
            playerGraveyard.add(playerActive);
            activeDeathCount++;
            playerActive = null;
            refreshAll();
            if (activeDeathCount >= MAX_ACTIVE_DEATHS || playerBench.isEmpty()) {
                log("All lives lost.");
                new Dialog("Defeat", skin) {
                    protected void result(Object obj) {
                        game.setScreen(new MainFernan(game));
                    }
                }.text("You LOSE!").button("OK", true).show(stage);
            }
        }
    }

    private void drawCards(int n) {
        for (int i = 0; i < n && !playerDeck.isEmpty(); i++) playerHand.add(playerDeck.remove(0));
    }

    private void log(String msg) {
        Label lbl = new Label(msg, new Label.LabelStyle(font, Color.LIGHT_GRAY));
        lbl.setFontScale(0.8f);
        logTable.add(lbl).left().row();
        logScroll.layout();
        logScroll.scrollTo(0, 0, 0, 0);
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void render(float delta) { ScreenUtils.clear(Color.DARK_GRAY); stage.act(delta); stage.draw(); }
    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); skin.dispose(); font.dispose(); }
}
