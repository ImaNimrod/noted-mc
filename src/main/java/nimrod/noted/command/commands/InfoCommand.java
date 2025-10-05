package nimrod.noted.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import nimrod.noted.Noted;
import nimrod.noted.command.Command;

public class InfoCommand extends Command {
    public InfoCommand() {
        super("info", "Prints info about Noted", "");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Noted.chatMessage(String.format("%s v%s created by %s", Noted.NAME, Noted.VERSION, Noted.AUTHOR));
            return SINGLE_SUCCESS;
        });
    }
}
