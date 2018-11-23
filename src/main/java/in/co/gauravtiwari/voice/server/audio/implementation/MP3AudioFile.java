/**
 Copyright [2018] [Gaurav Tiwari]

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package in.co.gauravtiwari.voice.server.audio.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * created by Gaurav Tiwari
 * <p>
 * An implementation of the AudioFile abstract class for the mp3 file extension
 */
public class MP3AudioFile extends AudioFile {
    private static final Logger LOG = LoggerFactory.getLogger(MP3AudioFile.class);

    /**
     * Constructor takes in an audioFile and generates the correct AudioFormat, AudioInputStream and duration objects
     *
     * @param audioFile - the File object for the actual audio file
     * @throws AudioException - thrown when it fails to initialize one of the objects
     */
    public MP3AudioFile(final File audioFile) throws AudioException {
        super(audioFile);
        AudioInputStream baseAudioInputStream = getBaseAudioInputStream(audioFile);
        setAudioFormat(initializeAudioFormat(baseAudioInputStream));
        setAudioInputStream(initializeAudioInputStream(getAudioFormat(), baseAudioInputStream));
        setDuration(initializeDuration());
    }

    /**
     * Calculates the duration of the audio file
     *
     * @return the duration of the audio file in microseconds
     * @throws AudioException throws when the audio file is unsupported or ioexception
     */
    private long initializeDuration() throws AudioException {
        try {
            AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(getAudioFile());
            // Tritonus SPI compliant audio file format.
            Map properties = ((TAudioFileFormat) audioFileFormat).properties();
            // duration is in microseconds
            Long duration = (Long) properties.get("duration");
            return duration;
        } catch (UnsupportedAudioFileException e) {
            LOG.info(e.getMessage() + e.getCause());
            throw new AudioException("Unsupported audio file", e);
        } catch (IOException e) {
            LOG.info(e.getMessage() + e.getCause());
            throw new AudioException("Cannot open audio file", e);
        }
    }

    /**
     * Creates the correct AudioFormat for this mp3 audio file
     *
     * @param baseAudioInputStream - the input stream is needed to get the base format
     * @return AudioFormat that correctly describes this mp3 audio file
     */
    private AudioFormat initializeAudioFormat(final AudioInputStream baseAudioInputStream) {
        AudioFormat baseFormat = baseAudioInputStream.getFormat();
        final int sampleSizeInBits = 16;
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                sampleSizeInBits,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                baseFormat.isBigEndian());
    }

    /**
     * Returns a new input stream with the correct audio format object after initializeAudioFormat is called
     *
     * @param audioFormat          - the correct audio format object
     * @param baseAudioInputStream - the base audio input stream
     * @return a new input stream with the correct audio format
     */
    private AudioInputStream initializeAudioInputStream(final AudioFormat audioFormat, final AudioInputStream baseAudioInputStream) {
        return AudioSystem.getAudioInputStream(audioFormat, baseAudioInputStream);
    }
}
