package com.facetoe.jreader;

import japa.parser.ast.body.*;
import japa.parser.ast.stmt.BlockStmt;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 16/10/13
 * Time: 12:41 PM
 */

/**
 * Abstract class to represent different declarations.
 *
 * @param <T>
 */
public abstract class JavaObject<T> {

    // The declaration
    final T typeDeclaration;

    /**
     * Full declaration including modifiers
     */
    String fullDeclaration;

    /**
     * Just the name and parameters
     */
    String declaration;

    /**
     * Line in the source file where this item begins
     */
    int beginLine;

    /**
     * Line in the source file where this item ends
     * Note: this includes the entire block, not just declarations
     */
    int endLine;

    /**
     * Column where this declaration begins
     */
    protected int beginColumn;

    /**
     * Column where this declaration ends.
     */
    int endColumn;

    /**
     * Constructor.
     * @param t The type declaration.
     */
    JavaObject(T t) {
        typeDeclaration = t;
        extractDeclaration();
        extractFullDeclaration();
    }

    /**
     * Abstract method for extracting full declarations.
     */
    abstract void extractFullDeclaration();

    /**
     * Abstract method for extracting declarations.
     */
    abstract void extractDeclaration();

    /**
     * Get the full declaration.
     * @return The declaration.
     */
    String getFullDeclaration() {
        return fullDeclaration;
    }

    /**
     * Get the short declaration.
     * @return The short declaration.
     */
    String getDeclaration() {
        return declaration;
    }

    /**
     * Get the line where this declaration begins
     * @return The start line.
     */
    public int getBeginLine() {
        return beginLine;
    }

    /**
     * Get the line where this declaration ends
     * @return The end line.
     */
    public int getEndLine() {
        return endLine;
    }

    /**
     * Get the column where this declaration begins
     * @return the start column
     */
    public int getBeginColumn() {
        return beginColumn;
    }

    /**
     * Get the column where this declaration ends
     * @return the end column.
     */
    public int getEndColumn() {
        return endColumn;
    }

    @Override
    public String toString() {
        return fullDeclaration;
    }
}

/**
 * Represents a class or interface. It can contain any number of methods, fields and constructors,
 * as well as multiple nested classes.
 */
class JavaClassOrInterface extends JavaObject<ClassOrInterfaceDeclaration> {

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
     * Magic number for a class.
     */
    private static final int CLASS = 0;

    /**
     * Magic number for an interface.
     */
    private static final int INTERFACE = 1;

    /**
     * Identifies whether this instance is a class or interface.
     */
    private int type;

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

    /**
     * Extract the short declaration.
     */
    @Override
    void extractDeclaration() {
        declaration = typeDeclaration.getName();
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
    HashMap<String, JavaMethod> getMethods() {
        return methods;
    }

    /**
     * Return this class or interfaces constructor.
     * @return Hash of the constructor with the name as key and object as the value..
     */
    HashMap<String, JavaConstructor> getConstructors() {
        return constructors;
    }

    /**
     * Return this class or interfaces enums.
     * @return Hash of the enums with the name as key and object as the value..
     */
    HashMap<String, JavaEnum> getEnums() {
        return enums;
    }

    /**
     * Return this class or interfaces fields.
     * @return Hash of the fields with the name as key and object as the value..
     */
    HashMap<String, JavaField> getFields() {
        return fields;
    }

    /**
     * Return this class or interfaces nested classes.
     * @return Hash of the nested classes with the name as key and object as the value..
     */
    HashMap<String, JavaClassOrInterface> getNestedClasses() {
        return nestedClasses;
    }

    /**
     * Returns an int with 0 representing an class and 1 representing a interface.
     * @return the type.
     */
    int getType() {
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

/**
 * Represents a field.
 */
class JavaField extends JavaObject<FieldDeclaration> {

    public JavaField(FieldDeclaration typeDec) {
        super(typeDec);
        beginColumn = typeDec.getBeginColumn();
        endColumn = typeDec.getEndColumn();
        beginLine = typeDec.getBeginLine();
        endLine = typeDec.getEndLine();
    }

    @Override
    void extractFullDeclaration() {
        String field = new FieldDeclaration(typeDeclaration.getModifiers(),
                typeDeclaration.getType(),
                typeDeclaration.getVariables())
                .toString()
                .replace(";", "");

        if ( field.contains("=") ) {
            field = field.substring(0, field.indexOf(" ="));
        }
        fullDeclaration = field.trim();
        declaration = field.substring(field.lastIndexOf(" ", field.length())).trim();
    }

    @Override
    void extractDeclaration() {
    }
}

/**
 * Represents an enum.
 */
class JavaEnum extends JavaObject<EnumDeclaration> {

    public JavaEnum(EnumDeclaration typeDec) {
        super(typeDec);
        beginColumn = typeDec.getBeginColumn();
        endColumn = typeDec.getEndColumn();
        beginLine = typeDec.getBeginLine();
        endLine = typeDec.getEndLine();
    }

    @Override
    void extractFullDeclaration() {
        fullDeclaration = new EnumDeclaration(typeDeclaration.getModifiers(),
                typeDeclaration.getName()).toString()
                .replace("{", "")
                .replace("}", "")
                .replaceAll("\\n", "");
    }

    @Override
    void extractDeclaration() {
        declaration = typeDeclaration.getName();
    }
}

/**
 * Represents a method.
 */
class JavaMethod extends JavaObject<MethodDeclaration> {

    public JavaMethod(MethodDeclaration typeDec) {
        super(typeDec);
        beginColumn = typeDec.getBeginColumn();
        endColumn = typeDec.getEndColumn();
        beginLine = typeDec.getBeginLine();
        endLine = typeDec.getEndLine();
    }

    @Override
    void extractFullDeclaration() {
        fullDeclaration = new MethodDeclaration(typeDeclaration.getModifiers(),
                typeDeclaration.getType(),
                typeDeclaration.getName(),
                typeDeclaration.getParameters())
                .toString()
                .replace(";", "");
    }

    @Override
    void extractDeclaration() {
        if ( typeDeclaration.getParameters() == null ) {
            declaration = typeDeclaration.getName() + "()";
        } else {
            declaration = (typeDeclaration.getName()
                    + typeDeclaration.getParameters())
                    .replace("[", "(")
                    .replace("]", ")");
        }
    }
}

/**
 * Represents a constructor.
 */
class JavaConstructor extends JavaObject<ConstructorDeclaration> {

    public JavaConstructor(ConstructorDeclaration typeDec) {
        super(typeDec);
        beginColumn = typeDec.getBeginColumn();
        endColumn = typeDec.getEndColumn();
        beginLine = typeDec.getBeginLine();
        endLine = typeDec.getEndLine();
    }

    @Override
    void extractFullDeclaration() {
        fullDeclaration = new ConstructorDeclaration(null,
                typeDeclaration.getModifiers(),
                null,
                typeDeclaration.getTypeParameters(),
                typeDeclaration.getName(),
                typeDeclaration.getParameters(),
                typeDeclaration.getThrows(),
                new BlockStmt()) // Add a new, empty block or it throws an error.
                .toString()
                .replace("{", "")
                .replace("}", "")
                .replaceAll("\\n", "");
    }

    @Override
    void extractDeclaration() {
        if ( typeDeclaration.getParameters() == null ) {
            declaration = typeDeclaration.getName() + "()";
        } else {
            declaration = (typeDeclaration.getName()
                    + typeDeclaration.getParameters())
                    .replace("[", "(")
                    .replace("]", ")");
        }
    }
}