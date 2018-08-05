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

package com.se2automate.audio.design;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.File;

/**
 * created by Gaurav Tiwari
 * <p>
 * An interface to describe methods which a certain file type will need to implement for the audio file to be played
 * by the AudioPlayer class.
 */
public interface AudioFileModel {
    /**
     * Getter method for the File object from this Audio File was instantiated with
     *
     * @return File object for the audio
     */
    File getAudioFile();

    /**
     * Getter method for an AudioInputStream for the audio file
     *
     * @return AudioInputStream object for the audio file
     */
    AudioInputStream getAudioInputStream();

    /**
     * Getter for an AudioFormat object that describes the audio file
     *
     * @return AudioFormat obejct that describes the audio file
     */
    AudioFormat getAudioFormat();

    /**
     * Getter for the duration of the audio in microseconds
     *
     * @return The duration of the audio in microseconds
     */
    long getDuration();
}
