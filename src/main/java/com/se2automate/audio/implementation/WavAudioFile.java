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

package com.se2automate.audio.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**
 * created by Gaurav Tiwari
 * <p>
 * An implementation of the AudioFile abstract class for the wav file extension
 */
public class WavAudioFile extends AudioFile {
    private static final Logger LOG = LoggerFactory.getLogger(com.se2automate.audio.implementation.WavAudioFile.class);
    private static final int SECONDS_TO_MICROSECONDS = 1000000;

    /**
     * Constructor takes in an audioFile and generates the correct AudioFormat, AudioInputStream and duration objects
     *
     * @param audioFile - the File object for the actual audio file
     * @throws AudioException - thrown when it fails to initialize one of the objects
     */
    public WavAudioFile(final File audioFile) throws AudioException {
        super(audioFile);
        setAudioInputStream(getBaseAudioInputStream(audioFile));
        setAudioFormat(getAudioInputStream().getFormat());
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
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getAudioFile());
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            double durationInSeconds = (frames + 0.0) / format.getFrameRate();
            return (long) (durationInSeconds * SECONDS_TO_MICROSECONDS);
        } catch (UnsupportedAudioFileException e) {
            LOG.info(e.getMessage() + e.getCause());
            throw new AudioException("Unsupported audio file", e);
        } catch (IOException e) {
            LOG.info(e.getMessage() + e.getCause());
            throw new AudioException("Cannot open audio file", e);
        }
    }
}
