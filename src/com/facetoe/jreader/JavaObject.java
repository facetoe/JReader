package com.facetoe.jreader;

import japa.parser.ast.body.*;
import japa.parser.ast.stmt.BlockStmt;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 16/10/13
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class JavaObject<T> {
    T typeDeclaration;
    protected String fullDeclaration;
    protected String declaration;

    public JavaObject() {
    }

    public JavaObject(T t) {
        typeDeclaration = t;
        extractDeclaration();
        extractFullDeclaration();
    }

    abstract void extractFullDeclaration();

    abstract void extractDeclaration();

    String getFullDeclaration() {
        return fullDeclaration;
    }

    String getDeclaration() {
        return declaration;
    }

    @Override
    public String toString() {
        return fullDeclaration;
    }
}

class JavaClassOrInterface extends JavaObject<ClassOrInterfaceDeclaration> {
    HashMap<String, String> methods = new HashMap<String, String>();
    HashMap<String, String> constructors = new HashMap<String, String>();
    HashMap<String, String> enums = new HashMap<String, String>();
    HashMap<String, String> fields = new HashMap<String, String>();

    static final int CLASS = 0;
    static final int INTERFACE = 1;

    int type;

    public JavaClassOrInterface(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        super(classOrInterfaceDeclaration);
    }

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
    void extractDeclaration() {
        declaration = typeDeclaration.getName();
    }

    public void addConstructor(JavaConstructor constructor) {
        constructors.put(constructor.declaration, constructor.fullDeclaration);
    }

    public void addField(JavaField field) {
        fields.put(field.declaration, field.fullDeclaration);
    }

    public void addEnum(JavaEnum javaEnum) {
        enums.put(javaEnum.declaration, javaEnum.fullDeclaration);
    }

    public void addMethod(JavaMethod method) {
        methods.put(method.declaration, method.fullDeclaration);
    }

    HashMap<String, String> getMethods() {
        return methods;
    }

    HashMap<String, String> getConstructors() {
        return constructors;
    }

    HashMap<String, String> getEnums() {
        return enums;
    }

    HashMap<String, String> getFields() {
        return fields;
    }

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


class JavaField extends JavaObject<FieldDeclaration> {

    public JavaField(FieldDeclaration fieldDeclaration) {
        super(fieldDeclaration);
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

class JavaEnum extends JavaObject<EnumDeclaration> {

    public JavaEnum(EnumDeclaration enumDeclaration) {
        super(enumDeclaration);
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

class JavaMethod extends JavaObject<MethodDeclaration> {

    public JavaMethod(MethodDeclaration typeDeclaration) {
        super(typeDeclaration);
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

class JavaConstructor extends JavaObject<ConstructorDeclaration> {

    public JavaConstructor(ConstructorDeclaration typeDeclaration) {
        super(typeDeclaration);
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
                new BlockStmt())
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