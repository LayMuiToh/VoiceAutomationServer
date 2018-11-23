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

package in.co.gauravtiwari.voice.server.voice.design;


import in.co.gauravtiwari.voice.server.voice.messagemodel.VoiceAutomationMessage;

import javax.ws.rs.core.Response;

/**
 * created by Gaurav Tiwari
 * <p>
 * An interface for all the methods the Voice Automation server should support.
 */
public interface VoiceAutomationServerAPIModel {

    /**
     * Downloads the voice file from given url and saves it as a unique filename into filesystem.
     *
     * @param request The request from the client in Json format serialized
     *                into a VoiceAutomationMessage object by jackson
     * @return Response to the client, providing a status and message to
     * the requested service
     */
    Response load(VoiceAutomationMessage request);

    /**
     * Plays the voice to device.
     *
     * @param request The request from the client in Json format serialized
     *                into a VoiceAutomationMessage object by jackson
     * @return Response to the client, providing a status and message to
     * the requested service
     */
    Response play(VoiceAutomationMessage request);

    /**
     * Records the audio played from the device
     *
     * @param request The request from the client in Json format serialized
     *                into a VoiceAutomationMessage object by jackson
     * @return Response to the client, providing a status and message to
     * the rquested service
     */
    Response record(VoiceAutomationMessage request);
}
