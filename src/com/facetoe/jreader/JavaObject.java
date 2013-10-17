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
public abstract class JavaObject<T> {
    T typeDeclaration;
    protected String fullDeclaration;
    protected String declaration;
    protected int beginLine;
    protected int endLine;
    protected int beginColumn;
    protected int endColumn;

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

    public int getBeginLine() {
        return beginLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getBeginColumn() {
        return beginColumn;
    }

    public int getEndColumn() {
        return endColumn;
    }

    @Override
    public String toString() {
        return fullDeclaration;
    }
}

class JavaClassOrInterface extends JavaObject<ClassOrInterfaceDeclaration> {
    HashMap<String, JavaObject> methods = new HashMap<String, JavaObject>();
    HashMap<String, JavaObject> constructors = new HashMap<String, JavaObject>();
    HashMap<String, JavaObject> enums = new HashMap<String, JavaObject>();
    HashMap<String, JavaObject> fields = new HashMap<String, JavaObject>();

    static final int CLASS = 0;
    static final int INTERFACE = 1;

    int type;

    public JavaClassOrInterface(ClassOrInterfaceDeclaration typedec) {
        super(typedec);
        beginColumn = typedec.getBeginColumn();
        endColumn = typedec.getEndColumn();
        beginLine = typedec.getBeginLine();
        endLine = typedec.getEndLine();
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
        constructors.put(constructor.declaration, constructor);
    }

    public void addField(JavaField field) {
        fields.put(field.declaration, field);
    }

    public void addEnum(JavaEnum javaEnum) {
        enums.put(javaEnum.declaration, javaEnum);
    }

    public void addMethod(JavaMethod method) {
        methods.put(method.declaration, method);
    }

    HashMap<String, JavaObject> getMethods() {
        return methods;
    }

    HashMap<String, JavaObject> getConstructors() {
        return constructors;
    }

    HashMap<String, JavaObject> getEnums() {
        return enums;
    }

    HashMap<String, JavaObject> getFields() {
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