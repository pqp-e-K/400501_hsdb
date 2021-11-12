package systems.pqp.hsdb;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import de.ard.sad.normdb.similarity.model.generic.GenericModel;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class ImportService {
    /**
     *
     */

    private static final Logger LOG = LoggerFactory.getLogger(ImportService.class.getName());

    private static final int RADIO_PLAY_ID = 42914712;
    private static final String API_URL = "https://api.ardaudiothek.de/editorialcategories";

    private ImportService(){}

    /**
     *
     * @return
     * @throws ImportException
     */
    static List<GenericObject> getRadioPlays() throws ImportException {
        return getRadioPlays(RADIO_PLAY_ID, API_URL);
    }

    /**
     *
     * @param radioPlayId
     * @param apiUrl
     * @return
     * @throws ImportException
     */
    static List<GenericObject> getRadioPlays(int radioPlayId, String apiUrl) throws ImportException {
        try {
            URL url = new URL(apiUrl + "/" + radioPlayId + "?offset=0&limit=1000000");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if( connection.getResponseCode() == 200 ) {
                String content = getContentFromInputStream(connection.getInputStream()).toString();
                connection.disconnect();
                if( LOG.isDebugEnabled() ) {
                    LOG.debug(content);
                }
                Gson gson = new Gson();
                Map result = gson.fromJson(content, Map.class);

                List<GenericObject> resultList = new ArrayList<>();
                ((ArrayList<LinkedTreeMap>)((LinkedTreeMap)result.get("_embedded")).get("mt:items")).forEach(
                        entry -> resultList.add(genericObjectFromJson(entry, UUID.randomUUID().toString()))
                );

                LOG.info("Num Program-Sets: {}", resultList.size());

                return resultList;

            } else {
                throw new ImportException("Response-Code: " + connection.getResponseCode());
            }

        } catch (IOException | ImportException ioException){
            LOG.error(ioException.getMessage());
            if( LOG.isDebugEnabled() ){
                LOG.debug(ioException.getMessage(), ioException);
            }
            throw new ImportException(ioException.getMessage());
        }
    }

    /**
     *
     * @param embeddedObject
     * @param uniqueId
     * @return
     */
    static GenericObject genericObjectFromJson(LinkedTreeMap embeddedObject, String uniqueId){
        String id = String.valueOf(embeddedObject.get("id"));
        String title = String.valueOf(embeddedObject.get("title"));
        String description = String.valueOf(embeddedObject.get("synopsis"));
        String duration = String.valueOf(embeddedObject.get("duration"));
        String publicationDt = String.valueOf(embeddedObject.get("publicationStartDateAndTime"));
        String linkAudiothek = String.valueOf(((LinkedTreeMap)((LinkedTreeMap)embeddedObject.get("_links")).get("mt:sharing")).get("href"));
        String linkBestQuality="";
        if(null != ((LinkedTreeMap)((LinkedTreeMap)embeddedObject.get("_links")).get("mt:bestQualityPlaybackUrl")).get("href"))
        {
            linkBestQuality = String.valueOf(((LinkedTreeMap) ((LinkedTreeMap) embeddedObject.get("_links")).get("mt:bestQualityPlaybackUrl")).get("href"));
        }

        GenericModel genericModel = new GenericModel(RadioPlayType.class);
        GenericObject radioPlay = new GenericObject(genericModel,uniqueId);

        radioPlay.addDescriptionProperty(RadioPlayType.ID, id);
        radioPlay.addDescriptionProperty(RadioPlayType.TITLE, title);
        radioPlay.addDescriptionProperty(RadioPlayType.DURATION, duration);
        radioPlay.addDescriptionProperty(RadioPlayType.PUBLICATION_DT, publicationDt);
        radioPlay.addDescriptionProperty(RadioPlayType.DESCRIPTION, description);
        radioPlay.addDescriptionProperty(RadioPlayType.LINK_BEST_QUALITY, linkBestQuality);
        radioPlay.addDescriptionProperty(RadioPlayType.LINK_AUDIOTHEK, linkAudiothek);


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
