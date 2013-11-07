package com.facetoe.jreader.java;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.*;
import japa.parser.ast.visitor.GenericVisitorAdapter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class parses a Java source file and extracts all the classes, interfaces, methods, constructors and fields.
 */
public class JavaSourceFileParser {
    private static final Logger log = Logger.getLogger(JavaSourceFileParser.class);


    public static void main(String[] args) {
        try {
            parse(new FileInputStream("/home/facetoe/.jreader/src-jdk/java/nio/file/StandardCopyOption.java"));
        } catch ( ParseException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

//        File startDir = new File("/home/facetoe/.jreader/src-jdk/");
//        recursiveTest(startDir);
    }

    public static void recursiveTest(File dir) {
        File[] contents = dir.listFiles();
        for ( File content : contents ) {
            if ( content.isDirectory() ) {
                recursiveTest(content);
            } else {
                if ( content.getName().endsWith(".java") ) {
                    try {
                        parse(new FileInputStream(content));
                    } catch ( ParseException e ) {
                        e.printStackTrace();
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    }
                }
            }
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
        log.debug("Parsing source file...");
        CompilationUnit cu = null;
        try {
            cu = JavaParser.parse(inputStream);
        } finally {
            inputStream.close();
        }
        log.debug("Parsing complete.");
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
            JavaSourceFile sourceFile = new JavaSourceFile();
            if ( cu != null && cu.getTypes() != null ) {
                for ( TypeDeclaration type : cu.getTypes() ) {

                    // Visit each class declaration and extract all the data from it.
                    if ( type instanceof ClassOrInterfaceDeclaration ) {
                        JavaClassOrInterface javaObj = ( JavaClassOrInterface ) visit(( ClassOrInterfaceDeclaration ) type, null);
                        sourceFile.addObject(javaObj);

                    } else if ( type instanceof AnnotationDeclaration ) {
                        System.out.println("Annotation");
                        JavaAnnotation annotation = ( JavaAnnotation ) visit(( AnnotationDeclaration ) type, null);
                        sourceFile.addObject(annotation);

                    } else if (type instanceof EnumDeclaration) {
                        JavaEnum javaEnum = (JavaEnum)visit((EnumDeclaration)type, null);
                        sourceFile.addObject(javaEnum);


                    } else {
                        System.out.println(type.getName());
                        System.out.println(type.getClass());
                        System.out.println();
                    }
                }
            } else {
                log.debug("Class or interface was null");
            }
            return sourceFile;
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
                     * gathering the data.  */
                } else if ( declaration instanceof ClassOrInterfaceDeclaration ) {
                    JavaClassOrInterface classOrInterface = ( JavaClassOrInterface ) visit(( ClassOrInterfaceDeclaration ) declaration, null);
                    javaObj.addNestedClassOrInterface(classOrInterface);

                } else if ( declaration instanceof AnnotationDeclaration ) {
                    JavaAnnotation annotation = ( JavaAnnotation ) visit(( AnnotationDeclaration ) declaration, null);
                    javaObj.addAnnotation(annotation);
                } // The only other things that would be caught here are EmptyTypeDeclaration, EmptyMemberDeclaration and InitializerDeclaration

            }
            return javaObj;
        }

        @Override
        public Object visit(ConstructorDeclaration n, Object arg) {
            return new JavaConstructor(n);
        }

        @Override
        public Object visit(EnumConstantDeclaration n, Object arg) {
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

        @Override
        public Object visit(AnnotationDeclaration n, Object arg) {
            return new JavaAnnotation(n);
        }
    }
}

