package com.facetoe.jreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


class Config {

    static private final Properties config = new Properties();
    static private final String configFilePath = System.getProperty("user.home") +
            File.separator +
            ".jreader" +
            File.separator +
            "config.properties";

    static public void setEntry(String key, String value) {
        try {
            config.load(new FileInputStream(configFilePath));

            config.setProperty(key, value);

            config.store(new FileOutputStream(configFilePath), null);

        } catch ( IOException ex ) {
            ex.printStackTrace();
        }
    }

    static public String getEntry(String key) {
        try {
            config.load(new FileInputStream(configFilePath));
        } catch ( IOException ex ) {
            ex.printStackTrace();
        }
        return config.getProperty(key);
    }
}