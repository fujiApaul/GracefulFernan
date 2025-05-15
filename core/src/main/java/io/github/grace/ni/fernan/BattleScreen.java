package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

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
    private final ConvergingMapScreen.Node battleNode; // Store the map node this battle is for

    public static final float VIRTUAL_WIDTH = 1600f;
    public static final float VIRTUAL_HEIGHT = 900f;

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
    private BitmapFont font; // General font, consider using specific fonts for different UI parts if needed
    private Table root;
    private Table topHudTable;
    private ScrollPane handScroll;
    private Table handTable;
    private Container<Table> benchContainer;
    private Table benchTable;
    private Table deckAndGraveTable;
    private Table battleLogTableContainer;
    private ScrollPane logScroll;
    private Label roundLabel, faithLabel, deathCountLabel, deckCountLabel;
    private Stack enemyStack, activeStack;
    private Label enemyHpLabel, activeHpLabel;
    private int roundNumber = 1;

    private Table skillsAndEndTable;
    private TextButton endTurnBtn;

    private Table graveTable;
    private Table logTable;

    // --- Size Constants ---
    private final float CARD_WIDTH_ACTIVE_V = VIRTUAL_WIDTH * 0.16f;
    private final float CARD_HEIGHT_ACTIVE_V = CARD_WIDTH_ACTIVE_V * 1.4f;
    private final float CARD_WIDTH_BENCH_HAND_V = VIRTUAL_WIDTH * 0.115f; // Increased for larger hand/bench cards
    private final float CARD_HEIGHT_BENCH_HAND_V = CARD_WIDTH_BENCH_HAND_V * 1.4f;
    private final float CARD_WIDTH_GRAVE_V = VIRTUAL_WIDTH * 0.05f;
    private final float CARD_HEIGHT_GRAVE_V = CARD_WIDTH_GRAVE_V * 1.4f;

    private final float ACTION_MENU_WIDTH_V = VIRTUAL_WIDTH * 0.18f;
    private final float ACTION_MENU_HEIGHT_V = CARD_HEIGHT_ACTIVE_V;
    private final float LOG_MENU_WIDTH_V = VIRTUAL_WIDTH * 0.25f;    // Increased log box width
    private final float LOG_MENU_HEIGHT_V = CARD_HEIGHT_ACTIVE_V;   // Height for log box

    // --- Font Scale Constants ---
    private final float HUD_FONT_SCALE_V = 1.5f * (VIRTUAL_WIDTH / 1600f);
    private final float SKILL_BUTTON_FONT_SCALE_V = 2.4f * (VIRTUAL_WIDTH / 1600f);  // Doubled
    private final float LOG_FONT_SCALE_V = 1.2f * (VIRTUAL_WIDTH / 1600f);
    private final float DIALOG_TEXT_FONT_SCALE_V = 2.4f * (VIRTUAL_WIDTH / 1600f); // Doubled
    private final float DIALOG_BUTTON_FONT_SCALE_V = 2.4f * (VIRTUAL_WIDTH / 1600f); // Doubled


    public BattleScreen(FernansGrace game, ConvergingMapScreen mapScreen, SaveProfile profile, ConvergingMapScreen.Node battleNode) {
        this.game = game;
        this.mapScreen = mapScreen;
        this.profile = profile;
        this.battleNode = battleNode;

        this.stage = new Stage(new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));
        Gdx.input.setInputProcessor(this.stage);

        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        this.font = new BitmapFont(Gdx.files.internal("ui/smalligator_white.fnt"));
        this.logTable = new Table(skin);

        DeckSelectionScreen.Deck activeDeck = null;
        if (profile != null && profile.decks != null) {
            activeDeck = profile.decks.stream()
                .filter(DeckSelectionScreen.Deck::isActive).findFirst().orElse(null);
        }

        if (activeDeck == null || activeDeck.getCards().isEmpty()) {
            logSafe("CRITICAL: No active deck or empty. Using placeholder deck.");
            playerDeck = new ArrayList<>();
            List<CardSystem.Card> allCardsFallback = CardSystem.loadCardsFromJson();
            if (!allCardsFallback.isEmpty()) {
                for (int i = 0; i < Math.min(5, allCardsFallback.size()); i++) {
                    playerDeck.add(allCardsFallback.get(i));
                }
                if (playerDeck.isEmpty()) {
                    logSafe("CRITICAL FALLBACK FAILED: No cards available at all.");
                }
            } else {
                logSafe("CRITICAL: cards.json seems empty or unloadable for fallback.");
            }
        } else {
            playerDeck = new ArrayList<>(activeDeck.getCards());
        }

        playerHand = new ArrayList<>();
        playerBench = new ArrayList<>();
        playerGraveyard = new ArrayList<>();
        playerActive = null;

        buildUI();
        drawCards(STARTING_HAND);

        // Enemy Selection Logic
        List<CardSystem.Card> allCards = CardSystem.loadCardsFromJson();
        if (allCards.isEmpty()) {
            logSafe("CRITICAL ERROR: No cards loaded from cards.json. Cannot select an enemy.");
            this.enemyActive = new CardSystem.Card("Fallback Enemy", 30, new ArrayList<>(), CardSystem.CardType.DIVINE, CardSystem.CardPantheon.NONE, "ui/card_slot_empty.png");
        } else {
            String enemyName = null;
            if (this.battleNode != null) {
                switch (this.battleNode.type) {
                    case COMBAT:
                        enemyName = "Cerberus";
                        break;
                    case MINIBOSS:
                        enemyName = "Medusa";
                        break;
                    case BOSS:
                        enemyName = "Minotaur";
                        break;
                    default:
                        logSafe("Warning: Battle initiated for non-combat related node type (" + this.battleNode.type + ") or null node. Picking random enemy.");
                        if (!allCards.isEmpty()) {
                            this.enemyActive = allCards.get(MathUtils.random(allCards.size() - 1));
                        } else {
                            this.enemyActive = new CardSystem.Card("Random Default", 50, new ArrayList<>(), CardSystem.CardType.DIVINE, CardSystem.CardPantheon.NONE, "ui/card_slot_empty.png");
                        }
                        break;
                }
            } else {
                logSafe("CRITICAL: battleNode is null in BattleScreen constructor. Picking random enemy.");
                if (!allCards.isEmpty()) {
                    this.enemyActive = allCards.get(MathUtils.random(allCards.size() - 1));
                } else {
                    this.enemyActive = new CardSystem.Card("Null Node Enemy", 50, new ArrayList<>(), CardSystem.CardType.DIVINE, CardSystem.CardPantheon.NONE, "ui/card_slot_empty.png");
                }
            }

            if (enemyName != null && this.enemyActive == null) { // Only try to load by name if not already set by default case
                final String finalEnemyName = enemyName;
                this.enemyActive = allCards.stream()
                    .filter(card -> finalEnemyName.equals(card.getName()))
                    .findFirst()
                    .orElse(null);
                if (this.enemyActive == null) {
                    logSafe("CRITICAL: Specified enemy '" + enemyName + "' not found in cards.json. Picking random enemy.");
                    if (!allCards.isEmpty()) {
                        this.enemyActive = allCards.get(MathUtils.random(allCards.size() - 1));
                    } else {
                        this.enemyActive = new CardSystem.Card("Missing Enemy", 50, new ArrayList<>(), CardSystem.CardType.DIVINE, CardSystem.CardPantheon.NONE, "ui/card_slot_empty.png");
                    }
                }
            }
            // Ensure the selected enemy has health
            if (this.enemyActive != null && this.enemyActive.getHealth() <= 0 &&
                this.enemyActive.getType() != CardSystem.CardType.ITEM &&
                this.enemyActive.getType() != CardSystem.CardType.ARTIFACT) {

                CardSystem.Card originalCardData = allCards.stream()
                    .filter(c -> this.enemyActive.getName().equals(c.getName())) // Safe getName()
                    .findFirst().orElse(null);
                if (originalCardData != null && originalCardData.getHealth() > 0) {
                    this.enemyActive.setHealth(originalCardData.getHealth());
                } else {
                    this.enemyActive.setHealth(90);
                }
                logSafe("Adjusted health for " + this.enemyActive.getName() + " to " + this.enemyActive.getHealth());
            }
        }
        updateEnemyStack();
        refreshAll();
        log("Battle started. Select a creature from your hand to make it active.");
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
    }

    private void logSafe(String message) {
        if (this.font != null && this.logTable != null && this.skin != null) {
            Label lbl = new Label(message, new Label.LabelStyle(font, Color.ORANGE));
            lbl.setFontScale(LOG_FONT_SCALE_V);
            lbl.setWrap(true);
            float logWidth = LOG_MENU_WIDTH_V * 0.92f;
            this.logTable.add(lbl).width(logWidth).left().padTop(2);
            this.logTable.row();
            this.logTable.layout();
            if (this.logScroll != null) {
                this.logScroll.layout();
                this.logScroll.scrollTo(0, 0, 0, 0);
            }
        } else {
            Gdx.app.log("BattleScreen (SafeLog)", message);
        }
    }

    private void buildUI() {
        if (root == null) {
            root = new Table(skin);
            root.setFillParent(true);
            Image backgroundImage = new Image(new Texture(Gdx.files.internal("ui/battlebackground.png")));
            backgroundImage.setFillParent(true);
            backgroundImage.setScaling(Scaling.fill);
            stage.addActor(backgroundImage);
            stage.addActor(root);
        } else {
            root.clear();
        }
        if (this.graveTable == null) this.graveTable = new Table(skin);

        topHudTable = new Table(skin);
        roundLabel = new Label("Round " + roundNumber, new Label.LabelStyle(font, Color.YELLOW));
        faithLabel = new Label("Faith: " + playerFaith, new Label.LabelStyle(font, Color.CYAN));
        deathCountLabel = new Label("Lives: " + (MAX_ACTIVE_DEATHS - activeDeathCount), new Label.LabelStyle(font, Color.RED));
        roundLabel.setFontScale(HUD_FONT_SCALE_V);
        faithLabel.setFontScale(HUD_FONT_SCALE_V);
        deathCountLabel.setFontScale(HUD_FONT_SCALE_V);

        topHudTable.add(roundLabel).pad(VIRTUAL_HEIGHT * 0.005f).expandX().left();
        topHudTable.add(faithLabel).pad(VIRTUAL_HEIGHT * 0.005f).expandX().center();
        topHudTable.add(deathCountLabel).pad(VIRTUAL_HEIGHT * 0.005f).expandX().right();

        String enemyImgPath = (enemyActive != null && enemyActive.getImagePath() != null) ? enemyActive.getImagePath() : "ui/card_slot_empty.png";
        int enemyHp = (enemyActive != null) ? enemyActive.getHealth() : 0;
        enemyStack = createCardStack(enemyImgPath, enemyHp, CARD_WIDTH_ACTIVE_V, CARD_HEIGHT_ACTIVE_V);
        if (enemyStack.getChildren().size > 1 && enemyStack.getChildren().get(1) instanceof Table) {
            Table enemyOverlayTable = (Table) enemyStack.getChildren().get(1);
            if (enemyOverlayTable.getChildren().size > 0 && enemyOverlayTable.getChildren().first() instanceof Label) {
                enemyHpLabel = (Label) enemyOverlayTable.getChildren().first();
            } else {
                enemyHpLabel = new Label("0", new Label.LabelStyle(font, Color.WHITE));
                logSafe("Warning: Could not find HP label in enemyStack overlay during buildUI.");
            }
        } else {
            enemyHpLabel = new Label("0", new Label.LabelStyle(font, Color.WHITE));
            logSafe("Warning: enemyStack overlay table not found or structured as expected during buildUI.");
        }

        activeStack = createCardStack("ui/card_slot_empty.png", 0, CARD_WIDTH_ACTIVE_V, CARD_HEIGHT_ACTIVE_V);
        if (activeStack.getChildren().size > 1 && activeStack.getChildren().get(1) instanceof Table) {
            Table activeOverlayTable = (Table) activeStack.getChildren().get(1);
            if (activeOverlayTable.getChildren().size > 0 && activeOverlayTable.getChildren().first() instanceof Label) {
                activeHpLabel = (Label) activeOverlayTable.getChildren().first();
            } else {
                activeHpLabel = new Label("", new Label.LabelStyle(font, Color.WHITE));
                logSafe("Warning: Could not find HP label in activeStack overlay during buildUI.");
            }
        } else {
            activeHpLabel = new Label("", new Label.LabelStyle(font, Color.WHITE));
            logSafe("Warning: activeStack overlay table not found or structured as expected during buildUI.");
        }

        benchTable = new Table(skin);
        benchContainer = new Container<>(benchTable);

        deckCountLabel = new Label("Deck: " + (playerDeck != null ? playerDeck.size() : 0), new Label.LabelStyle(font, Color.WHITE));
        deckCountLabel.setFontScale(HUD_FONT_SCALE_V * 0.9f);
        deckAndGraveTable = new Table(skin);

        handTable = new Table(skin);
        handScroll = new ScrollPane(handTable, skin);
        handScroll.setFadeScrollBars(false);
        handScroll.setScrollingDisabled(true, false);

        skillsAndEndTable = new Table(skin);
        skillsAndEndTable.setBackground(skin.newDrawable("white", new Color(0.2f, 0.2f, 0.2f, 0.8f)));
        skillsAndEndTable.pad(VIRTUAL_HEIGHT * 0.008f);

        endTurnBtn = new TextButton("End Turn", skin);
        endTurnBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (!playerTurn) return;
                if (playerActive == null && (playerHand != null && !playerHand.isEmpty()) && (playerBench != null && !playerBench.isEmpty())) {
                    boolean canMakeActive = false;
                    if (playerHand != null) {
                        for (CardSystem.Card card : playerHand) {
                            if (card.getType() == CardSystem.CardType.GOD || card.getType() == CardSystem.CardType.DIVINE) {
                                canMakeActive = true;
                                break;
                            }
                        }
                    }
                    if (!canMakeActive && playerBench != null) {
                        for (CardSystem.Card card : playerBench) {
                            if (card.getType() == CardSystem.CardType.GOD || card.getType() == CardSystem.CardType.DIVINE) {
                                canMakeActive = true;
                                break;
                            }
                        }
                    }
                    if (canMakeActive) {
                        log("Please set an active card before ending the turn.");
                    } else {
                        log("No playable active card. Player ends turn.");
                        playerTurn = false;
                        enemyTurn();
                    }
                } else if (playerActive == null && (playerHand == null || playerHand.isEmpty()) && (playerDeck == null || playerDeck.isEmpty()) && (playerBench == null || playerBench.isEmpty())) {
                    log("No cards left to play or in deck/bench. Player ends turn.");
                    playerTurn = false;
                    enemyTurn();
                }
                else if (playerActive == null) {
                    log("Cannot end turn: no active card. Please play a card from your hand or bench.");
                }
                else {
                    log("Player ends turn.");
                    playerTurn = false;
                    enemyTurn();
                }
            }
        });

        if (this.logTable == null) this.logTable = new Table(skin);
        logScroll = new ScrollPane(this.logTable, skin);
        logScroll.setFadeScrollBars(false);
        logScroll.setScrollingDisabled(false, false);
        logScroll.setForceScroll(false, true);

        battleLogTableContainer = new Table(skin);
        battleLogTableContainer.setBackground(skin.newDrawable("white", new Color(0.15f, 0.15f, 0.15f, 0.75f)));
        battleLogTableContainer.add(logScroll).grow();

        root.pad(VIRTUAL_HEIGHT * 0.011f);

        root.add(topHudTable).colspan(3).expandX().fillX().padBottom(VIRTUAL_HEIGHT * 0.011f).row();

        Table enemyZoneTable = new Table();
        enemyZoneTable.add(skillsAndEndTable).width(ACTION_MENU_WIDTH_V).minHeight(ACTION_MENU_HEIGHT_V).top().padRight(VIRTUAL_WIDTH * 0.01f);
        enemyZoneTable.add(enemyStack).width(CARD_WIDTH_ACTIVE_V).height(CARD_HEIGHT_ACTIVE_V).center().expandX();
        enemyZoneTable.add(battleLogTableContainer).width(LOG_MENU_WIDTH_V).minHeight(LOG_MENU_HEIGHT_V).top().padLeft(VIRTUAL_WIDTH * 0.01f);
        root.add(enemyZoneTable).colspan(3).expandX().fillX().padBottom(VIRTUAL_HEIGHT * 0.022f).row();

        Table playerStageTable = new Table();
        float benchWidth = MAX_BENCH_SLOTS * (CARD_WIDTH_BENCH_HAND_V + (VIRTUAL_WIDTH * 0.005f)) + (VIRTUAL_WIDTH * 0.01f);
        playerStageTable.add(benchContainer).width(benchWidth).height(CARD_HEIGHT_BENCH_HAND_V + (VIRTUAL_HEIGHT * 0.02f)).center().padRight(VIRTUAL_WIDTH * 0.01f);
        playerStageTable.add(activeStack).width(CARD_WIDTH_ACTIVE_V).height(CARD_HEIGHT_ACTIVE_V).center();
        playerStageTable.add(deckAndGraveTable).width(CARD_WIDTH_BENCH_HAND_V + (VIRTUAL_WIDTH * 0.03f))
            .height(CARD_HEIGHT_ACTIVE_V).center().padLeft(VIRTUAL_WIDTH * 0.01f);
        root.add(playerStageTable).colspan(3).expandX().center().padBottom(VIRTUAL_HEIGHT * 0.011f).row();

        root.add(handScroll).colspan(3).height(CARD_HEIGHT_BENCH_HAND_V + (VIRTUAL_HEIGHT * 0.03f)).fillX().expandX().bottom();
    }

    private void log(String msg) {
        if (font == null || this.logTable == null || this.logScroll == null || skin == null) {
            System.out.println("Log (UI not ready): " + msg);
            return;
        }
        Label lbl = new Label(msg, new Label.LabelStyle(font, Color.LIGHT_GRAY));
        lbl.setFontScale(LOG_FONT_SCALE_V);
        lbl.setWrap(true);
        float actualLogContentWidth = LOG_MENU_WIDTH_V * 0.92f;
        this.logTable.add(lbl).width(actualLogContentWidth).left().padTop(2);
        this.logTable.row();
        this.logTable.layout();
        this.logScroll.layout();
        this.logScroll.scrollTo(0, 0, 0, 0);
    }

    private void refreshAllUIModules() {
        refreshSkillsAndEndTurnUI();
        refreshHandUI();
        refreshBenchUI();
        refreshDeckAndGraveUI();
        updateActiveStack();
        updateEnemyStack();
        if (deckCountLabel != null) deckCountLabel.setText("Deck: " + (playerDeck != null ? playerDeck.size() : 0) );
        if (deathCountLabel != null) deathCountLabel.setText("Lives: " + (MAX_ACTIVE_DEATHS - activeDeathCount));
        if (faithLabel != null) faithLabel.setText("Faith: " + playerFaith);
        if (roundLabel != null) roundLabel.setText("Round " + roundNumber);
    }

    private Stack createCardStack(String imgPath, int hp, float width, float height) {
        Stack stack = new Stack();
        Image img;
        try {
            img = new Image(new Texture(Gdx.files.internal(imgPath)));
        } catch (Exception e) {
            logSafe("Error loading texture: " + imgPath + ". Using placeholder.");
            img = new Image(new Texture(Gdx.files.internal("ui/card_slot_empty.png")));
        }
        img.setScaling(Scaling.fit);

        Table overlay = new Table(skin);
        overlay.top().right();
        Label hpLabel = new Label(hp > 0 ? String.valueOf(hp) : (hp == 0 && (imgPath.contains("item") || imgPath.contains("artifact")) ? "" : "0"), new Label.LabelStyle(font, Color.WHITE));
        hpLabel.setFontScale(Math.max(0.8f, 0.0085f * width)); // Adjusted HP font scale
        hpLabel.setAlignment(Align.right);
        overlay.add(hpLabel).padTop(height * 0.015f).padRight(width * 0.025f);

        stack.add(img);
        stack.add(overlay);
        return stack;
    }

    private void refreshSkillsAndEndTurnUI() {
        skillsAndEndTable.clear();
        skillsAndEndTable.defaults().pad(VIRTUAL_HEIGHT * 0.006f).fillX().width(ACTION_MENU_WIDTH_V - (VIRTUAL_WIDTH * 0.01f));
        skillsAndEndTable.top();

        if (playerActive != null) {
            List<CardSystem.Skill> skills = playerActive.getSkills();
            if (skills != null && !skills.isEmpty()) {
                for (final CardSystem.Skill skill : skills) {
                    TextButton skillButton = new TextButton(skill.getName() + "\n(" + skill.getCost() + " Faith)", skin);
                    skillButton.getLabel().setWrap(true);
                    skillButton.getLabel().setAlignment(Align.center);
                    skillButton.getLabel().setFontScale(SKILL_BUTTON_FONT_SCALE_V);
                    if (!playerTurn || playerFaith < skill.getCost() || !playerActive.canUse()) {
                        skillButton.setDisabled(true);
                        skillButton.getLabel().setColor(Color.GRAY);
                    } else {
                        skillButton.getLabel().setColor(Color.WHITE);
                    }

                    skillButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            if (!playerTurn || skillButton.isDisabled()) return;

                            if ("Retreat".equals(skill.getName())) {
                                showRetreatDialog();
                            } else if (playerFaith < skill.getCost()) {
                                log("Insufficient faith for " + skill.getName());
                            } else {
                                log("Player uses " + skill.getName() + " from " + playerActive.getName());
                                playerActive.markUsed();
                                int oldEnemyHp = (enemyActive != null) ? enemyActive.getHealth() : 0;
                                int oldPlayerHp = playerActive.getHealth();

                                skill.apply(playerActive, enemyActive);
                                playerFaith -= skill.getCost();

                                if (enemyActive != null && enemyActive.getHealth() != oldEnemyHp) {
                                    log(enemyActive.getName() + " HP: " + oldEnemyHp + " -> " + enemyActive.getHealth());
                                }
                                if (playerActive.getHealth() != oldPlayerHp) {
                                    log(playerActive.getName() + " HP: " + oldPlayerHp + " -> " + playerActive.getHealth());
                                }
                                if (playerActive.getType() == CardSystem.CardType.ITEM && playerActive.getHealth() <=0) {
                                    playerActive.consume();
                                    log(playerActive.getName() + " was consumed.");
                                    if (playerGraveyard != null) playerGraveyard.add(playerActive);
                                }
                                refreshAll();
                                checkEnemyDefeated();
                                checkPlayerDefeated();
                            }
                        }
                    });
                    // Increased prefHeight to accommodate larger text from doubled font scale
                    skillsAndEndTable.add(skillButton).prefHeight(VIRTUAL_HEIGHT * 0.12f).row();
                }
            } else {
                Label noSkillsLabel = new Label("No skills available", skin);
                noSkillsLabel.setFontScale(SKILL_BUTTON_FONT_SCALE_V);
                skillsAndEndTable.add(noSkillsLabel).center().row();
            }
        } else {
            Label noActiveLabel = new Label("No active card", skin);
            noActiveLabel.setFontScale(SKILL_BUTTON_FONT_SCALE_V);
            skillsAndEndTable.add(noActiveLabel).center().row();
        }
        endTurnBtn.getLabel().setFontScale(SKILL_BUTTON_FONT_SCALE_V * 1.15f);
        // Increased prefHeight for End Turn button as well
        skillsAndEndTable.add(endTurnBtn).padTop(VIRTUAL_HEIGHT * 0.015f).center().prefHeight(VIRTUAL_HEIGHT * 0.10f);
    }

    private void refreshHandUI() {
        handTable.clear();
        handTable.left().bottom();
        handTable.defaults().size(CARD_WIDTH_BENCH_HAND_V, CARD_HEIGHT_BENCH_HAND_V).pad(VIRTUAL_WIDTH * 0.005f);

        if (playerHand == null) playerHand = new ArrayList<>();

        for (final CardSystem.Card c : playerHand) {
            ImageButton btn = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal(c.getImagePath())))));
            btn.getImageCell().grow();
            btn.setTouchable(Touchable.enabled);
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) {
                    if (!playerTurn) return;
                    Dialog dialog = new Dialog("Play Card", skin) {
                        @Override
                        protected void result(Object object) {
                            handleHandCard(c, object);
                        }
                    };
                    Label textLabel = new Label("What to do with " + c.getName() + "?", skin);
                    textLabel.setFontScale(DIALOG_TEXT_FONT_SCALE_V);
                    dialog.getContentTable().add(textLabel).pad(VIRTUAL_HEIGHT * 0.01f).row();

                    dialog.button("Set Active", "active");
                    dialog.button("To Bench", "bench");
                    dialog.button("Cancel", false);

                    Table buttonTable = dialog.getButtonTable();
                    for (Cell cell : buttonTable.getCells()) {
                        Actor actor = cell.getActor();
                        if (actor instanceof TextButton) {
                            ((TextButton) actor).getLabel().setFontScale(DIALOG_BUTTON_FONT_SCALE_V);
                        }
                    }
                    dialog.pad(VIRTUAL_HEIGHT * 0.05f);
                    buttonTable.padTop(VIRTUAL_HEIGHT * 0.02f);
                    dialog.show(stage);
                }
            });
            handTable.add(btn);
        }
    }

    private void refreshBenchUI() {
        benchTable.clear();
        benchTable.left();
        benchTable.defaults().size(CARD_WIDTH_BENCH_HAND_V, CARD_HEIGHT_BENCH_HAND_V).pad(VIRTUAL_WIDTH * 0.005f);
        Texture emptyTex = new Texture(Gdx.files.internal("ui/card_slot_empty.png"));

        if (playerBench == null) playerBench = new ArrayList<>();

        for (int i = 0; i < MAX_BENCH_SLOTS; i++) {
            if (i < playerBench.size()) {
                final CardSystem.Card c = playerBench.get(i);
                Stack s = createCardStack(c.getImagePath(), c.getHealth(), CARD_WIDTH_BENCH_HAND_V, CARD_HEIGHT_BENCH_HAND_V);
                s.setTouchable(Touchable.enabled);
                s.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (!playerTurn) return;
                        if (playerActive == null) {
                            playerActive = c;
                            playerBench.remove(c);
                            log(c.getName() + " moved from bench to active slot.");
                            refreshAll();
                        } else {
                            Dialog dialog = new Dialog("Swap with Active?", skin){
                                @Override
                                protected void result(Object obj){
                                    if (Boolean.TRUE.equals(obj)) {
                                        CardSystem.Card temp = playerActive;
                                        playerActive = c;
                                        playerBench.remove(c);
                                        playerBench.add(temp);
                                        log(c.getName() + " swapped with " + temp.getName() + " from bench.");
                                        refreshAll();
                                    }
                                }
                            };
                            Label textLabel = new Label("Swap " + c.getName() + " with " + playerActive.getName() + "?", skin);
                            textLabel.setFontScale(DIALOG_TEXT_FONT_SCALE_V);
                            dialog.getContentTable().add(textLabel).pad(VIRTUAL_HEIGHT * 0.01f);

                            dialog.button("Yes", true);
                            dialog.button("No", false);

                            Table buttonTable = dialog.getButtonTable();
                            for (Cell cell : buttonTable.getCells()) {
                                Actor actor = cell.getActor();
                                if (actor instanceof TextButton) {
                                    ((TextButton) actor).getLabel().setFontScale(DIALOG_BUTTON_FONT_SCALE_V);
                                }
                            }
                            dialog.pad(VIRTUAL_HEIGHT * 0.05f);
                            buttonTable.padTop(VIRTUAL_HEIGHT * 0.02f);
                            dialog.show(stage);
                        }
                    }
                });
                benchTable.add(s);
            } else {
                Image img = new Image(emptyTex);
                benchTable.add(img);
            }
        }
    }

    private void refreshDeckAndGraveUI() {
        if (this.deckAndGraveTable == null) {
            this.deckAndGraveTable = new Table(skin);
            logSafe("CRITICAL: deckAndGraveTable was null, reinitialized in refreshDeckAndGraveUI");
        }
        if (this.graveTable == null) {
            this.graveTable = new Table(skin);
            logSafe("CRITICAL: graveTable was null, reinitialized in refreshDeckAndGraveUI");
        }
        if (this.deckCountLabel == null) {
            this.deckCountLabel = new Label("Deck: 0", new Label.LabelStyle(font, Color.WHITE));
            logSafe("CRITICAL: deckCountLabel was null, reinitialized in refreshDeckAndGraveUI");
        }
        if (this.playerGraveyard == null) {
            this.playerGraveyard = new ArrayList<>();
            logSafe("CRITICAL: playerGraveyard was null, reinitialized in refreshDeckAndGraveUI");
        }
        if (this.playerDeck == null) {
            this.playerDeck = new ArrayList<>();
            logSafe("CRITICAL: playerDeck was null, reinitialized in refreshDeckAndGraveUI");
        }

        deckAndGraveTable.clear();
        this.graveTable.clear();

        deckAndGraveTable.top().pad(VIRTUAL_HEIGHT * 0.0055f);

        Label graveLabelText = new Label("Grave (" + playerGraveyard.size() + ")", new Label.LabelStyle(font, Color.LIGHT_GRAY));
        graveLabelText.setFontScale(HUD_FONT_SCALE_V * 0.85f);
        deckAndGraveTable.add(graveLabelText).colspan(MAX_BENCH_SLOTS).center().padBottom(VIRTUAL_HEIGHT * 0.005f).row();

        this.graveTable.defaults().size(CARD_WIDTH_GRAVE_V, CARD_HEIGHT_GRAVE_V).pad(VIRTUAL_WIDTH * 0.0015f);
        int graveDisplayCount = 0;
        for (int i = 0; i < playerGraveyard.size() && graveDisplayCount < MAX_BENCH_SLOTS; i++) {
            int R_INDEX = playerGraveyard.size() - 1 - i;
            if (R_INDEX >= 0 && R_INDEX < playerGraveyard.size()) {
                CardSystem.Card c = playerGraveyard.get(R_INDEX);
                if (c != null && c.getImagePath() != null) {
                    this.graveTable.add(createCardStack(c.getImagePath(), 0, CARD_WIDTH_GRAVE_V, CARD_HEIGHT_GRAVE_V)).pad(VIRTUAL_WIDTH * 0.001f);
                    graveDisplayCount++;
                } else {
                    logSafe("Warning: Null card or imagePath in graveyard at effective index " + R_INDEX);
                }
            } else {
                logSafe("Warning: Invalid index " + R_INDEX + " for playerGraveyard of size " + playerGraveyard.size());
            }
        }
        deckAndGraveTable.add(this.graveTable).colspan(MAX_BENCH_SLOTS).center().minHeight(CARD_HEIGHT_GRAVE_V + (VIRTUAL_HEIGHT * 0.01f)).row();

        deckCountLabel.setText("Deck: " + playerDeck.size());
        deckCountLabel.setFontScale(HUD_FONT_SCALE_V * 0.85f);
        try {
            deckAndGraveTable.add(deckCountLabel).colspan(MAX_BENCH_SLOTS).center().padTop(VIRTUAL_HEIGHT * 0.015f).row();
        } catch (Exception e) {
            logSafe("Error adding deckCountLabel to table: " + e.getMessage());
        }

        Texture deckTex = null;
        try {
            deckTex = new Texture(Gdx.files.internal("ui/back_card.png")); // Using specified deck back image
        } catch (Exception e) {
            logSafe("Error loading deck texture ui/back_card.png: " + e.getMessage() + ". Using placeholder.");
            try {
                deckTex = new Texture(Gdx.files.internal("ui/card_slot_empty.png"));
            } catch (Exception e2) {
                logSafe("Error loading fallback deck texture: " + e2.getMessage());
                return;
            }
        }
        Image deckImage = new Image(deckTex);
        try {
            deckAndGraveTable.add(deckImage).size(CARD_WIDTH_BENCH_HAND_V * 0.9f, CARD_HEIGHT_BENCH_HAND_V * 0.9f)
                .colspan(MAX_BENCH_SLOTS).center().padTop(VIRTUAL_HEIGHT * 0.008f);
        } catch (Exception e) {
            logSafe("Error adding deckImage to table: " + e.getMessage());
        }
    }

    private void updateActiveStack() {
        if (activeStack == null || activeHpLabel == null) {
            logSafe("Active stack UI not initialized in updateActiveStack.");
            return;
        }
        if (playerActive != null) {
            activeHpLabel.setText(String.valueOf(playerActive.getHealth()));
            Actor firstChild = activeStack.getChildren().size > 0 ? activeStack.getChildren().first() : null;
            if (firstChild instanceof Image) {
                Image img = (Image) firstChild;
                try {
                    img.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal(playerActive.getImagePath())))));
                } catch (Exception e) {
                    logSafe("Error loading active card image: " + playerActive.getImagePath());
                    img.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("ui/card_slot_empty.png")))));
                }
            }
        } else {
            activeHpLabel.setText("");
            Actor firstChild = activeStack.getChildren().size > 0 ? activeStack.getChildren().first() : null;
            if (firstChild instanceof Image) {
                Image img = (Image) firstChild;
                img.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("ui/card_slot_empty.png")))));
            }
        }
        refreshSkillsAndEndTurnUI();
    }

    private void updateEnemyStack() {
        if (enemyStack == null || enemyHpLabel == null) {
            logSafe("Enemy stack UI not initialized in updateEnemyStack.");
            return;
        }
        if(enemyActive == null) {
            enemyHpLabel.setText("");
            Actor firstChild = enemyStack.getChildren().size > 0 ? enemyStack.getChildren().first() : null;
            if (firstChild instanceof Image) {
                Image img = (Image) firstChild;
                img.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("ui/card_slot_empty.png")))));
            }
            return;
        }
        enemyHpLabel.setText(String.valueOf(enemyActive.getHealth()));
        Actor firstChild = enemyStack.getChildren().size > 0 ? enemyStack.getChildren().first() : null;
        if (firstChild instanceof Image) {
            Image img = (Image)firstChild;
            try {
                img.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal(enemyActive.getImagePath())))));
            } catch (Exception e) {
                logSafe("Error loading enemy card image: " + enemyActive.getImagePath());
                img.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("ui/card_slot_empty.png")))));
            }
        }
    }

    private void refreshAll() {
        refreshAllUIModules();
        if (playerActive != null) playerActive.resetTurnUsage();
        if (playerBench != null) {
            for(CardSystem.Card benchCard : playerBench) {
                benchCard.resetTurnUsage();
            }
        }
    }

    private void handleHandCard(final CardSystem.Card c, Object action) {
        if ("active".equals(action)) {
            if (playerActive == null) {
                playerActive = c;
                if (playerHand != null) playerHand.remove(c);
                log("Player set active to " + c.getName());
                refreshAll();
            } else {
                log("Active slot is already occupied.");
                if (playerBench != null && playerBench.size() < MAX_BENCH_SLOTS) {
                    Dialog dialog = new Dialog("Active Slot Occupied", skin) {
                        @Override
                        protected void result(Object moveChoiceObject) {
                            if ("moveToBench".equals(moveChoiceObject)) {
                                if (playerBench != null) playerBench.add(playerActive);
                                playerActive = c;
                                if (playerHand != null) playerHand.remove(c);
                                log(c.getName() + " is now active. Previous active ("+ (playerBench != null && !playerBench.isEmpty() ? playerBench.get(playerBench.size()-1).getName() : "N/A") +") moved to bench.");
                                refreshAll();
                            }
                        }
                    };
                    Label textLabel = new Label(playerActive.getName() + " is active. Move it to bench to play " + c.getName() + "?", skin);
                    textLabel.setFontScale(DIALOG_TEXT_FONT_SCALE_V);
                    textLabel.setWrap(true);
                    dialog.getContentTable().add(textLabel).width(VIRTUAL_WIDTH * 0.45f).pad(VIRTUAL_HEIGHT * 0.01f).row();

                    dialog.button("Yes, move to Bench", "moveToBench");
                    dialog.button("Cancel", false);

                    Table buttonTable = dialog.getButtonTable();
                    for(Cell cell : buttonTable.getCells()){
                        if(cell.getActor() instanceof TextButton){
                            ((TextButton)cell.getActor()).getLabel().setFontScale(DIALOG_BUTTON_FONT_SCALE_V);
                        }
                    }
                    dialog.pad(VIRTUAL_HEIGHT * 0.05f);
                    buttonTable.padTop(VIRTUAL_HEIGHT * 0.02f);
                    dialog.show(stage);
                } else {
                    log("Bench is full. Cannot move current active card to bench.");
                }
            }
        } else if ("bench".equals(action)) {
            if (playerBench != null && playerBench.size() < MAX_BENCH_SLOTS) {
                playerBench.add(c);
                if (playerHand != null) playerHand.remove(c);
                log("Player benched " + c.getName());
                refreshAll();
            } else {
                log("Bench is full. Cannot move " + c.getName() + " to bench.");
            }
        }
    }

    private void showRetreatDialog() {
        if (playerActive == null) { log("No active card to retreat."); return; }
        if (playerBench == null || playerBench.isEmpty()) { log("Cannot retreat: no cards on the bench."); return; }

        final SelectBox<String> box = new SelectBox<>(skin);
        List<String> benchCardNames = new ArrayList<>();
        for (CardSystem.Card benchCard : playerBench) { benchCardNames.add(benchCard.getName()); }

        if (benchCardNames.isEmpty()) { log("No cards on bench to select for retreat."); return; }
        box.setItems(benchCardNames.toArray(new String[0]));

        SelectBox.SelectBoxStyle boxStyle = new SelectBox.SelectBoxStyle(skin.get("default", SelectBox.SelectBoxStyle.class));
        boxStyle.font.getData().setScale(DIALOG_TEXT_FONT_SCALE_V);
        boxStyle.listStyle.font.getData().setScale(DIALOG_TEXT_FONT_SCALE_V);
        box.setStyle(boxStyle);

        final Dialog retreatDialog = new Dialog("Retreat: Select Card from Bench", skin) {
            @Override
            protected void result(Object obj) {
                if (Boolean.TRUE.equals(obj)) {
                    if (box.getItems().size == 0) { log("Error: No items in bench selection for retreat."); return; }
                    int selectedBenchIndex = box.getSelectedIndex();
                    if (playerBench == null || selectedBenchIndex < 0 || selectedBenchIndex >= playerBench.size()) {
                        log("Error: Invalid selection from bench for retreat."); return;
                    }
                    CardSystem.Card cardToMakeActive = playerBench.get(selectedBenchIndex);
                    playerBench.remove(selectedBenchIndex);
                    playerBench.add(playerActive);
                    playerActive = cardToMakeActive;
                    log(playerActive.getName() + " is now active (retreated). " + playerBench.get(playerBench.size()-1).getName() + " moved to bench.");
                    refreshAll();
                }
            }
        };
        retreatDialog.getTitleLabel().setFontScale(DIALOG_TEXT_FONT_SCALE_V * 1.1f);
        Label swapLabel = new Label("Swap with:", skin);
        swapLabel.setFontScale(DIALOG_TEXT_FONT_SCALE_V);
        retreatDialog.getContentTable().add(swapLabel).pad(VIRTUAL_HEIGHT * 0.01f).row();
        retreatDialog.getContentTable().add(box).width(VIRTUAL_WIDTH * 0.4f).pad(VIRTUAL_HEIGHT * 0.01f).row();

        retreatDialog.button("Swap", true);
        retreatDialog.button("Cancel", false);

        Table buttonTable = retreatDialog.getButtonTable();
        for(Cell cell : buttonTable.getCells()){
            if(cell.getActor() instanceof TextButton){
                ((TextButton)cell.getActor()).getLabel().setFontScale(DIALOG_BUTTON_FONT_SCALE_V);
            }
        }
        retreatDialog.pad(VIRTUAL_HEIGHT * 0.05f);
        buttonTable.padTop(VIRTUAL_HEIGHT * 0.02f);
        retreatDialog.show(stage);
    }

    private void enemyTurn() {
        if (enemyActive == null || enemyActive.getHealth() <= 0) {
            log("Enemy is already defeated or not present.");
            playerTurn = true;
            refreshSkillsAndEndTurnUI();
            return;
        }

        log("-- Enemy turn (Round " + roundNumber + ") --");
        if (playerActive != null && playerActive.getHealth() > 0) {
            if (enemyActive.getSkills() != null && !enemyActive.getSkills().isEmpty()) {
                CardSystem.Skill sk = enemyActive.getSkills().get(MathUtils.random(enemyActive.getSkills().size() - 1));
                log("Enemy " + enemyActive.getName() + " uses " + sk.getName());
                int oldHp = playerActive.getHealth();
                sk.apply(enemyActive, playerActive);
                log(playerActive.getName() + " HP: " + oldHp + " -> " + playerActive.getHealth());
                checkPlayerDefeated();
            } else {
                log(enemyActive.getName() + " has no skills to use.");
            }
        } else {
            log("Player has no active card to target or active card is defeated.");
        }
        if(enemyActive != null) {
            enemyActive.applyStartOfTurnStatuses();
            enemyActive.resetTurnUsage();
            enemyActive.applyEndOfTurnStatuses();
        }

        playerTurn = true;
        log("-- Player turn (Round " + (roundNumber + 1) + ") --");

        if (playerFaith < MAX_FAITH) {
            playerFaith++;
        }

        if(playerActive != null) playerActive.applyStartOfTurnStatuses();
        if (playerBench != null) {
            for(CardSystem.Card benchCard : playerBench) {
                benchCard.applyStartOfTurnStatuses();
            }
        }

        drawCards(1);
        roundNumber++;
        refreshAll();
    }

    private void checkEnemyDefeated() {
        if (enemyActive != null && enemyActive.getHealth() <= 0) {
            log("Enemy " + enemyActive.getName() + " defeated!");

            final boolean wasBossNodeFight = (this.battleNode != null && this.battleNode.type == ConvergingMapScreen.NodeType.BOSS);

            Dialog victoryDialog = new Dialog("Victory!", skin) {
                @Override
                protected void result(Object obj) {
                    if (wasBossNodeFight) {
                        log("Boss Node cleared! Returning to Game Menu.");
                        if (profile != null) {
                            profile.currentMapId = "n0";
                            SaveManager.saveProfile(profile);
                        }
                        game.setScreen(new GameMenuFernan(game, profile));
                    } else {
                        if (mapScreen != null && BattleScreen.this.battleNode != null) {
                            mapScreen.moveTo(BattleScreen.this.battleNode);
                        }
                        game.setScreen(mapScreen);
                    }
                }
            };
            Label victoryText = new Label("You have defeated " + enemyActive.getName() + "!", skin);
            victoryText.setFontScale(DIALOG_TEXT_FONT_SCALE_V);
            victoryDialog.getContentTable().add(victoryText).pad(VIRTUAL_HEIGHT * 0.01f);

            if (wasBossNodeFight) {
                victoryDialog.button("Finish Map & Return", true);
            } else {
                victoryDialog.button("Continue", true);
            }

            Table buttonTable = victoryDialog.getButtonTable();
            for(Cell cell : buttonTable.getCells()){
                if(cell.getActor() instanceof TextButton){
                    ((TextButton)cell.getActor()).getLabel().setFontScale(DIALOG_BUTTON_FONT_SCALE_V);
                }
            }
            victoryDialog.pad(VIRTUAL_HEIGHT * 0.055f);
            victoryDialog.show(stage);
        }
    }

    private void checkPlayerDefeated() {
        boolean activeCardWasDefeated = false;
        if (playerActive != null && playerActive.getHealth() <= 0) {
            log("Active card " + playerActive.getName() + " was defeated.");
            if (playerGraveyard != null) playerGraveyard.add(playerActive);
            activeDeathCount++;
            playerActive = null;
            activeCardWasDefeated = true;
        }

        if (activeCardWasDefeated) {
            if (activeDeathCount >= MAX_ACTIVE_DEATHS) {
                log("All lives lost! Game Over.");
                Dialog defeatDialog = new Dialog("Defeat!", skin) {
                    @Override
                    protected void result(Object obj) { game.setScreen(new MainFernan(game)); }
                };
                Label defeatText = new Label("You have run out of lives!", skin);
                defeatText.setFontScale(DIALOG_TEXT_FONT_SCALE_V);
                defeatDialog.getContentTable().add(defeatText).pad(VIRTUAL_HEIGHT * 0.01f);
                defeatDialog.button("To Main Menu", true);
                Table buttonTable = defeatDialog.getButtonTable();
                for(Cell cell : buttonTable.getCells()){
                    if(cell.getActor() instanceof TextButton){
                        ((TextButton)cell.getActor()).getLabel().setFontScale(DIALOG_BUTTON_FONT_SCALE_V);
                    }
                }
                defeatDialog.pad(VIRTUAL_HEIGHT * 0.055f);
                defeatDialog.show(stage);

            } else if ((playerBench == null || playerBench.isEmpty()) && (playerHand == null || playerHand.isEmpty()) && (playerDeck == null || playerDeck.isEmpty())){
                log("No cards left in hand, deck or bench! Game Over.");
                Dialog noCardsDialog = new Dialog("Defeat!", skin) {
                    @Override
                    protected void result(Object obj) { game.setScreen(new MainFernan(game)); }
                };
                Label noCardsText = new Label("You have no more cards to play!", skin);
                noCardsText.setFontScale(DIALOG_TEXT_FONT_SCALE_V);
                noCardsDialog.getContentTable().add(noCardsText).pad(VIRTUAL_HEIGHT * 0.01f);
                noCardsDialog.button("To Main Menu", true);
                Table buttonTable = noCardsDialog.getButtonTable();
                for(Cell cell : buttonTable.getCells()){
                    if(cell.getActor() instanceof TextButton){
                        ((TextButton)cell.getActor()).getLabel().setFontScale(DIALOG_BUTTON_FONT_SCALE_V);
                    }
                }
                noCardsDialog.pad(VIRTUAL_HEIGHT * 0.055f);
                noCardsDialog.show(stage);
            } else {
                if (playerBench != null && !playerBench.isEmpty()) {
                    log("Your active card was defeated. Select a card from your bench to make active.");
                } else if (playerHand != null && !playerHand.isEmpty()) {
                    log("Your active card was defeated. Play a card from your hand.");
                }
            }
            refreshAll();
        }
    }

    private void drawCards(int n) {
        if (playerDeck == null) playerDeck = new ArrayList<>();
        if (playerHand == null) playerHand = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (!playerDeck.isEmpty()) {
                CardSystem.Card drawnCard = playerDeck.remove(0);
                playerHand.add(drawnCard);
                if (logTable != null) {
                    log("Drew " + drawnCard.getName());
                } else {
                    Gdx.app.log("BattleScreen_drawCards", "Drew " + drawnCard.getName() + " (logging system not fully ready)");
                }
            } else {
                if (logTable != null) {
                    log("Deck is empty. Cannot draw more cards.");
                } else {
                    Gdx.app.log("BattleScreen_drawCards", "Deck is empty. (logging system not fully ready)");
                }
                break;
            }
        }
        if (handTable != null) {
            refreshHandUI();
        }
        if (deckCountLabel != null && deckAndGraveTable != null) {
            deckCountLabel.setText("Deck: " + playerDeck.size());
        }
    }

    @Override public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null && stage.getViewport() != null) {
            stage.getViewport().update(width, height, true);
        }
    }
    @Override public void pause() {}
    @Override public void resume() {}

    @Override public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (font != null) font.dispose();
        // Dispose any other Textures manually loaded by this screen if they are class fields
    }
}
