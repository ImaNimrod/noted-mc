package net.nimrod.noted;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.nimrod.noted.playing.SongPlayer;
import net.nimrod.noted.utils.RenderUtils;
import net.nimrod.noted.utils.LogUtils;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

public class Noted implements ModInitializer {

    public static final String NAME = "noted";
    public static final String VERSION = "1.0.0";
    public static final String AUTHOR = "nimrod";

    public static final Noted INSTANCE = new Noted();

    public SongPlayer songPlayer = new SongPlayer();

	@Override
	public void onInitialize() {
		LogUtils.consoleLog("Initializing " + NAME + " v" + VERSION + " created by " + AUTHOR);
	}

    public void onHudRender(MatrixStack matrixStack, float tickDelta) {
        RenderUtils.drawString(matrixStack, NAME + " v" + VERSION, 4, 4, Color.WHITE);

        if (songPlayer.getActive())
            RenderUtils.drawString(matrixStack, "Now Playing: " + songPlayer.getSongName(), 4, 16, Color.WHITE);
    }

    public void onWorldRender(MatrixStack matrixStack) {
        songPlayer.onWorldRender(matrixStack);
    }

    public void onKey(int key, int action) {
        if (key == GLFW.GLFW_KEY_Z && action == GLFW.GLFW_PRESS)
            songPlayer.setActive(!songPlayer.getActive());
    }

    public void onTick() {
        songPlayer.onTick(); 
    }

}
