package net.nimrod.noted.converters;

import java.io.*;
import java.nio.file.Files;
import net.nimrod.noted.song.*;

/* TODO: implement NBS parser */
public class NbsConverter {

	public static Song getSongFromFile(File file) throws IOException { 
		return getSong(Files.readAllBytes(file.toPath()), file.getName());
	}

	public static Song getSongFromBytes(byte[] bytes, String name) throws IOException {
		return getSong(bytes, name);
	}

    private static Song getSong(byte bytes[], String name) throws IOException {
        return null;
    }

    /* NBS data to noteblock instrument conversion */
    private static NoteblockInstrument[] instrumentIndex = new NoteblockInstrument[] {
            NoteblockInstrument.HARP,
            NoteblockInstrument.BASS,
            NoteblockInstrument.BASEDRUM,
            NoteblockInstrument.SNARE,
            NoteblockInstrument.HAT,
            NoteblockInstrument.GUITAR,
            NoteblockInstrument.FLUTE,
            NoteblockInstrument.BELL,
            NoteblockInstrument.CHIME,
            NoteblockInstrument.XYLOPHONE,
            NoteblockInstrument.IRON_XYLOPHONE,
            NoteblockInstrument.COW_BELL,
            NoteblockInstrument.DIDGERIDOO,
            NoteblockInstrument.BIT,
            NoteblockInstrument.BANJO,
            NoteblockInstrument.PLING,
    };

    private static class NbsNote {
        public int tick;
        public short layer;
        public byte instrument;
        public byte key;
        public byte velocity = 100;
        public byte panning = 100;
        public short pitch = 0;
    }

    private static class NbsLayer {
        public String name;
        public byte lock = 0;
        public byte volume;
        public byte stereo = 100;
    }

}
