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

package in.co.gauravtiwari.voice.server.voice.serverresources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * created by Gaurav Tiwari
 */
public final class VoiceAutomationServerUtils {

    // you can define the custom path by setting variable voiceFileDir
    public static final String DOWNLOAD_BASE_DIR =
            (System.getProperty("voiceFileDir")==null||System.getProperty("voiceFileDir").isEmpty())?
            System.getProperty("java.io.tmpdir")+ File.separator
            :System.getProperty("voiceFileDir")+ File.separator;
    private static final Logger LOG = LoggerFactory.getLogger(VoiceAutomationServerUtils.class);
    // Use a specific user-agent in case of the server blocks robots by checking user-agent
    private static final String USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)";
    private static final Set<String> MP3_CONTENT_TYPES = new HashSet<>(
            Arrays.asList("audio/mpeg3", "audio/x-mpeg-3", "audio/mpeg", "audio/x-mpeg")
    );
    private static final Set<String> WAV_CONTENT_TYPES = new HashSet<>(
            Arrays.asList("audio/wav", "audio/x-wav", "audio/x-ms-wax")
    );

    /**
     * Private constructor for VoiceAutomationServerUtils.
     */
    private VoiceAutomationServerUtils() {

    }

    /**
     * Trust the certificate if the SSL certificate isn't trusted by Java.
     */
    private static void trustCertificate() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            final java.security.cert.X509Certificate[] certs, final String authType) {
                    }

                    public void checkServerTrusted(
                            final java.security.cert.X509Certificate[] certs, final String authType) {
                    }
                }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Downloads the voice file from given url, and throws an exception if the download fails.
     * Only mp3 and wav format voices are accepted, and the voice will be saved on local filesystem.
     *
     * @param url - url of the voice file
     * @return - filename of the voice file if download succeeds else null
     * @throws DownloadVoiceFileException - exception in downloading voice file
     */
    public static String downloadVoiceFile(final String url) throws DownloadVoiceFileException {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        trustCertificate();

        try {
            URL u = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);

            boolean redirect = false;
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_SEE_OTHER) {
                    redirect = true;
                }
            }

            if (redirect) {
                String location = connection.getHeaderField("Location");
                String cookies = connection.getHeaderField("Set-Cookie");

                connection = (HttpURLConnection) new URL(location).openConnection();
                connection.setRequestProperty("Cookie", cookies);
                connection.setRequestProperty("User-Agent", USER_AGENT);
                LOG.info("Redirect to URL: " + location);
            }

            String contentType = connection.getHeaderField("Content-Type").toLowerCase();
            String extension = "";

            if (MP3_CONTENT_TYPES.contains(contentType)) {
                extension = ".mp3";
            } else if (WAV_CONTENT_TYPES.contains(contentType)) {
                extension = ".wav";
            } else if (contentType.startsWith("audio/")) {
                throw new DownloadVoiceFileException("Unsupported extension of voice file");
            }

            String fileName = UUID.randomUUID().toString() + extension;
            inputStream = new BufferedInputStream(connection.getInputStream());
            outputStream = new FileOutputStream(DOWNLOAD_BASE_DIR + fileName);

            final int buffSize = 2048;  // buffer size set multiple of 1024 to make file system access efficient
            byte[] bytes = new byte[buffSize];

            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }

            return fileName;

        } catch (MalformedURLException | IndexOutOfBoundsException e) {
            throw new DownloadVoiceFileException("Malformed URL of a wav or mp3 voice file: " + e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new DownloadVoiceFileException("Voice file to download is not found: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new DownloadVoiceFileException(e.getMessage(), e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException | NullPointerException e) {
                LOG.warn(e.getMessage());
            }
        }
    }
}
