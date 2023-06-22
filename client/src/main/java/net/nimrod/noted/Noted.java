package net.nimrod.noted;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.nimrod.noted.command.CommandManager;
import net.nimrod.noted.playing.SongPlayer;
import net.nimrod.noted.util.LogUtils;
import net.nimrod.noted.util.RenderUtils;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

public class Noted implements ModInitializer {

    public static final String NAME = "noted";
    public static final String VERSION = "1.1.0";
    public static final String AUTHOR = "nimrod";

    public static final Noted INSTANCE = new Noted();

    public final CommandManager commandManager = new CommandManager();
    public final SongPlayer songPlayer = new SongPlayer();

	@Override
	public void onInitialize() {
		LogUtils.consoleLog("Initializing " + NAME + " v" + VERSION + " created by " + AUTHOR);
	}

    public void onHudRender(MatrixStack matrixStack, float tickDelta) {
        RenderUtils.drawString(matrixStack, NAME + " v" + VERSION + " | " + (songPlayer.active ? "active" : "inactive"),
                               4, 4, Color.WHITE);

        if (songPlayer.active && songPlayer.currentSong != null)
            RenderUtils.drawString(matrixStack, "Now Playing: " + songPlayer.currentSong.getName(), 4, 16, Color.WHITE);
    }

    public void onWorldRender(MatrixStack matrixStack) {
        songPlayer.onWorldRender(matrixStack);
    }

    public void onKey(int key, int action) {
        if (key == GLFW.GLFW_KEY_Z && action == GLFW.GLFW_PRESS) {
            songPlayer.active = !songPlayer.active;

            if (!songPlayer.active)
                songPlayer.reset();
        }
    }

    public void onTick() {
        songPlayer.onTick(); 
    }

}
