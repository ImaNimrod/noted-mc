package net.nimrod.noted;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.sound.midi.InvalidMidiDataException;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.Instrument;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.nimrod.noted.converters.MidiConverter;
import net.nimrod.noted.song.*;
import net.nimrod.noted.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Noted implements ModInitializer {

    public static final String NAME = "noted";
    public static final String VERSION = "1.0.0";
    public static final String AUTHOR = "nimrod";

    public static final Noted INSTANCE = new Noted();

    public static final File ROOT_DIR = new File("noted");
    public static final File SONG_DIR = new File(ROOT_DIR, "songs");

    public boolean active = false;
    public boolean tuned = false;
    public boolean playing = false;

    public Song currentSong;

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private int tuneDelay = 0;
    private int pollDelay = 0;

    private HashMap<BlockPos, Integer> blockPitches = new HashMap<BlockPos, Integer>();
    private ArrayList<BlockPos> noteBlocks = new ArrayList<BlockPos>();

	@Override
	public void onInitialize() {
		LogUtils.consoleLog("Initializing " + NAME + " v" + VERSION);

        if (!ROOT_DIR.exists())
            ROOT_DIR.mkdir();

        if (!SONG_DIR.exists())
            SONG_DIR.mkdir();

	}

    public void onTick() {
        if (!active)
            return;

        if (currentSong == null) {
            if (pollDelay > 0) {
                pollDelay--;
                return;
            }

            String nextSongId = ApiUtils.getNextSong();
            if (nextSongId == null) {
                pollDelay = 20;
                return;
            }

            try {
                File songFile = ApiUtils.getSong(nextSongId);
                loadSong(songFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!tuned) {
            tuneNoteblocks();
            return;
        };

        currentSong.play(); 
        currentSong.advanceCurrentTime();

        while (currentSong.reachedNextNote()) {
            Note note = currentSong.getNextNote();

            for (Entry<BlockPos, Integer> e : blockPitches.entrySet()) {
                if (isNoteBlock(e.getKey()) && note.getNoteId() % 25 == getNote(e.getKey()))
                    playNoteblock(e.getKey());
            }
        }

        if (currentSong.finished()) {
            currentSong = null;
            playing = false;
            tuned = false;
        }
    }

    private void tuneNoteblocks() {
        for (Entry<BlockPos, Integer> e : blockPitches.entrySet()) {
            int note = getNote(e.getKey());
            if (note == -1)
                continue;

            if (note != e.getValue()) {
                if (tuneDelay < 5) {
                    tuneDelay++;
                    return;
                }

                int neededNote = e.getValue() < note ? e.getValue() + 25 : e.getValue();
                int reqTunes = Math.min(25, neededNote - note);

                for (int i = 0; i < reqTunes; i++) {
                    mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(e.getKey()), Direction.DOWN, e.getKey(), false), 0));
                }

                tuneDelay = 0;
            }
        }

        tuned = true;
    }

    private void loadSong(File songFile) throws IOException, InvalidMidiDataException {
        currentSong = MidiConverter.getSongFromFile(songFile);

        BlockPos playerEyePos = new BlockPos((int) mc.player.getEyePos().x, 
                                             (int) mc.player.getEyePos().y, 
                                             (int) mc.player.getEyePos().z);

        BlockPos.streamOutwards(playerEyePos, 4, 4, 4)
                .filter(Noted::isNoteBlock)
                .map(BlockPos::toImmutable)
                .forEach(noteBlocks::add);

        ArrayList<Note> songReq = new ArrayList<Note>();
        currentSong.getNotes().stream().distinct().forEach(songReq::add);
        
        blockPitches.clear();

        for (Note note : songReq) {
            for (BlockPos blockPos: noteBlocks) {
                if (blockPitches.containsKey(blockPos))
                    continue;

                int pitch = note.getNoteId() % 25;
                int instrument = note.getNoteId() / 25;
                int blockInstrument = getInstrument(blockPos).ordinal();

                if (instrument == blockInstrument && blockPitches.entrySet()
                    .stream()
                    .filter(e -> e.getValue() == pitch)
                    .noneMatch(e -> getInstrument(e.getKey())
                    .ordinal() == blockInstrument)) {
                    blockPitches.put(blockPos, pitch);
                    break;
                }
            }
        }
    }

    private void playNoteblock(BlockPos blockPos) {
        if (!isNoteBlock(blockPos))
            return;

        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.DOWN, 0));
    }

    private Instrument getInstrument(BlockPos blockPos) {
        if (!isNoteBlock(blockPos))
            return Instrument.HARP;

        return mc.world.getBlockState(blockPos).get(NoteBlock.INSTRUMENT);
    }

    private int getNote(BlockPos blockPos) {
        if (!isNoteBlock(blockPos))
            return -1;

        return mc.world.getBlockState(blockPos).get(NoteBlock.NOTE);
    }

    private static boolean isNoteBlock(BlockPos blockPos) {
        return mc.world.getBlockState(blockPos).getBlock() instanceof NoteBlock &&
               mc.world.getBlockState(blockPos.up()).isAir();
    }

}
