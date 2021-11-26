package systems.pqp.hsdb;

import com.google.gson.Gson;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class ApiImportServiceTest {


    /**
     * Stellt HTTP Verbindung zur API her
     */
    @Test
    public void getRadioPlays() throws ImportException {
        List<GenericObject> result = new ApiImportService().getRadioPlays();
        Assert.assertTrue("Ergebnismenge ist > 0",result.size() > 0);
        System.out.println(result);
    }

    @Test
    public void genericObjectsFromJson() throws IOException {
        Map apiResponse = loadJsonFromFile("api-examples/api.json");
        List<GenericObject> result = ApiImportService.genericObjectsFromJson(apiResponse);
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
