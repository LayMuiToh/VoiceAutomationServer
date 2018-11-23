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

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;

/**
 * created By Gaurav Tiwari
 * <p>
 * AudioRecorder class is used to record audio coming in from an audio input
 */
public final class AudioRecorder {

    private static final Logger LOG = LoggerFactory.getLogger(AudioRecorder.class);

    // default record duration, in milliseconds
    private static final long DEFAULT_RECORD_TIME = 10000;  // 10 seconds

    // default recording audio formats
    private static final float DEFAULT_SAMPLE_RATE = 48000;
    private static final int DEFAULT_SAMPLE_SIZE_IN_BITS = 16;
    private static final int DEFAULT_CHANNELS = 1;
    private static final boolean DEFAULT_PCM_SIGNED = true;
    private static final boolean DEFAULT_BIG_ENDIAN = true;

    /**
     * Private constructor
     */
    private AudioRecorder() {
    }

    /**
     * Creates an AudioFormat object with the default parameters for this:
     *
     * @return the default audio format that the usb audio adapter supports
     */
    public static AudioFormat getDefaultAudioFormat() {
        AudioFormat format = new AudioFormat(DEFAULT_SAMPLE_RATE, DEFAULT_SAMPLE_SIZE_IN_BITS,
                DEFAULT_CHANNELS, DEFAULT_PCM_SIGNED, DEFAULT_BIG_ENDIAN);
        return format;
    }

    /**
     * The record method that uses all the defaults
     * This method records with the default audio format, time and at any available mixer
     *
     * @return an ByteArrayOutputStream object to which the user can use for saving or sending it to asr or any other process
     * @throws AudioException is thrown if any error occurs
     */
    public static ByteArrayOutputStream record() throws AudioException {
        return record(getDefaultAudioFormat(), DEFAULT_RECORD_TIME, -1);
    }

    /**
     * The record method that allows the user to provide the audio format they desire
     * The method will try to find any line on the system that supports the audio format
     *
     * @param format an audio format object that defines the parameters for the recording
     * @return an ByteArrayOutputStream object to which the user can use for saving or sending it to asr or any other process
     * @throws AudioException is thrown if any error occurs
     */
    public static ByteArrayOutputStream record(final AudioFormat format) throws AudioException {
        return record(format, DEFAULT_RECORD_TIME, -1);
    }

    /**
     * The record method that allows the user to provide the recording time
     *
     * @param recordTime the recording time in milliseconds eg. 1000 for 1 second
     * @return an ByteArrayOutputStream object to which the user can use for saving or sending it to asr or any other process
     * @throws AudioException is thrown if any error occurs
     */
    public static ByteArrayOutputStream record(final long recordTime) throws AudioException {
        return record(getDefaultAudioFormat(), recordTime, -1);
    }

    /**
     * The record method that allows the user to specify which mixer to use
     * This number can be seen from using the AudioUtility.printLineInfo
     *
     * @param mixerNumber the number of the mixer desired
     * @return an ByteArrayOutputStream object to which the user can use for saving or sending it to asr or any other process
     * @throws AudioException is thrown if any error occurs
     */
    public static ByteArrayOutputStream record(final int mixerNumber) throws AudioException {
        return record(getDefaultAudioFormat(), DEFAULT_RECORD_TIME, mixerNumber);
    }

    /**
     * The record method that allows the user to specify the audio format and the record time
     *
     * @param format     an audio format object that defines the parameters for the recording
     * @param recordTime the recording time in milliseconds eg. 1000 for 1 second
     * @return an ByteArrayOutputStream object to which the user can use for saving or sending it to asr or any other process
     * @throws AudioException is thrown if any error occurs
     */
    public static ByteArrayOutputStream record(final AudioFormat format, final long recordTime) throws AudioException {
        return record(format, recordTime, -1);
    }

    /**
     * The record method that allows the user to specify the audio format and the record time
     *
     * @param format      an audio format object that defines the parameters for the recording
     * @param mixerNumber the number of the mixer desired
     * @return an ByteArrayOutputStream object to which the user can use for saving or sending it to asr or any other process
     * @throws AudioException is thrown if any error occurs
     */
    public static ByteArrayOutputStream record(final AudioFormat format, final int mixerNumber) throws AudioException {
        return record(format, DEFAULT_RECORD_TIME, mixerNumber);
    }

    /**
     * The record method that allows the user to specify the audio format and the record time
     *
     * @param recordTime  the recording time in milliseconds eg. 1000 for 1 second
     * @param mixerNumber the number of the mixer desired
     * @return an ByteArrayOutputStream object to which the user can use for saving or sending it to asr or any other process
     * @throws AudioException is thrown if any error occurs
     */
    public static ByteArrayOutputStream record(final long recordTime, final int mixerNumber) throws AudioException {
        return record(getDefaultAudioFormat(), recordTime, mixerNumber);
    }

    /**
     * The record method that allows the user to specify the audio format, the record time and mixer number
     *
     * @param format      an audio format object that defines the parameters for the recording
     * @param recordTime  the recording time in milliseconds eg. 1000 for 1 second
     * @param mixerNumber the number of the mixer desired, if -1 then the method will find any line suitable
     * @return an ByteArrayOutputStream object to which the user can use for saving or sending it to asr or any other process
     * @throws AudioException is thrown if any error occurs
     */
    public static ByteArrayOutputStream record(final AudioFormat format, final long recordTime, final int mixerNumber) throws AudioException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        // checks if system supports the data line
        if (!AudioSystem.isLineSupported(info)) {
            LOG.error("Requested audio format is not supported not supported");
            throw new AudioException("Requested audio format is not supported not supported");
        }

        try {
            // If the passed in integer is -1, Use AudioSystem to obtain any line that can be used as a TargetDataLine
            // If there is a specific integer, then go to that specific mixer in the mixer array and obtain a TargetDataLine from that mixer
            TargetDataLine line = mixerNumber == -1 ? (TargetDataLine) AudioSystem.getLine(info) : (TargetDataLine) AudioUtility.getMixers()[mixerNumber].getLine(info);

            // Reserve the line for use
            line.open(format);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            RecordingExecutor recordingExecutor = new RecordingExecutor(line, out);
            Thread recordingThread = new Thread(recordingExecutor);

            // Listen to events emitted by line when it stops and closes
            line.addLineListener(recordingExecutor);

            recordingThread.start();
            Thread.sleep(recordTime);

            // Stop recording after the recordTime
            line.stop();

            // Close the line to free up the resource
            line.close();

            return out;
        } catch (LineUnavailableException e) {
            LOG.error(e.getMessage() + e.getCause());
            throw new AudioException("The audio line for recording is unavailable", e);
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.error(e.getMessage() + e.getCause());
            throw new AudioException("The specified audio mixer is invalid", e);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage() + e.getCause());
            throw new AudioException("Interrupted while waiting for recording to finish", e);
        }
    }
}
