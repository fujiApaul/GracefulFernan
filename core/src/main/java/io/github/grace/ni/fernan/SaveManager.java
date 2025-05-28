package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
// com.badlogic.gdx.utils.Array; // This import was not used, can be removed
import com.badlogic.gdx.utils.Json; // Assuming SaveProfile.fromJson and toJson use this
import java.util.ArrayList;
import java.util.List;

public class SaveManager {
    private static final String SAVE_DIR = "saves"; // Your save directory
    private static final String EXT = ".json";    // Your save file extension
    private static Json json = new Json(); // Added for toJson and fromJson if they are static in SaveProfile

    /**
     * Lists all available save profiles.
     * @return A list of SaveProfile objects.
     */
    public static List<SaveProfile> listProfiles() {
        FileHandle dir = Gdx.files.local(SAVE_DIR);
        if (!dir.exists()) { // Ensure directory is created if it doesn't exist
            dir.mkdirs();
        }
        FileHandle[] files = dir.list();
        List<SaveProfile> profiles = new ArrayList<>();
        for (FileHandle f : files) {
            // Check if it's a file and has the correct extension
            if (!f.isDirectory() && f.extension().equalsIgnoreCase(EXT.substring(1))) { // Use substring(1) to remove dot from EXT for comparison
                try {
                    // Assuming SaveProfile has a static fromJson method
                    // If not, you'd instantiate Json here and use json.fromJson()
                    SaveProfile profile = json.fromJson(SaveProfile.class, f.readString());
                    if (profile != null) {
                        profiles.add(profile);
                    }
                } catch (Exception e) {
                    Gdx.app.error("SaveManager", "Failed to parse profile: " + f.name(), e);
                }
            }
        }
        return profiles;
    }

    /**
     * Saves the given profile to a file.
     * @param profile The SaveProfile object to save.
     */
    public static void saveProfile(SaveProfile profile) {
        if (profile == null || profile.saveName == null || profile.saveName.trim().isEmpty()) {
            Gdx.app.error("SaveManager", "Cannot save null profile or profile with empty name.");
            return;
        }
        FileHandle dir = Gdx.files.local(SAVE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileHandle file = dir.child(profile.saveName + EXT);
        try {
            // Assuming SaveProfile has a toJson method
            // If not, you'd use json.toJson(profile)
            file.writeString(json.toJson(profile), false); // false to overwrite
            Gdx.app.log("SaveManager", "Profile saved: " + file.path());
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Failed to save profile: " + profile.saveName, e);
        }
    }

    /**
     * Loads a save profile by its name.
     * @param saveName The name of the profile to load.
     * @return The loaded SaveProfile object, or null if not found or error.
     */
    public static SaveProfile loadProfile(String saveName) {
        if (saveName == null || saveName.trim().isEmpty()) {
            Gdx.app.error("SaveManager", "Cannot load profile with empty name.");
            return null;
        }
        FileHandle dir = Gdx.files.local(SAVE_DIR); // Get directory first
        FileHandle file = dir.child(saveName + EXT); // Then get child file

        if (file.exists() && !file.isDirectory()) {
            try {
                // Assuming SaveProfile has a static fromJson method
                SaveProfile profile = json.fromJson(SaveProfile.class, file.readString());
                Gdx.app.log("SaveManager", "Profile loaded: " + saveName);
                return profile;
            } catch (Exception e) {
                Gdx.app.error("SaveManager", "Failed to load or parse profile: " + saveName, e);
                return null;
            }
        }
        Gdx.app.log("SaveManager", "Profile not found: " + saveName);
        return null;
    }

    /**
     * Checks if a save profile with the given name already exists.
     *
     * @param saveName The name of the save profile to check.
     * @return true if the profile file exists, false otherwise.
     */
    public static boolean profileExists(String saveName) {
        if (saveName == null || saveName.trim().isEmpty()) {
            return false;
        }
        FileHandle dir = Gdx.files.local(SAVE_DIR); // Get directory first
        FileHandle file = dir.child(saveName + EXT); // Then get child file
        return file.exists() && !file.isDirectory(); // Ensure it's a file and not a directory with the same name
    }
}
