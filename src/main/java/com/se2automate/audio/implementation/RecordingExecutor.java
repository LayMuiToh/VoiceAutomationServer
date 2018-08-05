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

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;

/**
 * created by Gaurav Tiwari
 * <p>
 * Thread for actually performing the recording
 */
public class RecordingExecutor implements Runnable, LineListener {
    private static final Logger LOG = LoggerFactory.getLogger(com.se2automate.audio.implementation.RecordingExecutor.class);
    // Java recommends the data buffer where we catch the data to be a certain ratio of the line buffer
    // We should not set our buffer the same size of the line buffer
    // Their example used 1/5 of the line buffer, this can be fine tuned if needed
    private static final int BUFFER_RATIO = 5;
    // Recording time in milliseconds
    private final TargetDataLine line;
    private final ByteArrayOutputStream out;
    private boolean recordingCompleted = false;
    private byte[] data;

    /**
     * The recording executor would need the line to start and the output stream to put the recorded data
     *
     * @param line A TargetDataLine object to call start() on
     * @param out  A ByteArrayOutputStream to write the data to
     */
    public RecordingExecutor(final TargetDataLine line, final ByteArrayOutputStream out) {
        this.line = line;
        this.out = out;
        this.data = new byte[line.getBufferSize() / BUFFER_RATIO];
    }

    /**
     * Run method for this runnable to put data from the line buffer to output stream
     */
    public void run() {
        int numBytesRead;

        // Begin audio capture.
        line.start();

        // Continue to record until interrupted
        while (!recordingCompleted) {
            // Read the next chunk of data from the TargetDataLine.
            numBytesRead = line.read(data, 0, data.length);
            // Save this chunk of data.
            out.write(data, 0, numBytesRead);
        }

        cleanup();
    }

    /**
     * Listener for line events.
     */
    @Override
    public void update(final LineEvent event) {
        LineEvent.Type eventType = event.getType();
        if (eventType == LineEvent.Type.START) {
            LOG.info("Recording started");
        } else if (eventType == LineEvent.Type.STOP) {
            recordingCompleted = true;
            LOG.info("Recording completed");
        }
    }

    /**
     * Method called after recording has stopped. We want to call the drain method.
     * The drain method will cause the mixer's remaining data to get delivered to the target data line's buffer.
     * If we don't drain the data, the captured sound might seem to be truncated prematurely at the end.
     */
    private void cleanup() {
        line.drain();
        int numBytesRead = line.read(data, 0, data.length);
        out.write(data, 0, numBytesRead);
    }
}
