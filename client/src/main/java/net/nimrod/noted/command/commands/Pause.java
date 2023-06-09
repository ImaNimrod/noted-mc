package net.nimrod.noted.command.commands;

import net.nimrod.noted.Noted;
import net.nimrod.noted.command.Command;

public class Pause extends Command {

    public Pause() {
        super("pause", "Pauses the currently playing song", "");
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length == 0) {
            Noted.INSTANCE.songPlayer.togglePaused();
            return true;
        }

        return false;
    }

}
