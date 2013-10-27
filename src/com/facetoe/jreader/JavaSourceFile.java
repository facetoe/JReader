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
    private JavaClassOrInterface enclosingClass;

    /* When a JavaSourceFile object is created, gather all the declarations into <code>allObjects</code> for easy access */
    public JavaSourceFile(ArrayList<JavaClassOrInterface> fileContents) {
        this.fileContents = fileContents;
        for ( JavaClassOrInterface fileContent : fileContents ) {
            addAllObjectData(fileContent);
        }
    }


    private void addAllObjectData(JavaClassOrInterface classOrInterface) {
        allObjects.putAll(classOrInterface.getConstructors());
        allObjects.putAll(classOrInterface.getMethods());
        allObjects.putAll(classOrInterface.getFields());
        allObjects.putAll(classOrInterface.getEnums());
        allObjects.putAll(classOrInterface.getNestedClasses());
        HashMap<String, JavaClassOrInterface> nestedClasses = classOrInterface.getNestedClasses();

        /* For each nested class, recurse through it and each nested class it contains gathering all the declaration data */
        for ( String s : nestedClasses.keySet() ) {
            JavaClassOrInterface nestedClass = nestedClasses.get(s);
            addAllObjectData(nestedClass);
        }
    }

    public ArrayList<String> getAllDeclarations() {
        return new ArrayList<String>(allObjects.keySet());
    }

    public JavaObject getItem(String item) {
        return allObjects.get(item);
    }

    public ArrayList<JavaClassOrInterface> getFileContents() {
        return fileContents;
    }

    public JavaClassOrInterface getEnclosingClass() {
        if ( fileContents.size() > 0 ) {

            // The enclosing class is always first.
            return fileContents.get(0);
        }
        return null;
    }
}

