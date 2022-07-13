package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.junit.jupiter.api.Test;
import systems.pqp.hsdb.dao.AudiothekDao;
import systems.pqp.hsdb.dao.HsdbDao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class AppTest {

    @Test
    public void validateLinks() throws ImportException, InterruptedException {
        String[] args = new String[]{"-validate"};
        App.validateLinks(App.createCLI(args));
    }

    @Test //großer integrations-test -- dauert derzeit bei 10 threads ca. 4h
    public void similarityCheck() throws ImportException, ExecutionException, InterruptedException, FileNotFoundException {
        String[] args = new String[]{"-l"};
        App.runSimilarityCheck(App.createCLI(args));
    }

    //@Test //großer integrations-test -- dauert derzeit bei 10 threads ca. 4h
    @Deprecated
    public void similarityCheckFromDump() throws ImportException, ExecutionException, InterruptedException, IOException {
        AudiothekDao audiothekDao = new AudiothekDao();
        Map<String, GenericObject> audiothekObjects = AudiothekDao.genericObjectsFromDisk("api-examples/api.json.zip");

        HsdbDao hsdbDao = new HsdbDao();
        Map<String, GenericObject> hsdbObjects = hsdbDao.getRadioPlays();
        App.calculateSimilarities(audiothekObjects, hsdbObjects);
    }

}
