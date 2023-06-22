package net.nimrod.noted.playing;

import net.nimrod.noted.Noted;
import net.nimrod.noted.converters.*;
import net.nimrod.noted.song.Song;
import net.nimrod.noted.util.ApiUtils;

import java.io.IOException;

public class SongLoaderThread extends Thread {
    
    public Song song;
    public Exception exception;

    public void run() {
        try {
            String nextSongId = null; 

            while (nextSongId == null) {
                nextSongId = ApiUtils.getNextSongId();
                Thread.sleep(1000);
            }

            byte[] bytes = ApiUtils.getSong(nextSongId);

            try {
                String songName = ApiUtils.getSongName(nextSongId);
                song = MidiConverter.getSongFromBytes(bytes, songName);
            } catch (Exception e) {
                exception = e;
            }
            
            if (song == null)
                throw new IOException("Invalid song format");
        } catch (Exception e) {
            exception = e;
        }
    }

}
