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
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.Pixmap;

import java.util.HashMap;
import java.util.Map;

public class MapNavigationFernan implements Screen {
    private static final float DEFAULT_ICON_SIZE = 3f;

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
    private final float labelOffset = 5f;
    private final float labelScale = 0.05f;

    private Vector2 playerPosition;
    private final Map<Rectangle, Integer> clickableIconIndices;
    private final Array<Vector2> iconPositions;
    private final Map<Integer, String> iconTypes;
    private int currentIconIndex = 0;
    private Integer lastValidMoveIndex = null;

    private final Viewport viewport;
    private final Stage stage;
    private final Label validPathLabel;
    private final BitmapFont labelFont;
    private final Color pathLabelColor = Color.YELLOW;

    public MapNavigationFernan(final FernansGrace game) {
        this.game = game;
        this.batch = new SpriteBatch();

        // Load textures with error handling
        this.backgroundTexture = loadTexture("map_background.png");
        this.playerTexture = loadTexture("yellow_x.png");
        this.enemy1Texture = loadTexture("swords.png");
        this.rewardTexture = loadTexture("cart.png");
        this.enemy2Texture = loadTexture("axes.png");
        this.healingTexture = loadTexture("temple.png");
        this.bossTexture = loadTexture("skull.png");

        viewport = new FitViewport(48, 27); // Adjusted to match your world coordinates
        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        labelFont = new BitmapFont();
        Label.LabelStyle labelStyle = new Label.LabelStyle(labelFont, Color.GREEN);
        validPathLabel = new Label("", labelStyle);
        validPathLabel.setPosition(10, viewport.getWorldHeight() - 20);
        validPathLabel.setFontScale(0.5f);
        stage.addActor(validPathLabel);

        // Initialize collections
        iconPositions = new Array<>();
        clickableIconIndices = new HashMap<>();
        iconTypes = new HashMap<>();

        setupMapPositions();
    }

    private Texture loadTexture(String path) {
        try {
            return new Texture(Gdx.files.internal(path));
        } catch (Exception e) {
            Gdx.app.error("MapNavigationFernan", "Error loading texture: " + path, e);
            // Create a 1x1 white pixel as fallback
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            Texture fallback = new Texture(pixmap);
            pixmap.dispose();
            return fallback;
        }
    }

    private void setupMapPositions() {
        // Define positions (using your original coordinates)
        iconPositions.addAll(
            new Vector2(7, 4),    // 0: Player
            new Vector2(10, 20),  // 1: Enemy1
            new Vector2(15, 15),  // 2: Reward
            new Vector2(10, 8),    // 3: Enemy1
            new Vector2(24, 22),   // 4: Enemy2
            new Vector2(38, 22),   // 5: Healing
            new Vector2(42, 18),   // 6: Boss
            new Vector2(35, 6),    // 7: Reward
            new Vector2(20, 4),    // 8: Enemy2
            new Vector2(18, 6),   // 9: Reward
            new Vector2(26, 6)     // 10: Enemy1
        );

        playerPosition = new Vector2(iconPositions.get(0));

        // Setup icon types and clickable areas
        iconTypes.put(0, "player");
        addIcon(1, "enemy1", enemy1IconSize);
        addIcon(2, "reward", rewardIconSize);
        addIcon(3, "enemy1", enemy1IconSize);
        addIcon(4, "enemy2", enemy2IconSize);
        addIcon(5, "healing", healingIconSize);
        addIcon(6, "boss", bossIconSize);
        addIcon(7, "reward", rewardIconSize);
        addIcon(8, "enemy2", enemy2IconSize);
        addIcon(9, "reward", rewardIconSize);
        addIcon(10, "enemy1", enemy1IconSize);
    }

    private void addIcon(int index, String type, float size) {
        iconTypes.put(index, type);
        Vector2 pos = iconPositions.get(index);
        Rectangle rect = new Rectangle(pos.x - size / 2, pos.y - size / 2, size, size);
        clickableIconIndices.put(rect, index);
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        // Draw background
        batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        // Draw all icons
        for (int i = 0; i < iconPositions.size; i++) {
            Vector2 pos = iconPositions.get(i);
            String type = iconTypes.get(i);

            if (i == currentIconIndex) {
                // Draw player
                batch.draw(playerTexture,
                    playerPosition.x - playerIconSize / 2,
                    playerPosition.y - playerIconSize / 2,
                    playerIconSize, playerIconSize);
            } else {
                // Draw other icons
                Texture texture = getTextureByType(type);
                if (texture != null) {
                    float size = getIconSizeByType(type);
                    batch.draw(texture, pos.x - size / 2, pos.y - size / 2, size, size);
                }
            }

            // Draw movement indicators
            drawMovementIndicators(i);
        }

        batch.end();

        stage.act(delta);
        stage.draw();

        handleInput();
    }

    private void drawMovementIndicators(int iconIndex) {
        if (iconIndex == currentIconIndex) return;

        Vector2 pos = iconPositions.get(iconIndex);
        String type = iconTypes.get(iconIndex);
        float size = getIconSizeByType(type);

        labelFont.getData().setScale(labelScale);
        float scaledLabelOffset = labelOffset * labelScale;
        float labelHeight = labelFont.getCapHeight() * labelScale;

        // Check if this is a valid next move
        if (isValidMove(currentIconIndex, iconIndex)) {
            labelFont.setColor(pathLabelColor);

            // Determine direction label
            String direction = getDirectionLabel(currentIconIndex, iconIndex);
            float labelWidth = calculateLabelWidth(direction);

            // Position label appropriately
            if (direction.equals("UP")) {
                labelFont.draw(batch, direction,
                    pos.x - labelWidth / 2,
                    pos.y + size / 2 + scaledLabelOffset + labelHeight);
            } else if (direction.equals("SIDE")) {
                labelFont.draw(batch, direction,
                    pos.x + size / 2 + scaledLabelOffset,
                    pos.y + labelHeight / 2);
            }
        }

        labelFont.getData().setScale(1.0f);
    }

    private boolean isValidMove(int fromIndex, int toIndex) {
        // Basic linear progression
        if (toIndex == fromIndex + 1) return true;

        // Special cases for side movements
        if ((fromIndex == 1 && toIndex == 2) || (fromIndex == 2 && toIndex == 1)) return true;
        if ((fromIndex == 3 && toIndex == 8) || (fromIndex == 8 && toIndex == 3)) return true;
        if ((fromIndex == 4 && toIndex == 5) || (fromIndex == 5 && toIndex == 4)) return true;
        if ((fromIndex == 7 && toIndex == 9) || (fromIndex == 9 && toIndex == 7)) return true;

        return false;
    }

    private String getDirectionLabel(int fromIndex, int toIndex) {
        // Simple heuristic - if higher in array, consider it "UP"
        return toIndex > fromIndex ? "UP" : "SIDE";
    }

    private float calculateLabelWidth(String text) {
        float width = 0;
        for (char c : text.toCharArray()) {
            width += labelFont.getData().getGlyph(c).width * labelScale;
        }
        return width;
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector2 touchPos = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

            for (Map.Entry<Rectangle, Integer> entry : clickableIconIndices.entrySet()) {
                if (entry.getKey().contains(touchPos)) {
                    int targetIndex = entry.getValue();

                    if (isValidMove(currentIconIndex, targetIndex)) {
                        movePlayer(targetIndex);
                    } else {
                        validPathLabel.setText("Invalid Path!");
                    }
                    return;
                }
            }
        }
    }

    private void movePlayer(int targetIndex) {
        playerPosition.set(iconPositions.get(targetIndex));
        currentIconIndex = targetIndex;
        lastValidMoveIndex = targetIndex;
        validPathLabel.setText("Moved to: " + iconTypes.get(targetIndex));
        handleIconInteraction(iconTypes.get(targetIndex));
    }

    private void handleIconInteraction(String type) {
        Gdx.app.log("MapNavigation", "Entering: " + type);

        switch (type) {
            case "enemy1":
                System.out.println("MA'AM PLEASE EXTEND");
                break;
            case "reward":
                System.out.println("MA'AM PLEASE EXTEND");
                break;
            case "enemy2":
                System.out.println("MA'AM PLEASE EXTEND");
                break;
            case "healing":
                System.out.println("MA'AM PLEASE EXTEND");
                break;
            case "boss":
                System.out.println("MA'AM PLEASE EXTEND");
                break;
            default:
                System.out.println("Unknown icon type: " + type);
                break;
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        Disposable[] resources = {
            backgroundTexture, playerTexture, enemy1Texture,
            rewardTexture, enemy2Texture, healingTexture,
            bossTexture, batch, stage, labelFont
        };

        for (Disposable resource : resources) {
            if (resource != null) {
                resource.dispose();
            }
        }
    }

    // Other required Screen methods
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    // Helper methods
    private float getIconSizeByType(String type) {
        switch (type) {
            case "player": return playerIconSize;
            case "enemy1": return enemy1IconSize;
            case "reward": return rewardIconSize;
            case "enemy2": return enemy2IconSize;
            case "healing": return healingIconSize;
            case "boss": return bossIconSize;
            default: return DEFAULT_ICON_SIZE;
        }
    }

    private Texture getTextureByType(String type) {
        switch (type) {
            case "enemy1": return enemy1Texture;
            case "reward": return rewardTexture;
            case "enemy2": return enemy2Texture;
            case "healing": return healingTexture;
            case "boss": return bossTexture;
            default: return null;
        }
    }
}


