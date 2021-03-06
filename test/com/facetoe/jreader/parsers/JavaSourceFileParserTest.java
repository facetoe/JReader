package com.facetoe.jreader.parsers;

import com.facetoe.jreader.JReaderTest;
import com.facetoe.jreader.helpers.Config;
import japa.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 * Created by facetoe on 15/01/14.
 */
public class JavaSourceFileParserTest extends JReaderTest {

    //This takes a while as it parses every Java source file
    @org.junit.Test
    public void testParse() throws Exception {
        pm.setCurrentProfile(Config.DEFAULT_PROFILE_NAME);
        recursiveTest(new File(pm.getSrcDir()));
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
