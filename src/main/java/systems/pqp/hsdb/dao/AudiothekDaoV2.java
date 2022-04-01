package systems.pqp.hsdb.dao;

import com.google.common.net.HttpHeaders;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import systems.pqp.hsdb.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AudiothekDaoV2 {
    /**
     *
     */

    private static final Logger LOG = LogManager.getLogger(AudiothekDaoV2.class.getName());
    private static final Config CONFIG = Config.Config();

    static final String API_URL = CONFIG.getProperty("api.v2.url");
    static final String RESOURCE = CONFIG.getProperty("api.v2.resource");
    static final String PROXY_HOST = CONFIG.getProperty("api.v2.proxy.url", null);
    static final String PROXY_PORT = CONFIG.getProperty("api.v2.proxy.port", null);
    static final String LIMIT = CONFIG.getProperty("api.v2.limit","100000");

    String userPass = "deliver:J9Xsbzg4SkHvjvrgpx*c";
    String authorizationString = "Basic " + Base64.encodeBase64String(userPass.getBytes(StandardCharsets.UTF_8));

    public AudiothekDaoV2(){}

    /**
     *
     * @return
     * @throws ImportException
     */
    public Map<String, GenericObject> getRadioPlays() throws ImportException {
        return getRadioPlays(API_URL);
    }

    /**
     *
     * @param apiUrl
     * @return
     * @throws ImportException
     */
    public Map<String, GenericObject> getRadioPlays(String apiUrl) throws ImportException {
        return null;
    }



    /**
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    private static StringBuilder getContentFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(inputStream));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content;
    }

    String fetchAll() throws ImportException, IOException {
        return this.fetchAll(API_URL, RESOURCE);
    }

    String fetchAll(String apiUrl, String resource) throws IOException, ImportException {
        LOG.info("Fetching Radio-Plays from api...");
        URL url = new URL(apiUrl + "/" + resource);
        HttpURLConnection connection;
        if(null != PROXY_HOST) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, Integer.parseInt(PROXY_PORT)));
            connection = (HttpURLConnection) url.openConnection(proxy);
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }
        LOG.info("Using proxy? {}", connection.usingProxy());
        connection.setRequestProperty(HttpHeaders.AUTHORIZATION, authorizationString);
        if( connection.getResponseCode() == 200 ) {
            String content = getContentFromInputStream(connection.getInputStream()).toString();
            connection.disconnect();
            if( LOG.isDebugEnabled() ) {
                LOG.debug(content);
            }
            return content;

        } else {
            throw new ImportException("Response-Code: " + connection.getResponseCode());
        }
    }
}
