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
import nimrod.noted.Noted;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class MidiFileArgumentType implements ArgumentType<Path> {
    private static final MidiFileArgumentType INSTANCE = new MidiFileArgumentType();
    private static final DynamicCommandExceptionType NO_SUCH_FILE = new DynamicCommandExceptionType(name -> Text.literal("MIDI file with name " + name + " doesn't exist."));

    public static MidiFileArgumentType create() {
        return INSTANCE;
    }

    private MidiFileArgumentType() {}

    @Override
    public Path parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());

        Path path = Noted.FOLDER.resolve(argument);
        if (!Files.exists(path)) {
            throw NO_SUCH_FILE.create(argument);
        }

        return path;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        try {
            return CommandSource.suggestMatching(Files.list(Noted.FOLDER).filter(f -> Files.isRegularFile(f) && f.toString().toLowerCase().endsWith(".mid")).map(f -> f.getFileName().toString()), builder);
        } catch (Exception e) {
            e.printStackTrace();
            return Suggestions.empty();
        }
    }
}
