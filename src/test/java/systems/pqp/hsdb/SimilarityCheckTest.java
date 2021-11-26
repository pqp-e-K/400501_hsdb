package systems.pqp.hsdb;

import com.google.gson.Gson;
import de.ard.sad.normdb.similarity.compare.generic.GenericSimilarity;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class SimilarityCheckTest {

    @Test
    public void testSimilarity() throws IOException {
        // Jules Verne Reise von der Erde zum Mond
        // GenericObject aus Database
        List<GenericObject> databaseObjects =
                new DatabaseImportService().getRadioPlays(
                        "WHERE VOLLINFO LIKE \"%Erde zum Mond%\""
                );
        Assert.assertEquals(1, databaseObjects.size());
        GenericObject dbObject = databaseObjects.get(0);

        // GenericObject aus Api (mocked aus Datei)
        GenericObject apiObject = ApiImportService.genericObjectFromJson(loadJsonFromFile("api-examples/jules-verne-95022544.json"));

        GenericSimilarity gs = new GenericSimilarity();
        gs.calcSimilarity(apiObject, dbObject);

    }

    @Test
    public void testSimilarity2() throws IOException {
        // Christa Wolf: Kassandra
        List<GenericObject> databaseObjects =
                new DatabaseImportService().getRadioPlays(
                        "WHERE VOLLINFO LIKE \"%Christa Wolf%\" AND VOLLINFO LIKE \"%Kassandra%\""
                );
        Assert.assertEquals(5, databaseObjects.size());

        GenericObject apiObject = ApiImportService.genericObjectFromJson(loadJsonFromFile("api-examples/christa-wolf-94736562.json"));

        GenericSimilarity gs = new GenericSimilarity();

        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(0), apiObject), 0.0);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(1), apiObject), 0.0);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(2), apiObject), 0.0);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(3), apiObject), 0.0);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(4), apiObject), 0.0);

    }

    @Test
    public void testSimilarity3() throws IOException {
        // Fjodor Dostojewski: Der Doppelgänger
        List<GenericObject> databaseObjects =
                new DatabaseImportService().getRadioPlays(
                        "where VOLLINFO like \"%Dostojewski%\" and VOLLINFO like \"%Der Doppelgänger%\""
                );
        Assert.assertEquals(4, databaseObjects.size());

        GenericObject apiObject = ApiImportService.genericObjectFromJson(loadJsonFromFile("api-examples/dostojewski-94663538.json"));

        GenericSimilarity gs = new GenericSimilarity();
        databaseObjects.forEach(
                databaseObject -> {
                    System.out.println(gs.calcSimilarity(databaseObject, apiObject));
                }
        );
    }

    @Test
    public void testSimilarity4() throws IOException {
        // Sodom und Gomorrha
        List<GenericObject> databaseObjects =
                new DatabaseImportService().getRadioPlays(
                        "WHERE VOLLINFO like \"%Sodom und Gomorrha%\""
                );
        Assert.assertEquals(9, databaseObjects.size());

        GenericObject apiObject = ApiImportService.genericObjectFromJson(loadJsonFromFile("api-examples/sodom-und-gomorrha-94512976.json"));

        GenericSimilarity gs = new GenericSimilarity();
        databaseObjects.forEach(
                databaseObject -> {
                    System.out.println(gs.calcSimilarity(databaseObject, apiObject));
                }
        );
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
