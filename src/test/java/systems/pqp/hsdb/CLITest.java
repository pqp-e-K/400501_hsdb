package systems.pqp.hsdb;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

public class CLITest {

    @Test
    public void createCLITest() {
        String[] args = new String[]{ "--config-file=path/to/file" };
        CommandLine cli = App.createCLI(args);
        Assertions.assertTrue(cli.hasOption("c"));
    }

    @Test
    public void similarityCheckTest(){
        String[] args = new String[]{ "-c=path/to/file","-link" };
        CommandLine cli = App.createCLI(args);
        Assertions.assertTrue(cli.hasOption("link"));
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
        String[] args = new String[]{ "--config-file=path/to/file", "-d=pfad/zu/dump/api.json" };
        CommandLine cli = App.createCLI(args);
        Assertions.assertTrue(cli.hasOption("c"));
        Assertions.assertTrue(cli.hasOption("use-dump"));
        Assertions.assertEquals("pfad/zu/dump/api.json", cli.getOptionValue("d"));
    }

    //@Test
    public void smallIntegrationTest() throws ParseException, ImportException, FileNotFoundException, ExecutionException, InterruptedException {
        String[] args = new String[]{ "--config-file=/Users/gabrielschneider/IdeaProjects/400501_hsdb/src/main/resources/application.properties", "-l=/Users/gabrielschneider/IdeaProjects/400501_hsdb/src/test/resources/api-examples/mini-test.json" };
        CommandLine cli = App.createCLI(args);
        Assertions.assertTrue(cli.hasOption("c"));
        Assertions.assertTrue(cli.hasOption("local-audiothek-dump-file"));
        Assertions.assertEquals("/Users/gabrielschneider/IdeaProjects/400501_hsdb/src/test/resources/api-examples/mini-test.json", cli.getOptionValue("l"));
        App.runSimilarityCheck(cli);
    }

}
