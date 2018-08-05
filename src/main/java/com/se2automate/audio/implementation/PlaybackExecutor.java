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

import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

/**
 * created by Gaurav Tiwari
 * <p>
 * A Thread for actually performing the playback
 */
public class PlaybackExecutor implements Runnable, LineListener {
    private static final Logger LOG = LoggerFactory.getLogger(com.se2automate.audio.implementation.PlaybackExecutor.class);
    private static final int SLEEP_TIME_MILLIS = 100;
    private Clip audioClip;
    private boolean playCompleted;

    /**
     * The constructor sets the audio clip and playCompleted boolean
     * It is assumed that the clip has already opened an audio input stream
     *
     * @param audioClip - the audioClip to be played
     */
    public PlaybackExecutor(final Clip audioClip) {
        this.audioClip = audioClip;
        this.playCompleted = false;
    }

    /**
     * The run method simply calls start on the Clip to start playback and binds itself as a listener
     * in order to detect when the playback finishes. Since there could be multiple situations that could
     * wake the thread from wait(), the recommended usage for wait is to continue waiting until a condition
     * has been met. Java recommends wait to always be used in a loop:
     * https://docs.oracle.com/javase/7/docs/api/java/lang/Object.html#wait()
     */
    public void run() {
        audioClip.addLineListener(this);
        audioClip.start();
        synchronized (this) {
            try {
                while (!playCompleted) {
                    wait(SLEEP_TIME_MILLIS);
                }
            } catch (Exception e) {
                LOG.error("Interrupted while playback is unfinished");
            }
        }
    }

    @Override
    public void update(final LineEvent event) {
        LineEvent.Type eventType = event.getType();
        if (eventType == LineEvent.Type.START) {
            LOG.info("Playback started");
        } else if (eventType == LineEvent.Type.STOP) {
            LOG.info("Playback completed");
            synchronized (this) {
                audioClip.removeLineListener(this);
                playCompleted = true;
                this.notify();
            }
        }
    }
}
