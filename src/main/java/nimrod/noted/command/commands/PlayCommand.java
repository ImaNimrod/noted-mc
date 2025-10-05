package nimrod.noted.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import nimrod.noted.Noted;
import nimrod.noted.command.Command;
import nimrod.noted.command.arguments.MidiFileArgumentType;
import nimrod.noted.song.MidiConverter;
import nimrod.noted.song.Song;

import java.nio.file.Files;
import java.nio.file.Path;

public class PlayCommand extends Command {
    public PlayCommand() {
        super("play", "Plays a song", "<song>");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("path", MidiFileArgumentType.create()).executes(context -> {
            Path midiFile = context.getArgument("path", Path.class);

            try {
                Song song = MidiConverter.getSongFromBytes(Files.readAllBytes(midiFile), midiFile.getFileName().toString());
                Noted.INSTANCE.songPlayer.setSong(song);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return SINGLE_SUCCESS;
        }));
    }
}
