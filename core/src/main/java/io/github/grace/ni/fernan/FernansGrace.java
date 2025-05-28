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
    private boolean isMusicEnabled = true;
    private SaveProfile currentSaveProfile; // Added to hold the active profile

    @Override
    public void create() {
        viewport = new FitViewport(16 * 3, 9 * 3);
        batch = new SpriteBatch();

        try {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("BGM1.mp3"));
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.2f);
            if (isMusicEnabled) {
                backgroundMusic.play();
            }
        } catch (Exception e) {
            Gdx.app.error("FernansGrace", "Failed to load or play background music.", e);
        }

        this.setScreen(new MainFernan(this)); // MainFernan is the initial screen
    }

    // --- Music Control Methods ---
    public boolean isMusicPlaying() {
        return backgroundMusic != null && backgroundMusic.isPlaying();
    }

    public boolean isMusicEnabled() {
        return isMusicEnabled;
    }

    public void toggleMusic() {
        isMusicEnabled = !isMusicEnabled;
        if (backgroundMusic != null) {
            if (isMusicEnabled) {
                if (!backgroundMusic.isPlaying()) {
                    backgroundMusic.play();
                }
            } else {
                if (backgroundMusic.isPlaying()) {
                    backgroundMusic.pause();
                }
            }
        }
        // Consider saving this preference (e.g., using Gdx.app.getPreferences())
        Gdx.app.log("FernansGrace", "Music enabled: " + isMusicEnabled);
    }

    public void ensureMusicState() {
        if (backgroundMusic != null) {
            if (isMusicEnabled && !backgroundMusic.isPlaying()) {
                backgroundMusic.play();
            } else if (!isMusicEnabled && backgroundMusic.isPlaying()) {
                backgroundMusic.pause();
            }
        }
    }
    // --- End Music Control Methods ---

    // --- SaveProfile Management ---
    public SaveProfile getCurrentSaveProfile() {
        return currentSaveProfile;
    }

    public void setCurrentSaveProfile(SaveProfile profile) {
        this.currentSaveProfile = profile;
        this.isInGame = (profile != null); // Update isInGame status based on profile presence
        Gdx.app.log("FernansGrace", "Current save profile set to: " + (profile != null ? profile.saveName : "null"));
    }
    // --- End SaveProfile Management ---


    @Override
    public void render() {
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) { // Add null check for viewport
            viewport.update(width, height, true);
        }
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (backgroundMusic != null) backgroundMusic.dispose();
        super.dispose();
    }
}
