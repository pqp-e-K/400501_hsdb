package systems.pqp.hsdb;


import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import de.ard.sad.normdb.similarity.model.generic.GenericModel;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ImportService {

    private static final Logger LOG = LoggerFactory.getLogger(ImportService.class.getName());

    private static final int HOERSPIEL_ID = 42914712;
    private static final String API_URL = "https://api.ardaudiothek.de/editorialcategories";

    private ImportService(){}

    static List<GenericObject> getRadioPlays() throws ImportException {
        try {
            URL url = new URL(API_URL + "/" + HOERSPIEL_ID);
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
                ((ArrayList<LinkedTreeMap>)((LinkedTreeMap)result.get("_embedded")).get("mt:programSets")).forEach(
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

    static GenericObject genericObjectFromJson(LinkedTreeMap embeddedObject, String uniqueId){

        String id = String.valueOf(embeddedObject.get("id"));
        String title = String.valueOf(embeddedObject.get("title"));
        String description = String.valueOf(embeddedObject.get("synopsis"));
        String linkSelf = String.valueOf(((LinkedTreeMap)((LinkedTreeMap)embeddedObject.get("_links")).get("self")).get("href"));
        String linkAudiothek = String.valueOf(((LinkedTreeMap)((LinkedTreeMap)embeddedObject.get("_links")).get("mt:sharing")).get("href"));
        String publicationServiceGenre = String.valueOf(((LinkedTreeMap)((LinkedTreeMap)embeddedObject.get("_embedded")).get("mt:publicationService")).get("genre"));
        String publicationServiceId = String.valueOf(((LinkedTreeMap)((LinkedTreeMap)embeddedObject.get("_embedded")).get("mt:publicationService")).get("id"));
        String publicationServiceOrganization = String.valueOf(((LinkedTreeMap)((LinkedTreeMap)embeddedObject.get("_embedded")).get("mt:publicationService")).get("organizationName"));
        String publicationServiceTitle = String.valueOf(((LinkedTreeMap)((LinkedTreeMap)embeddedObject.get("_embedded")).get("mt:publicationService")).get("title"));
        String publicationServiceLinkSelf = String.valueOf(((LinkedTreeMap)(((LinkedTreeMap)((LinkedTreeMap)embeddedObject.get("_embedded")).get("mt:publicationService")).get("_links"))).get("self"));
        String publicationServiceLinkOnline = String.valueOf(((LinkedTreeMap)(((LinkedTreeMap)((LinkedTreeMap)embeddedObject.get("_embedded")).get("mt:publicationService")).get("_links"))).get("mt:onlinePortal"));

        GenericModel genericModel = new GenericModel(RadioPlayType.class);
        GenericObject radioPlay = new GenericObject(genericModel,uniqueId);

        radioPlay.addDescriptionProperty(RadioPlayType.ID, id);
        radioPlay.addDescriptionProperty(RadioPlayType.TITLE, title);
        radioPlay.addDescriptionProperty(RadioPlayType.DESCRIPTION, description);
        radioPlay.addDescriptionProperty(RadioPlayType.LINK_SELF, linkSelf);
        radioPlay.addDescriptionProperty(RadioPlayType.LINK_AUDIOTHEK, linkAudiothek);
        radioPlay.addDescriptionProperty(RadioPlayType.PUBLICATION_SERVICE_ID, publicationServiceId);
        radioPlay.addDescriptionProperty(RadioPlayType.PUBLICATION_SERVICE_GENRE, publicationServiceGenre);
        radioPlay.addDescriptionProperty(RadioPlayType.PUBLICATION_SERVICE_ORGANIZATION, publicationServiceOrganization);
        radioPlay.addDescriptionProperty(RadioPlayType.PUBLICATION_SERVICE_TITLE, publicationServiceTitle);
        radioPlay.addDescriptionProperty(RadioPlayType.LINK_SELF, publicationServiceLinkSelf);
        radioPlay.addDescriptionProperty(RadioPlayType.PUBLICATION_SERVICE_LINK_ONLINE, publicationServiceLinkOnline);

        return radioPlay;
    }

    private static StringBuffer getContentFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(inputStream));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content;
    }
}
