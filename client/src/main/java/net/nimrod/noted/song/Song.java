package net.nimrod.noted.song;

import java.util.ArrayList;
import java.util.Collections;

public class Song {

    private final String name;

    /* song time accounting (all in millis) */
    private long startTime = 0;
    private long currentTime = 0;
    private long length = 0;

    /* song play state */
    private boolean paused = true; 

    /* song note data */
    private final ArrayList<Note> notes = new ArrayList<Note>();
    private int position = 0;

    public Song(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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

    public void loop() {
        startTime = System.currentTimeMillis() - currentTime;
        position = 0;
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
        if (position >= notes.size())
            return null;

        return notes.get(position++);
    }
    
    public boolean reachedNextNote() {
        if (position < notes.size())
            return notes.get(position).getTime() <= currentTime;

        return false;
    }

}
