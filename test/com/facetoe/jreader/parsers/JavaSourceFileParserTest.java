package com.facetoe.jreader.parsers;

import japa.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 * Created by facetoe on 15/01/14.
 */
public class JavaSourceFileParserTest {

    @org.junit.Test
    // This takes a while as it parses every Java source file
    public void testParse() throws Exception {
        recursiveTest(new File("/home/facetoe/.jreader/src-jdk"));
    }

    private static void recursiveTest(File dir) throws ParseException, IOException {
        File[] contents = dir.listFiles();
        if (contents != null) {
            for (File content : contents) {
                if (content.isDirectory()) {
                    recursiveTest(content);
                } else if (content.getName().endsWith(".java")) {
                    assertNotNull(JavaSourceFileParser.parse(new FileInputStream(content)));
                }
            }
        }
    }

}
