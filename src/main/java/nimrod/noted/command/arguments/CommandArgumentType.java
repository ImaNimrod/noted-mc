package nimrod.noted.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import nimrod.noted.command.Command;
import nimrod.noted.command.CommandManager;

import java.util.concurrent.CompletableFuture;

public class CommandArgumentType implements ArgumentType<Command> {
    private static final CommandArgumentType INSTANCE = new CommandArgumentType();
    private static final DynamicCommandExceptionType NO_SUCH_COMMAND = new DynamicCommandExceptionType(name -> Text.literal("Command with name " + name + " doesn't exist."));

    public static CommandArgumentType create() {
        return INSTANCE;
    }

    private CommandArgumentType() {}

    @Override
    public Command parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();
        Command command = CommandManager.get(argument);
        if (command == null) {
            throw NO_SUCH_COMMAND.create(argument);
        }

        return command;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(CommandManager.COMMANDS.stream().map(command -> command.getName()), builder);
    }
}
