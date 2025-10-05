package nimrod.noted.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import nimrod.noted.Noted;
import nimrod.noted.command.Command;

public class StatusCommand extends Command {
    public StatusCommand() {
        super("status", "Prints status information about the currently playing song", "");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Noted.chatMessage(Noted.INSTANCE.songPlayer.getStatus());
            return SINGLE_SUCCESS;
        });
    }
}
