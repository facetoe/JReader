package com.facetoe.jreader.helpers;

import com.facetoe.jreader.parsers.JavaDocParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Created by facetoe on 16/01/14.
 */
public class UtilitiesTest extends JReaderTest {

    private static HashMap<String, String> links;


    private final static String TEST_DOC_DIR = "/home/facetoe/IdeaProjects/JReader/testFiles/swingx-all-1.6.4-javadoc/";
    private final static String ALL_CLASSES_DOCFILE = "/swingx-all-1.6.4-javadoc/allclasses-noframe.html";

    @BeforeClass
    public static void setUpClass() throws Exception {
        pm = ProfileManager.getInstance();
        pm.newProfile(testProfileName, testProfileDocDir, testProfileSourceDir);
        pm.setCurrentProfile(testProfileName);
        JavaDocParser parser = new JavaDocParser();
        links = parser.parse(new File(UtilitiesTest.class.getResource(ALL_CLASSES_DOCFILE).toURI()));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        pm.deleteProfile(testProfileName);
    }

    @Test
    public void testReadFile() throws Exception { // TODO this is dumb, choose a better test file
        String fileData = Utilities.readFile(Config.configFilePath, Charset.forName("UTF-8"));
        assertNotNull(fileData);
        assertEquals(false, fileData.isEmpty());
    }

    @Test
    public void testDocPathToSourcePath() throws Exception {
        File testFile;
        for (String s : links.keySet()) {
            String sourcePath = Utilities.docPathToSourcePath(TEST_DOC_DIR + File.separator + links.get(s));
            testFile = new File(sourcePath);
            assertEquals(true, testFile.exists());
        }
    }

    @Test
    public void testBrowserPathToSystemPath() throws Exception {
        File testFile;
        for (String path : links.keySet()) {
            String systemPath = Utilities.browserPathToSystemPath("file://" + TEST_DOC_DIR + links.get(path));
            testFile = new File(systemPath);
            assertEquals(true, testFile.exists());
        }
    }

    @Test
    public void testExtractTitle() throws Exception {
        for (String className : links.keySet()) {
            String htmlPath = Utilities.browserPathToSystemPath("file://" + TEST_DOC_DIR + links.get(className));
            String title = Utilities.extractTitle(htmlPath);
            assertEquals(true, title.toLowerCase().contains(className.toLowerCase()));
        }
    }

    @Test
    public void testExtractFileName() throws Exception {
        for (String className : links.keySet()) {
            assertEquals(true, className.equals(Utilities.extractFileName(className)));
        }
    }

    @Test
    public void testIsGoodSourcePath() throws Exception {

    }

    @Test
    public void testCheckAndCreateFileIfNotPresent() throws Exception {

    }

    @Test
    public void testCheckAndCreateDirectoryIfNotPresent() throws Exception {

    }

    @Test
    public void testGetFile() throws Exception {

    }

    @Test
    public void testConstructPath() throws Exception {

    }

    @Test
    public void testIsJavaDocsDir() throws Exception {

    }

    @Test
    public void testGetHomePage() throws Exception {

    }

    @Test
    public void testFindDocDir() throws Exception {

    }

    @Test
    public void testDeleteDirectoryAndContents() throws Exception {

    }

    @Test
    public void testReadIcon() throws Exception {

    }

    @Test
    public void testReadIcon1() throws Exception {

    }
}
