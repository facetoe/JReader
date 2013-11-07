package com.facetoe.jreader.java;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 1/11/13
 * Time: 12:01 PM
 */

import japa.parser.ast.body.EnumConstantDeclaration;
import japa.parser.ast.body.EnumDeclaration;

import java.util.HashMap;

/**
 * Represents an enum.
 */
public class JavaEnum extends JavaObject<EnumDeclaration> {
    private HashMap<String, JavaEnumConstantDeclaration> constants = new HashMap<String, JavaEnumConstantDeclaration>();

    public JavaEnum(EnumDeclaration typeDec) {
        super(typeDec);
        extractFullDeclaration();
        extractDeclaration();
        extractConstants();
        beginColumn = typeDec.getBeginColumn();
        endColumn = typeDec.getEndColumn();
        beginLine = typeDec.getBeginLine();
        endLine = typeDec.getEndLine();
        type = ENUM;
    }

    private void extractConstants() {
        for ( EnumConstantDeclaration constant : typeDeclaration.getEntries() ) {
            constants.put(constant.getName(), new JavaEnumConstantDeclaration(constant));
        }
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

    @Override
    public int getModifiers() {
        return typeDeclaration.getModifiers();
    }

    public HashMap<String, JavaEnumConstantDeclaration> getConstants() {
        return constants;
    }

    public JavaEnumConstantDeclaration getConstant(String name) {
        return constants.get(name);
    }
}

class JavaEnumConstantDeclaration extends JavaObject<EnumConstantDeclaration> {

    public JavaEnumConstantDeclaration(EnumConstantDeclaration typeDec) {
        super(typeDec);
        extractDeclaration();
        extractFullDeclaration();
        beginColumn = typeDec.getBeginColumn();
        endColumn = typeDec.getEndColumn();
        beginLine = typeDec.getBeginLine();
        endLine = typeDec.getEndLine();
        type = ENUM;
    }

    @Override
    void extractFullDeclaration() {
        fullDeclaration = typeDeclaration.getName();
    }

    @Override
    void extractDeclaration() {
        declaration = typeDeclaration.getName();
    }

    @Override
    public int getModifiers() {
        return -1;  // No modifiers for constants.
    }
}