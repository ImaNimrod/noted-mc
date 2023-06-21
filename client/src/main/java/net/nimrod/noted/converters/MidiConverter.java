package net.nimrod.noted.converters;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.sound.midi.*;
import net.nimrod.noted.Noted;
import net.nimrod.noted.song.*;

public class MidiConverter {

    private static final int NOTE_ON         = 0x90;
    private static final int NOTE_OFF        = 0x80;
    private static final int SET_TEMPO       = 0x51;
    private static final int SET_INSTRUMENT  = 0xC0;

	public static Song getSongFromFile(File file) throws InvalidMidiDataException, IOException {
        MidiFileFormat midiFormat = MidiSystem.getMidiFileFormat(file);
        Noted.INSTANCE.LOGGER.info(midiFormat.properties().toString());

		Sequence sequence = MidiSystem.getSequence(file);
		return getSong(sequence, file.getName());
	}

	public static Song getSongFromBytes(byte[] bytes, String name) throws InvalidMidiDataException, IOException {
		Sequence sequence = MidiSystem.getSequence(new ByteArrayInputStream(bytes));
		return getSong(sequence, name);
	}

	private static Song getSong(Sequence sequence, String name) {
		Song song = new Song(name);

		ArrayList<MidiEvent> tempoEvents = new ArrayList<MidiEvent>();

		for (Track track : sequence.getTracks()) {
			for (int i = 0; i < track.size(); i++) {
				MidiEvent event = track.get(i);
				MidiMessage message = event.getMessage();

				if (message instanceof MetaMessage) {
					MetaMessage mm = (MetaMessage) message;

					if (mm.getType() == SET_TEMPO)
						tempoEvents.add(event);
				}

			}
		}

		Collections.sort(tempoEvents, (a, b) -> Long.compare(a.getTick(), b.getTick()));

        for (Track track : sequence.getTracks()) {
            long microTime = 0;
            int[] instrumentIds = new int[16];
            int mpq = 500000;
            long tpq = sequence.getResolution();
            int tempoEventIdx = 0;
            long prevTick = 0;

            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();

                while (tempoEventIdx < tempoEvents.size() && event.getTick() > tempoEvents.get(tempoEventIdx).getTick()) {
                    long deltaTick = tempoEvents.get(tempoEventIdx).getTick() - prevTick;
                    prevTick = tempoEvents.get(tempoEventIdx).getTick();
                    microTime += (mpq / tpq) * deltaTick;

                    MetaMessage mm = (MetaMessage) tempoEvents.get(tempoEventIdx).getMessage();
                    byte[] data = mm.getData();

                    int new_mpq = (data[2] & 0xFF) | ((data[1] & 0xFF) << 8) | ((data[0] & 0xFF) <<16);
                    if (new_mpq != 0)
                        mpq = new_mpq;

                    tempoEventIdx++;
                }

                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    int command = sm.getCommand();

                    long deltaTick = 0;
                    long time = 0;

                    switch (command) {
                        case SET_INSTRUMENT:
                            instrumentIds[sm.getChannel()] = sm.getData1(); 
                        case NOTE_ON:
                            if (sm.getData2() == 0)
                                continue;

                            deltaTick = event.getTick() - prevTick;

                            prevTick = event.getTick();
                            microTime += (mpq / tpq) * deltaTick;

                            Note note = null;
                            int pitch = sm.getData1();

                            if (sm.getChannel() != 9)
                                note = getMidiInstrumentNote(instrumentIds[sm.getChannel()], pitch, microTime);

                            if (note != null)
                                song.getNotes().add(note);

                            time = microTime / 1000L;
                            if (time > song.getLength())
                                song.setLength(time);

                            break;
                        case NOTE_OFF:
                            deltaTick = event.getTick() - prevTick;

                            prevTick = event.getTick();
                            microTime += (mpq / tpq) * deltaTick;

                            time = microTime / 1000L;
                            if (time > song.getLength())
                                song.setLength(time);

                            break;
                    }
                }
            }
        }

        song.sortNotes();
        return song;
    }

	private static Note getMidiInstrumentNote(int midiNoteblockInstrument, int midiPitch, long microTime) {
		NoteblockInstrument instrument = null;
		NoteblockInstrument[] instrumentList = instrumentMap.get(midiNoteblockInstrument);

		if (instrumentList != null) {
			for (NoteblockInstrument candidateNoteblockInstrument : instrumentList) {
				if (midiPitch >= candidateNoteblockInstrument.offset && midiPitch <= candidateNoteblockInstrument.offset + 24) {
					instrument = candidateNoteblockInstrument;
					break;
				}
			}
		}

		if (instrument == null)
			return null;

		int noteId = (midiPitch - instrument.offset) + instrument.instrumentId * 25;
		long time = microTime / 1000L;

		return new Note(noteId, time);
	}

    public static HashMap<Integer, NoteblockInstrument[]> instrumentMap = new HashMap<Integer, NoteblockInstrument[]>();
    static {
        // Piano (HARP BASS BELL)
        instrumentMap.put(0,  new NoteblockInstrument[] {NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Acoustic Grand Piano
        instrumentMap.put(1,  new NoteblockInstrument[] {NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Bright Acoustic Piano
        instrumentMap.put(2,  new NoteblockInstrument[] {NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL}); // Electric Grand Piano
        instrumentMap.put(3,  new NoteblockInstrument[] {NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Honky-tonk Piano
        instrumentMap.put(4,  new NoteblockInstrument[] {NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL}); // Electric Piano 1
        instrumentMap.put(5,  new NoteblockInstrument[] {NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL}); // Electric Piano 2
        instrumentMap.put(6,  new NoteblockInstrument[] {NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Harpsichord
        instrumentMap.put(7,  new NoteblockInstrument[] {NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Clavinet

        // Chromatic Percussion (IRON_XYLOPHONE XYLOPHONE BASS)
        instrumentMap.put(8,  new NoteblockInstrument[] {NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE}); // Celesta
        instrumentMap.put(9,  new NoteblockInstrument[] {NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE}); // Glockenspiel
        instrumentMap.put(10, new NoteblockInstrument[] {NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE}); // Music Box
        instrumentMap.put(11, new NoteblockInstrument[] {NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE}); // Vibraphone
        instrumentMap.put(12, new NoteblockInstrument[] {NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE}); // Marimba
        instrumentMap.put(13, new NoteblockInstrument[] {NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE}); // Xylophone
        instrumentMap.put(14, new NoteblockInstrument[] {NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE}); // Tubular Bells
        instrumentMap.put(15, new NoteblockInstrument[] {NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE}); // Dulcimer

        // Organ (BIT DIDGERIDOO BELL)
        instrumentMap.put(16, new NoteblockInstrument[] {NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE}); // Drawbar Organ
        instrumentMap.put(17, new NoteblockInstrument[] {NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE}); // Percussive Organ
        instrumentMap.put(18, new NoteblockInstrument[] {NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE}); // Rock Organ
        instrumentMap.put(19, new NoteblockInstrument[] {NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE}); // Church Organ
        instrumentMap.put(20, new NoteblockInstrument[] {NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE}); // Reed Organ
        instrumentMap.put(21, new NoteblockInstrument[] {NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE}); // Accordian
        instrumentMap.put(22, new NoteblockInstrument[] {NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE}); // Harmonica
        instrumentMap.put(23, new NoteblockInstrument[] {NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE}); // Tango Accordian

        // Guitar (BIT DIDGERIDOO BELL)
        instrumentMap.put(24, new NoteblockInstrument[] {NoteblockInstrument.GUITAR,     NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Acoustic Guitar (nylon)
        instrumentMap.put(25, new NoteblockInstrument[] {NoteblockInstrument.GUITAR,     NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Acoustic Guitar (steel)
        instrumentMap.put(26, new NoteblockInstrument[] {NoteblockInstrument.GUITAR,     NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Electric Guitar (jazz)
        instrumentMap.put(27, new NoteblockInstrument[] {NoteblockInstrument.GUITAR,     NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Electric Guitar (clean)
        instrumentMap.put(28, new NoteblockInstrument[] {NoteblockInstrument.GUITAR,     NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Electric Guitar (muted)
        instrumentMap.put(29, new NoteblockInstrument[] {NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE});             // Overdriven Guitar
        instrumentMap.put(30, new NoteblockInstrument[] {NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE});             // Distortion Guitar
        instrumentMap.put(31, new NoteblockInstrument[] {NoteblockInstrument.GUITAR,     NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Guitar Harmonics

        // Bass
        instrumentMap.put(32, new NoteblockInstrument[] {NoteblockInstrument.BASS,        NoteblockInstrument.HARP, NoteblockInstrument.BELL});      // Acoustic Bass
        instrumentMap.put(33, new NoteblockInstrument[] {NoteblockInstrument.BASS,        NoteblockInstrument.HARP, NoteblockInstrument.BELL});      // Electric Bass (finger)
        instrumentMap.put(34, new NoteblockInstrument[] {NoteblockInstrument.BASS,        NoteblockInstrument.HARP, NoteblockInstrument.BELL});      // Electric Bass (pick)
        instrumentMap.put(35, new NoteblockInstrument[] {NoteblockInstrument.BASS,        NoteblockInstrument.HARP, NoteblockInstrument.BELL});      // Fretless Bass
        instrumentMap.put(36, new NoteblockInstrument[] {NoteblockInstrument.DIDGERIDOO,  NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE}); // Slap Bass 1
        instrumentMap.put(37, new NoteblockInstrument[] {NoteblockInstrument.DIDGERIDOO,  NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE}); // Slap Bass 2
        instrumentMap.put(38, new NoteblockInstrument[] {NoteblockInstrument.DIDGERIDOO,  NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE}); // Synth Bass 1
        instrumentMap.put(39, new NoteblockInstrument[] {NoteblockInstrument.DIDGERIDOO,  NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE}); // Synth Bass 2

        // Strings
        instrumentMap.put(40, new NoteblockInstrument[] {NoteblockInstrument.FLUTE,       NoteblockInstrument.GUITAR,     NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Violin
        instrumentMap.put(41, new NoteblockInstrument[] {NoteblockInstrument.FLUTE,       NoteblockInstrument.GUITAR,     NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Viola
        instrumentMap.put(42, new NoteblockInstrument[] {NoteblockInstrument.FLUTE,       NoteblockInstrument.GUITAR,     NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Cello
        instrumentMap.put(43, new NoteblockInstrument[] {NoteblockInstrument.FLUTE,       NoteblockInstrument.GUITAR,     NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Contrabass
        instrumentMap.put(44, new NoteblockInstrument[] {NoteblockInstrument.BIT,         NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL});                  // Tremolo Strings
        instrumentMap.put(45, new NoteblockInstrument[] {NoteblockInstrument.HARP,        NoteblockInstrument.BASS,       NoteblockInstrument.BELL});                  // Pizzicato Strings
        instrumentMap.put(46, new NoteblockInstrument[] {NoteblockInstrument.HARP,        NoteblockInstrument.BASS,       NoteblockInstrument.CHIME});                 // Orchestral Harp
        instrumentMap.put(47, new NoteblockInstrument[] {NoteblockInstrument.HARP,        NoteblockInstrument.BASS,       NoteblockInstrument.BELL});                  // Timpani

        // Ensenble
        instrumentMap.put(48, new NoteblockInstrument[] {NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // String Ensemble 1
        instrumentMap.put(49, new NoteblockInstrument[] {NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // String Ensemble 2
        instrumentMap.put(50, new NoteblockInstrument[] {NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Synth Strings 1
        instrumentMap.put(51, new NoteblockInstrument[] {NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Synth Strings 2
        instrumentMap.put(52, new NoteblockInstrument[] {NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Choir Aahs
        instrumentMap.put(53, new NoteblockInstrument[] {NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Voice Oohs
        instrumentMap.put(54, new NoteblockInstrument[] {NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Synth Choir
        instrumentMap.put(55, new NoteblockInstrument[] {NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL}); // Orchestra Hit

        // Brass
        instrumentMap.put(56, new NoteblockInstrument[] {NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL});
        instrumentMap.put(57, new NoteblockInstrument[] {NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL});
        instrumentMap.put(58, new NoteblockInstrument[] {NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL});
        instrumentMap.put(59, new NoteblockInstrument[] {NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL});
        instrumentMap.put(60, new NoteblockInstrument[] {NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL});
        instrumentMap.put(61, new NoteblockInstrument[] {NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL});
        instrumentMap.put(62, new NoteblockInstrument[] {NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL});
        instrumentMap.put(63, new NoteblockInstrument[] {NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL});

        // Reed
        instrumentMap.put(64, new NoteblockInstrument[] {NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL});
        instrumentMap.put(65, new NoteblockInstrument[] {NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL});
        instrumentMap.put(66, new NoteblockInstrument[] {NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL});
        instrumentMap.put(67, new NoteblockInstrument[] {NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL});
        instrumentMap.put(68, new NoteblockInstrument[] {NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL});
        instrumentMap.put(69, new NoteblockInstrument[] {NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL});
        instrumentMap.put(70, new NoteblockInstrument[] {NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL});
        instrumentMap.put(71, new NoteblockInstrument[] {NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL});

        // Pipe
        instrumentMap.put(72, new NoteblockInstrument[] {NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL});
        instrumentMap.put(73, new NoteblockInstrument[] {NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL});
        instrumentMap.put(74, new NoteblockInstrument[] {NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL});
        instrumentMap.put(75, new NoteblockInstrument[] {NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL});
        instrumentMap.put(76, new NoteblockInstrument[] {NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL});
        instrumentMap.put(77, new NoteblockInstrument[] {NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL});
        instrumentMap.put(78, new NoteblockInstrument[] {NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL});
        instrumentMap.put(79, new NoteblockInstrument[] {NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL});

        // Synth Lead
        instrumentMap.put(80, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(81, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(82, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(83, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(84, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(85, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(86, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(87, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.BASS,       NoteblockInstrument.BELL});

        // Synth Pad
        instrumentMap.put(88, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(89, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(90, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(91, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(92, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(93, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(94, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(95, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.BASS,       NoteblockInstrument.BELL});

        // Synth Effects
        instrumentMap.put(98, new NoteblockInstrument[]{ NoteblockInstrument.BIT,   NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL});
        instrumentMap.put(99, new NoteblockInstrument[]{ NoteblockInstrument.HARP,  NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(100, new NoteblockInstrument[] {NoteblockInstrument.HARP, NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(101, new NoteblockInstrument[] {NoteblockInstrument.HARP, NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(102, new NoteblockInstrument[] {NoteblockInstrument.HARP, NoteblockInstrument.BASS,       NoteblockInstrument.BELL});
        instrumentMap.put(103, new NoteblockInstrument[] {NoteblockInstrument.HARP, NoteblockInstrument.BASS,       NoteblockInstrument.BELL});

        // Ethnic
        instrumentMap.put(104, new NoteblockInstrument[] {NoteblockInstrument.BANJO, NoteblockInstrument.BASS, NoteblockInstrument.BELL});
        instrumentMap.put(105, new NoteblockInstrument[] {NoteblockInstrument.BANJO, NoteblockInstrument.BASS, NoteblockInstrument.BELL});
        instrumentMap.put(106, new NoteblockInstrument[] {NoteblockInstrument.BANJO, NoteblockInstrument.BASS, NoteblockInstrument.BELL});
        instrumentMap.put(107, new NoteblockInstrument[] {NoteblockInstrument.BANJO, NoteblockInstrument.BASS, NoteblockInstrument.BELL});
        instrumentMap.put(108, new NoteblockInstrument[] {NoteblockInstrument.BANJO, NoteblockInstrument.BASS, NoteblockInstrument.BELL});
        instrumentMap.put(109, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL});
        instrumentMap.put(110, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL});
        instrumentMap.put(111, new NoteblockInstrument[] {NoteblockInstrument.HARP,  NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL});

        // Percussive
        instrumentMap.put(112, new NoteblockInstrument[] {NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE});
        instrumentMap.put(113, new NoteblockInstrument[] {NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE});
        instrumentMap.put(114, new NoteblockInstrument[] {NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE});
        instrumentMap.put(115, new NoteblockInstrument[] {NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE});
        instrumentMap.put(116, new NoteblockInstrument[] {NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE});
        instrumentMap.put(117, new NoteblockInstrument[] {NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE});
        instrumentMap.put(118, new NoteblockInstrument[] {NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE});
        instrumentMap.put(119, new NoteblockInstrument[] {NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE});
    }

}
