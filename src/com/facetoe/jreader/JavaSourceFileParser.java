package com.facetoe.jreader;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class JavaSourceFileParser {

    public static void main(String[] args) {
        File dir = new File("/home/facetoe/.jreader/src-jdk/java/awt/");
        File[] files = dir.listFiles();
        int errors = 0;
        int parsedFiles = 0;
        long startTime = System.nanoTime();
        if ( files != null ) {
            for ( File file : files ) {
                if ( file.getName().contains(".java") ) {
                    System.out.println(file.getName());
                    try {
                        JavaSourceFile sourceFile = parse(new FileInputStream(file));
                        System.out.println(sourceFile.getAllDeclarations().size());
                        parsedFiles++;
                    } catch ( ParseException e ) {
                        errors++;
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    }
                }
            }
        }
        long estimatedTime = System.nanoTime() - startTime;

        System.out.println(String.format("Parsed %d files in %.2f seconds with %d errors.", parsedFiles, (estimatedTime / 1000000000.0), errors));
    }

    public static JavaSourceFile parse(FileInputStream inputStream) throws ParseException, IOException {
        CompilationUnit cu = null;
        try {
            cu = JavaParser.parse(inputStream);
        } finally {
            inputStream.close();
        }
        return ( JavaSourceFile ) new SourceFileVisitor().visit(cu, null);
    }
}

