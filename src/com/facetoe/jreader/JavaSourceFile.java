package com.facetoe.jreader;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 16/10/13
 * Time: 12:49 PM
 */
public class JavaSourceFile {
    private ArrayList<JavaClassOrInterface> fileContents = new ArrayList<JavaClassOrInterface>();
    private HashMap<String, JavaObject> allObjects = new HashMap<String, JavaObject>();

    public JavaSourceFile(ArrayList<JavaClassOrInterface> fileContents) {
        this.fileContents = fileContents;
        for ( JavaClassOrInterface fileContent : fileContents ) {
            allObjects.putAll(fileContent.getConstructors());
            allObjects.putAll(fileContent.getMethods());
            allObjects.putAll(fileContent.getFields());
            allObjects.putAll(fileContent.getEnums());
        }
    }

    public ArrayList<String> getAllDeclarations() {
        return new ArrayList<String>(allObjects.keySet());
    }

    public JavaObject getItem(String item) {
        return allObjects.get(item);
    }
}

