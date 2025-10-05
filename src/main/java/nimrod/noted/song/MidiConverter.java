package nimrod.noted.song;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.sound.midi.*;

public class MidiConverter {
    private static final int NOTE_ON = 0x90;
    private static final int NOTE_OFF = 0x80;
    private static final int SET_TEMPO = 0x51;
    private static final int SET_INSTRUMENT = 0xC0;

    public static final HashMap<Integer, NoteblockInstrument[]> INSTRUMENT_MAP = new HashMap<Integer, NoteblockInstrument[]>() {
        {
            /* Piano (HARP BASS BELL) */
            put(0,  new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL});         // Acoustic Grand Piano
            put(1,  new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL});         // Bright Acoustic Piano
            put(2,  new NoteblockInstrument[] { NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL});    // Electric Grand Piano
            put(3,  new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL});         // Honky-tonk Piano
            put(4,  new NoteblockInstrument[] { NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL});    // Electric Piano 1
            put(5,  new NoteblockInstrument[] { NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL});    // Electric Piano 2
            put(6,  new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL});         // Harpsichord
            put(7,  new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL});         // Clavinet

            /* Chromatic Percussion (IRON_XYLOPHONE XYLOPHONE BASS) */
            put(8,  new NoteblockInstrument[] { NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE }); // Celesta
            put(9,  new NoteblockInstrument[] { NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE }); // Glockenspiel
            put(10, new NoteblockInstrument[] { NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE }); // Music Box
            put(11, new NoteblockInstrument[] { NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE }); // Vibraphone
            put(12, new NoteblockInstrument[] { NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE }); // Marimba
            put(13, new NoteblockInstrument[] { NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE }); // Xylophone
            put(14, new NoteblockInstrument[] { NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE }); // Tubular Bells
            put(15, new NoteblockInstrument[] { NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE }); // Dulcimer

            /* Organ (BIT DIDGERIDOO BELL) */
            put(16, new NoteblockInstrument[] { NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE }); // Drawbar Organ
            put(17, new NoteblockInstrument[] { NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE }); // Percussive Organ
            put(18, new NoteblockInstrument[] { NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE }); // Rock Organ
            put(19, new NoteblockInstrument[] { NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE }); // Church Organ
            put(20, new NoteblockInstrument[] { NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE }); // Reed Organ
            put(21, new NoteblockInstrument[] { NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE }); // Accordian
            put(22, new NoteblockInstrument[] { NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE }); // Harmonica
            put(23, new NoteblockInstrument[] { NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT,  NoteblockInstrument.XYLOPHONE }); // Tango Accordian

            /* Guitar (BIT DIDGERIDOO BELL) */
            put(24, new NoteblockInstrument[] { NoteblockInstrument.GUITAR, NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });    // Acoustic Guitar (nylon)
            put(25, new NoteblockInstrument[] { NoteblockInstrument.GUITAR, NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });    // Acoustic Guitar (steel)
            put(26, new NoteblockInstrument[] { NoteblockInstrument.GUITAR, NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });    // Electric Guitar (jazz)
            put(27, new NoteblockInstrument[] { NoteblockInstrument.GUITAR, NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });    // Electric Guitar (clean)
            put(28, new NoteblockInstrument[] { NoteblockInstrument.GUITAR, NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });    // Electric Guitar (muted)
            put(29, new NoteblockInstrument[] { NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT, NoteblockInstrument.XYLOPHONE });                      // Overdriven Guitar
            put(30, new NoteblockInstrument[] { NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT, NoteblockInstrument.XYLOPHONE });                      // Distortion Guitar
            put(31, new NoteblockInstrument[] { NoteblockInstrument.GUITAR, NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });    // Guitar Harmonics

            /* Bass */
            put(32, new NoteblockInstrument[] { NoteblockInstrument.BASS, NoteblockInstrument.HARP, NoteblockInstrument.BELL });                                // Acoustic Bass
            put(33, new NoteblockInstrument[] { NoteblockInstrument.BASS, NoteblockInstrument.HARP, NoteblockInstrument.BELL });                                // Electric Bass (finger)
            put(34, new NoteblockInstrument[] { NoteblockInstrument.BASS, NoteblockInstrument.HARP, NoteblockInstrument.BELL });                                // Electric Bass (pick)
            put(35, new NoteblockInstrument[] { NoteblockInstrument.BASS, NoteblockInstrument.HARP, NoteblockInstrument.BELL });                                // Fretless Bass
            put(36, new NoteblockInstrument[] { NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT, NoteblockInstrument.XYLOPHONE });                      // Slap Bass 1
            put(37, new NoteblockInstrument[] { NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT, NoteblockInstrument.XYLOPHONE });                      // Slap Bass 2
            put(38, new NoteblockInstrument[] { NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT, NoteblockInstrument.XYLOPHONE });                      // Synth Bass 1
            put(39, new NoteblockInstrument[] { NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BIT, NoteblockInstrument.XYLOPHONE });                      // Synth Bass 2

            /* Strings */
            put(40, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.GUITAR, NoteblockInstrument.BASS, NoteblockInstrument.BELL });   // Violin
            put(41, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.GUITAR, NoteblockInstrument.BASS, NoteblockInstrument.BELL });   // Viola
            put(42, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.GUITAR, NoteblockInstrument.BASS, NoteblockInstrument.BELL });   // Cello
            put(43, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.GUITAR, NoteblockInstrument.BASS, NoteblockInstrument.BELL });   // Contrabass
            put(44, new NoteblockInstrument[] { NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL });                           // Tremolo Strings
            put(45, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });                                // Pizzicato Strings
            put(46, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.CHIME });                               // Orchestral Harp
            put(47, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });                                // Timpani

            /* Ensenble */
            put(48, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });    // String Ensemble 1
            put(49, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });    // String Ensemble 2
            put(50, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });    // Synth Strings 1
            put(51, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });    // Synth Strings 2
            put(52, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });    // Choir Aahs
            put(53, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });    // Voice Oohs
            put(54, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });    // Synth Choir
            put(55, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });    // Orchestra Hit

            /* Brass */
            put(56, new NoteblockInstrument[] { NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL });
            put(57, new NoteblockInstrument[] { NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL });
            put(58, new NoteblockInstrument[] { NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL });
            put(59, new NoteblockInstrument[] { NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL });
            put(60, new NoteblockInstrument[] { NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL });
            put(61, new NoteblockInstrument[] { NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL });
            put(62, new NoteblockInstrument[] { NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL });
            put(63, new NoteblockInstrument[] { NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL });

            /* Reed */
            put(64, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL });
            put(65, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL });
            put(66, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL });
            put(67, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL });
            put(68, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL });
            put(69, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL });
            put(70, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL });
            put(71, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL });

            /* Pipe */
            put(72, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL });
            put(73, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL });
            put(74, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL });
            put(75, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL });
            put(76, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL });
            put(77, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL });
            put(78, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL });
            put(79, new NoteblockInstrument[] { NoteblockInstrument.FLUTE, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BELL });

            /* Synth Lead */
            put(80, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(81, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(82, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(83, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(84, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(85, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(86, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(87, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });

            /* Synth Pad (HARP BASS BELL) */
            put(88, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(89, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(90, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(91, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(92, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(93, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(94, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(95, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });

            /* Synth Effects */
            put(98,  new NoteblockInstrument[] { NoteblockInstrument.BIT, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL });
            put(99,  new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(100, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(101, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(102, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(103, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.BASS, NoteblockInstrument.BELL });

            /* Ethnic */
            put(104, new NoteblockInstrument[] { NoteblockInstrument.BANJO, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(105, new NoteblockInstrument[] { NoteblockInstrument.BANJO, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(106, new NoteblockInstrument[] { NoteblockInstrument.BANJO, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(107, new NoteblockInstrument[] { NoteblockInstrument.BANJO, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(108, new NoteblockInstrument[] { NoteblockInstrument.BANJO, NoteblockInstrument.BASS, NoteblockInstrument.BELL });
            put(109, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL });
            put(110, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL });
            put(111, new NoteblockInstrument[] { NoteblockInstrument.HARP, NoteblockInstrument.DIDGERIDOO, NoteblockInstrument.BELL });

            /* Percussive */
            put(112, new NoteblockInstrument[] { NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE });
            put(113, new NoteblockInstrument[] { NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE });
            put(114, new NoteblockInstrument[] { NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE });
            put(115, new NoteblockInstrument[] { NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE });
            put(116, new NoteblockInstrument[] { NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE });
            put(117, new NoteblockInstrument[] { NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE });
            put(118, new NoteblockInstrument[] { NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE });
            put(119, new NoteblockInstrument[] { NoteblockInstrument.IRON_XYLOPHONE, NoteblockInstrument.BASS, NoteblockInstrument.XYLOPHONE });
        }
    };

    public static final HashMap<Integer, Integer> PERCUSSION_MAP = new HashMap<Integer, Integer>() {
        {
            put(35, 10 + 25 * NoteblockInstrument.BASEDRUM.instrumentId);
            put(36, 6  + 25 * NoteblockInstrument.BASEDRUM.instrumentId);
            put(37, 6  + 25 * NoteblockInstrument.HAT.instrumentId);
            put(38, 8  + 25 * NoteblockInstrument.SNARE.instrumentId);
            put(39, 6  + 25 * NoteblockInstrument.HAT.instrumentId);
            put(40, 4  + 25 * NoteblockInstrument.SNARE.instrumentId);
            put(41, 6  + 25 * NoteblockInstrument.BASEDRUM.instrumentId);
            put(42, 22 + 25 * NoteblockInstrument.SNARE.instrumentId);
            put(43, 13 + 25 * NoteblockInstrument.BASEDRUM.instrumentId);
            put(44, 22 + 25 * NoteblockInstrument.SNARE.instrumentId);
            put(45, 15 + 25 * NoteblockInstrument.BASEDRUM.instrumentId);
            put(46, 18 + 25 * NoteblockInstrument.SNARE.instrumentId);
            put(47, 20 + 25 * NoteblockInstrument.BASEDRUM.instrumentId);
            put(48, 23 + 25 * NoteblockInstrument.BASEDRUM.instrumentId);
            put(49, 17 + 25 * NoteblockInstrument.SNARE.instrumentId);
            put(50, 23 + 25 * NoteblockInstrument.BASEDRUM.instrumentId);
            put(51, 24 + 25 * NoteblockInstrument.SNARE.instrumentId);
            put(52, 8  + 25 * NoteblockInstrument.SNARE.instrumentId);
            put(53, 13 + 25 * NoteblockInstrument.SNARE.instrumentId);
            put(54, 18 + 25 * NoteblockInstrument.HAT.instrumentId);
            put(55, 18 + 25 * NoteblockInstrument.SNARE.instrumentId);
            put(56, 1  + 25 * NoteblockInstrument.HAT.instrumentId);
            put(57, 13 + 25 * NoteblockInstrument.SNARE.instrumentId);
            put(58, 2  + 25 * NoteblockInstrument.HAT.instrumentId);
            put(59, 13 + 25 * NoteblockInstrument.SNARE.instrumentId);
            put(60, 9  + 25 * NoteblockInstrument.HAT.instrumentId);
            put(61, 2  + 25 * NoteblockInstrument.HAT.instrumentId);
            put(62, 8  + 25 * NoteblockInstrument.HAT.instrumentId);
            put(63, 22 + 25 * NoteblockInstrument.BASEDRUM.instrumentId);
            put(64, 15 + 25 * NoteblockInstrument.BASEDRUM.instrumentId);
            put(65, 13 + 25 * NoteblockInstrument.SNARE.instrumentId);
            put(66, 8  + 25 * NoteblockInstrument.SNARE.instrumentId);
            put(67, 8  + 25 * NoteblockInstrument.HAT.instrumentId);
            put(68, 3  + 25 * NoteblockInstrument.HAT.instrumentId);
            put(69, 20 + 25 * NoteblockInstrument.HAT.instrumentId);
            put(70, 23 + 25 * NoteblockInstrument.HAT.instrumentId);
            put(71, 24 + 25 * NoteblockInstrument.HAT.instrumentId);
            put(72, 24 + 25 * NoteblockInstrument.HAT.instrumentId);
            put(73, 17 + 25 * NoteblockInstrument.HAT.instrumentId);
            put(74, 11 + 25 * NoteblockInstrument.HAT.instrumentId);
            put(75, 18 + 25 * NoteblockInstrument.HAT.instrumentId);
            put(76, 9  + 25 * NoteblockInstrument.HAT.instrumentId);
            put(77, 5  + 25 * NoteblockInstrument.HAT.instrumentId);
            put(78, 22 + 25 * NoteblockInstrument.HAT.instrumentId);
            put(79, 19 + 25 * NoteblockInstrument.SNARE.instrumentId);
            put(80, 17 + 25 * NoteblockInstrument.HAT.instrumentId);
            put(81, 22 + 25 * NoteblockInstrument.HAT.instrumentId);
            put(82, 22 + 25 * NoteblockInstrument.SNARE.instrumentId);
            put(83, 24 + 25 * NoteblockInstrument.CHIME.instrumentId);
            put(84, 24 + 25 * NoteblockInstrument.CHIME.instrumentId);
            put(85, 21 + 25 * NoteblockInstrument.HAT.instrumentId);
            put(86, 14 + 25 * NoteblockInstrument.BASEDRUM.instrumentId);
            put(87, 7  + 25 * NoteblockInstrument.BASEDRUM.instrumentId);
        }
    };

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

                if (message instanceof MetaMessage metaMessage) {
                    if (metaMessage.getType() == SET_TEMPO) {
                        tempoEvents.add(event);
                    }
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
                    if (new_mpq != 0) {
                        mpq = new_mpq;
                    }

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
                            break;
                        case NOTE_ON:
                            if (sm.getData2() == 0) {
                                continue;
                            }

                            deltaTick = event.getTick() - prevTick;

                            prevTick = event.getTick();
                            microTime += (mpq / tpq) * deltaTick;

                            Note note = null;
                            if (sm.getChannel() != 9) {
                                note = getMidiInstrumentNote(instrumentIds[sm.getChannel()], sm.getData1(), microTime);
                            }

                        /*
                            if (sm.getChannel() == 9) {
                                note = getMidiPercussionNote(sm.getData1(), microTime);
                            } else {
                                note = getMidiInstrumentNote(instrumentIds[sm.getChannel()], sm.getData1(), microTime);
                            }
                        */

                            if (note != null) {
                                song.getNotes().add(note);
                            }

                            time = microTime / 1000L;
                            if (time > song.getLength()) {
                                song.setLength(time);
                            }

                            break;
                            case NOTE_OFF:
                            deltaTick = event.getTick() - prevTick;

                            prevTick = event.getTick();
                            microTime += (mpq / tpq) * deltaTick;

                            time = microTime / 1000L;
                            if (time > song.getLength()) {
                                song.setLength(time);
                            }

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
        NoteblockInstrument[] instrumentList = INSTRUMENT_MAP.get(midiNoteblockInstrument);

        if (instrumentList != null) {
            for (NoteblockInstrument candidateNoteblockInstrument : instrumentList) {
                if (midiPitch >= candidateNoteblockInstrument.offset && midiPitch <= candidateNoteblockInstrument.offset + 24) {
                    instrument = candidateNoteblockInstrument;
                    break;
                }
            }
        }

        if (instrument == null) {
            return null;
        }

        int noteId = (midiPitch - instrument.offset) + instrument.instrumentId * 25;
        long time = microTime / 1000L;
        return new Note(noteId, time);
    }

    private static Note getMidiPercussionNote(int midiPitch, long microTime) {
        if (PERCUSSION_MAP.containsKey(midiPitch)) {
            int noteId = PERCUSSION_MAP.get(midiPitch);
            long time = microTime / 1000L;
            return new Note(noteId, time);
        }

        return null;
    }
}
