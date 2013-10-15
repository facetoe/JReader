package com.facetoe.jreader;

import java.util.prefs.Preferences;


class Config {

    static private final Preferences prefs = Preferences.userRoot().node("com/facetoe/jreader");

    static public void setString(String key, String value) {
        prefs.put(key, value);
    }

    static public void setBool(String key, boolean value) {
        prefs.putBoolean(key, value);
    }

    static public String getString(String key) {
        return prefs.get(key, null);
    }

    static public boolean getBool(String key, boolean def) {
        return prefs.getBoolean(key, def);
    }
}