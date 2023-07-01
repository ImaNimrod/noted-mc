package net.nimrod.noted.command.commands;

import net.nimrod.noted.Noted;
import net.nimrod.noted.command.Command;

public class Loop extends Command {

    public Loop() {
        super("loop", "Toggles looping on or off; when looping, the current song will play indefinitely", "");
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length == 0) {
            Noted.INSTANCE.songPlayer.toggleLooping();
            return true;
        }

        return false;
    }

}
