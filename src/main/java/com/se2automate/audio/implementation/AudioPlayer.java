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

import com.se2automate.audio.design.AudioFileModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * created By Gaurav Tiwari
 * AudioPlayer class is used to play audio to any audio mixer
 * More info -  https://docs.oracle.com/javase/tutorial/sound/sampled-overview.html
 */
public final class AudioPlayer {

    private static final Logger LOG = LoggerFactory.getLogger(com.se2automate.audio.implementation.AudioPlayer.class);

    /**
     * Private constructor
     */
    private AudioPlayer() {
    }

    /**
     * Plays the audio file using the any supported mixer and if it is mp3, the default mp3 format.
     *
     * @param file - File object of the audio
     * @throws AudioException - exception in playing voice
     */
    public static void playAudio(final File file) throws AudioException {
        AudioFileModel audioFile = AudioUtility.getAudioFile(file);
        playAudio(audioFile, -1, audioFile.getAudioFormat(), audioFile.getAudioInputStream());
    }

    /**
     * Plays the audio file using the audio format passed in
     *
     * @param file        - File object of the audio
     * @param audioFormat - Format to use when instantiating the AudioInputStream
     * @throws AudioException - exception in playing voice
     */
    public static void playAudio(final File file, final AudioFormat audioFormat) throws AudioException {
        AudioFileModel audioFile = AudioUtility.getAudioFile(file);
        playAudio(audioFile, -1, audioFormat, audioFile.getAudioInputStream());
    }

    /**
     * Plays the audio using the default mp3 format or the wmv format to a specific mixer
     *
     * @param file        - File object of the audio
     * @param mixerNumber - The index number for the mixer array
     * @throws AudioException - exception in playing voice
     */
    public static void playAudio(final File file, final int mixerNumber) throws AudioException {
        AudioFileModel audioFile = AudioUtility.getAudioFile(file);
        playAudio(audioFile, mixerNumber, audioFile.getAudioFormat(), audioFile.getAudioInputStream());
    }

    /**
     * Plays the audio with a specific format and to a specific mixer
     *
     * @param file        - File object of the audio
     * @param mixerNumber - The index number for the mixer array
     * @param audioFormat - Format to use when instantiating the AudioInputStream
     * @throws AudioException - exception in playing voice
     */
    public static void playAudio(final File file, final int mixerNumber, final AudioFormat audioFormat) throws AudioException {
        AudioFileModel audioFile = AudioUtility.getAudioFile(file);
        playAudio(audioFile, mixerNumber, audioFormat, audioFile.getAudioInputStream());
    }

    /**
     * Plays the audio from the file path using the provided port number,
     * and throws an exception if the play fails.
     *
     * @param audioFile        - The File object of the audio
     * @param mixerNumber      - The index number for the mixer in the mixer array, pass -1 for default output
     * @param audioFormat      - Format to use
     * @param audioInputStream - The audio input stream
     * @throws AudioException - exception in playing voice
     */
    private static void playAudio(final AudioFileModel audioFile, final int mixerNumber, final AudioFormat audioFormat, final AudioInputStream audioInputStream) throws AudioException {

        Clip audioClip = null;

        try {
            DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);

            //If no valid integer is passed in, we use AudioSystem to get any line that is capable of playing a Clip
            //If there is a valid integer, get the mixer from the array of mixers and get the line that supports Clip
            audioClip = mixerNumber < 0 ? (Clip) AudioSystem.getLine(info) : (Clip) AudioUtility.getMixers()[mixerNumber].getLine(info);
            audioClip.open(audioInputStream);

            PlaybackExecutor playingExecutor = new PlaybackExecutor(audioClip);

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(playingExecutor);

            //Wait for executor service to finish
            executorService.shutdown();
            //Wait for up to the duration of the audio to terminate the thread
            executorService.awaitTermination(audioFile.getDuration(), TimeUnit.MICROSECONDS);

        } catch (LineUnavailableException e) {
            LOG.error(e.getMessage() + e.getCause());
            throw new AudioException("The audio line for playing is unavailable", e);
        } catch (IOException e) {
            LOG.error(e.getMessage() + e.getCause());
            throw new AudioException("Failed to play audio file", e);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            LOG.error(e.getMessage() + e.getCause());
            throw new AudioException("Invalid mixer number: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage() + e.getCause());
            throw new AudioException("The playing thread was interrupted during the playback", e);
        } finally {
            if (audioClip != null) {
                audioClip.close();
            }
        }
    }
}
