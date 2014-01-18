package com.facetoe.jreader.parsers;

import com.facetoe.jreader.JReaderTest;
import com.facetoe.jreader.helpers.Config;
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
    int SWINGX_CLASSES_NUM = 477;
    public static JavaDocParser parser;

    @BeforeClass
    public static void setUpClass() {
        parser = new JavaDocParser();
    }

    @Test
    public void testParseJava7Docs() throws Exception {
        pm.setCurrentProfile(Config.DEFAULT_PROFILE_NAME);
        HashMap<String, String> classData = parser.parse(new File(pm.getDocDir() + "allclasses-noframe.html"));
        assertEquals(JAVA7_CLASSES_NUM, classData.size());
    }

    @Test
    public void testParseSwingXDocs() throws Exception {
        HashMap<String, String> classData = parser.parse(new File(TEST_PROFILE_DOC_DIR + File.separator + "allclasses-noframe.html"));
        assertEquals(SWINGX_CLASSES_NUM, classData.size());
    }
}
