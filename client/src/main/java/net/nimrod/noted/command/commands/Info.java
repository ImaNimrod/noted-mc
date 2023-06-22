package net.nimrod.noted.command.commands;

import net.nimrod.noted.Noted;
import net.nimrod.noted.command.Command;
import net.nimrod.noted.util.LogUtils;

public class Info extends Command {

    public Info() {
        super("info", "Prints info about noted-client", "");
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length == 0) {
            LogUtils.chatLog(Noted.NAME + " v" + Noted.VERSION + " created by " + Noted.AUTHOR);
            return true;
        }

        return false;
    }

}
