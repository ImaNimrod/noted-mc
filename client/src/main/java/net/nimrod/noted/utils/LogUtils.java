package net.nimrod.noted.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.nimrod.noted.Noted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(Noted.NAME);
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void consoleLog(String message) {
        logger.info(message);
    }

    public static void chatLog(String message) {
		mc.inGameHud.getChatHud().addMessage(Text.of("§5[" + Noted.NAME + "]§f " + message));
    }

}
