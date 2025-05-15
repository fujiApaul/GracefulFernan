package io.github.grace.ni.fernan;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.ArrayList;
import java.util.List;

/**
 * Main game class for FernansGrace.
 * Handles initial screen setup, music playback, and resource management.
 */
public class FernansGrace extends Game {

    public SpriteBatch batch;
    public FitViewport viewport;
    public boolean isInGame = false;

    private Music backgroundMusic;
    public boolean isMusicEnabled = true;

    @Override
    public void create() {
        viewport = new FitViewport(16 * 3, 9 * 3);
        batch = new SpriteBatch();

        try {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("BGM1.mp3"));
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.2f);
            backgroundMusic.play();
        } catch (Exception e) {
            Gdx.app.error("FernansGrace", "Failed to load or play background music.", e);
        }

        this.setScreen(new MainFernan(this));
    }

    public void toggleMusic() {
        if (backgroundMusic == null) return;

        isMusicEnabled = !isMusicEnabled;
        if (isMusicEnabled) {
            backgroundMusic.play();
        } else {
            backgroundMusic.pause();
        }
    }

    public boolean isMusicPlaying() {
        return isMusicEnabled;
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (backgroundMusic != null) backgroundMusic.dispose();
        super.dispose();
    }
}

