package nimrod.noted;

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
import nimrod.noted.song.SongPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public class Noted implements ClientModInitializer {
    public static final String MOD_ID = "noted";
    public static final String NAME;
    public static final String VERSION;
    public static final String AUTHOR;

    public static final String COMMAND_PREFIX = "@";
    public static final Path FOLDER = FabricLoader.getInstance().getGameDir().resolve(MOD_ID);
    public static final Noted INSTANCE = new Noted();
    public static final Logger LOGGER;
    public static final MinecraftClient MC = MinecraftClient.getInstance();

    public final CommandManager commandManager = new CommandManager();
    public final SongPlayer songPlayer = new SongPlayer();

    private static final String CHAT_PREFIX = "\u00a76[noted]\u00a7r ";

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
