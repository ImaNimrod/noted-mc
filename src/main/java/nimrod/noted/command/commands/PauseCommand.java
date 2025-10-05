package nimrod.noted.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import nimrod.noted.Noted;
import nimrod.noted.command.Command;

public class PauseCommand extends Command {
    public PauseCommand() {
        super("pause", "Pauses the currently playing song", "");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Noted.SONG_PLAYER.togglePaused();
            return SINGLE_SUCCESS;
        });
    }
}
