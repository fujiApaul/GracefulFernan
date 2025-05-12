package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.List;

public class SaveManager {
    private static final String SAVE_DIR = "saves";
    private static final String EXT = ".json";

    public static List<SaveProfile> listProfiles() {
        FileHandle dir = Gdx.files.local(SAVE_DIR);
        dir.mkdirs();
        FileHandle[] files = dir.list();
        List<SaveProfile> profiles = new ArrayList<>();
        for (FileHandle f : files) {
            if (f.extension().equals("json")) {
                profiles.add(SaveProfile.fromJson(f.readString()));
            }
        }
        return profiles;
    }

    public static void saveProfile(SaveProfile profile) {
        FileHandle dir = Gdx.files.local(SAVE_DIR);
        dir.mkdirs();
        FileHandle file = dir.child(profile.saveName + EXT);
        file.writeString(profile.toJson(), false);
    }

    public static SaveProfile loadProfile(String saveName) {
        FileHandle file = Gdx.files.local(SAVE_DIR + "/" + saveName + EXT);
        return file.exists() ? SaveProfile.fromJson(file.readString()) : null;
    }
}
