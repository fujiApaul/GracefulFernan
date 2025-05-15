package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor; // Added for casting
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
    private BitmapFont font;
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

    private final float CARD_WIDTH_ACTIVE_V = VIRTUAL_WIDTH * 0.16f;
    private final float CARD_HEIGHT_ACTIVE_V = CARD_WIDTH_ACTIVE_V * 1.4f;
    private final float CARD_WIDTH_BENCH_HAND_V = VIRTUAL_WIDTH * 0.09f;
    private final float CARD_HEIGHT_BENCH_HAND_V = CARD_WIDTH_BENCH_HAND_V * 1.4f;
    private final float CARD_WIDTH_GRAVE_V = VIRTUAL_WIDTH * 0.05f;
    private final float CARD_HEIGHT_GRAVE_V = CARD_WIDTH_GRAVE_V * 1.4f;

    private final float ACTION_MENU_WIDTH_V = VIRTUAL_WIDTH * 0.18f;
    private final float ACTION_MENU_HEIGHT_V = CARD_HEIGHT_ACTIVE_V;
    private final float LOG_MENU_WIDTH_V = VIRTUAL_WIDTH * 0.18f;
    private final float LOG_MENU_HEIGHT_V = CARD_HEIGHT_ACTIVE_V;

    private final float HUD_FONT_SCALE_V = 1.3f * (VIRTUAL_WIDTH / 1600f);
    private final float SKILL_BUTTON_FONT_SCALE_V = 1.1f * (VIRTUAL_WIDTH / 1600f);
    private final float LOG_FONT_SCALE_V = 0.9f * (VIRTUAL_WIDTH / 1600f);
    private final float DIALOG_TEXT_FONT_SCALE_V = 1.1f * (VIRTUAL_WIDTH / 1600f);
    private final float DIALOG_BUTTON_FONT_SCALE_V = 1.0f * (VIRTUAL_WIDTH / 1600f);


    public BattleScreen(FernansGrace game, ConvergingMapScreen mapScreen, SaveProfile profile) {
        this.game = game;
        this.mapScreen = mapScreen;
        this.profile = profile;

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
            logSafe("CRITICAL: No active deck or empty. Using placeholder.");
            playerDeck = new ArrayList<>();
            // As a fallback, you might want to give the player a default set of cards
            // For example:
            // List<CardSystem.Card> allCards = CardSystem.loadCardsFromJson();
            // if (!allCards.isEmpty()) {
            //     for (int i = 0; i < 5 && i < allCards.size(); i++) {
            //         playerDeck.add(allCards.get(i)); // Add first 5 cards as a default
            //     }
            // }
            // if (playerDeck.isEmpty()) {
            //    // Still empty, this is a critical error, maybe throw exception or exit
            //    Gdx.app.error("BattleScreen", "Fallback deck creation failed. No cards available.");
            // }
        } else {
            playerDeck = new ArrayList<>(activeDeck.getCards());
        }


        playerHand = new ArrayList<>();
        playerBench = new ArrayList<>();
        playerGraveyard = new ArrayList<>();
        playerActive = null;

        buildUI();

        drawCards(STARTING_HAND);

        List<CardSystem.Card> all = CardSystem.loadCardsFromJson();
        if (all.isEmpty()) {
            throw new IllegalStateException("No cards loaded from cards.json for enemy selection.");
        }
        enemyActive = all.get(MathUtils.random(all.size() - 1));
        if (enemyActive.getHealth() <= 0 && enemyActive.getType() != CardSystem.CardType.ITEM && enemyActive.getType() != CardSystem.CardType.ARTIFACT) {
            enemyActive.setHealth(90); // Ensure non-item/artifact enemies have health
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
            float logWidth = LOG_MENU_WIDTH_V - (VIRTUAL_WIDTH * 0.0125f); // Default padding deduction
            if (this.logScroll != null && this.logScroll.getWidth() > 20) { // Check if scrollpane width is valid
                logWidth = this.logScroll.getWidth() - (VIRTUAL_WIDTH * 0.00625f); // More precise padding for scroll
            }
            this.logTable.add(lbl).width(logWidth).left().padTop(2);
            this.logTable.row();
            this.logTable.layout(); // Ensure table layout is updated
            if (this.logScroll != null) {
                this.logScroll.layout(); // Ensure scrollpane layout is updated
                this.logScroll.scrollTo(0, 0, 0, 0); // Scroll to bottom
            }
        } else {
            Gdx.app.log("BattleScreen (SafeLog)", message);
        }
    }

    private void buildUI() {
        if (root == null) {
            root = new Table(skin);
            root.setFillParent(true);
            // Background Image
            Image backgroundImage = new Image(new Texture(Gdx.files.internal("BG2.png"))); // Ensure this path is correct
            backgroundImage.setFillParent(true);
            stage.addActor(backgroundImage); // Add background first
            stage.addActor(root); // Then add root table on top
        } else {
            root.clear(); // Clear existing widgets if rebuilding
        }
        this.graveTable = new Table(skin); // Initialize graveTable

        // Top HUD
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

        // Enemy and Player Active Cards
        String enemyImgPath = (enemyActive != null) ? enemyActive.getImagePath() : "ui/card_slot_empty.png";
        int enemyHp = (enemyActive != null) ? enemyActive.getHealth() : 0;
        enemyStack = createCardStack(enemyImgPath, enemyHp, CARD_WIDTH_ACTIVE_V, CARD_HEIGHT_ACTIVE_V);
        enemyHpLabel = (Label)((Table)enemyStack.getChildren().get(1)).getChildren().first(); // Assuming HP label is always there

        activeStack = createCardStack("ui/card_slot_empty.png", 0, CARD_WIDTH_ACTIVE_V, CARD_HEIGHT_ACTIVE_V);
        activeHpLabel = (Label)((Table)activeStack.getChildren().get(1)).getChildren().first();

        // Bench
        benchTable = new Table(skin);
        benchContainer = new Container<>(benchTable);
        // benchContainer.setBackground(skin.newDrawable("white", new Color(0.1f,0.1f,0.1f,0.5f))); // Optional BG for bench area

        // Deck and Graveyard Info
        deckCountLabel = new Label("Deck: " + playerDeck.size(), new Label.LabelStyle(font, Color.WHITE));
        deckCountLabel.setFontScale(HUD_FONT_SCALE_V * 0.9f);
        deckAndGraveTable = new Table(skin);
        // deckAndGraveTable.setBackground(skin.newDrawable("white", new Color(0.1f,0.1f,0.1f,0.5f))); // Optional BG

        // Hand
        handTable = new Table(skin);
        // handTable.setBackground(skin.newDrawable("white", new Color(0.1f,0.1f,0.1f,0.5f))); // Optional BG
        handScroll = new ScrollPane(handTable, skin);
        handScroll.setFadeScrollBars(false);
        handScroll.setScrollingDisabled(true, false); // Only horizontal scroll for hand

        // Skills and End Turn Area
        skillsAndEndTable = new Table(skin);
        skillsAndEndTable.setBackground(skin.newDrawable("white", new Color(0.2f, 0.2f, 0.2f, 0.8f))); // Semi-transparent background
        skillsAndEndTable.pad(VIRTUAL_HEIGHT * 0.008f);

        endTurnBtn = new TextButton("End Turn", skin);
        endTurnBtn.getLabel().setFontScale(SKILL_BUTTON_FONT_SCALE_V * 1.15f); // Slightly larger end turn button
        endTurnBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (!playerTurn) return;
                // Simplified logic for ending turn for now
                if (playerActive == null && !playerHand.isEmpty() && !playerBench.isEmpty()) {
                    boolean canMakeActive = false;
                    for (CardSystem.Card card : playerHand) {
                        if (card.getType() == CardSystem.CardType.GOD || card.getType() == CardSystem.CardType.DIVINE) {
                            canMakeActive = true;
                            break;
                        }
                    }
                    if (!canMakeActive && !playerBench.isEmpty()) {
                        // Check bench for playable cards if hand has no playable ones initially
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
                } else if (playerActive == null && playerHand.isEmpty() && playerDeck.isEmpty() && playerBench.isEmpty()) {
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


        // Battle Log Area
        // Ensure logTable is initialized before use in logScroll
        if (this.logTable == null) this.logTable = new Table(skin); // Moved initialization here
        logScroll = new ScrollPane(this.logTable, skin);
        logScroll.setFadeScrollBars(false);
        logScroll.setScrollingDisabled(false, false); // Enable vertical scroll
        logScroll.setForceScroll(false, true); // Force vertical scrollbar if content overflows

        battleLogTableContainer = new Table(skin); // Container for the log
        battleLogTableContainer.setBackground(skin.newDrawable("white", new Color(0.15f, 0.15f, 0.15f, 0.75f)));
        battleLogTableContainer.add(logScroll).grow();


        // Layout in Root Table
        root.pad(VIRTUAL_HEIGHT * 0.011f); // Overall padding for the root table

        // Row 1: Top HUD
        root.add(topHudTable).colspan(3).expandX().fillX().padBottom(VIRTUAL_HEIGHT * 0.011f).row();

        // Row 2: Enemy Zone (Skills | Enemy Active | Log)
        Table enemyZoneTable = new Table();
        enemyZoneTable.add(skillsAndEndTable).width(ACTION_MENU_WIDTH_V).minHeight(ACTION_MENU_HEIGHT_V).top().padRight(VIRTUAL_WIDTH * 0.01f);
        enemyZoneTable.add(enemyStack).width(CARD_WIDTH_ACTIVE_V).height(CARD_HEIGHT_ACTIVE_V).center().expandX(); // Enemy card centered
        enemyZoneTable.add(battleLogTableContainer).width(LOG_MENU_WIDTH_V).minHeight(LOG_MENU_HEIGHT_V).top().padLeft(VIRTUAL_WIDTH * 0.01f);
        root.add(enemyZoneTable).colspan(3).expandX().fillX().padBottom(VIRTUAL_HEIGHT * 0.022f).row();

        // Row 3: Player Stage (Bench | Player Active | Deck/Grave)
        Table playerStageTable = new Table();
        // Calculate bench width dynamically based on number of slots and card width
        float benchWidth = MAX_BENCH_SLOTS * (CARD_WIDTH_BENCH_HAND_V + (VIRTUAL_WIDTH * 0.005f)) + (VIRTUAL_WIDTH * 0.01f); // padding between cards + overall padding
        playerStageTable.add(benchContainer).width(benchWidth).height(CARD_HEIGHT_BENCH_HAND_V + (VIRTUAL_HEIGHT * 0.02f)).center().padRight(VIRTUAL_WIDTH * 0.01f); // Bench on the left
        playerStageTable.add(activeStack).width(CARD_WIDTH_ACTIVE_V).height(CARD_HEIGHT_ACTIVE_V).center(); // Player active card
        playerStageTable.add(deckAndGraveTable).width(CARD_WIDTH_BENCH_HAND_V + (VIRTUAL_WIDTH * 0.03f)) // Increased width for deck/grave
            .height(CARD_HEIGHT_ACTIVE_V).center().padLeft(VIRTUAL_WIDTH * 0.01f); // Deck/Grave on the right
        root.add(playerStageTable).colspan(3).expandX().center().padBottom(VIRTUAL_HEIGHT * 0.011f).row();

        // Row 4: Player Hand
        root.add(handScroll).colspan(3).height(CARD_HEIGHT_BENCH_HAND_V + (VIRTUAL_HEIGHT * 0.03f)).fillX().expandX().bottom();

        // refreshAllUIModules(); // Initial population
    }



    private void log(String msg) {
        // Ensure UI components are initialized before logging
        if (font == null || this.logTable == null || this.logScroll == null || skin == null) {
            System.out.println("Log (UI not ready): " + msg); // Fallback to console
            return;
        }
        Label lbl = new Label(msg, new Label.LabelStyle(font, Color.LIGHT_GRAY));
        lbl.setFontScale(LOG_FONT_SCALE_V);
        lbl.setWrap(true);

        // Calculate available width for log entries, considering scrollbar if present
        float logContentWidth = LOG_MENU_WIDTH_V - (VIRTUAL_WIDTH * 0.0125f); // Approx padding
        if (logScroll.getWidth() > (VIRTUAL_WIDTH * 0.0125f)) { // Check if scrollPane has a valid width
            logContentWidth = logScroll.getWidth() - (VIRTUAL_WIDTH * 0.00625f); // Reduce padding slightly for scrollbar
        }


        this.logTable.add(lbl).width(logContentWidth).left().padTop(2);
        this.logTable.row();
        this.logTable.layout(); // Important: relayout the table
        this.logScroll.layout(); // And the scroll pane
        this.logScroll.scrollTo(0, 0, 0, 0); // Scroll to the newest entry (bottom)
    }


    private void refreshAllUIModules() {
        refreshSkillsAndEndTurnUI();
        refreshHandUI();
        refreshBenchUI();
        refreshDeckAndGraveUI();
        updateActiveStack(); // Ensure active card UI is up-to-date
        updateEnemyStack();  // Ensure enemy card UI is up-to-date
        // Update HUD labels
        if (deckCountLabel != null) deckCountLabel.setText("Deck: " + playerDeck.size());
        if (deathCountLabel != null) deathCountLabel.setText("Lives: " + (MAX_ACTIVE_DEATHS - activeDeathCount));
        if (faithLabel != null) faithLabel.setText("Faith: " + playerFaith);
        if (roundLabel != null) roundLabel.setText("Round " + roundNumber);
    }


    // Creates a Stack containing a card image and an overlay for HP
    private Stack createCardStack(String imgPath, int hp, float width, float height) {
        Stack stack = new Stack();
        Image img = new Image(new Texture(Gdx.files.internal(imgPath))); // Ensure path is valid
        img.setScaling(Scaling.fit); // Ensure image fits within bounds

        Table overlay = new Table(skin);
        overlay.top().right(); // Align HP to top-right
        // Display HP only if > 0, or empty for items/artifacts if hp is 0. Show "0" if it's a creature with 0 HP.
        Label hpLabel = new Label(hp > 0 ? String.valueOf(hp) : (hp == 0 && (imgPath.contains("item") || imgPath.contains("artifact")) ? "" : "0"), new Label.LabelStyle(font, Color.WHITE));
        hpLabel.setFontScale(Math.max(0.6f, 0.0075f * width)); // Scale font based on card width, with a minimum
        hpLabel.setAlignment(Align.right);
        overlay.add(hpLabel).padTop(height * 0.015f).padRight(width * 0.025f); // Padding for HP label

        stack.add(img);
        stack.add(overlay);
        return stack;
    }


    private void refreshSkillsAndEndTurnUI() {
        skillsAndEndTable.clear(); // Clear previous skills
        skillsAndEndTable.defaults().pad(VIRTUAL_HEIGHT * 0.006f).fillX().width(ACTION_MENU_WIDTH_V - (VIRTUAL_WIDTH * 0.01f)); // Standard padding and width for skill buttons
        skillsAndEndTable.top(); // Align content to the top

        if (playerActive != null) {
            List<CardSystem.Skill> skills = playerActive.getSkills();
            if (skills != null && !skills.isEmpty()) {
                for (final CardSystem.Skill skill : skills) {
                    TextButton skillButton = new TextButton(skill.getName() + "\n(" + skill.getCost() + " Faith)", skin);
                    skillButton.getLabel().setWrap(true);
                    skillButton.getLabel().setAlignment(Align.center);
                    skillButton.getLabel().setFontScale(SKILL_BUTTON_FONT_SCALE_V); // Apply font scale
                    // Disable button if not player's turn, not enough faith, or card cannot be used (e.g., item already used)
                    if (!playerTurn || playerFaith < skill.getCost() || !playerActive.canUse()) {
                        skillButton.setDisabled(true);
                        skillButton.getLabel().setColor(Color.GRAY); // Visual cue for disabled
                    } else {
                        skillButton.getLabel().setColor(Color.WHITE);
                    }

                    skillButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            if (!playerTurn || skillButton.isDisabled()) return; // Double-check disabled state

                            if ("Retreat".equals(skill.getName())) {
                                showRetreatDialog(); // Handle retreat separately
                            } else if (playerFaith < skill.getCost()) {
                                log("Insufficient faith for " + skill.getName());
                            } else {
                                log("Player uses " + skill.getName() + " from " + playerActive.getName());
                                playerActive.markUsed(); // Mark artifact/item as used
                                int oldEnemyHp = enemyActive.getHealth();
                                int oldPlayerHp = playerActive.getHealth();

                                skill.apply(playerActive, enemyActive); // Apply skill effects
                                playerFaith -= skill.getCost(); // Deduct faith cost

                                // Log HP changes
                                if (enemyActive.getHealth() != oldEnemyHp) {
                                    log(enemyActive.getName() + " HP: " + oldEnemyHp + " -> " + enemyActive.getHealth());
                                }
                                if (playerActive.getHealth() != oldPlayerHp) {
                                    log(playerActive.getName() + " HP: " + oldPlayerHp + " -> " + playerActive.getHealth());
                                }
                                if (playerActive.getType() == CardSystem.CardType.ITEM && playerActive.getHealth() <=0) { // Special case for ITEM type
                                    playerActive.consume(); // ITEM is consumed after use
                                    log(playerActive.getName() + " was consumed.");
                                    playerGraveyard.add(playerActive); // Move to graveyard
                                    // playerActive might become null here if it was consumed, handle this state
                                }
                                refreshAll(); // Update all UI elements
                                checkEnemyDefeated();
                                checkPlayerDefeated();
                            }
                        }
                    });
                    skillsAndEndTable.add(skillButton).prefHeight(VIRTUAL_HEIGHT * 0.07f).row(); // Set preferred height for skill buttons
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
        // Add End Turn button at the bottom of the skills list
        endTurnBtn.getLabel().setFontScale(SKILL_BUTTON_FONT_SCALE_V * 1.15f); // Slightly larger font for End Turn
        skillsAndEndTable.add(endTurnBtn).padTop(VIRTUAL_HEIGHT * 0.015f).center().prefHeight(VIRTUAL_HEIGHT * 0.06f);
    }


    private void refreshHandUI() {
        handTable.clear();
        handTable.left().bottom(); // Align cards to the left and bottom of the scroll pane
        handTable.defaults().size(CARD_WIDTH_BENCH_HAND_V, CARD_HEIGHT_BENCH_HAND_V).pad(VIRTUAL_WIDTH * 0.005f); // Default size and padding for hand cards

        for (final CardSystem.Card c : playerHand) {
            ImageButton btn = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal(c.getImagePath())))));
            btn.getImageCell().grow(); // Make image fill the button
            btn.setTouchable(Touchable.enabled); // Ensure button is touchable

            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) {
                    if (!playerTurn) return; // Actions only on player's turn
                    Dialog dialog = new Dialog("Play Card", skin) {
                        @Override
                        protected void result(Object object) {
                            handleHandCard(c, object); // Handle dialog result
                        }
                    };
                    // Create label, style it, then add to dialog
                    Label textLabel = new Label("What to do with " + c.getName() + "?", skin);
                    textLabel.setFontScale(DIALOG_TEXT_FONT_SCALE_V); // Use scaled font
                    dialog.getContentTable().add(textLabel).pad(VIRTUAL_HEIGHT * 0.01f).row(); // Add styled label

                    // Add buttons and then style their labels
                    dialog.button("Set Active", "active");
                    dialog.button("To Bench", "bench");
                    dialog.button("Cancel", false);

                    // Style button labels after they are added
                    Table buttonTable = dialog.getButtonTable();
                    for (Cell cell : buttonTable.getCells()) {
                        Actor actor = cell.getActor();
                        if (actor instanceof TextButton) {
                            ((TextButton) actor).getLabel().setFontScale(DIALOG_BUTTON_FONT_SCALE_V); // Use scaled font for buttons
                        }
                    }
                    dialog.pad(VIRTUAL_HEIGHT * 0.03f); // Overall dialog padding
                    buttonTable.padTop(VIRTUAL_HEIGHT * 0.02f); // Padding above buttons
                    dialog.show(stage);
                }
            });
            handTable.add(btn); // Add card button to hand table
        }
    }

    private void refreshBenchUI() {
        benchTable.clear(); // Clear previous bench cards
        benchTable.left(); // Align bench cards to the left
        benchTable.defaults().size(CARD_WIDTH_BENCH_HAND_V, CARD_HEIGHT_BENCH_HAND_V).pad(VIRTUAL_WIDTH * 0.005f); // Default size and padding for bench cards
        Texture emptyTex = new Texture(Gdx.files.internal("ui/card_slot_empty.png")); // Texture for empty slots

        for (int i = 0; i < MAX_BENCH_SLOTS; i++) {
            if (i < playerBench.size()) {
                final CardSystem.Card c = playerBench.get(i);
                Stack s = createCardStack(c.getImagePath(), c.getHealth(), CARD_WIDTH_BENCH_HAND_V, CARD_HEIGHT_BENCH_HAND_V);
                s.setTouchable(Touchable.enabled); // Make stack touchable

                s.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (!playerTurn) return; // Actions only on player's turn
                        if (playerActive == null) { // If no active card, move this to active
                            playerActive = c;
                            playerBench.remove(c);
                            log(c.getName() + " moved from bench to active slot.");
                            refreshAll(); // Update UI
                        } else { // If active card exists, prompt to swap
                            Dialog dialog = new Dialog("Swap with Active?", skin){
                                @Override
                                protected void result(Object obj){
                                    if (Boolean.TRUE.equals(obj)) { // If "Yes"
                                        CardSystem.Card temp = playerActive;
                                        playerActive = c;
                                        playerBench.remove(c);
                                        playerBench.add(temp); // Move old active to bench
                                        log(c.getName() + " swapped with " + temp.getName() + " from bench.");
                                        refreshAll(); // Update UI
                                    }
                                }
                            };
                            Label textLabel = new Label("Swap " + c.getName() + " with " + playerActive.getName() + "?", skin);
                            textLabel.setFontScale(DIALOG_TEXT_FONT_SCALE_V); // Use scaled font
                            dialog.getContentTable().add(textLabel).pad(VIRTUAL_HEIGHT * 0.01f);

                            dialog.button("Yes", true);
                            dialog.button("No", false);

                            Table buttonTable = dialog.getButtonTable();
                            for (Cell cell : buttonTable.getCells()) { // Style dialog buttons
                                Actor actor = cell.getActor();
                                if (actor instanceof TextButton) {
                                    ((TextButton) actor).getLabel().setFontScale(DIALOG_BUTTON_FONT_SCALE_V);
                                }
                            }
                            dialog.pad(VIRTUAL_HEIGHT * 0.03f);
                            buttonTable.padTop(VIRTUAL_HEIGHT * 0.02f);
                            dialog.show(stage);
                        }
                    }
                });
                benchTable.add(s); // Add card stack to bench table
            } else {
                Image img = new Image(emptyTex); // Add empty slot image
                benchTable.add(img);
            }
        }
    }


    private void refreshDeckAndGraveUI() {
        // Ensure tables are initialized (should be done in buildUI)
        if (this.deckAndGraveTable == null) this.deckAndGraveTable = new Table(skin);
        if (this.graveTable == null) this.graveTable = new Table(skin);
        if (this.deckCountLabel == null) this.deckCountLabel = new Label("Deck: 0", new Label.LabelStyle(font, Color.WHITE));

        deckAndGraveTable.clear(); // Clear previous content
        this.graveTable.clear();    // Clear grave display area

        deckAndGraveTable.top().pad(VIRTUAL_HEIGHT * 0.0055f); // Align to top with padding

        // Graveyard Label
        Label graveLabelText = new Label("Grave (" + playerGraveyard.size() + ")", new Label.LabelStyle(font, Color.LIGHT_GRAY));
        graveLabelText.setFontScale(HUD_FONT_SCALE_V * 0.85f); // Slightly smaller than main HUD font
        deckAndGraveTable.add(graveLabelText).colspan(MAX_BENCH_SLOTS).center().padBottom(VIRTUAL_HEIGHT * 0.005f).row();

        // Graveyard Cards (display last few, e.g., 3)
        this.graveTable.defaults().size(CARD_WIDTH_GRAVE_V, CARD_HEIGHT_GRAVE_V).pad(VIRTUAL_WIDTH * 0.0015f);
        int graveDisplayCount = 0;
        for (int i = playerGraveyard.size() - 1; i >= 0 && graveDisplayCount < MAX_BENCH_SLOTS; i--) {
            CardSystem.Card c = playerGraveyard.get(i);
            this.graveTable.add(createCardStack(c.getImagePath(), 0, CARD_WIDTH_GRAVE_V, CARD_HEIGHT_GRAVE_V)).pad(VIRTUAL_WIDTH * 0.001f); // HP 0 for grave cards
            graveDisplayCount++;
        }
        // Fill remaining grave display slots with empty images if fewer than MAX_BENCH_SLOTS cards are in grave
        // Texture emptySlotTexture = new Texture(Gdx.files.internal("ui/card_slot_empty.png")); // Assuming you have an empty slot texture
        // while (graveDisplayCount < MAX_BENCH_SLOTS) {
        //     this.graveTable.add(new Image(emptySlotTexture)).size(CARD_WIDTH_GRAVE_V, CARD_HEIGHT_GRAVE_V).pad(VIRTUAL_WIDTH * 0.001f);
        //     graveDisplayCount++;
        // }


        deckAndGraveTable.add(this.graveTable).colspan(MAX_BENCH_SLOTS).center().minHeight(CARD_HEIGHT_GRAVE_V + (VIRTUAL_HEIGHT * 0.01f)).row();

        // Deck Count Label and Image
        deckCountLabel.setText("Deck: " + playerDeck.size());
        deckCountLabel.setFontScale(HUD_FONT_SCALE_V * 0.85f);
        deckAndGraveTable.add(deckCountLabel).colspan(MAX_BENCH_SLOTS).center().padTop(VIRTUAL_HEIGHT * 0.015f).row();
        Texture deckTex = new Texture(Gdx.files.internal("ui/card_slot_empty.png")); // Placeholder for deck image
        Image deckImage = new Image(deckTex);
        deckAndGraveTable.add(deckImage).size(CARD_WIDTH_BENCH_HAND_V * 0.9f, CARD_HEIGHT_BENCH_HAND_V * 0.9f) // Slightly smaller deck image
            .colspan(MAX_BENCH_SLOTS).center().padTop(VIRTUAL_HEIGHT * 0.008f);
    }

    // Updates the player's active card display
    private void updateActiveStack() {
        if (playerActive != null) {
            activeHpLabel.setText(String.valueOf(playerActive.getHealth())); // Update HP
            Image img = (Image)activeStack.getChildren().first(); // Get the Image widget from the Stack
            // Ensure the drawable is updated with the correct card image
            img.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal(playerActive.getImagePath())))));
        } else {
            activeHpLabel.setText(""); // No HP if no active card
            Image img = (Image)activeStack.getChildren().first();
            img.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("ui/card_slot_empty.png"))))); // Show empty slot
        }
        refreshSkillsAndEndTurnUI(); // Active card change might affect available skills
    }


    // Updates the enemy's active card display
    private void updateEnemyStack() {
        if(enemyActive == null) return; // Guard clause if no enemy
        enemyHpLabel.setText(String.valueOf(enemyActive.getHealth())); // Update HP
        Image img = (Image)enemyStack.getChildren().first(); // Get the Image widget
        // Update drawable with enemy's card image
        img.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal(enemyActive.getImagePath())))));
    }


    // Call this method to refresh all relevant UI parts after an action
    private void refreshAll() {
        refreshAllUIModules(); // Centralized UI refresh
        // Reset turn-based statuses/flags for player cards if it's the start of their turn
        if (playerActive != null) playerActive.resetTurnUsage(); // For artifacts, etc.
        for(CardSystem.Card benchCard : playerBench) {
            benchCard.resetTurnUsage();
        }
    }


    private void handleHandCard(final CardSystem.Card c, Object action) {
        if ("active".equals(action)) {
            if (playerActive == null) { // If no active card, make this one active
                playerActive = c;
                playerHand.remove(c);
                log("Player set active to " + c.getName());
                refreshAll(); // Update UI
            } else { // Active slot is occupied
                log("Active slot is already occupied.");
                if (playerBench.size() < MAX_BENCH_SLOTS) { // Check if bench has space
                    Dialog dialog = new Dialog("Active Slot Occupied", skin) {
                        @Override
                        protected void result(Object moveChoiceObject) {
                            if ("moveToBench".equals(moveChoiceObject)) { // If player chooses to move current active to bench
                                playerBench.add(playerActive); // Move current active to bench
                                playerActive = c; // Set new card as active
                                playerHand.remove(c);
                                log(c.getName() + " is now active. Previous active ("+ playerBench.get(playerBench.size()-1).getName() +") moved to bench.");
                                refreshAll(); // Update UI
                            }
                        }
                    };
                    Label textLabel = new Label(playerActive.getName() + " is active. Move it to bench to play " + c.getName() + "?", skin);
                    textLabel.setFontScale(DIALOG_TEXT_FONT_SCALE_V); // Use scaled font
                    textLabel.setWrap(true); // Allow text wrapping
                    dialog.getContentTable().add(textLabel).width(VIRTUAL_WIDTH * 0.35f).pad(VIRTUAL_HEIGHT * 0.01f).row();


                    dialog.button("Yes, move to Bench", "moveToBench");
                    dialog.button("Cancel", false);

                    // Style dialog buttons
                    Table buttonTable = dialog.getButtonTable();
                    for(Cell cell : buttonTable.getCells()){
                        if(cell.getActor() instanceof TextButton){
                            ((TextButton)cell.getActor()).getLabel().setFontScale(DIALOG_BUTTON_FONT_SCALE_V);
                        }
                    }
                    dialog.pad(VIRTUAL_HEIGHT * 0.03f);
                    buttonTable.padTop(VIRTUAL_HEIGHT * 0.02f);
                    dialog.show(stage);
                } else {
                    log("Bench is full. Cannot move current active card to bench.");
                }
            }
        } else if ("bench".equals(action)) {
            if (playerBench.size() < MAX_BENCH_SLOTS) { // Check if bench has space
                playerBench.add(c);
                playerHand.remove(c);
                log("Player benched " + c.getName());
                refreshAll(); // Update UI
            } else {
                log("Bench is full. Cannot move " + c.getName() + " to bench.");
            }
        }
        // "Cancel" or other actions do nothing here
    }


    private void showRetreatDialog() {
        if (playerActive == null) { log("No active card to retreat."); return; }
        if (playerBench.isEmpty()) { log("Cannot retreat: no cards on the bench."); return; }

        final SelectBox<String> box = new SelectBox<>(skin);
        List<String> benchCardNames = new ArrayList<>();
        for (CardSystem.Card benchCard : playerBench) { benchCardNames.add(benchCard.getName()); }
        if (benchCardNames.isEmpty()) { log("No cards on bench to select for retreat."); return; } // Should not happen if playerBench is not empty
        box.setItems(benchCardNames.toArray(new String[0]));

        // Apply font scale to SelectBox style
        SelectBox.SelectBoxStyle boxStyle = new SelectBox.SelectBoxStyle(skin.get("default", SelectBox.SelectBoxStyle.class));
        boxStyle.font.getData().setScale(DIALOG_TEXT_FONT_SCALE_V); // Scale the font of the selected item
        boxStyle.listStyle.font.getData().setScale(DIALOG_TEXT_FONT_SCALE_V); // Scale the font of items in the dropdown list
        box.setStyle(boxStyle);


        final Dialog retreatDialog = new Dialog("Retreat: Select Card from Bench", skin) {
            @Override
            protected void result(Object obj) {
                if (Boolean.TRUE.equals(obj)) { // If "Swap" is pressed
                    if (box.getItems().size == 0) { log("Error: No items in bench selection for retreat."); return; } // Should ideally not happen
                    int selectedBenchIndex = box.getSelectedIndex();
                    if (selectedBenchIndex < 0 || selectedBenchIndex >= playerBench.size()) { log("Error: Invalid selection from bench for retreat."); return; }

                    CardSystem.Card cardToMakeActive = playerBench.get(selectedBenchIndex);
                    playerBench.remove(selectedBenchIndex); // Remove from bench
                    playerBench.add(playerActive);           // Add old active to bench
                    playerActive = cardToMakeActive;        // Set new active card

                    log(playerActive.getName() + " is now active (retreated). " + playerBench.get(playerBench.size()-1).getName() + " moved to bench.");
                    refreshAll(); // Update UI
                }
            }
        };
        retreatDialog.getTitleLabel().setFontScale(DIALOG_TEXT_FONT_SCALE_V * 1.1f); // Scale title font

        Label swapLabel = new Label("Swap with:", skin);
        swapLabel.setFontScale(DIALOG_TEXT_FONT_SCALE_V); // Scale label font
        retreatDialog.getContentTable().add(swapLabel).pad(VIRTUAL_HEIGHT * 0.01f).row();
        retreatDialog.getContentTable().add(box).width(VIRTUAL_WIDTH * 0.3f).pad(VIRTUAL_HEIGHT * 0.01f).row(); // Add SelectBox

        retreatDialog.button("Swap", true);
        retreatDialog.button("Cancel", false);

        // Scale button fonts
        Table buttonTable = retreatDialog.getButtonTable();
        for(Cell cell : buttonTable.getCells()){
            if(cell.getActor() instanceof TextButton){
                ((TextButton)cell.getActor()).getLabel().setFontScale(DIALOG_BUTTON_FONT_SCALE_V);
            }
        }
        retreatDialog.pad(VIRTUAL_HEIGHT * 0.03f); // Overall dialog padding
        buttonTable.padTop(VIRTUAL_HEIGHT * 0.02f); // Padding for button table
        retreatDialog.show(stage);
    }


    private void enemyTurn() {
        if (enemyActive == null || enemyActive.getHealth() <= 0) {
            log("Enemy is already defeated or not present.");
            playerTurn = true; // Give turn back to player or end battle
            refreshSkillsAndEndTurnUI(); // Update UI to reflect player's turn
            return;
        }

        log("-- Enemy turn (Round " + roundNumber + ") --");
        if (playerActive != null && playerActive.getHealth() > 0) { // Check if player has an active card that can be targeted
            if (enemyActive.getSkills() != null && !enemyActive.getSkills().isEmpty()) {
                // Simple AI: Enemy uses a random skill
                CardSystem.Skill sk = enemyActive.getSkills().get(MathUtils.random(enemyActive.getSkills().size() - 1));
                log("Enemy " + enemyActive.getName() + " uses " + sk.getName());
                int oldHp = playerActive.getHealth();
                sk.apply(enemyActive, playerActive); // Enemy (user) targets player's active card (target)
                log(playerActive.getName() + " HP: " + oldHp + " -> " + playerActive.getHealth());
                checkPlayerDefeated(); // Check if player's card was defeated
            } else {
                log(enemyActive.getName() + " has no skills to use.");
            }
        } else {
            log("Player has no active card to target or active card is defeated.");
        }
        // Apply enemy's start/end of turn statuses
        if(enemyActive != null) { // Check again in case it was defeated by a reflect/thorns status etc.
            enemyActive.applyStartOfTurnStatuses();
            enemyActive.resetTurnUsage(); // For its own artifacts/items if any
            enemyActive.applyEndOfTurnStatuses();
        }

        // Transition to player's turn
        playerTurn = true;
        log("-- Player turn (Round " + (roundNumber + 1) + ") --"); // Increment round for display for next player turn

        // Player turn start-up: increase faith, apply statuses, draw card
        if (playerFaith < MAX_FAITH) {
            playerFaith++;
        }

        if(playerActive != null) playerActive.applyStartOfTurnStatuses();
        for(CardSystem.Card benchCard : playerBench) { // Apply statuses to benched cards too
            benchCard.applyStartOfTurnStatuses();
        }

        drawCards(1); // Player draws one card at the start of their turn
        roundNumber++; // Actual round increment
        refreshAll(); // Refresh UI for player's turn
    }


    private void checkEnemyDefeated() {
        if (enemyActive != null && enemyActive.getHealth() <= 0) {
            log("Enemy " + enemyActive.getName() + " defeated!");
            // Victory Dialog
            Dialog victoryDialog = new Dialog("Victory!", skin) {
                @Override
                protected void result(Object obj) {
                    // On "Continue", move to the current node on map and return to map screen
                    if (mapScreen != null && mapScreen.getCurrentNode() != null) {
                        mapScreen.moveTo(mapScreen.getCurrentNode()); // Assumes current node is the battle node
                    }
                    game.setScreen(mapScreen); // Return to the map screen
                }
            };
            Label victoryText = new Label("You have defeated " + enemyActive.getName() + "!", skin);
            victoryText.setFontScale(DIALOG_TEXT_FONT_SCALE_V);
            victoryDialog.getContentTable().add(victoryText).pad(VIRTUAL_HEIGHT * 0.01f);
            victoryDialog.button("Continue", true); // true is the object passed to result()

            // Scale button fonts
            Table buttonTable = victoryDialog.getButtonTable();
            for(Cell cell : buttonTable.getCells()){
                if(cell.getActor() instanceof TextButton){
                    ((TextButton)cell.getActor()).getLabel().setFontScale(DIALOG_BUTTON_FONT_SCALE_V);
                }
            }
            victoryDialog.pad(VIRTUAL_HEIGHT * 0.04f); // Apply padding to the dialog itself
            victoryDialog.show(stage); // Show the dialog
        }
    }

    private void checkPlayerDefeated() {
        boolean activeCardWasDefeated = false;
        if (playerActive != null && playerActive.getHealth() <= 0) {
            log("Active card " + playerActive.getName() + " was defeated.");
            playerGraveyard.add(playerActive); // Move defeated card to graveyard
            activeDeathCount++; // Increment death count
            playerActive = null; // Clear active slot
            activeCardWasDefeated = true;
        }

        if (activeCardWasDefeated) { // Only proceed if an active card was just defeated
            if (activeDeathCount >= MAX_ACTIVE_DEATHS) {
                log("All lives lost! Game Over.");
                Dialog defeatDialog = new Dialog("Defeat!", skin) {
                    @Override
                    protected void result(Object obj) { game.setScreen(new MainFernan(game)); } // Return to main menu
                };
                Label defeatText = new Label("You have run out of lives!", skin);
                defeatText.setFontScale(DIALOG_TEXT_FONT_SCALE_V);
                defeatDialog.getContentTable().add(defeatText).pad(VIRTUAL_HEIGHT * 0.01f);
                defeatDialog.button("To Main Menu", true);
                // Scale button fonts
                Table buttonTable = defeatDialog.getButtonTable();
                for(Cell cell : buttonTable.getCells()){
                    if(cell.getActor() instanceof TextButton){
                        ((TextButton)cell.getActor()).getLabel().setFontScale(DIALOG_BUTTON_FONT_SCALE_V);
                    }
                }
                defeatDialog.pad(VIRTUAL_HEIGHT * 0.04f);
                defeatDialog.show(stage);

            } else if (playerBench.isEmpty() && playerHand.isEmpty() && playerDeck.isEmpty()){ // Check if player has any cards left
                log("No cards left in hand, deck or bench! Game Over.");
                Dialog noCardsDialog = new Dialog("Defeat!", skin) {
                    @Override
                    protected void result(Object obj) { game.setScreen(new MainFernan(game)); } // Return to main menu
                };
                Label noCardsText = new Label("You have no more cards to play!", skin);
                noCardsText.setFontScale(DIALOG_TEXT_FONT_SCALE_V);
                noCardsDialog.getContentTable().add(noCardsText).pad(VIRTUAL_HEIGHT * 0.01f);
                noCardsDialog.button("To Main Menu", true);
                // Scale button fonts
                Table buttonTable = noCardsDialog.getButtonTable();
                for(Cell cell : buttonTable.getCells()){
                    if(cell.getActor() instanceof TextButton){
                        ((TextButton)cell.getActor()).getLabel().setFontScale(DIALOG_BUTTON_FONT_SCALE_V);
                    }
                }
                noCardsDialog.pad(VIRTUAL_HEIGHT * 0.04f);
                noCardsDialog.show(stage);
            } else {
                // Player still has lives and cards, prompt to select new active card
                if (!playerBench.isEmpty()) {
                    log("Your active card was defeated. Select a card from your bench to make active.");
                } else if (!playerHand.isEmpty()) { // If bench is empty, check hand
                    log("Your active card was defeated. Play a card from your hand.");
                }
                // If both bench and hand are empty but deck is not, player must end turn to draw or game over if deck is also empty (covered above)
            }
            refreshAll(); // Refresh UI to reflect changes
        }
    }


    // Draws n cards from the player's deck to their hand
    private void drawCards(int n) {
        for (int i = 0; i < n; i++) {
            if (!playerDeck.isEmpty()) {
                CardSystem.Card drawnCard = playerDeck.remove(0); // Remove from top of deck
                playerHand.add(drawnCard); // Add to hand
                if (logTable != null) { // Check if log system is initialized
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
                break; // Stop drawing if deck is empty
            }
        }
        // Refresh UI elements related to hand and deck count
        if (handTable != null) { // Check if handTable is initialized
            refreshHandUI();
        }
        if (deckCountLabel != null && deckAndGraveTable != null) { // Check if deck/grave UI is initialized
            deckCountLabel.setText("Deck: " + playerDeck.size());
            // You might also want to call refreshDeckAndGraveUI() if grave display changes on draw (e.g. milling)
        }
    }


    @Override public void render(float delta) {
        ScreenUtils.clear(Color.BLACK); // Clear the screen
        stage.getViewport().apply(); // Apply viewport settings
        stage.act(delta); // Update stage actors
        stage.draw(); // Draw the stage
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null && stage.getViewport() != null) {
            stage.getViewport().update(width, height, true); // Update viewport on resize
        }
    }
    @Override public void pause() {}
    @Override public void resume() {}

    @Override public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (font != null) font.dispose();
        // Dispose any other manually loaded textures here if they are class members and loaded in this screen
    }
}
