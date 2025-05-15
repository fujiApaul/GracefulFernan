package io.github.grace.ni.fernan;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class FernansGrace extends Game {

    public SpriteBatch batch;
    public FitViewport viewport;
    public boolean isInGame = false;

    private Music backgroundMusic;
    public boolean isMusicOn = true; // Music toggle flag

    @Override
    public void create() {
        viewport = new FitViewport(16 * 3, 9 * 3);
        batch = new SpriteBatch();

        try {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("BGM1.mp3"));
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.2f);
            if (isMusicOn) backgroundMusic.play();
        } catch (Exception e) {
            Gdx.app.error("FernansGrace", "Failed to load or play background music.", e);
        }

        setScreen(new MainFernan(this));
    }

    public void toggleMusic() {
        if (backgroundMusic == null) return;

        if (isMusicOn) {
            backgroundMusic.pause();
            isMusicOn = false;
        } else {
            backgroundMusic.play();
            isMusicOn = true;
        }
    }

    public boolean isMusicPlaying() {
        return isMusicOn;
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (backgroundMusic != null) backgroundMusic.dispose();
        super.dispose();
    }
}
