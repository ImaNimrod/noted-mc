package net.nimrod.noted.command.commands;

import net.nimrod.noted.Noted;
import net.nimrod.noted.command.Command;
import net.nimrod.noted.util.LogUtils;

public class Skip extends Command {

    public Skip() {
        super("skip", "Skips to the next song", "");
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length == 0) {
            LogUtils.chatLog("Skipping song...");
            Noted.INSTANCE.songPlayer.reset();
            return true;
        }

        return false;
    }

}
