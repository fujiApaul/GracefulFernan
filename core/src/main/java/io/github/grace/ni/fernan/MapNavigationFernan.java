package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.HashMap;
import java.util.Map;

public class MapNavigationFernan implements Screen {

    private final FernansGrace game;
    private final SpriteBatch batch;
    private final Texture backgroundTexture;
    private final Texture playerTexture;
    private final Texture enemy1Texture;
    private final Texture rewardTexture;
    private final Texture enemy2Texture;
    private final Texture healingTexture;
    private final Texture bossTexture;

    private final float playerIconSize = 4f;
    private final float enemy1IconSize = 3f;
    private final float rewardIconSize = 3f;
    private final float enemy2IconSize = 5f;
    private final float healingIconSize = 3f;
    private final float bossIconSize = 8f;

    private Vector2 playerPosition;
    private Map<Rectangle, String> clickableIcons;
    private Array<Vector2> iconPositions;
    private Map<Integer, String> iconTypes; // Map index to type
    private int currentIconIndex = 0;
    private boolean canGoUp = true;
    private Integer lastValidMoveIndex = null; // Track the last valid move

    private Viewport viewport;
    private Stage stage;
    private Label validPathLabel;

    public MapNavigationFernan(final FernansGrace game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.backgroundTexture = new Texture(Gdx.files.internal("map_background.png"));
        this.playerTexture = new Texture(Gdx.files.internal("yellow_x.png"));
        this.enemy1Texture = new Texture(Gdx.files.internal("swords.png"));
        this.rewardTexture = new Texture(Gdx.files.internal("cart.png"));
        this.enemy2Texture = new Texture(Gdx.files.internal("axes.png"));
        this.healingTexture = new Texture(Gdx.files.internal("temple.png"));
        this.bossTexture = new Texture(Gdx.files.internal("skull.png"));

        viewport = new FitViewport(16 * 3, 9 * 3);
        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        BitmapFont font = new BitmapFont(); // You might want a better font
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.GREEN);
        validPathLabel = new Label("", labelStyle);
        validPathLabel.setPosition(10, viewport.getWorldHeight() - 20); // Adjust position as needed
        stage.addActor(validPathLabel);

        // Define the approximate positions of the icons based on image2.png
        // Adjusted for the 48x27 world size
        iconPositions = new Array<>();
        iconPositions.add(new Vector2(7, 4));       // Player (Yellow X)
        iconPositions.add(new Vector2(10, 20));    // Enemy 1 (Swords) - Top left
        iconPositions.add(new Vector2(15, 15));    // Reward (Cart) - Middle left
        iconPositions.add(new Vector2(10, 8));     // Enemy 1 (Swords) - Bottom left
        iconPositions.add(new Vector2(24, 22));    // Enemy 2 (Axes) - Top middle
        iconPositions.add(new Vector2(38, 22));    // Healing (Temple) - Top right
        iconPositions.add(new Vector2(42, 18));    // Boss (Skull) - Top rightmost
        iconPositions.add(new Vector2(35, 6));     // Reward (Cart) - Bottom right
        iconPositions.add(new Vector2(20, 4));     // Enemy 2 (Axes) - Bottom middle
        iconPositions.add(new Vector2(18, 6));     // Reward (Cart) - Bottom left
        iconPositions.add(new Vector2(26, 6));     // Enemy 1 (Swords) - Near bottom axes

        playerPosition = new Vector2(iconPositions.get(0));
        clickableIcons = new HashMap<>();
        iconTypes = new HashMap<>();

        // Create rectangles for each icon and associate them with a type
        for (int i = 0; i < iconPositions.size; i++) {
            Vector2 pos = iconPositions.get(i);
            String type = "";
            float currentIconSize;
            switch (i) {
                case 0:
                    type = "player";
                    currentIconSize = playerIconSize;
                    break;
                case 1:
                case 3:
                case 10:
                    type = "enemy1";
                    currentIconSize = enemy1IconSize;
                    break;
                case 2:
                case 7:
                case 9:
                    type = "reward";
                    currentIconSize = rewardIconSize;
                    break;
                case 4:
                case 8:
                    type = "enemy2";
                    currentIconSize = enemy2IconSize;
                    break;
                case 5:
                    type = "healing";
                    currentIconSize = healingIconSize;
                    break;
                case 6:
                    type = "boss";
                    currentIconSize = bossIconSize;
                    break;
                default:
                    currentIconSize = 3f; // Default size
                    break;
            }
            Rectangle rect = new Rectangle(pos.x - currentIconSize / 2, pos.y - currentIconSize / 2, currentIconSize, currentIconSize);
            iconTypes.put(i, type);
            if (!type.equals("player")) {
                clickableIcons.put(rect, type);
            }
        }
    }

    @Override
    public void show() {
        // Called when this screen becomes the current screen of the Game.
    }

    @Override
    public void render(float delta) {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        // Draw the player
        float playerSize = playerIconSize;
        batch.draw(playerTexture, playerPosition.x - playerSize / 2, playerPosition.y - playerSize / 2, playerSize, playerSize);

        // Draw the other icons
        for (int i = 0; i < iconPositions.size; i++) {
            if (i == 0) continue; // Skip the player
            Vector2 pos = iconPositions.get(i);
            String type = iconTypes.get(i);
            Texture texture = getTextureByType(type);
            float currentSize = getIconSizeByType(type);
            batch.draw(texture, pos.x - currentSize / 2, pos.y - currentSize / 2, currentSize, currentSize);
        }

        batch.end();

        stage.act(delta);
        stage.draw();

        handleInput();
    }

    private float getIconSizeByType(String type) {
        switch (type) {
            case "player":
                return playerIconSize;
            case "enemy1":
                return enemy1IconSize;
            case "reward":
                return rewardIconSize;
            case "enemy2":
                return enemy2IconSize;
            case "healing":
                return healingIconSize;
            case "boss":
                return bossIconSize;
            default:
                return 3f; // Default size
        }
    }

    private Texture getTextureByType(String type) {
        switch (type) {
            case "enemy1":
                return enemy1Texture;
            case "reward":
                return rewardTexture;
            case "enemy2":
                return enemy2Texture;
            case "healing":
                return healingTexture;
            case "boss":
                return bossTexture;
            default:
                return null;
        }
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector2 touchPos = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

            // Check for clicks on the clickable icons
            for (Map.Entry<Rectangle, String> entry : clickableIcons.entrySet()) {
                if (entry.getKey().contains(touchPos)) {
                    String type = entry.getValue();
                    int clickedIndex = -1;
                    for (int i = 0; i < iconPositions.size; i++) {
                        Vector2 pos = iconPositions.get(i);
                        float currentSize = getIconSizeByType(type);
                        if (new Rectangle(pos.x - currentSize / 2, pos.y - currentSize / 2, currentSize, currentSize).contains(touchPos)) {
                            clickedIndex = i;
                            break;
                        }
                    }

                    if (clickedIndex != -1 && clickedIndex != currentIconIndex) {
                        int diff = clickedIndex - currentIconIndex;
                        boolean isValidMove = false;

                        // Basic movement logic (adjust based on image2)
                        if (Math.abs(diff) == 1 || (canGoUp && diff > 0 && diff <= 3)) {
                            isValidMove = true;
                        }

                        if (isValidMove) {
                            playerPosition.set(iconPositions.get(clickedIndex));
                            currentIconIndex = clickedIndex;
                            canGoUp = false; // Once moved, can't go back to the "above" icon (basic logic)
                            lastValidMoveIndex = clickedIndex;
                            validPathLabel.setText("Moved to: " + type);
                            handleIconInteraction(type);
                        } else {
                            validPathLabel.setText("Invalid Path");
                        }
                    }
                    break; // Only process one click per touch
                }
            }
        }
    }
    private void handleIconInteraction(String type) {
        System.out.println("Clicked on: " + type);
        // Based on the type, navigate to the next screen
        switch (type) {
            case "enemy1":
                //game.setScreen(new BattleScreen(game, "Enemy 1")); // Replace with your battle screen
                System.out.println("MA'AM PLEASE EXTEND");
                break;
            case "reward":
                //game.setScreen(new RewardScreen(game)); // Replace with your reward screen
                System.out.println("MA'AM PLEASE EXTEND");
                break;
            case "enemy2":
                //game.setScreen(new BattleScreen(game, "Enemy 2")); // Replace with your battle screen
                System.out.println("MA'AM PLEASE EXTEND");
                break;
            case "healing":
                //game.setScreen(new HealingScreen(game)); // Replace with your healing screen
                System.out.println("MA'AM PLEASE EXTEND");
                break;
            case "boss":
                //game.setScreen(new BossBattleScreen(game)); // Replace with your boss battle screen
                System.out.println("MA'AM PLEASE EXTEND");
                break;
            default:
                System.out.println("Unknown icon type: " + type);
                break;
        }
        dispose(); // Dispose of the map screen after moving to the next
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {
        // Called when the game is paused.
    }

    @Override
    public void resume() {
        // Called when the game is resumed.
    }

    @Override
    public void hide() {
        // Called when this screen is no longer the current screen of the Game.
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        playerTexture.dispose();
        enemy1Texture.dispose();
        rewardTexture.dispose();
        enemy2Texture.dispose();
        healingTexture.dispose();
        bossTexture.dispose();
        batch.dispose();
        stage.dispose();
    }
}
