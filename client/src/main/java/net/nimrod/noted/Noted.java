package net.nimrod.noted;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.nimrod.noted.command.CommandManager;
import net.nimrod.noted.playing.SongPlayer;
import net.nimrod.noted.util.LogUtils;
import net.nimrod.noted.util.RenderUtils;
import org.lwjgl.glfw.GLFW;

public class Noted implements ModInitializer {

    public static final String NAME = "noted";
    public static final String VERSION = "1.2.0";
    public static final String AUTHOR = "nimrod";

    public static final Noted INSTANCE = new Noted();

    public final CommandManager commandManager = new CommandManager();
    public final SongPlayer songPlayer = new SongPlayer();

    private final MinecraftClient mc = MinecraftClient.getInstance();

	@Override
	public void onInitialize() {
		LogUtils.consoleLog("Initializing " + NAME + " v" + VERSION + " created by " + AUTHOR);
	}

    public void onHudRender(DrawContext context, float tickDelta) {
        RenderUtils.drawString(context, NAME + " v" + VERSION, 4, 4, 0xffffffff);
        songPlayer.onHudRender(context, tickDelta);
    }

    public void onWorldRender(MatrixStack matrixStack) {
        songPlayer.onWorldRender(matrixStack);
    }

    public void onTick() {
        songPlayer.onTick(); 
    }

    public void onKey(int key, int action) {
        if (mc.currentScreen == null && mc.getOverlay() == null) {
            if (commandManager.getPrefix().equals(GLFW.glfwGetKeyName(key, 0)) && action == GLFW.GLFW_RELEASE) {
                mc.setScreen(new ChatScreen(commandManager.getPrefix()));
                return;
            } else if (key == GLFW.GLFW_KEY_I && action == GLFW.GLFW_PRESS) {
                songPlayer.toggleActive();
            } else if (key == GLFW.GLFW_KEY_O && action == GLFW.GLFW_PRESS) {
                songPlayer.togglePaused();
            }
        }
    }

}
