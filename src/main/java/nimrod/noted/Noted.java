package nimrod.noted;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import nimrod.noted.command.CommandManager;
import nimrod.noted.config.Config;
import nimrod.noted.song.SongPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class Noted implements ClientModInitializer {
    public static final String MOD_ID = "noted";
    public static final String NAME;
    public static final String VERSION;
    public static final String AUTHOR;

    public static final Path FOLDER = FabricLoader.getInstance().getGameDir().resolve(MOD_ID);
    public static final Noted INSTANCE = new Noted();
    public static final Logger LOGGER;
    public static final MinecraftClient MC = MinecraftClient.getInstance();

    public static Config CONFIG;

    public final CommandManager commandManager = new CommandManager();
    public final SongPlayer songPlayer = new SongPlayer();

    private static final String CHAT_PREFIX = "\u00a76[noted]\u00a7r ";
    private static final Gson GSON = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    static {
        ModMetadata metadata = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata();
        NAME = metadata.getName();
        VERSION = metadata.getVersion().toString();
        AUTHOR = metadata.getAuthors().stream().findFirst().orElseThrow().getName();

        LOGGER = LoggerFactory.getLogger(NAME);
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing {} v{} created by {}", NAME, VERSION, AUTHOR);

        if (!Files.exists(FOLDER)) {
            FOLDER.getParent().toFile().mkdirs();
            FOLDER.toFile().mkdir();
        }

        try (FileReader reader = new FileReader(FOLDER.resolve("config.json").toFile())) {
            CONFIG = GSON.fromJson(reader, Config.class);
            if (CONFIG == null) {
                CONFIG = new Config();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load configuration file:");
            e.printStackTrace();

            CONFIG = new Config();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try (FileWriter writer = new FileWriter(FOLDER.resolve("config.json").toFile())) {
                GSON.toJson(CONFIG, writer);
            } catch (Exception e) {
                LOGGER.error("Failed to save configuration file:");
                e.printStackTrace();
            }
        }));
    }

    public void onRender2D(DrawContext context, float delta) {
        songPlayer.onRender2D(context, delta);
    }

    public void onRender3D(MatrixStack matrices) {
    }

    public void onTick() {
        if (MC.player == null || MC.world == null) {
            return;
        }

        songPlayer.onTick(); 
    }

    public static void chatMessage(String message) {
        MutableText prefix = Text.literal(CHAT_PREFIX);
        MC.inGameHud.getChatHud().addMessage(prefix.append(Text.literal(message)));
    }
}
