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
        GenericObject apiObject = ApiImportService.genericObjectFromJson(loadJsonFromFile("jules-verne-95022544.json"));

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

        GenericObject apiObject = ApiImportService.genericObjectFromJson(loadJsonFromFile("christa-wolf-94736562.json"));

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
