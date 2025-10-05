package nimrod.noted.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import nimrod.noted.Noted;
import nimrod.noted.command.Command;
import nimrod.noted.command.CommandManager;
import nimrod.noted.command.arguments.CommandArgumentType;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "Displays information on a command", "<command>");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            StringBuilder stringBuilder = new StringBuilder();

            for (Command command : CommandManager.COMMANDS) {
                stringBuilder.append(command.getName() + ", ");
            }

            Noted.chatMessage("Commands:");
            Noted.chatMessage(stringBuilder.substring(0, stringBuilder.length() - 2));
            return SINGLE_SUCCESS;
        });

        builder.then(argument("command", CommandArgumentType.create()).executes(context -> {
            Command command = context.getArgument("command", Command.class);
            Noted.chatMessage(command.getName() + " - " + command.getDescription());
            Noted.chatMessage("Syntax: " + command.getName() + " " + command.getSyntax());
            return SINGLE_SUCCESS;
        }));
    }
}
