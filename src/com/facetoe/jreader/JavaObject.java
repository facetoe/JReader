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
 * Might be better to refactor it as an interface.
 *
 * @param <T>
 */
public abstract class JavaObject<T> {

    // The declaration
    T typeDeclaration;

    // Full declaration including modifiers
    protected String fullDeclaration;

    // Just the name and parameters
    protected String declaration;

    // Line in the source file where this item begins
    protected int beginLine;

    // Line in the source file where this item ends
    // Note, this includes the entire block, not just declarations
    protected int endLine;

    //Column where the declaration begins
    protected int beginColumn;

    //Column where the declaration ends.
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

    // All of this class or interfaces methods, constructors, enums and fields.
    // The key is the name and parameters with the object as the value.
    HashMap<String, JavaClassOrInterface> nestedClasses = new HashMap<String, JavaClassOrInterface>();
    HashMap<String, JavaMethod> methods = new HashMap<String, JavaMethod>();
    HashMap<String, JavaConstructor> constructors = new HashMap<String, JavaConstructor>();
    HashMap<String, JavaEnum> enums = new HashMap<String, JavaEnum>();
    HashMap<String, JavaField> fields = new HashMap<String, JavaField>();

    static final int CLASS = 0;
    static final int INTERFACE = 1;
    int type;

    public JavaClassOrInterface(ClassOrInterfaceDeclaration typeDec) {
        super(typeDec);
        beginColumn = typeDec.getBeginColumn();
        endColumn = typeDec.getEndColumn();
        beginLine = typeDec.getBeginLine();
        endLine = typeDec.getEndLine();
    }

    @Override
    void extractFullDeclaration() {
        // The object passed in from JavaSourceFileParser has lots of information we don't want,
        // such as comments, javadoc etc. Creating a new object and calling toString()
        // makes it much easier to parse
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

    public void addNestedClassOrInterface(JavaClassOrInterface classOrInterface) {
        nestedClasses.put(classOrInterface.declaration, classOrInterface);
    }

    HashMap<String, JavaMethod> getMethods() {
        return methods;
    }

    HashMap<String, JavaConstructor> getConstructors() {
        return constructors;
    }

    HashMap<String, JavaEnum> getEnums() {
        return enums;
    }

    HashMap<String, JavaField> getFields() {
        return fields;
    }

    HashMap<String, JavaClassOrInterface> getNestedClasses() {
        return nestedClasses;
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

    // The object passed in from JavaSourceFileParser has lots of information we don't want,
    // such as comments, javadoc etc. Creating a new object and calling toString()
    // makes it much easier to parse
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

    // The object passed in from JavaSourceFileParser has lots of information we don't want,
    // such as comments, javadoc etc. Creating a new object and calling toString()
    // makes it much easier to parse
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

    // The object passed in from JavaSourceFileParser has lots of information we don't want,
    // such as comments, javadoc etc. Creating a new object and calling toString()
    // makes it much easier to parse
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

    // The object passed in from JavaSourceFileParser has lots of information we don't want,
    // such as comments, javadoc etc. Creating a new object and calling toString()
    // makes it much easier to parse
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