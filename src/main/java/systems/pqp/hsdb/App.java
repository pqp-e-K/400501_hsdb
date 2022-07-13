package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import systems.pqp.hsdb.dao.AudiothekDaoV2;
import systems.pqp.hsdb.dao.HsdbDao;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class App {

    private static final Logger LOGGER = LogManager.getLogger(App.class);
    private static Options cliOptions;

    /**
     * @param args String[]
     * @return CommandLine
     */
    public static CommandLine createCLI(String[] args) {
        cliOptions = new Options();
        Option help = new Option("help", "Zeige diese Ansicht");
        Option similarityCheck = Option.builder("l").longOpt("link")
                .required(false)
                .optionalArg(true)
                .desc("Hauptfunktion. HSDB-Audiothek-Abgleich starten")
                .type(Boolean.class)
                .build();
        Option validateLinks = Option.builder("validate")
                .required(false)
                .optionalArg(true)
                .desc("Vorhandende Audiothek-Verknüpfungen überprüfen und ggf. aus DB-Tabelle entfernen")
                .type(Boolean.class)
                .build();
        Option configFilePath = Option.builder("c").longOpt("config-file")
                .argName("file")
                .hasArg(true)
                .required(false)
                .optionalArg(true)
                .desc("Pfad zur Konfigurationsdatei, z.B. /pfad/zur/datei/application.properties . " +
                        "Wenn nicht gesetzt, werden Default-Properties im Classpath verwendet.")
                .type(String.class)
                .build();
        cliOptions.addOption(help);
        cliOptions.addOption(similarityCheck);
        cliOptions.addOption(validateLinks);
        cliOptions.addOption(configFilePath);

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cli = parser.parse(cliOptions, args);
            if( !cli.hasOption("-l") && !cli.hasOption("-validate") ){
                printHelp();
            }
            return cli;
        } catch (ParseException e) {
            printHelp();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     *
     */
    private static void printHelp(){
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("HSPDB - ARD Audiothek Abgleich by pqp e.K. 2022", cliOptions);
        System.exit(0);
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
                Integer.parseInt(Config.Config().getProperty(Config.NUM_THREADS,"1")));
    }

    /**
     *
     * @param cli
     * @throws FileNotFoundException
     * @throws ImportException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void runSimilarityCheck(CommandLine cli) throws FileNotFoundException, ImportException, ExecutionException, InterruptedException {
        Config.Config(cli.getOptionValue("c"));
        Map<String, GenericObject> audiothekObjects = new AudiothekDaoV2().getRadioPlays();
        if( cli.hasOption("d") ){
            LOGGER.info("Derzeit nicht implementiert. Daten werden aus GraphQL geladen.");
        } else {
            audiothekObjects = new AudiothekDaoV2().getRadioPlays();
            LOGGER.info("ARD Audiothek-Daten aus Api geladen.");
        }
        HsdbDao hsdbDao = new HsdbDao();
        Map<String, GenericObject> hsdbObjects = hsdbDao.getRadioPlays();
        LOGGER.info("HSPDB-Daten geladen.");
        LOGGER.info("Starte Abgleich...");
        calculateSimilarities(audiothekObjects, hsdbObjects);
        LOGGER.info("Abgleich Beendet.");
    }

    /**
     *
     */
    public static void validateLinks(CommandLine cli) throws ImportException, InterruptedException {
        Config.Config(cli.getOptionValue("c"));
        LOGGER.info("Validiere Links in HSDB...");
        List<String> episodes = new AudiothekDaoV2().getUnpublishedRadioPlayIds();
        HsdbDao hsdbDao = new HsdbDao();
        hsdbDao.validateMany(episodes);
        LOGGER.info("Validierung abgeschlossen.");
    }

    /**
     *
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException{
        CommandLine cli = createCLI(args);
        if(cli.hasOption("help")){ // show help, terminate application
            printHelp();
        }

        if(cli.hasOption("link")){
            try {
                runSimilarityCheck(cli);
            } catch (FileNotFoundException | ImportException | ExecutionException e) {
                LOGGER.error(e.getMessage(), e);
                System.exit(1);
            }
        }

        if(cli.hasOption("validate")){
            try {
                validateLinks(cli);
            } catch (ImportException e) {
                LOGGER.info(e.getMessage(), e);
                System.exit(1);
            }
        }

    }
}
