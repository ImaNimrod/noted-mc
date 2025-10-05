package nimrod.noted.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nimrod.noted.Noted;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
    private static transient final Gson GSON = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    public String commandPrefix = "@";
    public boolean swingHand = false;

    public static Config load() {
        Config config = null;

        try (FileReader reader = new FileReader(Noted.FOLDER.resolve("config.json").toFile())) {
            config = GSON.fromJson(reader, Config.class);
            if (config == null) {
                config = new Config();
            }
        } catch (Exception e) {
            e.printStackTrace();
            config = new Config();
        }

        return config;
    }

    public void save() throws IOException {
        try (FileWriter writer = new FileWriter(Noted.FOLDER.resolve("config.json").toFile())) {
            GSON.toJson(this, writer);
        }
    }
}
