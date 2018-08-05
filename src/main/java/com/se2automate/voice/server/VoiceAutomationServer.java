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

package com.se2automate.voice.server;

import com.se2automate.voice.design.VoiceAutomationServerModel;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.glassfish.grizzly.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of a http server for Voice Automation.
 * Set -DVoiceAutomationServerPort in VM options to specify port, defaults to 9010.
 * The server will try to obtain hostname and begin listening on specified port.
 * If the server cannot obtain the hostname, it defaults to localhost.
 */

/**
 * created by Gaurav Tiwari
 */

public class VoiceAutomationServer implements VoiceAutomationServerModel {
    private static final Logger LOG = LoggerFactory.getLogger(VoiceAutomationServer.class);
    //According to browserstack, best port to use are
    //Any port between 9000 and 9100
    //Any port between 9200 and 9400
    private static final int DEFAULT_PORT = 9090;
    private static final URI BASE_URI = UriBuilder.fromUri("http://" + getHostName() + "/").port(getPort(DEFAULT_PORT)).build();
    private HttpServer httpServer;

    /**
     * getPort will retrieve the port from -DVoiceAutomationPort and start HTTP server listening at that port
     * if no port number is supplied, the port will be set to defaultPort, which is the parameter
     *
     * @param defaultPort - the default port number to listen to if no port is supplied
     * @return the port number to listen on
     */
    private static int getPort(final int defaultPort) {
        String port = System.getProperty("VoiceAutomationServerPort");
        if (null != port) {
            try {
                LOG.info("Specific port supplied, using port " + port);
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
                LOG.info("No specific port supplied, using default port 9010");
            }
        }
        return defaultPort;
    }

    /**
     * This method returns the hostname of the current machine, if hostname cannot be obtained, it uses localhost
     *
     * @return hostname of the machine or the default localhost
     */
    private static String getHostName() {
        String hostName = "localhost";
        try {
            hostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            LOG.warn("Cannot obtain hostname, defaulting to localhost");
        }
        return hostName;
    }

    /**
     * Main method to keep the server running.
     *
     * @param args - inputted arguments
     */
    public static void main(String[] args) {
        VoiceAutomationServer vaServer = new VoiceAutomationServer();
        try {
            vaServer.startServer();
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is the getter for the uri that the server can be reached at, with the port num
     *
     * @return uri to reach server
     */
    public String getURI() {
        return BASE_URI.toString();
    }

    /**
     * Creates an instance of HttpServer by loading resources in serverresources.
     */
    public void startServer() {
        LOG.info("Initializing Voice Automation server");
        ResourceConfig rc = new PackagesResourceConfig("com.se2automate.voice.serverresources");
        //Create config map and add to resource config to turn
        // on json serialization for a POJO
        Map<String, Object> config = new HashMap<String, Object>();
        config.put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
        rc.setPropertiesAndFeatures(config);
        try {
            this.httpServer = GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
            LOG.info("Voice Automation server started at " + BASE_URI);
            this.httpServer.start();
        } catch (IOException ex) {
            LOG.error("Voice Automation server failed to start: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
