package com.facetoe.jreader;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.*;
import japa.parser.ast.visitor.GenericVisitorAdapter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class JavaSourceFileParser {

    public static void main(String[] args) {
        try {
            JavaSourceFile file = parse(new FileInputStream("/home/facetoe/.jreader/src-jdk/java/awt/Container.java"));
            for ( String s : file.getAllDeclarations() ) {
                System.out.println(s);
            }
        } catch ( FileNotFoundException ex ) {
            ex.printStackTrace();
        } catch ( IOException ex ) {
            ex.printStackTrace();
        } catch ( ParseException ex ) {
            ex.printStackTrace();
        }
    }

    /**
     * Parses a Java source file and extracts constructor, method, field and enum declarations.
     *
     * @param inputStream for the file being parsed
     * @return JavaSourceFile object that contains the result of the parsing.
     * @throws ParseException
     * @throws IOException
     */
    public static JavaSourceFile parse(FileInputStream inputStream) throws ParseException, IOException {
        CompilationUnit cu = null;
        try {
            cu = JavaParser.parse(inputStream);
        } finally {
            inputStream.close();
        }
        return ( JavaSourceFile ) new SourceFileVisitor().visit(cu, null);
    }

    /**
     * This class is responsible for extracting the information out of the file.
     */
    private static class SourceFileVisitor extends GenericVisitorAdapter {

        /**
         * Visits each type declaration and extracts full and partial declarations from it.
         *
         * @param cu  Compilation unit returned by JavaParser.parse()
         * @param arg This isn't used.
         * @return JavaSourceFile object containing all the data.
         */
        @Override
        public Object visit(CompilationUnit cu, Object arg) {
            ArrayList<JavaClassOrInterface> classesAndInterfaces = new ArrayList<JavaClassOrInterface>();
            if ( cu != null && cu.getTypes() != null ) {
                for ( TypeDeclaration type : cu.getTypes() ) {

                    // Visit each class declaration and extract all the data from it.
                    if ( type instanceof ClassOrInterfaceDeclaration ) {
                            JavaClassOrInterface javaObj = (JavaClassOrInterface)visit((ClassOrInterfaceDeclaration)type, null);
                            classesAndInterfaces.add(javaObj);
                    }
                }
            } else {
                System.err.println("Null");
            }
            return new JavaSourceFile(classesAndInterfaces);
        }

        @Override
        public Object visit(ClassOrInterfaceDeclaration n, Object arg) {
            // Store each class or interface's data in this object
            JavaClassOrInterface javaObj = new JavaClassOrInterface(n);

            for ( BodyDeclaration declaration : n.getMembers() ) {
                if ( declaration instanceof MethodDeclaration ) {
                    JavaMethod method = ( JavaMethod ) visit(( MethodDeclaration ) declaration, null);
                    javaObj.addMethod(method);

                } else if ( declaration instanceof FieldDeclaration ) {
                    JavaField field = ( JavaField ) visit(( FieldDeclaration ) declaration, null);
                    javaObj.addField(field);

                } else if ( declaration instanceof EnumDeclaration ) {
                    JavaEnum javaEnum = ( JavaEnum ) visit(( EnumDeclaration ) declaration, null);
                    javaObj.addEnum(javaEnum);

                } else if ( declaration instanceof ConstructorDeclaration ) {
                    JavaConstructor constructor = ( JavaConstructor ) visit(( ConstructorDeclaration ) declaration, null);
                    javaObj.addConstructor(constructor);


                    /* If we have a nested class, recurse through it and each nested class that it contains
                     * gathering data.  */
                }else if (declaration instanceof ClassOrInterfaceDeclaration) {
                    JavaClassOrInterface classOrInterface = (JavaClassOrInterface)visit((ClassOrInterfaceDeclaration) declaration, null);
                    javaObj.addNestedClassOrInterface(classOrInterface);

                } else {
                    // As far as I can tell this only catches annotations, which might be useful to add later.
                }
            }
            return javaObj;
        }

        public Object parseClassData(ClassOrInterfaceDeclaration classDec) {
            return new JavaClassOrInterface(classDec).getDeclaration();
        }

        @Override
        public Object visit(ConstructorDeclaration n, Object arg) {
            return new JavaConstructor(n);
        }

        @Override
        public Object visit(EnumConstantDeclaration n, Object arg) {
            System.out.println("Called enumConstant");
            System.exit(0); //TODO remove this!
            return null;
        }

        @Override
        public Object visit(EnumDeclaration n, Object arg) {
            return new JavaEnum(n);
        }

        @Override
        public Object visit(FieldDeclaration n, Object arg) {
            return new JavaField(n);
        }

        @Override
        public Object visit(MethodDeclaration n, Object arg) {
            return new JavaMethod(n);
        }
    }
}

