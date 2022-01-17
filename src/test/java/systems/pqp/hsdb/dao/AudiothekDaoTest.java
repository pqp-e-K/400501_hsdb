package systems.pqp.hsdb.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import de.ard.sad.normdb.similarity.model.generic.types.RadioPlayType;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.pqp.hsdb.ImportException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AudiothekDaoTest {

    Logger logger = LoggerFactory.getLogger(AudiothekDaoTest.class);

    /**
     * Stellt HTTP Verbindung zur API her
     */
    @Test
    public void getRadioPlays() throws ImportException {
        Map<String, GenericObject> result = new AudiothekDao().getRadioPlays();
        Assert.assertTrue("Ergebnismenge ist > 0",result.size() > 0);
    }

    @Test
    public void checkLinks() throws IOException {
        Map apiResponse = loadJsonFromFile("api-examples/christa-wolf-94736562.json");
        GenericObject result = AudiothekDao.genericObjectFromJson(apiResponse);
        Assert.assertEquals("https://audiothek.ardmediathek.de/items/94736562", result.getProperties(RadioPlayType.LINK_AUDIOTHEK).get(0).getDescriptions().get(0));
    }

    @Test
    public void genericObjectsFromJson() throws IOException {
        Map<String, GenericObject> result = AudiothekDao.genericObjectsFromDisk("api-examples/api.json.zip");
        Assert.assertTrue("Ergebnismenge ist > 100",result.size() > 100);
        //System.out.println(result);
    }

    @Test
    public void fetchAndSaveAll() throws ImportException, IOException {
        String result = new AudiothekDao().fetchAll(AudiothekDao.RADIO_PLAY_ID, AudiothekDao.API_URL);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter resultWriter = new FileWriter("audiothek.json");
        gson.toJson(gson.fromJson(result, Map.class), resultWriter);
        resultWriter.flush();
        resultWriter.close();
    }


    /**
     * Aggregiert alle in der Api gefundenen Publisher-Versionen
     * Beachtet werden:
     * _embedded > mt:programSet > mt:publicationService > _embedded > mt:publicationService > title
     * tracking > play > source ---> wird als key für Ergebnismenge verwendet
     * tracking > play > lra
     * Ergebnis wird in ./api-publisher.tsv geschrieben
     * @throws IOException wenn test/resources/api-examples/api.json nicht gefunden wird
     */
    //@Test
    public void aggregateAllPublisher() throws IOException {
        Map<String, Map<String, Integer>> publisherAggregationMap = new HashMap<>();
        Map apiResponse = loadJsonFromFile("api-examples/api.json");

        ((ArrayList<LinkedTreeMap>)((LinkedTreeMap)apiResponse.get("_embedded")).get("mt:items")).forEach(
                embeddedObject -> {
                    Map embedded = (LinkedTreeMap)embeddedObject.get("_embedded");
                    Map tracking = (LinkedTreeMap)embeddedObject.get("tracking");
                    Map programSet = (LinkedTreeMap)embedded.get("mt:programSet");
                    String key = "";
                    if(((LinkedTreeMap)tracking.get("play")).containsKey("source")){
                        String publisher = (String) ((LinkedTreeMap)tracking.get("play")).get("source");
                        key = publisher;
                        if(publisherAggregationMap.containsKey(publisher)){
                            if(publisherAggregationMap.get(publisher).containsKey(publisher)){
                                int val = publisherAggregationMap.get(publisher).get(publisher);
                                publisherAggregationMap.get(publisher).put(publisher, val + 1);
                            } else {
                                publisherAggregationMap.get(publisher).put(publisher, 1);
                            }
                        } else {
                            Map<String, Integer> val = new HashMap<>();
                            val.put(publisher, 1);
                            publisherAggregationMap.put(publisher, val);
                        }
                    } else {
                        logger.info("Source not found...skipping");
                        return;
                    }
                    if(((LinkedTreeMap)tracking.get("play")).containsKey("lra")){
                        String publisher = (String) ((LinkedTreeMap)tracking.get("play")).get("source");
                        if(publisherAggregationMap.get(key).containsKey(publisher)){
                            int val = publisherAggregationMap.get(key).get(publisher);
                            publisherAggregationMap.get(key).put(publisher, val + 1);
                        } else {
                            publisherAggregationMap.get(key).put(publisher, 1);
                        }
                    }
                    if(null != ((LinkedTreeMap)programSet.get("_embedded")).get("mt:publicationService")) {
                        String publisher = (String) ((LinkedTreeMap) ((LinkedTreeMap) programSet.get("_embedded")).get("mt:publicationService")).get("title");
                        if(publisherAggregationMap.get(key).containsKey(publisher)){
                            int val = publisherAggregationMap.get(key).get(publisher);
                            publisherAggregationMap.get(key).put(publisher, val + 1);
                        } else {
                            publisherAggregationMap.get(key).put(publisher, 1);
                        }
                    }
                }
        );
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter writer = new FileWriter("rundfunkanstalten/api-publisher.json");
        gson.toJson(publisherAggregationMap, writer);
        writer.flush();
        writer.close();
    }

    @Test
    public void genericObjectFromDisk() throws IOException {
        Map<String, GenericObject> result = AudiothekDao.genericObjectsFromDisk("api-examples/api.json.zip");
        Assert.assertTrue(result.size() > 0);
    }

    /**
     * Helfer um json-datei aus test/resources/ zu laden
     * @param fileName name der datei in test/resources
     * @return Map
     * @throws IOException wenn json-datei nicht in test/resources/ gefunden wird
     */
    Map loadJsonFromFile(String fileName) throws IOException {
        Gson gson = new Gson();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(fileName);
        assert stream != null;
        Map json = gson.fromJson(new InputStreamReader(stream), Map.class);
        stream.close();
        return json;
    }


}
