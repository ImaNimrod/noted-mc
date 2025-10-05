package nimrod.noted.song;

import java.util.ArrayList;
import java.util.Collections;

public class Song {
    private final String name;
    private final ArrayList<Note> notes = new ArrayList<Note>();

    private long length = 0;
    private long startTime = 0;
    private long currentTime = 0;

    private int position = 0;
    private boolean paused = true; 

    public Song(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void advanceCurrentTime() {
        currentTime = System.currentTimeMillis() - startTime;
    }

    public void play() {
        if (paused) {
            paused = false;
            startTime = System.currentTimeMillis() - currentTime;
        }
    }

    public void pause() {
        if (!paused) {
            paused = true;
            advanceCurrentTime();
        }
    }

    public boolean finished() {
        return currentTime > length;
    }

    public ArrayList<Note> getNotes() {
        return notes;
    }

    public Note getNote(int noteId) {
        return notes.get(noteId);
    }

    public void sortNotes() {
        Collections.sort(notes);
    }

    public Note getNextNote() {
        if (position >= notes.size()) {
            return null;
        }

        return notes.get(position++);
    }

    public boolean reachedNextNote() {
        if (position < notes.size()) {
            return notes.get(position).getTime() <= currentTime;
        }

        return false;
    }
}
