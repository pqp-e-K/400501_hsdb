package systems.pqp.hsdb;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config extends Properties {

    public static final String HSDB_USER = "hsdb.user";
    public static final String HSDB_PASS = "hsdb.pass";
    public static final String HSDB_URL = "hsdb.url";
    public static final String HSDB_DB = "hsdb.db";
    public static final String HSDB_TABLE = "hsdb.table";
    public static final String HSDB_MAPPING_TABLE = "hsdb.mapping.table";
    public static final String THRESHOLD = "check.threshold";
    public static final String NUM_THREADS = "check.threads";
    public static final String AUDIOTHEK_GRAPHQL_URL = "check.audiothek.graphql.url";
    public static final String AUDIOTHEK_GRAPHQL_IGNORE_SSL = "check.audiothek.graphql.ignore-ssl";
    public static final String AUDIOTHEK_EXCLUDES = "check.audiothek.excludes";
    public static final String AUDIOTHEK_EXCLUDE_UNPUBLISHED = "check.audiothek.exclude.unpublished";

    private static final String PREFIX = "similarity";
    private static final Logger LOG = LogManager.getLogger(Config.class);
    private static Config instance = null;

    protected Config(String path){
        init(path);
    }

    public static Config Config(){
        return Config(null);
    }

    public static Config Config(String path){
        if( null != instance ){
            return instance;
        }
        instance = new Config(path);
        return instance;
    }

    private void init(String path){
        try {
            load(path);
        } catch (IOException e) {
            LOG.error("Fehler beim Laden der Konfigurationsdatei!", e);
        }
    }

    private void load(String path) throws IOException {
        Properties tmp = new Properties();
        if( null == path ){
            LOG.info("Lade Konfiguration aus Classpath ...");
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream stream = loader.getResourceAsStream("application.properties");
            tmp.load(stream);
        } else {
            LOG.log(Level.INFO, "Lade Konfiguration aus Pfad: {} ...", path);
            try(FileInputStream inputStream = new FileInputStream(path)){
                tmp.load(inputStream);
            }
        }

        for(String key: tmp.stringPropertyNames()){
            if( key.startsWith(PREFIX) ){
                put(key.split(PREFIX + ".")[1], tmp.getProperty(key));
            }
        }
    }
}
