package net.nimrod.noted.command.commands;

import net.nimrod.noted.Noted;
import net.nimrod.noted.command.Command;
import net.nimrod.noted.util.LogUtils;

public class Help extends Command {

    public Help() {
        super("help", "Displays information on a command", "<command>");
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length == 0) {
            StringBuilder stringBuilder = new StringBuilder();

            for (Command command : Noted.INSTANCE.commandManager.getCommands())
                stringBuilder.append(command.getName() + ", ");

            LogUtils.chatLog("Commands:");
            LogUtils.chatLog(stringBuilder.substring(0, stringBuilder.length() - 2));
            return true;
        } else if (args.length == 1) {
            for (Command command : Noted.INSTANCE.commandManager.getCommands()) {
                if (args[0].equalsIgnoreCase(command.getName())) {
                    LogUtils.chatLog(command.getName() + " - " + command.getDescription());
                    LogUtils.chatLog("Syntax: " + Noted.INSTANCE.commandManager.getPrefix() + command.getName() + " " + command.getSyntax());
                    return true;
                }
            }

            LogUtils.chatLog("Command not found: " + args[0]);
            return true;
        }

        return false;
    }

}
