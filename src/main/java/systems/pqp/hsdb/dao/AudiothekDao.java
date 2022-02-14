package systems.pqp.hsdb.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import de.ard.sad.normdb.similarity.model.generic.GenericModel;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import de.ard.sad.normdb.similarity.model.generic.types.RadioPlayType;
import systems.pqp.hsdb.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class AudiothekDao {
    /**
     *
     */

    private static final Logger LOG = LogManager.getLogger(AudiothekDao.class.getName());
    private static final Config CONFIG = Config.Config();

    static final int RADIO_PLAY_ID = Integer.parseInt(CONFIG.getProperty("api.category.id"));
    static final String API_URL = CONFIG.getProperty("api.url");
    private static final String LIMIT = CONFIG.getProperty("api.limit","100000");
    private static final DataHarmonizer DATA_HARMONIZER = new DataHarmonizer();
    private static final DataExtractor DATA_EXTRACTOR = new DataExtractor();

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
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Map result = gson.fromJson(fetchAll(radioPlayId, apiUrl), Map.class);

            return genericObjectsFromJson(result);

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
        LOG.info("Fetch finished...Num Program-Sets: {}", resultMap.size());

        Map<String, GenericObject> results = DataExtractor.removeReadings(resultMap);
        results.putAll(DataExtractor.createVirtualRadioPlayOnProgramSet(results));

        LOG.info("Num Program-Sets after removeReadings(): {}", resultMap.size());

        return resultMap;
    }

    /**
     *
     * @param embeddedObject
     * @return
     */
    public static GenericObject genericObjectFromJson(Map embeddedObject){
        String id = String.valueOf(embeddedObject.get("id"));
        String title = String.valueOf(embeddedObject.get("title")).trim();

        String description = String.valueOf(embeddedObject.get("synopsis"));
        String duration = String.valueOf(embeddedObject.get("duration"));
        String publicationDt = String.valueOf(embeddedObject.get("publicationStartDateAndTime"));

        Map tracking = (LinkedTreeMap)embeddedObject.get("tracking");
        Map links = (LinkedTreeMap)embeddedObject.get("_links");
        Map embedded = (LinkedTreeMap)embeddedObject.get("_embedded");
        Map programSet = (LinkedTreeMap)embedded.get("mt:programSet");
        Map programSetLinks = (LinkedTreeMap)programSet.get("_links");


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

        HashSet<String> titles = new HashSet<>();
        titles.add(title);
        titles.add(DATA_EXTRACTOR.getTitleWithoutEpisodeOrSeason(title));
        //Überflüssige Klammerung entfernen

        if(title.indexOf("(") < title.indexOf(")")) {
            titles.add(title.replaceAll("\\(.*\\)", "").trim());
        }
        radioPlay.addDescriptionProperty(RadioPlayType.TITLE, new ArrayList<String>(titles));
        //radioPlay.addDescriptionProperty(RadioPlayType.BIO, description);
        //radioPlay.addDescriptionProperty(RadioPlayType.DESCRIPTION, description);
        radioPlay.addDescriptionProperty(RadioPlayType.DURATION, duration);
        try {
            radioPlay.addDescriptionProperty(RadioPlayType.PUBLICATION_DT, DATA_HARMONIZER.date(publicationDt));
        } catch (DataHarmonizerException e) {
            LOG.warn(e.getMessage(), e);
        }
        radioPlay.addDescriptionProperty(RadioPlayType.PUBLISHER, publisher);
        //radioPlay.addDescriptionProperty(RadioPlayType.BIO, description);
        radioPlay.addDescriptionProperty(RadioPlayType.DESCRIPTION, description);
        radioPlay.addDescriptionProperty(RadioPlayType.LINK, linkAudiothek);

        if( programSet.containsKey("title")){
            String programSetTitle = (String)programSet.get("title");
            radioPlay.addDescriptionProperty(RadioPlayType.PROGRAMSET_TITLE, programSetTitle);
        }

        if( programSet.containsKey("synopsis")){
            String programSetDescription = (String)programSet.get("synopsis");
            radioPlay.addDescriptionProperty(RadioPlayType.PROGRAMSET_DESCRIPTION, programSetDescription);
        }

        if( programSetLinks.containsKey("mt:sharing")){
            String programSetLink = (String)((LinkedTreeMap)programSetLinks.get("mt:sharing")).get("href");
            radioPlay.addDescriptionProperty(RadioPlayType.PROGRAMSET_LINK, programSetLink);
        }

        if( programSet.containsKey("id")){
            String programSetId = (String)programSet.get("id");
            radioPlay.addDescriptionProperty(RadioPlayType.PROGRAMSET_ID, programSetId);
        }

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

    /**
     * Methode fuer Chefe
     * Laedt und entpackt api.json.zip aus test/resources/api-examples und gibt genericObjectsFromJson zurueck
     * @param path String
     * @return Map<String, GenericObject>
     * @throws IOException
     */
    public static Map<String, GenericObject> genericObjectsFromDisk(String path) throws IOException {

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        File input = new File(loader.getResource(path).getFile());

        try (ZipFile zipFile = new ZipFile(input)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            if (entries.hasMoreElements()) {
                Gson gson = new Gson();
                ZipEntry entry = entries.nextElement();
                InputStream stream = zipFile.getInputStream(entry);
                Map<String, GenericObject> results = genericObjectsFromJson(gson.fromJson(new InputStreamReader(stream), Map.class));

                return results;
            }
        }

        return new HashMap<>();
    }

    String fetchAll(int radioPlayId, String apiUrl) throws IOException, ImportException {
        LOG.info("Fetching Radio-Plays from api...");
        URL url = new URL(apiUrl + "/" + radioPlayId + "?offset=0&limit="+LIMIT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        if( connection.getResponseCode() == 200 ) {
            String content = getContentFromInputStream(connection.getInputStream()).toString();
            connection.disconnect();
            if( LOG.isDebugEnabled() ) {
                LOG.debug(content);
            }
            /*Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Map result = gson.fromJson(content, Map.class);
            FileWriter writer = new FileWriter("api.json",false);
            gson.toJson(result, writer);
            writer.flush();*/

            return content;

        } else {
            throw new ImportException("Response-Code: " + connection.getResponseCode());
        }
    }
}
