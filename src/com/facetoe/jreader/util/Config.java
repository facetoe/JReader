package com.facetoe.jreader.util;

import java.util.prefs.Preferences;


public class Config {
    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    private static Config instance = new Config();

    private Config() {

    }

    public static Config getInstance() {
        return instance;
    }

    public void setString(String key, String value) {
        prefs.put(key, value);
    }

    public void setBool(String key, boolean value) {
        prefs.putBoolean(key, value);
    }

    public String getString(String key) {
        return prefs.get(key, null);
    }

    public boolean getBool(String key, boolean def) {
        return prefs.getBoolean(key, def);
    }
}