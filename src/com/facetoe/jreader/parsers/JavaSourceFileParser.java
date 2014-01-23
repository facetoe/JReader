/*   
*    Copyright (C) 2013  facetoe - facetoe@ymail.com
*
*    This program is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation; either version 2 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License along
*    with this program; if not, write to the Free Software Foundation, Inc.,
*    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package com.facetoe.jreader.parsers;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.*;
import japa.parser.ast.visitor.GenericVisitorAdapter;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class parses a Java source file and extracts all the classes, interfaces, methods, constructors and fields.
 */
public class JavaSourceFileParser {
    private static final Logger log = Logger.getLogger(JavaSourceFileParser.class);

    /**
     * Parses a Java source file and extracts constructor, method, field and enum declarations.
     *
     * @param inputStream for the file being parsed
     * @return JavaSourceFile object that contains the result of the parsing.
     * @throws ParseException
     * @throws IOException
     */
    public static JavaSourceFile parse(InputStream inputStream) throws ParseException, IOException {
        CompilationUnit cu = null;
        try {
            cu = JavaParser.parse(inputStream);
        } finally {
            inputStream.close();
        }
        return (JavaSourceFile) new SourceFileVisitor().visit(cu, null);
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
            if (cu != null && cu.getTypes() != null) {
                for (TypeDeclaration type : cu.getTypes()) {
                    if (type instanceof ClassOrInterfaceDeclaration) {
                        JavaClassOrInterface javaObj = (JavaClassOrInterface) visit((ClassOrInterfaceDeclaration) type, null);
                        sourceFile.addObject(javaObj);

                    } else if (type instanceof AnnotationDeclaration) {
                        JavaAnnotation annotation = (JavaAnnotation) visit((AnnotationDeclaration) type, null);
                        sourceFile.addObject(annotation);

                    } else if (type instanceof EnumDeclaration) {
                        JavaEnum javaEnum = (JavaEnum) visit((EnumDeclaration) type, null);
                        sourceFile.addObject(javaEnum);
                    }
                }
            }

            /* Extract all the data here so we don't forget to later... */
            sourceFile.extractAllObjectData();
            return sourceFile;
        }

        @Override
        public Object visit(ClassOrInterfaceDeclaration n, Object arg) {
            JavaClassOrInterface javaObj = new JavaClassOrInterface(n);

            for (BodyDeclaration declaration : n.getMembers()) {
                if (declaration instanceof MethodDeclaration) {
                    JavaMethod method = (JavaMethod) visit((MethodDeclaration) declaration, null);
                    javaObj.addMethod(method);

                } else if (declaration instanceof FieldDeclaration) {
                    JavaField field = (JavaField) visit((FieldDeclaration) declaration, null);
                    javaObj.addField(field);

                } else if (declaration instanceof EnumDeclaration) {
                    JavaEnum javaEnum = (JavaEnum) visit((EnumDeclaration) declaration, null);
                    javaObj.addEnum(javaEnum);

                } else if (declaration instanceof ConstructorDeclaration) {
                    JavaConstructor constructor = (JavaConstructor) visit((ConstructorDeclaration) declaration, null);
                    javaObj.addConstructor(constructor);

                    /* If we have a nested class, recurse through it and each nested class that it contains
                     * gathering the data.  */
                } else if (declaration instanceof ClassOrInterfaceDeclaration) {
                    JavaClassOrInterface classOrInterface = (JavaClassOrInterface) visit((ClassOrInterfaceDeclaration) declaration, null);
                    javaObj.addNestedClassOrInterface(classOrInterface);

                } else if (declaration instanceof AnnotationDeclaration) {
                    JavaAnnotation annotation = (JavaAnnotation) visit((AnnotationDeclaration) declaration, null);
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

