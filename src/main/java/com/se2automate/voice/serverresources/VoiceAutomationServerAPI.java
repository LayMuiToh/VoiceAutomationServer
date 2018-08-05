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

package com.se2automate.voice.serverresources;

import com.se2automate.audio.implementation.AudioException;
import com.se2automate.audio.implementation.AudioPlayer;
import com.se2automate.audio.implementation.AudioRecorder;
import com.se2automate.voice.design.VoiceAutomationServerAPIModel;
import com.se2automate.voice.messagemodel.VoiceAutomationMessage;
import com.se2automate.voice.messagemodel.VoiceAutomationMessageStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.File;

import static com.se2automate.voice.serverresources.VoiceAutomationServerUtils.DOWNLOAD_BASE_DIR;

/**
 * A resource class that gets loaded in voice.
 * This resource class provides the mapping from API paths to functions.
 * All APIs accept HTTP POST requests with Json and returns a response in Json.
 * The response is VoiceAutomationMessage and serialized into a Json payload.
 */
@Path("/")
public class VoiceAutomationServerAPI implements VoiceAutomationServerAPIModel {
    private static final Logger LOG = LoggerFactory.getLogger(VoiceAutomationServerAPI.class);

    /**
     * The load voice file from url API.
     */
    @Override
    @POST
    @Path("load")
    @Produces(MediaType.APPLICATION_JSON)
    public Response load(final VoiceAutomationMessage request) {
        LOG.info("entered load controller");
        String voiceFileUrl = request.getVoiceFilePath();
        VoiceAutomationMessage vam;
        LOG.info("file url {}", voiceFileUrl);
        try {
            String fileName = VoiceAutomationServerUtils.downloadVoiceFile(voiceFileUrl);
            vam = new VoiceAutomationMessage(VoiceAutomationMessageStatus.SUCCESS, "Loaded", fileName);
            LOG.info("loaded file {}", fileName);
        } catch (DownloadVoiceFileException e) {
            vam = new VoiceAutomationMessage(VoiceAutomationMessageStatus.FAIL, e.getMessage());
            LOG.info("exception loading file");
        }

        return Response.status(Response.Status.OK).entity(vam).build();
    }

    /**
     * The play voice to device API.
     */
    @Override
    @POST
    @Path("play")
    @Produces(MediaType.APPLICATION_JSON)
    public Response play(final VoiceAutomationMessage request) {
        VoiceAutomationMessage vam;
        String fileName = request.getVoiceFilePath();
        String portNumberStr = System.getProperty("audioPort");

        if (portNumberStr != null && !portNumberStr.isEmpty()) {
            try {
                int portNumber = Integer.parseInt(portNumberStr);
                AudioPlayer.playAudio(new File(DOWNLOAD_BASE_DIR + fileName), portNumber);
                vam = new VoiceAutomationMessage(VoiceAutomationMessageStatus.SUCCESS, "Played");
            } catch (NumberFormatException e) {
                vam = new VoiceAutomationMessage(VoiceAutomationMessageStatus.FAIL, "Unable to convert audio port to integer");
            } catch (AudioException e) {
                vam = new VoiceAutomationMessage(VoiceAutomationMessageStatus.FAIL, e.getMessage());
            }
        } else {
            LOG.info("No audio port set, using default 0");
            try {
                AudioPlayer.playAudio(new File(DOWNLOAD_BASE_DIR + fileName));
                vam = new VoiceAutomationMessage(VoiceAutomationMessageStatus.SUCCESS, "Played");
            } catch (AudioException e) {
                vam = new VoiceAutomationMessage(VoiceAutomationMessageStatus.FAIL, e.getMessage());
            }
        }
        return Response.status(Response.Status.OK).entity(vam).build();
    }

    /**
     * The record audio from device api
     */
    @Override
    @POST
    @Path("record")
    @Produces(MediaType.APPLICATION_JSON)
    public Response record(final VoiceAutomationMessage request) {
        VoiceAutomationMessage vam;
        String portNumberStr = System.getProperty("audioPort");
        long recodingDuration = request.getRecordingDuration();
        if (portNumberStr != null && !portNumberStr.isEmpty()) {
            try {
                int portNumber = Integer.parseInt(portNumberStr);
                ByteArrayOutputStream byteArrayOutputStream = AudioRecorder.record(recodingDuration, portNumber);
                vam = new VoiceAutomationMessage(VoiceAutomationMessageStatus.SUCCESS, "Recorded");
                vam.setAudioData(byteArrayOutputStream.toByteArray());
                LOG.info(vam.getVoiceFilePath());
            } catch (NumberFormatException e) {
                vam = new VoiceAutomationMessage(VoiceAutomationMessageStatus.FAIL, "Unable to convert audio port to integer");
            } catch (AudioException e) {
                vam = new VoiceAutomationMessage(VoiceAutomationMessageStatus.FAIL, e.getMessage());
            }
        } else {
            LOG.info("No audio port set, using default 0");
            try {
                ByteArrayOutputStream byteArrayOutputStream = AudioRecorder.record(recodingDuration);
                vam = new VoiceAutomationMessage(VoiceAutomationMessageStatus.SUCCESS, "Recorded");
                vam.setAudioData(byteArrayOutputStream.toByteArray());
                LOG.info(vam.getVoiceFilePath());
            } catch (AudioException e) {
                vam = new VoiceAutomationMessage(VoiceAutomationMessageStatus.FAIL, e.getMessage());
            }
        }

        return Response.status(Response.Status.OK).entity(vam).build();
    }
}
