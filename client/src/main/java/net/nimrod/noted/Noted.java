package net.nimrod.noted;

import java.io.File;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.nimrod.noted.converters.MidiConverter;
import net.nimrod.noted.song.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Noted implements ModInitializer {

    public static final String NAME = "noted";
    public static final String VERSION = "1.0.0";
    public static final String AUTHOR = "nimrod";

	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    public static final Noted INSTANCE = new Noted();

    public static final File ROOT_DIR = new File("noted");
    public static final File SONG_DIR = new File(ROOT_DIR, "songs");

    public static final MinecraftClient MC = MinecraftClient.getInstance();

    public static SongPlayer songPlayer;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing " + NAME + " v" + VERSION);

        if (!ROOT_DIR.exists())
            ROOT_DIR.mkdir();

        if (!SONG_DIR.exists())
            SONG_DIR.mkdir();
	}

    public void onRender(MatrixStack matrixStack, float tickDelta) {
        if (songPlayer != null)
            songPlayer.onRender(matrixStack, tickDelta);
    }

    public void onTick() {
        if (songPlayer != null)
            songPlayer.onTick();
    }

    public void loadSong(String name) {
        try {
            LOGGER.info("loading song: " + name);
            songPlayer = new SongPlayer(name);
        } catch (Exception e) {
            LOGGER.info("exception thrown: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
