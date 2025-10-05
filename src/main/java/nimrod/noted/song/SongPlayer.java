package nimrod.noted.song;

import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import nimrod.noted.Noted;
import nimrod.noted.utils.RenderUtils;
import nimrod.noted.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map.Entry;

import static nimrod.noted.Noted.MC;

public class SongPlayer {
    public Song currentSong = null;

    private final List<BlockPos> noteBlockStage = new ArrayList<>();
    private final HashMap<BlockPos, Integer> pitchMap = new HashMap<>();
    private final List<BlockPos> playedNoteBlocks = new ArrayList<>();
    private final int tuneNoteBlockDelay = 5;

    private boolean paused = false;
    private State state = State.WAITING;
    private int tuneNoteBlockDelayCount = 0;

    public String getStatus() {
        switch (state) {
            case STAGING:
            case TUNING:
                return "Tuning to play: " + currentSong.getName();
            case PLAYING:
                return String.format("Playing: %s | Time: %s/%s",
                    currentSong.getName(), TimeUtils.formatTime(currentSong.getCurrentTime()), TimeUtils.formatTime(currentSong.getLength()));
            default:
                return "Nothing currently playing";
        }
    }

    public void setSong(Song song) {
        if (this.currentSong != null) {
            reset();
        }

        this.currentSong = song;
    }

    public void reset() {
        currentSong = null;
        state = State.WAITING;
        paused = false;
    } 

    public void togglePaused() {
        if (currentSong == null) {
            Noted.chatMessage("No song playing");
            return;
        }

        paused = !paused;
        Noted.chatMessage("Song " + (paused ? "§cpaused§f" : "§aunpaused§f"));
    }

    public void onRender2D(DrawContext context, float tickDelta) {
        if (currentSong == null) {
            return;
        }

        context.state.goUpLayer();
        context.drawText(MC.textRenderer, getStatus(), 5, MC.getWindow().getScaledHeight() - 24, 0xffffffff, true);
        context.drawText(MC.textRenderer, Noted.getAttribution(), 5, MC.getWindow().getScaledHeight() - 12, 0xffffffff, true);
    }

    public void onRender3D(MatrixStack matrices, Camera camera) {
        switch (state) {
            case STAGING:
            case TUNING:
                for (BlockPos noteBlock : noteBlockStage) {
                    RenderUtils.draw3DBox(matrices, camera, new Box(noteBlock), 0xffffff);
                }
                break;
            case PLAYING:
                for (BlockPos noteBlock : noteBlockStage) {
                    if (playedNoteBlocks.contains(noteBlock)) {
                        RenderUtils.draw3DBox(matrices, camera, new Box(noteBlock), 0x14ec05);
                    } else {
                        Integer pitch = pitchMap.get(noteBlock);
                        if (pitch == null) {
                            continue;
                        }

                        if (pitch != getNoteBlockNote(noteBlock)) {
                            RenderUtils.draw3DBox(matrices, camera, new Box(noteBlock), 0xed0524);
                        }
                    }
                }
                break;
        }
    }

    public void onTick() {
        switch (state) {
            case WAITING:
                if (currentSong != null) {
                    state = State.STAGING;
                }
                break;
            case STAGING:
                centerPlayer();

                Noted.chatMessage("Preparing noteblock stage...");

                locateNotBlockStage();
                if (noteBlockStage.size() == 0) {
                    Noted.chatMessage("Could not find any noteblocks within range");
                    state = State.ERROR;
                    break;
                }

                setupPitchMap();
                if (pitchMap.isEmpty()) {
                    Noted.chatMessage("Could not create pitch to noteblock mapping");
                    state = State.ERROR;
                    break;
                }

                Noted.chatMessage("Tuning noteblocks...");
                state = State.TUNING;
                break;
            case TUNING:
                tuneNoteBlocks();
                break; 
            case PLAYING:
                playSongTick();
                break;
            case ERROR:
                reset();
                break;
        }
    }

    private void centerPlayer() {
        MC.player.setPosition(MathHelper.floor(MC.player.getX()) + 0.5, MC.player.getY(), MathHelper.floor(MC.player.getZ()) + 0.5);
        MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getPos(), MC.player.isOnGround(), MC.player.horizontalCollision));
    }

    private NoteBlockInstrument getNoteBlockInstrument(BlockPos blockPos) {
        if (!isValidNoteBlock(blockPos)) {
            return NoteBlockInstrument.HARP;
        }

        return MC.world.getBlockState(blockPos).get(NoteBlock.INSTRUMENT);
    }

    private int getNoteBlockNote(BlockPos blockPos) {
        if (!isValidNoteBlock(blockPos)) {
            return -1;
        }

        return MC.world.getBlockState(blockPos).get(NoteBlock.NOTE);
    }

    private boolean isValidNoteBlock(BlockPos blockPos) {
        return MC.world.getBlockState(blockPos).getBlock() instanceof NoteBlock && MC.world.getBlockState(blockPos.up()).isAir();
    }

    private void locateNotBlockStage() {
        noteBlockStage.clear();  

        for (int y = -4; y <= 4; y++) {
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos blockPos = MC.player.getBlockPos().add(x, y + 1, z); // y + 1 accounts for eye height
                    if (!isValidNoteBlock(blockPos)) {
                        continue;
                    }

                    noteBlockStage.add(blockPos);
                }
            }
        }
    }

    private void playNoteBlock(BlockPos blockPos) {
        if (!isValidNoteBlock(blockPos)) {
            return;
        }

        MC.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.DOWN, 0));
    }

    private void playSongTick() {
        if (paused) {
            currentSong.pause();
            return;
        }

        playedNoteBlocks.clear();

        currentSong.play(); 
        currentSong.advanceCurrentTime();

        if (Noted.CONFIG.swingHand) {
            MC.player.swingHand(Hand.MAIN_HAND);
        }

        while (currentSong.reachedNextNote()) {
            Note note = currentSong.getNextNote();

            for (Entry<BlockPos, Integer> e : pitchMap.entrySet()) {
                if (note.getNoteId() % 25 == getNoteBlockNote(e.getKey())) {
                    playedNoteBlocks.add(e.getKey());
                    playNoteBlock(e.getKey());
                }
            }
        }

        if (currentSong.finished()) {
            reset();
        }
    }

    private void setupPitchMap() {
        pitchMap.clear();

        List<Note> songRequirements = new ArrayList<>();
        currentSong.getNotes().stream().distinct().forEach(songRequirements::add);

        for (Note note : songRequirements) {
            for (BlockPos blockPos : noteBlockStage) {
                if (pitchMap.containsKey(blockPos)) {
                    continue;
                }

                int pitch = note.getNoteId() % 25;
                int instrument = note.getNoteId() / 25;
                int noteBlockInstrument = getNoteBlockInstrument(blockPos).ordinal();

                if (instrument == noteBlockInstrument && pitchMap
                .entrySet()
                .stream()
                .filter(e -> e.getValue() == pitch)
                .noneMatch(e -> getNoteBlockInstrument(e.getKey()).ordinal() == noteBlockInstrument)) {
                    pitchMap.put(blockPos, pitch);
                    break;
                }
            }
        }
    }

    private void tuneNoteBlock(BlockPos blockPos) {
        if (!isValidNoteBlock(blockPos)) {
            return;
        }

        MC.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(blockPos), Direction.DOWN, blockPos, false), 0));
    }

    private void tuneNoteBlocks() {
        for (Entry<BlockPos, Integer> e : pitchMap.entrySet()) {
            int currentNote = getNoteBlockNote(e.getKey());
            if (currentNote == -1) {
                continue;
            }

            if (currentNote == e.getValue()) {
                continue;
            }

            if (++tuneNoteBlockDelayCount < tuneNoteBlockDelay) {
                return;
            }

            tuneNoteBlockDelayCount = 0;

            if (Noted.CONFIG.swingHand) {
                MC.player.swingHand(Hand.MAIN_HAND);
            }

            int targetNote = e.getValue() < currentNote ? e.getValue() + 25 : e.getValue();
            int requiredHits = Math.min(25, targetNote - currentNote);

            for (int i = 0; i < requiredHits; i++) {
                tuneNoteBlock(e.getKey());
            }
        }

        Noted.chatMessage("Playing song...");
        state = State.PLAYING;
    }

    enum State {
        WAITING,
        STAGING,
        TUNING,
        PLAYING,
        ERROR,
    }
}
