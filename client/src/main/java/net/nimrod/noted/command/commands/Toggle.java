package net.nimrod.noted.command.commands;

import net.nimrod.noted.Noted;
import net.nimrod.noted.command.Command;

public class Toggle extends Command {

    public Toggle() {
        super("toggle", "Toggles noted-client on or off", "");
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length == 0) {
            Noted.INSTANCE.songPlayer.toggleActive();
            return true;
        }

        return false;
    }

}
