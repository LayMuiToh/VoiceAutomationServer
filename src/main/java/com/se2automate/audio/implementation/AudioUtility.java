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
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.util.Arrays;

/**
 * created by Gaurav Tiwari
 * This class if for printing / getting general info for the audio
 */
public final class AudioUtility {
    private static final Logger LOG = LoggerFactory.getLogger(com.se2automate.audio.implementation.AudioUtility.class);

    /**
     * private constructor
     */
    private AudioUtility() {
    }

    /**
     * Prints the information of all the audio formats of all the lines that a mixer supports
     *
     * @param mixer - the mixer to print out the info
     */
    private static void printMixerInfo(final Mixer mixer) {
        Line.Info[] sourceLineInfo = mixer.getSourceLineInfo();
        Line.Info[] targetLineInfo = mixer.getTargetLineInfo();

        System.out.print(String.format("\tSupported output line audio formats:%n"));
        for (int j = 0; j < sourceLineInfo.length; j++) {
            printLineInfo(sourceLineInfo[j]);
        }

        System.out.print(String.format("\tSupported input line audio formats:%n"));
        for (int j = 0; j < targetLineInfo.length; j++) {
            printLineInfo(targetLineInfo[j]);
        }
    }

    /**
     * Prints the information of the audio format that a specific line supports
     *
     * @param line - the line to print ouf the info
     */
    private static void printLineInfo(final Line.Info line) {
        System.out.print(String.format("\t\t* %s%n", line));
        try {
            AudioFormat[] audioFormats = ((DataLine.Info) line).getFormats();
            for (int k = 0; k < audioFormats.length; k++) {
                System.out.print(String.format("\t\t\t- %s%n", audioFormats[k]));
            }
        } catch (ClassCastException e) {
            System.out.print(String.format("\t\t\tNo supported audio formats%n"));
        }
    }

    /**
     * Utility static method that lists out the audio mixers information
     */
    public static void printAudioInfo() {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        System.out.print(String.format("=======Mixer Information=======%n"));

        for (int i = 0; i < mixerInfos.length; i++) {
            System.out.print(String.format("Mixer Number: %d : %s%n", i, mixerInfos[i].getName()));
            Mixer mixer = AudioSystem.getMixer(mixerInfos[i]);

            printMixerInfo(mixer);

            System.out.print(String.format("=====================%n"));
        }
    }

    /**
     * Given a File object, checks the extension and returns another object that implements the AudioFileModel interface
     * It is basically a factory for AudioFileModel
     *
     * @param audioFile - the File object that points to the audio file
     * @return - The correct AudioFile implementation based on the file extension
     * @throws AudioException - throws when the file extension has no corresponding AudioFile implementation
     */
    public static AudioFileModel getAudioFile(final File audioFile) throws AudioException {
        if (!audioFile.isFile()) {
            throw new AudioException("File object supplied is not a File");
        }

        String fileExtension = FilenameUtils.getExtension(audioFile.toString());

        switch (fileExtension) {
            case "wav":
                return new WavAudioFile(audioFile);
            case "mp3":
                return new MP3AudioFile(audioFile);
            default:
                throw new AudioException("The specified audio file format is not supported");
        }
    }

    /**
     * Returns the mixers available to this system.
     *
     * @return an array of mixer objects
     */
    public static Mixer[] getMixers() {
        return Arrays
                .stream(AudioSystem.getMixerInfo())
                .map(AudioSystem::getMixer)
                .toArray(Mixer[]::new);
    }
}
