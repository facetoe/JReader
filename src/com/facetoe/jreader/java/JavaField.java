package com.facetoe.jreader.java;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 1/11/13
 * Time: 12:02 PM
 */

import japa.parser.ast.body.FieldDeclaration;

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
        type = FIELD;
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

    @Override
    public int getModifiers() {
        return typeDeclaration.getModifiers();
    }
}