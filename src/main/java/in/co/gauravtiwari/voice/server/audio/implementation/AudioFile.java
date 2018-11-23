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

import in.co.gauravtiwari.voice.server.audio.design.AudioFileModel;
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
 * An abstract class that has the default constructor to wrap the File object used to instantiate the audio file
 */
public abstract class AudioFile implements AudioFileModel {
    private static final Logger LOG = LoggerFactory.getLogger(AudioFile.class);
    private File audioFile;
    private AudioFormat audioFormat;
    private AudioInputStream audioInputStream;
    private long duration;

    /**
     * Constructor to set the passed in file object as a member
     *
     * @param audioFile - the File object that points to the audio file
     * @throws AudioException - throws when the File is a directory
     */
    public AudioFile(final File audioFile) throws AudioException {
        this.audioFile = audioFile;
    }

    /**
     * A base method that returns the AudioInputStream simply from the AudioSystem.getAudioInputStream call
     * Wraps some of the exceptions in AudioException
     *
     * @param audioFile - the File object that points to the audio file
     * @return An AudioInputStream object instantiated by tby the AudioSystem
     * @throws AudioException - throws when the file can't be opened or the audio file type is unsupported
     */
    public AudioInputStream getBaseAudioInputStream(final File audioFile) throws AudioException {
        try {
            return AudioSystem.getAudioInputStream(audioFile);
        } catch (UnsupportedAudioFileException e) {
            LOG.info(e.getMessage() + e.getCause());
            throw new AudioException("Unsupported audio file", e);
        } catch (IOException e) {
            LOG.info(e.getMessage() + e.getCause());
            throw new AudioException("Cannot open audio file", e);
        }
    }

    @Override
    public AudioFormat getAudioFormat() {
        return this.audioFormat;
    }

    /**
     * Setter for audio format
     *
     * @param audioFormat - the audio format to set
     */
    protected void setAudioFormat(final AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
    }

    @Override
    public AudioInputStream getAudioInputStream() {
        return this.audioInputStream;
    }

    /**
     * Setter for AudioInputStream
     *
     * @param audioInputStream - the AudioInputStream to set
     */
    protected void setAudioInputStream(final AudioInputStream audioInputStream) {
        this.audioInputStream = audioInputStream;
    }

    @Override
    public long getDuration() {
        return this.duration;
    }

    /**
     * Setter for duration
     *
     * @param duration - the duration to set
     */
    protected void setDuration(final long duration) {
        this.duration = duration;
    }

    @Override
    public File getAudioFile() {
        return audioFile;
    }
}
