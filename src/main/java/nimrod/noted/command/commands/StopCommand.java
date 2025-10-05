package nimrod.noted.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import nimrod.noted.Noted;
import nimrod.noted.command.Command;

public class StopCommand extends Command {
    public StopCommand() {
        super("stop", "Stops playing music", "");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Noted.chatMessage("Stopped playing");
            Noted.INSTANCE.songPlayer.togglePaused();
            return SINGLE_SUCCESS;
        });
    }
}
