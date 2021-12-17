package systems.pqp.hsdb;

import com.google.gson.Gson;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.pqp.hsdb.dao.AudiothekDao;
import systems.pqp.hsdb.dao.HsdbDao;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static Options cliOptions;

    /**
     * @param args String[]
     * @return CommandLine
     */
    public static CommandLine createCLI(String[] args) {
        cliOptions = new Options();
        Option help = new Option("help", "Zeige diese Ansicht");
        Option configFilePath   = Option.builder("c").longOpt("config-file")
                .argName("file")
                .hasArg(true)
                .required(true)
                .desc("Pfad zur Konfigurationsdatei, z.B. /pfad/zur/datei/application.properties")
                .type(String.class)
                .build();
        Option validate = Option.builder("validate")
                .required(false)
                .optionalArg(true)
                .desc("Vorhandende Verknüpfungen überprüfen und ggf. aus DB-Tabelle entfernen?")
                .type(Boolean.class)
                .build();
        Option useLocalApiDump = Option.builder("l").longOpt("local-audiothek-dump-file")
                .required(false)
                .hasArg(true)
                .optionalArg(true)
                .desc("Einen lokalen Audiothek-Dump anstatt eines REST-Calls gegen die Api verwenden")
                .type(String.class)
                .build();
        cliOptions.addOption(help);
        cliOptions.addOption(configFilePath);
        cliOptions.addOption(validate);
        cliOptions.addOption(useLocalApiDump);

        CommandLineParser parser = new DefaultParser();

        try {
            return parser.parse(cliOptions, args);
        } catch (ParseException e) {
            printHelp();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static void printHelp(){
        HelpFormatter helpFormatter = new HelpFormatter();
                                                        // TODO @Holger: Wollen wir uns hier verewigen? :^)
        helpFormatter.printHelp("HSPDB - ARD Audiothek Abgleich (c) pqp e.K. 2021", cliOptions);
    }


    /**
     *
     * @param audiothekObjects
     * @param hsdbObjects
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void calculateSimilarities(Map<String, GenericObject> audiothekObjects, Map<String, GenericObject> hsdbObjects)
            throws ExecutionException, InterruptedException {
        SimilarityCheck similarityCheck = new SimilarityCheck();
        similarityCheck.mapSimilarities(hsdbObjects, audiothekObjects,
                Integer.parseInt(Config.Config().getProperty(Config.NUM_THREADS)));
    }

    public static void runSimilarityCheck(CommandLine cli) throws FileNotFoundException, ImportException, ExecutionException, InterruptedException {
        Config.Config(cli.getOptionValue("c"));
        LOGGER.info("Lade Daten...");
        Map<String, GenericObject> audiothekObjects;
        if( cli.hasOption("l") ){
            Gson gson = new Gson();
            FileReader reader = new FileReader(cli.getOptionValue("l"));
            Map dumpFile = gson.fromJson(reader, Map.class);
            audiothekObjects = AudiothekDao.genericObjectsFromJson(dumpFile);
            LOGGER.info("ARD Audiothek-Daten aus lokaler Datei geladen.");
        } else {
            audiothekObjects = new AudiothekDao().getRadioPlays();
            LOGGER.info("ARD Audiothek-Daten aus Api geladen.");
        }
        HsdbDao hsdbDao = new HsdbDao();
        Map<String, GenericObject> hsdbObjects = hsdbDao.getRadioPlays();
        LOGGER.info("HSPDB-Daten geladen.");
        LOGGER.info("Starte Abgleich...");
        calculateSimilarities(audiothekObjects, hsdbObjects);
        LOGGER.info("Finished Abgleich.");
        if( cli.hasOption("validate") ){
            // TODO validate
        }
    }

    public static void main(String[] args) throws InterruptedException{
        CommandLine cli = createCLI(args);
        if(cli.hasOption("help")){ // show help, terminate application
            printHelp();
        }

        try {
            runSimilarityCheck(cli);
        } catch (FileNotFoundException | ImportException | ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(1);
        }
    }
}
