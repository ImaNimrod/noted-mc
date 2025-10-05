package nimrod.noted.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import nimrod.noted.command.commands.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static nimrod.noted.Noted.MC;

public class CommandManager {
    public static final List<Command> COMMANDS = new ArrayList<Command>();
    public static final CommandDispatcher<CommandSource> DISPATCHER = new CommandDispatcher<CommandSource>();

    static {
        add(new HelpCommand());
        add(new InfoCommand());
        add(new ListCommand());
        add(new PauseCommand());
        add(new PlayCommand());
        add(new StatusCommand());
        add(new StopCommand());
        COMMANDS.sort(Comparator.comparing(Command::getName));
    }

    public static void dispatch(String message) throws CommandSyntaxException {
        DISPATCHER.execute(message, MC.getNetworkHandler().getCommandSource());
    }

    public static Command get(String name) {
        for (Command command : COMMANDS) {
            if (command.getName().equals(name)) {
                return command;
            }
        }

        return null;
    }

    private static void add(Command command) {
        command.register(DISPATCHER);
        COMMANDS.add(command);
    }
}
