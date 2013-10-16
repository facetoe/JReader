package com.facetoe.jreader;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 16/10/13
 * Time: 12:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class JavaSourceFile {
    private ArrayList<JavaClassOrInterface> fileContents = new ArrayList<JavaClassOrInterface>();
    private HashMap<String, String> allDeclarationsLong = new HashMap<String, String>();

    public JavaSourceFile(ArrayList<JavaClassOrInterface> fileContents) {
        this.fileContents = fileContents;
        for ( JavaClassOrInterface fileContent : fileContents ) {
            allDeclarationsLong.putAll(fileContent.getConstructors());
            allDeclarationsLong.putAll(fileContent.getMethods());
            allDeclarationsLong.putAll(fileContent.getFields());
            allDeclarationsLong.putAll(fileContent.getEnums());
        }
    }

    public ArrayList<String> getAllDeclarations() {
        ArrayList<String> declarations = new ArrayList<String>();
        declarations.addAll(getConstructorDeclarations());
        declarations.addAll(getMethodDeclarations());
        declarations.addAll(getFieldDeclarations());
        declarations.addAll(getEnumDeclarations());
        return declarations;
    }

    public String getItemDeclarationLong(String item) {
        return allDeclarationsLong.get(item);
    }

    public ArrayList<String> getConstructorDeclarations() {
        ArrayList<String> constructors = new ArrayList<String>();
        for ( JavaClassOrInterface fileContent : fileContents ) {
            constructors.addAll(getDeclarationsAsArrayList(fileContent.getConstructors()));
        }
        return constructors;
    }

    public ArrayList<String> getMethodDeclarations() {
        ArrayList<String> methods = new ArrayList<String>();
        for ( JavaClassOrInterface fileContent : fileContents ) {
            methods.addAll(getDeclarationsAsArrayList(fileContent.getMethods()));
        }
        return methods;
    }

    public ArrayList<String> getFieldDeclarations() {
        ArrayList<String> fields = new ArrayList<String>();
        for ( JavaClassOrInterface fileContent : fileContents ) {
            fields.addAll(getDeclarationsAsArrayList(fileContent.getFields()));
        }
        return fields;
    }

    public ArrayList<String> getEnumDeclarations() {
        ArrayList<String> enums = new ArrayList<String>();
        for ( JavaClassOrInterface fileContent : fileContents ) {
            enums.addAll(getDeclarationsAsArrayList(fileContent.getEnums()));
        }
        return enums;
    }

    private ArrayList<String> getDeclarationsAsArrayList(HashMap<String, String> declarations) {
        return new ArrayList<String>(declarations.keySet());
    }
}

