package com.facetoe.jreader.java;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 16/10/13
 * Time: 12:49 PM
 */

/**
 * This class represents and Java source code file. It contains all the methods, constructors, fields and nested classes
 * as well as methods for accessing them.
 */
public class JavaSourceFile {

    /**
     * All classes and interfaces..
     */
    private ArrayList<JavaClassOrInterface> fileContents = new ArrayList<JavaClassOrInterface>();

    /**
     * Every declaration.
     */
    private final HashMap<String, JavaObject> allObjects = new HashMap<String, JavaObject>();

    /**
     * Constructor.
     * @param fileContents All the data for this file.
     */
    public JavaSourceFile(ArrayList<JavaClassOrInterface> fileContents) {
        this.fileContents = fileContents;
        for ( JavaClassOrInterface fileContent : fileContents ) {
            addAllObjectData(fileContent);
        }
    }


    /**
     * Populate the <code>allObjects</code> ArrayList with all the declarations.
     * @param classOrInterface The <code>JavaClassOrInterface</code> to add.
     */
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

    /**
     * Get the object associated with a declaration.
     * @param itemDeclaration Short declaration of the desired object.
     * @return The Object associated with this declaration.
     */
    public JavaObject getObject(String itemDeclaration) {
        return allObjects.get(itemDeclaration);
    }

    /**
     * Get all the declarations short.
     * @return The declarations.
     */
    public ArrayList<String> getAllDeclarations() {
        return new ArrayList<String>(allObjects.keySet());
    }

    /**
     * Get the fileContents ArrayList.
     * @return The fileContents ArrayList
     */
    public ArrayList<JavaClassOrInterface> getFileContents() {
        return fileContents;
    }

    /**
     * Get the enclosing class. This is the top level class that contains all the
     * other data. In other words, this is the class the file is named after.
     * @return The enclosing class.
     */
    public JavaClassOrInterface getEnclosingClass() {
        if ( fileContents.size() > 0 ) {

            // The enclosing class is always first.
            return fileContents.get(0);
        }
        return null;
    }
}

