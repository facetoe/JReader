package com.facetoe.jreader.java;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 1/11/13
 * Time: 11:44 AM
 */

import japa.parser.ast.body.ClassOrInterfaceDeclaration;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a class or interface. It can contain any number of methods, fields and constructors,
 * as well as multiple nested classes.
 */
public class JavaClassOrInterface extends JavaObject<ClassOrInterfaceDeclaration> {

    /**
     * The nested classes or interaces contained in this class or interface.
     */
    private final HashMap<String, JavaClassOrInterface> nestedClasses = new HashMap<String, JavaClassOrInterface>();

    /**
     * All the methods.
     */
    private final HashMap<String, JavaMethod> methods = new HashMap<String, JavaMethod>();

    /**
     * All the constructors.
     */
    private final HashMap<String, JavaConstructor> constructors = new HashMap<String, JavaConstructor>();

    /**
     * All the enums.
     */
    private final HashMap<String, JavaEnum> enums = new HashMap<String, JavaEnum>();

    /**
     * All the fields.
     */
    private final HashMap<String, JavaField> fields = new HashMap<String, JavaField>();

    /**
     * All the annotations.
     */
    private final HashMap<String, JavaAnnotation> annotations = new HashMap<String, JavaAnnotation>();

    /**
     * Constructor.
     * @param typeDec the ClassOrInterfaceDeclaration
     */

    public JavaClassOrInterface(ClassOrInterfaceDeclaration typeDec) {
        super(typeDec);
        beginColumn = typeDec.getBeginColumn();
        endColumn = typeDec.getEndColumn();
        beginLine = typeDec.getBeginLine();
        endLine = typeDec.getEndLine();
    }

    /**
     * Extract the full declaration.
     */
    @Override
    void extractFullDeclaration() {
        fullDeclaration = new ClassOrInterfaceDeclaration(typeDeclaration.getModifiers(),
                typeDeclaration.isInterface(),
                typeDeclaration.getName())
                .toString()
                .replace("{", "")
                .replace("}", "")
                .replaceAll("\\n", "");

        if ( fullDeclaration.contains("class") ) {
            type = CLASS;
        } else {
            type = INTERFACE;
        }
    }

    @Override
    public int getModifiers() {
        return typeDeclaration.getModifiers();
    }

    public boolean hasConstructors() {
        return constructors.size() > 0;
    }

    public boolean hasMethods() {
        return methods.size() > 0;
    }

    public boolean hasEnums() {
        return enums.size() > 0;
    }

    public boolean hasFields() {
        return fields.size() > 0;
    }

    public boolean hasNestedClasses() {
        return nestedClasses.size() > 0;
    }

    public boolean hasAnnotations() {
        return annotations.size() > 0;
    }

    /**
     * Extract the short declaration.
     */
    @Override
    void extractDeclaration() {
        declaration = typeDeclaration.getName();
    }

    public String getDeclaration() {
        return declaration;
    }

    /**
     * Add a constructor.
     * @param constructor The constructor to add.
     */
    public void addConstructor(JavaConstructor constructor) {
        constructors.put(constructor.declaration, constructor);
    }


    /**
     * Add a field.
     * @param field The field to add.
     */
    public void addField(JavaField field) {
        fields.put(field.declaration, field);
    }

    /**
     * Add an enum.
     * @param javaEnum The enum to add.
     */
    public void addEnum(JavaEnum javaEnum) {
        enums.put(javaEnum.declaration, javaEnum);
    }

    /**
     * Add a method.
     * @param method The method to add.
     */
    public void addMethod(JavaMethod method) {
        methods.put(method.declaration, method);
    }

    /**
     * Add an annotation.
     * @param annotation
     */
    public void addAnnotation(JavaAnnotation annotation) {
        annotations.put(annotation.getDeclaration(), annotation);
    }

    /**
     * Add a nested class or interface.
     * @param classOrInterface The nested class or interface to add.
     */
    public void addNestedClassOrInterface(JavaClassOrInterface classOrInterface) {
        nestedClasses.put(classOrInterface.declaration, classOrInterface);
    }

    /**
     * Return this class or interfaces methods.
     * @return Hash of the methods with the name as key and object as the value..
     */
    public HashMap<String, JavaMethod> getMethods() {
        return methods;
    }

    /**
     * Return this class or interfaces constructor.
     * @return Hash of the constructor with the name as key and object as the value..
     */
    public HashMap<String, JavaConstructor> getConstructors() {
        return constructors;
    }

    /**
     * Return this class or interfaces enums.
     * @return Hash of the enums with the name as key and object as the value..
     */
    public HashMap<String, JavaEnum> getEnums() {
        return enums;
    }

    /**
     * Return this class or interfaces fields.
     * @return Hash of the fields with the name as key and object as the value..
     */
    public HashMap<String, JavaField> getFields() {
        return fields;
    }

    /**
     * Return this class or interfaces annotations.
     * @return
     */
    public HashMap<String, JavaAnnotation> getAnnotations() {
        return annotations;
    }

    /**
     * Return this class or interfaces nested classes.
     * @return Hash of the nested classes with the name as key and object as the value..
     */
    public HashMap<String, JavaClassOrInterface> getNestedClasses() {
        return nestedClasses;
    }

    public ArrayList<JavaClassOrInterface> getNestedClassesAsArrayList() {
        return new ArrayList<JavaClassOrInterface>(nestedClasses.values());
    }

    /**
     * Returns an int with 0 representing an class and 1 representing a interface.
     * @return the type.
     */
    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("Name: %s\nMethods: %d\nFields: %d\nEnums: %d\nConstructors: %d\n\n", fullDeclaration,
                methods.size(),
                fields.size(),
                enums.size(),
                constructors.size());
    }
}
