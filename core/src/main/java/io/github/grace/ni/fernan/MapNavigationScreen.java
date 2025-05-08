package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class MapNavigationScreen implements Screen {
    private final FernansGrace game;
    private OrthographicCamera camera;
    private SpriteBatch batch;

    // Textures
    private Texture background;
    private Texture pathUp;
    private Texture pathSide;
    private Texture yellowX;

    // Path rectangles for click detection
    private Rectangle upPathRect;
    private Rectangle sidePathRect;

    // Path states
    private boolean upPathHovered = false;
    private boolean sidePathHovered = false;
    private boolean upPathSelected = false;
    private boolean sidePathSelected = false;

    // Colors
    private final Color DARKENED = new Color(0.5f, 0.5f, 0.5f, 1f);
    private final Color HIGHLIGHTED = new Color(1f, 1f, 1f, 1f);

    public MapNavigationScreen(final FernansGrace game) {
        this.game = game;

        // Set up camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        batch = new SpriteBatch();

        // Load textures (replace with your actual textures)
            background = new Texture(Gdx.files.internal("map_background.png"));
        pathUp = new Texture(Gdx.files.internal("path_up.png"));
        pathSide = new Texture(Gdx.files.internal("path_side.png"));
        yellowX = new Texture(Gdx.files.internal("yellow_x.png"));

        // Define path areas (adjust coordinates and sizes as needed)
        upPathRect = new Rectangle(350, 240, 100, 200);
        sidePathRect = new Rectangle(450, 200, 200, 100);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Handle input
        handleInput();

        // Draw everything
        batch.begin();

        // Draw background
        batch.draw(background, 0, 0, 800, 480);

        // Draw yellow X (player position)
        batch.draw(yellowX, 350, 200, 100, 100);

        // Draw paths with appropriate color based on state
        // Up path
        if (upPathSelected) {
            batch.setColor(HIGHLIGHTED);
            batch.draw(pathUp, upPathRect.x, upPathRect.y, upPathRect.width, upPathRect.height);
        } else if (upPathHovered) {
            batch.setColor(HIGHLIGHTED);
            batch.draw(pathUp, upPathRect.x, upPathRect.y, upPathRect.width, upPathRect.height);
        } else {
            batch.setColor(DARKENED);
            batch.draw(pathUp, upPathRect.x, upPathRect.y, upPathRect.width, upPathRect.height);
        }

        // Side path
        if (sidePathSelected) {
            batch.setColor(HIGHLIGHTED);
            batch.draw(pathSide, sidePathRect.x, sidePathRect.y, sidePathRect.width, sidePathRect.height);
        } else if (sidePathHovered) {
            batch.setColor(HIGHLIGHTED);
            batch.draw(pathSide, sidePathRect.x, sidePathRect.y, sidePathRect.width, sidePathRect.height);
        } else {
            batch.setColor(DARKENED);
            batch.draw(pathSide, sidePathRect.x, sidePathRect.y, sidePathRect.width, sidePathRect.height);
        }

        // Reset color
        batch.setColor(Color.WHITE);

        batch.end();
    }

    private void handleInput() {
        // Get touch/mouse position
        Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);

        // Check hover states
        upPathHovered = upPathRect.contains(touchPos.x, touchPos.y) && !sidePathSelected;
        sidePathHovered = sidePathRect.contains(touchPos.x, touchPos.y) && !upPathSelected;

        // Handle clicks
        if (Gdx.input.justTouched()) {
            if (upPathRect.contains(touchPos.x, touchPos.y)) {
                if (upPathSelected) {
                    // Second click - navigate to next screen
                    //game.setScreen(new NextScreen(game));
                    System.out.println("upPath clicked");
                } else {
                    // First click - select this path
                    upPathSelected = true;
                    sidePathSelected = false;
                }
            } else if (sidePathRect.contains(touchPos.x, touchPos.y)) {
                if (sidePathSelected) {
                    // Second click - navigate to next screen
                    //game.setScreen(new NextScreen(game));
                    System.out.println("sidePath clicked");
                } else {
                    // First click - select this path
                    sidePathSelected = true;
                    upPathSelected = false;
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        pathUp.dispose();
        pathSide.dispose();
        yellowX.dispose();
    }
}
