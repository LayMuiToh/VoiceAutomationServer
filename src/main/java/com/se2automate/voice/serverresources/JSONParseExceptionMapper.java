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

import com.se2automate.voice.messagemodel.VoiceAutomationMessage;
import com.se2automate.voice.messagemodel.VoiceAutomationMessageStatus;
import org.codehaus.jackson.JsonParseException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Implementation of an Exception Mapper.
 * Registered this class by tagging it with @Provider.
 * Jersey uses this class to create an HTTP response when the exception occurs.
 * JsonParseException occurs when the HTTP request body is not a valid json.
 */

/**
 * created by Gaurav Tiwari
 */

@Provider
public class JSONParseExceptionMapper implements
        ExceptionMapper<JsonParseException> {

    /**
     * Creates a response for the client when JsonParseException occurs.
     *
     * @param jpe the JsonParseException object
     * @return Response to client, providing a FAILED status and the exception
     */
    public Response toResponse(final JsonParseException jpe) {
        VoiceAutomationMessage vam = new VoiceAutomationMessage(VoiceAutomationMessageStatus.FAIL, jpe.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).entity(vam).build();
    }
}
