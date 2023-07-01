package net.nimrod.noted.command;

import net.nimrod.noted.command.commands.*;
import net.nimrod.noted.util.LogUtils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final String prefix = ".";

    private List<Command> commands = new ArrayList<>();

    public CommandManager() {
        commands.add(new Help());
        commands.add(new Info());
        commands.add(new Loop());
        commands.add(new Pause());
        commands.add(new Skip());
        commands.add(new Toggle());
    }

    public String getPrefix() {
        return prefix;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void runCommand(String text) {
        String[] split = text.split(" ");
        if (split.length == 0)
            return;

        String commandName = split[0];
        String args[] = Arrays.copyOfRange(split, 1, split.length);

        for (Command command : commands) {
            if (commandName.equalsIgnoreCase(command.getName())) {
                if (!command.execute(args))
                    LogUtils.chatLog("Syntax: " + prefix + command.getName() + " " + command.getSyntax());

                return;
            }
        }

        LogUtils.chatLog("Unknown command: " + commandName);
    }

}
