package net.nimrod.noted.playing;

import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.Instrument;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.nimrod.noted.song.*;
import net.nimrod.noted.utils.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map.Entry;

public class SongPlayer {

    private boolean active = false;                     /* is song player playing */ 
    private Song currentSong = null;                    /* currently playing song structure */
    private String currentSongName = null;              /* currently playing song name */

    private State currentState = State.WAITING;         /* the current state of the bot */
    private SongLoaderThread songLoaderThread = null;   /* seperate execution thread for fetching songs from api */

    private final List<BlockPos> noteBlockStage = new ArrayList<>();        /* holds the block positions of playable noteblocks */
    private final HashMap<BlockPos, Integer> pitchMap = new HashMap<>();    /* a mapping of noteblocks to note pitches */

    private final int tuneNoteBlockDelay = 5;           /* the time between tuning individual noteblocks (in ticks) */ 
    private int tuneNoteBlockDelayCount = 0;

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Song getSong() {
        return currentSong;
    }

    public String getSongName() {
        return currentSongName; 
    }

    public void onWorldRender(MatrixStack matrixStack) {
        if (currentState == State.WAITING)
            return;

        for (BlockPos noteBlock : noteBlockStage)
            RenderUtils.drawBoxOutline(matrixStack, new Box(noteBlock), Color.WHITE);
    }

    public void onTick() {
        if (!active)
            return;
        
        if (songLoaderThread == null) {
            songLoaderThread = new SongLoaderThread();
            songLoaderThread.start();
        } 

        if (!songLoaderThread.isAlive()) {
            if (songLoaderThread.exception != null) {
                LogUtils.chatLog("Failed to load song: " + songLoaderThread.exception.getMessage());
            } else {
                if (currentSong == null) {
                    currentSong = songLoaderThread.song; 
                    currentSongName = songLoaderThread.songName;
                    LogUtils.chatLog("Loaded song: " + currentSongName);

                    currentState = State.STAGING;
                }
            }
        }

        switch (currentState) {
            case STAGING:
                /* center player */
                mc.player.setPosition(MathHelper.floor(mc.player.getX()) + 0.5, mc.player.getY(), MathHelper.floor(mc.player.getZ()) + 0.5);
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));

                /* look straight ahead */
                mc.player.setYaw(-90.0f);
                mc.player.setPitch(0.0f);

                LogUtils.chatLog("Preparing noteblock stage...");

                scanNoteBlockStage();
                if (noteBlockStage.size() == 0) {
                    LogUtils.chatLog("Could not find any noteblocks within range");
                    currentState = State.ERROR;
                }

                setupPitchMap();
                if (pitchMap.isEmpty()) {
                    LogUtils.chatLog("Could not create pitch to noteblock mapping");
                    currentState = State.ERROR;
                }

                LogUtils.chatLog("Tuning noteblocks...");
                currentState = State.TUNING;
                break;
            case TUNING:
                tuneNoteBlocks();
                break; 
            case PLAYING:
                playSongTick();
                break;
        }
    }

    public void reset() {
        currentState = State.WAITING;
        songLoaderThread = null;
        currentSong = null;
        currentSongName = null;
    } 

    private void scanNoteBlockStage() {
        noteBlockStage.clear();  

        /* locate all playable noteblocks within reach of the player */
        int min = (int) (-mc.interactionManager.getReachDistance()) - 2;
        int max = (int) mc.interactionManager.getReachDistance() + 2;

        for (int y = min; y < max; y++) {
            for (int x = min; x < max; x++) {
                for (int z = min; z < max; z++) {
                    BlockPos blockPos = mc.player.getBlockPos().add(x, y + 1, z); // y + 1 accounts for eye height
                    
                    if (!isValidNoteBlock(blockPos))
                        continue;

                    double distSquared = mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(blockPos));
                    if (distSquared > ServerPlayNetworkHandler.MAX_BREAK_SQUARED_DISTANCE)
                        continue;

                    noteBlockStage.add(blockPos);
                }
            }
        }
    }

    private void setupPitchMap() {
        pitchMap.clear();

        /* define the requirements (distinct notes) of the song */
        List<Note> songRequirements = new ArrayList<>();
        currentSong.getNotes().stream().distinct().forEach(songRequirements::add);

        for (Note note : songRequirements) {
            for (BlockPos blockPos : noteBlockStage) {
                if (pitchMap.containsKey(blockPos))
                    continue;

                int pitch = note.getNoteId() % 25;
                int instrument = note.getNoteId() / 25;
                int noteBlockInstrument = getNoteBlockInstrument(blockPos).ordinal();

                if (instrument == noteBlockInstrument && pitchMap.entrySet()
                    .stream()
                    .filter(e -> e.getValue() == pitch)
                    .noneMatch(e -> getNoteBlockInstrument(e.getKey())
                    .ordinal() == noteBlockInstrument)) {
                    pitchMap.put(blockPos, pitch);
                    break;
                }
            }
        }
    }

    private void tuneNoteBlocks() {
        for (Entry<BlockPos, Integer> e : pitchMap.entrySet()) {
            int currentNote = getNoteBlockNote(e.getKey());
            if (currentNote == -1)
                continue;

            if (currentNote != e.getValue()) {
                if (++tuneNoteBlockDelayCount < tuneNoteBlockDelay)
                    return;

                tuneNoteBlockDelayCount = 0;

                mc.player.swingHand(Hand.MAIN_HAND);

                int targetNote = e.getValue() < currentNote ? e.getValue() + 25 : e.getValue();
                int requiredHits = Math.min(25, targetNote - currentNote);

                for (int i = 0; i < requiredHits; i++)
                    tuneNoteBlock(e.getKey());
            }
        }
        
        LogUtils.chatLog("Playing song...");
        currentState = State.PLAYING;
    }

    private void playSongTick() {
        currentSong.play(); 
        currentSong.advanceCurrentTime();

        mc.player.swingHand(Hand.MAIN_HAND);

        while (currentSong.reachedNextNote()) {
            Note note = currentSong.getNextNote();

            for (Entry<BlockPos, Integer> e : pitchMap.entrySet()) {
                if (note.getNoteId() % 25 == getNoteBlockNote(e.getKey()))
                    playNoteBlock(e.getKey());
            }
        }

        if (currentSong.finished())
            reset();
    }

    private void playNoteBlock(BlockPos blockPos) {
        if (!isValidNoteBlock(blockPos))
            return;

        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.DOWN, 0));
    }

    private void tuneNoteBlock(BlockPos blockPos) {
        if (!isValidNoteBlock(blockPos))
            return;

        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(blockPos), Direction.DOWN, blockPos, false), 0));
    }

    private Instrument getNoteBlockInstrument(BlockPos blockPos) {
        if (!isValidNoteBlock(blockPos))
            return Instrument.HARP;

        return mc.world.getBlockState(blockPos).get(NoteBlock.INSTRUMENT);
    }

    private int getNoteBlockNote(BlockPos blockPos) {
        if (!isValidNoteBlock(blockPos))
            return -1;

        return mc.world.getBlockState(blockPos).get(NoteBlock.NOTE);
    }

    private static boolean isValidNoteBlock(BlockPos blockPos) {
        return mc.world.getBlockState(blockPos).getBlock() instanceof NoteBlock &&
               mc.world.getBlockState(blockPos.up()).isAir();
    }

    public enum State {
        WAITING,
        STAGING,
        TUNING,
        PLAYING,
        ERROR
    }

}
