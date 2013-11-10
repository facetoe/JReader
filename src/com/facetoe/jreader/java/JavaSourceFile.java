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
 * This class represents and Java source code file. It contains all the methods, constructors, fields and nested classOrInterfaces
 * as well as methods for accessing them.
 */
public class JavaSourceFile {

    /**
     * All classOrInterfaces and interfaces..
     */
    private ArrayList<AbstractJavaObject> fileContents = new ArrayList<AbstractJavaObject>();

    /**
     * Every declaration.
     */
    private final HashMap<String, AbstractJavaObject> allObjects = new HashMap<String, AbstractJavaObject>();

    public JavaSourceFile() {

    }

    public void addObject(AbstractJavaObject object) {
        fileContents.add(object);
    }

    public void extractAllObjectData() {
        for ( AbstractJavaObject object : fileContents ) {
            if ( object instanceof JavaClassOrInterface ) {
                extractClassOrInterfaceData(( JavaClassOrInterface ) object);
            } else if ( object instanceof JavaEnum ) {
                allObjects.putAll((( JavaEnum ) object).getConstants());
            } else if ( object instanceof JavaAnnotation ) {
                JavaAnnotation annotation = ( JavaAnnotation ) object;
                allObjects.put(annotation.declaration, annotation);
            }
        }
    }

    /**
     * Populate the <code>allObjects</code> ArrayList with all the declarations.
     *
     * @param classOrInterface The <code>JavaClassOrInterface</code> to add.
     */
    private void extractClassOrInterfaceData(JavaClassOrInterface classOrInterface) {
        allObjects.putAll(classOrInterface.getConstructors());
        allObjects.putAll(classOrInterface.getMethods());
        allObjects.putAll(classOrInterface.getFields());
        allObjects.putAll(classOrInterface.getNestedClasses());
        allObjects.putAll(classOrInterface.getAnnotations());

        /* Add all the enums and the enum constants. */
        HashMap<String, JavaEnum> enums = classOrInterface.getEnums();
        for ( String enumName : enums.keySet() ) {
            allObjects.put(enumName, enums.get(enumName));
            allObjects.putAll(enums.get(enumName).getConstants());
        }

        HashMap<String, JavaClassOrInterface> nestedClasses = classOrInterface.getNestedClasses();
        /* For each nested class, recurse through it and each nested class it contains gathering all the declaration data */
        for ( String s : nestedClasses.keySet() ) {
            JavaClassOrInterface nestedClass = nestedClasses.get(s);
            extractClassOrInterfaceData(nestedClass);
        }
    }

    /**
     * Get the object associated with a declaration.
     *
     * @param itemDeclaration Short declaration of the desired object.
     * @return The Object associated with this declaration.
     */
    public AbstractJavaObject getObject(String itemDeclaration) {
        return allObjects.get(itemDeclaration);
    }

    /**
     * Get all the declarations short.
     *
     * @return The declarations.
     */
    public ArrayList<String> getAllDeclarations() {
        return new ArrayList<String>(allObjects.keySet());
    }

    /**
     * This is the top level object in the file. It could be a Class, Interface
     * or Annotation.
     *
     * @return The enclosing object.
     */
    public AbstractJavaObject getEnclosingObject() {
        if ( fileContents.size() > 0 ) {
            return fileContents.get(0);
        }
        return null;
    }

    public ArrayList<AbstractJavaObject> getFileContents() {
        return fileContents;
    }
}

