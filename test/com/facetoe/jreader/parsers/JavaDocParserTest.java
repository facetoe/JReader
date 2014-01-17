package com.facetoe.jreader.parsers;

import com.facetoe.jreader.JReaderTest;
import com.sun.java_cup.internal.runtime.lr_parser;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * Created by facetoe on 17/01/14.
 */
public class JavaDocParserTest extends JReaderTest {
    int JAVA7_CLASSES_NUM = 3891;
    int JAVA6_CLASSES_NUM = 3663;
    int SWINGX_CLASSES_NUM = 477;
    public static JavaDocParser parser;

    @BeforeClass
    public static void setUpClass() {
        parser = new JavaDocParser();
    }

    @Test
    public void testParseJava7Docs() throws Exception {
        HashMap<String, String> classData = parser.parse(new File(JAVA7_DOC_DIR + "allclasses-noframe.html"));
        assertEquals(JAVA7_CLASSES_NUM, classData.size());
    }

    @Test
    public void testParseJava6Docs() throws Exception {
        HashMap<String, String> classData = parser.parse(new File(JAVA6_DOC_DIR + "allclasses-noframe.html"));
        assertEquals(JAVA6_CLASSES_NUM, classData.size());
    }

    @Test
    public void testParseSwingXDocs() throws Exception {
        HashMap<String, String> classData = parser.parse(new File(TEST_PROFILE_DOC_DIR + File.separator + "allclasses-noframe.html"));
        assertEquals(SWINGX_CLASSES_NUM, classData.size());
    }
}
