/*
 * 03.11.2021
 * Copyright (c) 2021 HUK-COBURG. All Rights Reserved.
 */

package systems.pqp.hsdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config extends Properties {

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
            LOG.error("FAILED to load configuration!", e);
        }
    }

    private void load(String path) throws IOException {
        Properties tmp = new Properties();
        if( null == path ){
            if( LOG.isDebugEnabled() ) {
                LOG.debug("Loading config from classpath ...");
            }
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream stream = loader.getResourceAsStream("application.properties");
            tmp.load(stream);
        } else {
            if( LOG.isDebugEnabled() ) {
                LOG.debug(String.format("Loading config from path: %1$s ...", path));
            }
            try(FileInputStream inputStream = new FileInputStream(path)){
                tmp.load(inputStream);
            }
        }

        for(String key: tmp.stringPropertyNames()){
            if( key.startsWith(PREFIX) ){
                put(key.split(PREFIX + ".")[1], tmp.getProperty(key));
            }
        }

        LOG.info("FINISHED loading configuration");
    }
}
