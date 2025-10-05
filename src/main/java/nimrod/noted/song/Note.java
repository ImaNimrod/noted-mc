package nimrod.noted.song;

public class Note implements Comparable<Note> {
    private final int noteId;
    private final long time; 

    public Note(int noteId, long time) {
        this.noteId = noteId;
        this.time = time;
    }

    public int getNoteId() {
        return noteId;
    }

    public long getTime() {
        return time;
    }

    @Override
    public int compareTo(Note other) {
        if (time < other.getTime()) {
            return -1;
        } else if (time > other.getTime()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Note)) {
            return false;
        }

        Note other = (Note) obj;
        return noteId == other.noteId; 
    }

    @Override
    public int hashCode() {
        return (noteId % 25) * 31 + (noteId / 25);
    }
}
