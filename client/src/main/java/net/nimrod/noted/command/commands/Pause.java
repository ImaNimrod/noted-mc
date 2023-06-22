package net.nimrod.noted.command.commands;

import net.nimrod.noted.Noted;
import net.nimrod.noted.command.Command;
import net.nimrod.noted.util.LogUtils;

public class Pause extends Command {

    public Pause() {
        super("pause", "Pauses the currently playing song", "");
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length == 0) {
            if (Noted.INSTANCE.songPlayer.currentSong == null) {
                LogUtils.chatLog("No song playing");
                return true;
            }

            Noted.INSTANCE.songPlayer.paused = !Noted.INSTANCE.songPlayer.paused;
            LogUtils.chatLog("Song " + (Noted.INSTANCE.songPlayer.paused ? "paused" : "unpaused"));
            return true;
        }

        return false;
    }

}
