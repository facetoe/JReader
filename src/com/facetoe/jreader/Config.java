package com.facetoe.jreader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


class Config {

    static private final Properties config = new Properties();
    static private final String configFilePath = "/com/facetoe/jreader/config.properties";

    static public void setEntry(String key, String value) {
        try {
            config.load(config.getClass().getResourceAsStream(configFilePath));

            config.setProperty(key, value);

            config.store(new FileOutputStream(configFilePath), null);

        } catch ( IOException ex ) {
            ex.printStackTrace();
        }
    }

    static public String getEntry(String key) {
        try {
            config.load(config.getClass().getResourceAsStream(configFilePath));
        } catch ( IOException ex ) {
            ex.printStackTrace();
        }
        return config.getProperty(key);
    }
}