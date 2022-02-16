package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import systems.pqp.hsdb.dao.AudiothekDao;
import systems.pqp.hsdb.dao.HsdbDao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class AppTest {

    @Test
    public void createCLITest() {
        String[] args = new String[]{ "--config-file=path/to/file" };
        CommandLine cli = App.createCLI(args);
        Assertions.assertTrue(cli.hasOption("c"));
    }

    @Test
    public void showHelpTest() {
        String[] args = new String[]{ "-help" };
        CommandLine cli = App.createCLI(args);
        Assertions.assertTrue(cli.hasOption("help"));
    }

    @Test
    public void validateTest() throws ParseException {
        String[] args = new String[]{ "--config-file=path/to/file", "-validate" };
        CommandLine cli = App.createCLI(args);
        Assertions.assertTrue(cli.hasOption("c"));
        Assertions.assertTrue(cli.hasOption("validate"));
    }

    @Test
    public void lokalDumpTest() throws ParseException {
        String[] args = new String[]{ "--config-file=path/to/file", "-l=pfad/zu/dump/api.json" };
        CommandLine cli = App.createCLI(args);
        Assertions.assertTrue(cli.hasOption("c"));
        Assertions.assertTrue(cli.hasOption("local-audiothek-dump-file"));
        Assertions.assertEquals("pfad/zu/dump/api.json", cli.getOptionValue("l"));
    }

    @Test
    public void smallIntegrationTest() throws ParseException, ImportException, FileNotFoundException, ExecutionException, InterruptedException {
        String[] args = new String[]{ "--config-file=/Users/gabrielschneider/IdeaProjects/400501_hsdb/src/main/resources/application.properties", "-l=/Users/gabrielschneider/IdeaProjects/400501_hsdb/src/test/resources/api-examples/mini-test.json" };
        CommandLine cli = App.createCLI(args);
        Assertions.assertTrue(cli.hasOption("c"));
        Assertions.assertTrue(cli.hasOption("local-audiothek-dump-file"));
        Assertions.assertEquals("/Users/gabrielschneider/IdeaProjects/400501_hsdb/src/test/resources/api-examples/mini-test.json", cli.getOptionValue("l"));
        App.runSimilarityCheck(cli);
    }


    @Test //großer integrations-test -- dauert derzeit bei 10 threads ca. 4h
    public void similarityCheck() throws ImportException, ExecutionException, InterruptedException {
        AudiothekDao audiothekDao = new AudiothekDao();
        Map<String, GenericObject> audiothekObjects = audiothekDao.getRadioPlays();

        HsdbDao hsdbDao = new HsdbDao();
        Map<String, GenericObject> hsdbObjects = hsdbDao.getRadioPlays();
        App.calculateSimilarities(audiothekObjects, hsdbObjects);
    }

    @Test //großer integrations-test -- dauert derzeit bei 10 threads ca. 4h
    public void similarityCheckFromDump() throws ImportException, ExecutionException, InterruptedException, IOException {
        AudiothekDao audiothekDao = new AudiothekDao();
        Map<String, GenericObject> audiothekObjects = AudiothekDao.genericObjectsFromDisk("api-examples/api.json.zip");

        HsdbDao hsdbDao = new HsdbDao();
        Map<String, GenericObject> hsdbObjects = hsdbDao.getRadioPlays();
        App.calculateSimilarities(audiothekObjects, hsdbObjects);
    }

}
