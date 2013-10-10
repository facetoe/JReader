package com.facetoe.jreader;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 10/10/13
 * Time: 7:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class JavaClassData implements Serializable {
    private HashMap<String, String> classes;
    private HashMap<String, HashMap<String, String>> allClassData;

    HashMap<String, String> getClasses() {
        return classes;
    }

    void setClasses(HashMap<String, String> classes) {
        this.classes = classes;
    }

    HashMap<String, HashMap<String, String>> getAllClassData() {
        return allClassData;
    }

    void setAllClassData(HashMap<String, HashMap<String, String>> allClassData) {
        this.allClassData = allClassData;
    }
}

