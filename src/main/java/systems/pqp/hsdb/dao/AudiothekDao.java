package systems.pqp.hsdb.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import de.ard.sad.normdb.similarity.model.generic.GenericModel;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.pqp.hsdb.Config;
import systems.pqp.hsdb.DataHarmonizer;
import systems.pqp.hsdb.ImportException;
import systems.pqp.hsdb.RadioPlayType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AudiothekDao {
    /**
     *
     */

    private static final Logger LOG = LoggerFactory.getLogger(AudiothekDao.class.getName());
    private static final Config CONFIG = Config.Config();

    private static final int RADIO_PLAY_ID = Integer.parseInt(CONFIG.getProperty("api.category.id"));
    private static final String API_URL = CONFIG.getProperty("api.url");
    private static final String LIMIT = CONFIG.getProperty("api.limit","100000");
    private static final DataHarmonizer DATA_HARMONIZER = new DataHarmonizer();

    public AudiothekDao(){}

    /**
     *
     * @return
     * @throws ImportException
     */
    public Map<String, GenericObject> getRadioPlays() throws ImportException {
        return getRadioPlays(RADIO_PLAY_ID, API_URL);
    }

    /**
     *
     * @param radioPlayId
     * @param apiUrl
     * @return
     * @throws ImportException
     */
    public Map<String, GenericObject> getRadioPlays(int radioPlayId, String apiUrl) throws ImportException {
        try {
            URL url = new URL(apiUrl + "/" + radioPlayId + "?offset=0&limit="+LIMIT);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if( connection.getResponseCode() == 200 ) {
                String content = getContentFromInputStream(connection.getInputStream()).toString();
                connection.disconnect();
                if( LOG.isDebugEnabled() ) {
                    LOG.debug(content);
                }
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Map result = gson.fromJson(content, Map.class);
                FileWriter writer = new FileWriter("api.json",false);
                gson.toJson(result, writer);
                writer.flush();

                return genericObjectsFromJson(result);

            } else {
                throw new ImportException("Response-Code: " + connection.getResponseCode());
            }

        } catch (IOException ioException){
            if( LOG.isDebugEnabled() ){
                LOG.debug(ioException.getMessage(), ioException);
            }
            throw new ImportException("Import failed!", ioException);
        }
    }

    public static Map<String, GenericObject> genericObjectsFromJson(Map apiResponse){
        Map<String, GenericObject> resultMap = new HashMap<>();
        ((ArrayList<LinkedTreeMap>)((LinkedTreeMap)apiResponse.get("_embedded")).get("mt:items")).forEach(
                entry -> resultMap.put((String) entry.get("id"),(genericObjectFromJson(entry)))
        );

        LOG.info("Num Program-Sets: {}", resultMap.size());

        return resultMap;
    }

    /**
     *
     * @param embeddedObject
     * @return
     */
    public static GenericObject genericObjectFromJson(Map embeddedObject){
        String id = String.valueOf(embeddedObject.get("id"));
        String title = String.valueOf(embeddedObject.get("title"));
        List<String> involvedNames = new ArrayList<>();

        //Überflüssige Klammerung entfernen
        if(title.indexOf("(") < title.indexOf(")")) {
            title = title.replaceAll("\\(.*\\)", "").trim();
        }

        //Doppelpunkt Prefix extrahieren & ggf. als involved Person hinzufügen
        int idx = title.indexOf(":")+1;
        if(idx > 0) {
            String prefix = title.substring(0,idx-1).trim();
            int idxWhitespace = prefix.indexOf(" ");
            if(idxWhitespace > -1 && prefix.length()-(idxWhitespace+1) > 2) {
                involvedNames.add(prefix);
            }
            title = title.substring(idx).trim();
        }

        //Abschließenden Suffix entfernen
        idx = title.indexOf("/");
        if(idx > 0) {
            title = title.substring(0,idx).trim();
        }

        String description = String.valueOf(embeddedObject.get("synopsis"));
        String duration = String.valueOf(embeddedObject.get("duration"));
        String publicationDt = String.valueOf(embeddedObject.get("publicationStartDateAndTime"));

        Map tracking = (LinkedTreeMap)embeddedObject.get("tracking");
        Map links = (LinkedTreeMap)embeddedObject.get("_links");
        Map embedded = (LinkedTreeMap)embeddedObject.get("_embedded");
        Map programSet = (LinkedTreeMap)embedded.get("mt:programSet");
        Map programSetLinks = (LinkedTreeMap)programSet.get("_links");

        String showTitle = "";
        if( programSet.containsKey("title")){
            showTitle = (String)programSet.get("title");
        }

        //String linkAudiothek = (String)(((LinkedTreeMap)programSetLinks.get("mt:sharing")).get("href"));
        String linkAudiothek = (String)(((LinkedTreeMap)links.get("mt:sharing")).get("href"));

        List<String> publisher = new ArrayList<>();
        if(null != ((LinkedTreeMap)programSet.get("_embedded")).get("mt:publicationService")) {
            publisher.add((String) ((LinkedTreeMap) ((LinkedTreeMap) programSet.get("_embedded")).get("mt:publicationService")).get("title"));
        }
        if(((LinkedTreeMap)tracking.get("play")).containsKey("source")){
            publisher.add((String) ((LinkedTreeMap)tracking.get("play")).get("source"));
        }
        if(((LinkedTreeMap)tracking.get("play")).containsKey("lra")){
            publisher.add((String) ((LinkedTreeMap)tracking.get("play")).get("lra"));
        }

        GenericModel genericModel = new GenericModel(RadioPlayType.class);
        GenericObject radioPlay = new GenericObject(genericModel,id);

        radioPlay.addDescriptionProperty(RadioPlayType.TITLE, title);
        radioPlay.addDescriptionProperty(RadioPlayType.SHOW_TITLE, showTitle);
        radioPlay.addDescriptionProperty(RadioPlayType.BIO, description);
        radioPlay.addDescriptionProperty(RadioPlayType.DESCRIPTION, description);
        radioPlay.addDescriptionProperty(RadioPlayType.DURATION, duration);
        radioPlay.addDescriptionProperty(RadioPlayType.PUBLICATION_DT, DATA_HARMONIZER.date(publicationDt));
        radioPlay.addDescriptionProperty(RadioPlayType.PUBLISHER, publisher);
        radioPlay.addDescriptionProperty(RadioPlayType.BIO, description);
        radioPlay.addDescriptionProperty(RadioPlayType.DESCRIPTION, description);
        radioPlay.addDescriptionProperty(RadioPlayType.LINK_AUDIOTHEK, linkAudiothek);
        radioPlay.addDescriptionProperty(RadioPlayType.LONG_TITLE, title);
        if(involvedNames.size()>0)
            radioPlay.addDescriptionProperty(RadioPlayType.PERSON_INVOLVED, involvedNames);


        return radioPlay;
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
}
