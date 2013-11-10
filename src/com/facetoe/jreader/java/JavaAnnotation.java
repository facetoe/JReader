package com.facetoe.jreader.java;

import japa.parser.ast.body.AnnotationDeclaration;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 3/11/13
 * Time: 10:35 AM
 */
public class JavaAnnotation extends AbstractJavaObject<AnnotationDeclaration> {

//    private ArrayList<>

    public JavaAnnotation(AnnotationDeclaration typeDec) {
        super(typeDec);
        beginColumn = typeDec.getBeginColumn();
        endColumn = typeDec.getEndColumn();
        beginLine = typeDec.getBeginLine();
        endLine = typeDec.getEndLine();
        type = ANNOTATION;
    }

    @Override
    void extractFullDeclaration() {
        fullDeclaration = new AnnotationDeclaration(typeDeclaration.getModifiers(), typeDeclaration.getName())
                .toString()
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
}
