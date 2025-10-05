package nimrod.noted.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import nimrod.noted.Noted;
import nimrod.noted.command.Command;

import java.nio.file.Files;

public class ListCommand extends Command {
    public ListCommand() {
        super("list", "Displays the available songs to play", "");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Noted.chatMessage("§nAvailable songs:§r");

            try {
                Files.walk(Noted.FOLDER)
                    .filter(f -> Files.isRegularFile(f) && f.toString().toLowerCase().endsWith(".mid"))
                    .map(f -> f.getFileName().toString())
                    .forEach(Noted::chatMessage);
            } catch (Exception e) {
                e.printStackTrace();
                Noted.chatMessage("Failed to list songs");
            }

            return SINGLE_SUCCESS;
        });
    }
}
