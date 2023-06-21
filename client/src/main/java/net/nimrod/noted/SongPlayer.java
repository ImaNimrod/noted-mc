package net.nimrod.noted;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.sound.midi.InvalidMidiDataException;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.Instrument;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.nimrod.noted.Noted;
import net.nimrod.noted.converters.MidiConverter;
import net.nimrod.noted.song.*;
import net.nimrod.noted.utils.BlockUtils;
import net.nimrod.noted.utils.RenderUtils;

public class SongPlayer {

    private Song song;

    private HashMap<BlockPos, Integer> blockPitches = new HashMap<BlockPos, Integer>();
    private ArrayList<BlockPos> noteBlocks = new ArrayList<BlockPos>();

    private int tuneDelay = 0;
    private boolean playing = false;

    public SongPlayer(String name) throws IOException, InvalidMidiDataException {
        File file = new File(Noted.INSTANCE.SONG_DIR, name);
        if (file != null) {
            if (file.getName().endsWith(".mid") || file.getName().endsWith(".midi")) {
                this.song = MidiConverter.getSongFromFile(file);
                loadSong();
            } else {
                throw new IOException("file incorrect format: " + file.getName());
            }
        } else {
            throw new IOException("file not found: " + name);
        }
    }

    public Song getSong() {
        return song;
    }

    public void stop() {
        song = null;
        playing = false;
    }

    public void onRender(MatrixStack matrixStack, float tickDelta) {
        if (noteBlocks.size() > 0) {
            Noted.INSTANCE.LOGGER.info(String.valueOf(noteBlocks.size()));
            for (BlockPos blockPos : noteBlocks) {
                Box box = new Box(blockPos);

                RenderUtils.draw3DBox(matrixStack, box);
            }
        }
    }

    public void onTick() {
        if (!playing)
            return;

        // noteblock tuning 
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
                    Noted.MC.interactionManager.interactBlock(Noted.MC.player, 
                             Hand.MAIN_HAND, 
                             new BlockHitResult(Vec3d.ofCenter(e.getKey(), 1), Direction.UP, e.getKey(), true));
                }

                tuneDelay = 0;

                return;
            }
        }

        song.play(); 
        song.advanceCurrentTime();

        while (song.reachedNextNote()) {
            Note note = song.getNextNote();
            Noted.INSTANCE.LOGGER.info(note.getNoteId() + " " + note.getTime());


            for (Entry<BlockPos, Integer> e : blockPitches.entrySet()) {
                if (isNoteBlock(e.getKey()) && note.getNoteId() % 25 == getNote(e.getKey()))
                    playNoteblock(e.getKey());
            }
        }

        if (song.finished()) {
            song = null;
            playing = false; 
        }
    }

    private void loadSong() {
        BlockPos playerEyePos = new BlockPos((int) Noted.MC.player.getEyePos().x, 
                                             (int) Noted.MC.player.getEyePos().y, 
                                             (int) Noted.MC.player.getEyePos().z);

        BlockPos.streamOutwards(playerEyePos, 4, 4, 4)
                .filter(SongPlayer::isNoteBlock)
                .map(BlockPos::toImmutable)
                .forEach(noteBlocks::add);

        ArrayList<Note> songReq = new ArrayList<Note>();
        song.getNotes().stream().distinct().forEach(songReq::add);
        
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

        playing = true;
    }

    private void playNoteblock(BlockPos blockPos) {
        if (!isNoteBlock(blockPos))
            return;

        Noted.MC.interactionManager.attackBlock(blockPos, Direction.UP);
        Noted.MC.player.swingHand(Hand.MAIN_HAND);
    }

    private Instrument getInstrument(BlockPos blockPos) {
        if (!isNoteBlock(blockPos))
            return Instrument.HARP;

        return Noted.MC.world.getBlockState(blockPos).get(NoteBlock.INSTRUMENT);
    }

    private int getNote(BlockPos blockPos) {
        if (!isNoteBlock(blockPos))
            return -1;

        return Noted.MC.world.getBlockState(blockPos).get(NoteBlock.NOTE);
    }

    private static boolean isNoteBlock(BlockPos blockPos) {
        return Noted.MC.world.getBlockState(blockPos).getBlock() instanceof NoteBlock &&
               Noted.MC.world.getBlockState(blockPos.up()).isAir();
    }

}
