package net.nimrod.noted.playing;

import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.Instrument;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
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
import net.nimrod.noted.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map.Entry;

public class SongPlayer {

    public boolean active = false;                      /* is song player playing */ 
    public boolean paused = false;                      /* is the current song paused */

    public Song currentSong = null;                     /* currently playing song structure */

    private State state = State.WAITING;                /* the current state of the bot */
    private SongLoaderThread songLoaderThread = null;   /* seperate execution thread for fetching songs from api */

    private final List<BlockPos> noteBlockStage = new ArrayList<>();        /* holds the block positions of playable noteblocks */
    private final List<BlockPos> playedNoteBlocks = new ArrayList<>();      /* holds the block positions of the noteblocks played during a single tick */
    private final HashMap<BlockPos, Integer> pitchMap = new HashMap<>();    /* a mapping of noteblocks to note pitches */

    private final int tuneNoteBlockDelay = 5;           /* the time between tuning individual noteblocks (in ticks) */ 
    private int tuneNoteBlockDelayCount = 0;

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public void toggleActive() {
        active = !active;
        if (!active)
            reset();

        LogUtils.chatLog("Toggled noted-client " + (active ? "§aon§f" : "§coff§f"));
    }

    public void togglePaused() {
        if (currentSong == null) {
            LogUtils.chatLog("No song playing");
            return;
        }

        paused = !paused;
        LogUtils.chatLog("Song " + (paused ? "§cpaused§f" : "§aunpaused§f"));
    }

    public void reset() {
        paused = false;
        currentSong = null;
        state = State.WAITING;
        songLoaderThread = null;
    } 

    public void onHudRender(DrawContext context, float tickDelta) {
        if (active && currentSong != null) {
            String playingString = "Now Playing: " + currentSong.getName();
            int playingStringX = mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth(playingString) - 2;

            String timeString = TimeUtils.formatTime(currentSong.getCurrentTime()) + "/" + TimeUtils.formatTime(currentSong.getLength());
            int timeStringX = mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth(timeString) - 2;

            RenderUtils.drawString(context, playingString, playingStringX, 4, 0xffffff);
            RenderUtils.drawString(context, timeString, timeStringX, 16, 0xffffff);
        }
    }

    public void onWorldRender(MatrixStack matrixStack) {
        switch (state) {
            case STAGING:
            case TUNING:
                for (BlockPos noteBlock : noteBlockStage)
                    RenderUtils.drawBoxOutline(matrixStack, new Box(noteBlock), 0xffffff);
                break;
            case PLAYING:
                for (BlockPos noteBlock : noteBlockStage) {
                    if (playedNoteBlocks.contains(noteBlock)) {
                        RenderUtils.drawBoxFilled(matrixStack, new Box(noteBlock), 0x14ec05);
                    } else {
                        Integer pitch = pitchMap.get(noteBlock);
                        if (pitch == null)
                            continue;

                        if (pitch != getNoteBlockNote(noteBlock))
                            RenderUtils.drawBoxFilled(matrixStack, new Box(noteBlock), 0xed0524);
                    }
                }
                break;
        }
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
                state = State.ERROR;
            } else {
                if (currentSong == null) {
                    currentSong = songLoaderThread.song; 
                    LogUtils.chatLog("Loaded song: " + currentSong.getName());

                    state = State.STAGING;
                }
            }
        }

        switch (state) {
            case STAGING:
                /* center player */
                mc.player.setPosition(MathHelper.floor(mc.player.getX()) + 0.5, mc.player.getY(), MathHelper.floor(mc.player.getZ()) + 0.5);
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));

                LogUtils.chatLog("Preparing noteblock stage...");

                scanNoteBlockStage();
                if (noteBlockStage.size() == 0) {
                    LogUtils.chatLog("Could not find any noteblocks within range");
                    state = State.ERROR;
                    return;
                }

                setupPitchMap();
                if (pitchMap.isEmpty()) {
                    LogUtils.chatLog("Could not create pitch to noteblock mapping");
                    state = State.ERROR;
                    return;
                }

                LogUtils.chatLog("Tuning noteblocks...");
                state = State.TUNING;
                break;
            case TUNING:
                tuneNoteBlocks();
                break; 
            case PLAYING:
                playSongTick();
                break;
            case ERROR:
                toggleActive();
                break;
        }
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
                    .ordinal() == noteBlockInstrument)) 
                {
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
        state = State.PLAYING;
    }

    private void playSongTick() {
        if (paused) {
            currentSong.pause();
            return;
        }

        playedNoteBlocks.clear();

        currentSong.play(); 
        currentSong.advanceCurrentTime();

        mc.player.swingHand(Hand.MAIN_HAND);

        while (currentSong.reachedNextNote()) {
            Note note = currentSong.getNextNote();

            for (Entry<BlockPos, Integer> e : pitchMap.entrySet()) {
                if (note.getNoteId() % 25 == getNoteBlockNote(e.getKey())) {
                    playedNoteBlocks.add(e.getKey());
                    playNoteBlock(e.getKey());
                }
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
