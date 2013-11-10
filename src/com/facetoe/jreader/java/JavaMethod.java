package com.facetoe.jreader.java;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 1/11/13
 * Time: 12:02 PM
 */

import japa.parser.ast.body.MethodDeclaration;

/**
 * Represents a method.
 */
public class JavaMethod extends AbstractJavaObject<MethodDeclaration> {

    public JavaMethod(MethodDeclaration typeDec) {
        super(typeDec);
        beginColumn = typeDec.getBeginColumn();
        endColumn = typeDec.getEndColumn();
        beginLine = typeDec.getBeginLine();
        endLine = typeDec.getEndLine();
        type = METHOD;
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

    @Override
    public int getModifiers() {
        return typeDeclaration.getModifiers();
    }
}
