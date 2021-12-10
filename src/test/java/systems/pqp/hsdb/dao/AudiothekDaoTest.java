package systems.pqp.hsdb.dao;

import com.google.gson.Gson;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.junit.Assert;
import org.junit.Test;
import systems.pqp.hsdb.ImportException;
import systems.pqp.hsdb.RadioPlayType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class AudiothekDaoTest {


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
        Map apiResponse = loadJsonFromFile("api-examples/api.json");
        Map<String, GenericObject> result = AudiothekDao.genericObjectsFromJson(apiResponse);
        Assert.assertTrue("Ergebnismenge ist > 0",result.size() > 0);
        System.out.println(result);
    }

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
