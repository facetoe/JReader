package com.facetoe.jreader.helpers;

import com.facetoe.jreader.JReaderTest;
import com.facetoe.jreader.parsers.JavaDocParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Created by facetoe on 16/01/14.
 */
public class UtilitiesTest extends JReaderTest {

    private static HashMap<String, String> links;
    private final static String TEST_DOC_DIR = BASE_PATH + "testFiles/swingx-all-1.6.4-javadoc/";
    private final static String ALL_CLASSES_DOCFILE = BASE_PATH + "testFiles/swingx-all-1.6.4-javadoc/allclasses-noframe.html";

    private static final String TEST_CREATE_DIR_PATH = TEST_FILE_DIR + File.separator + "testDir";
    private static final String TEST_DELETE_DIR_PATH = TEST_CREATE_DIR_PATH + "Deletion";
    private static final String TEST_CREATE_FILE_PATH = TEST_FILE_DIR + File.separator + "testFile";

    @BeforeClass
    public static void setUpClass() throws Exception {
        pm.newProfile(TEST_PROFILE_NAME, new File(TEST_PROFILE_DOC_DIR), new File(TEST_PROFILE_SOURCE_DIR));
        pm.setCurrentProfile(TEST_PROFILE_NAME);
        JavaDocParser parser = new JavaDocParser();
        links = parser.parse(new File(ALL_CLASSES_DOCFILE));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        pm.deleteProfile(TEST_PROFILE_NAME);
        File testDir = new File(TEST_CREATE_DIR_PATH);
        if(testDir.exists())
            testDir.delete();
        File testFile = new File(TEST_CREATE_FILE_PATH);
        if(testFile.exists())
            testFile.delete();
    }

    @Test
    public void testReadFile() throws Exception {
        String fileText = Utilities.readFile(TEST_TEXT_FILE_PATH, Charset.forName("UTF-8"));
        assertEquals("This is a text file.", fileText);
    }

    @Test
    public void testDocPathToSourcePath() throws Exception {
        File testFile;
        for (String className : links.keySet()) {
            String sourcePath = Utilities.docPathToSourcePath(TEST_DOC_DIR + File.separator + links.get(className));
            testFile = new File(sourcePath);
            assertEquals(true, testFile.exists());
        }
    }

    @Test
    public void testBrowserPathToSystemPath() throws Exception {
        File testFile;
        for (String className : links.keySet()) {
            String systemPath = Utilities.browserPathToSystemPath("file://" + TEST_DOC_DIR + links.get(className));
            testFile = new File(systemPath);
            assertEquals(true, testFile.exists());
        }
    }

    @Test
    public void testExtractTitle() throws Exception {
        for (String className : links.keySet()) {
            String htmlPath = Utilities.browserPathToSystemPath("file://" + TEST_DOC_DIR + links.get(className));
            String title = Utilities.extractTitle(htmlPath);
            assertEquals(true, title.contains(className));
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
        File testSourceDir = new File(TEST_PROFILE_SOURCE_DIR);
        ArrayList<File> javaFiles = getFilesForSuffix(testSourceDir, ".java");
        for (File javaFile : javaFiles) {
            assertEquals(true, Utilities.isGoodSourcePath(javaFile.getAbsolutePath()));
        }
    }

    @Test
    public void testIsNotGoodSourcePath() throws Exception {
        File testDocDir = new File(TEST_PROFILE_DOC_DIR);
        ArrayList<File> htmlFiles = getFilesForSuffix(testDocDir, ".html");
        for (File htmlFile : htmlFiles) {
            assertEquals(false, Utilities.isGoodSourcePath(htmlFile.getAbsolutePath()));
        }
    }

    private ArrayList<File> getFilesForSuffix(File parentDirectory, String suffix) {
        ArrayList<File> sourceFiles = new ArrayList<File>();
        for (File file : parentDirectory.listFiles()) {
            if(file.isDirectory()) {
                sourceFiles.addAll(getFilesForSuffix(file, suffix));
            } else if (file.getName().endsWith(suffix)) {
                sourceFiles.add(file);
            }
        }
        return sourceFiles;
    }

    @Test
    public void testCheckAndCreateFileIfNotPresent() throws Exception {
        File testFile = new File(TEST_CREATE_FILE_PATH);
        Utilities.checkAndCreateDirectoryIfNotPresent(testFile);
        assertEquals(true, testFile.exists());
    }

    @Test
    public void testCheckAndCreateDirectoryIfNotPresent() throws Exception {
        File testDir = new File(TEST_CREATE_DIR_PATH);
        Utilities.checkAndCreateDirectoryIfNotPresent(testDir);
        assertEquals(true, testDir.exists());
    }

    @Test
    public void testGetFile() throws Exception {
        File testTextFile = Utilities.getFileFromPathElements(BASE_PATH, "testFiles", "testTextFile.txt");
        assertEquals(true, testTextFile.exists());
    }

    @Test
    public void testConstructPath() throws Exception {
        String[] pathElements = ALL_CLASSES_DOCFILE.split(File.pathSeparator);
        String path = Utilities.constructPath(pathElements);
        assertEquals(true, new File(path).exists());
    }

    @Test
    public void testIsJavaDocsDir() throws Exception {
        assertEquals(true, Utilities.isJavaDocsDir(new File(TEST_PROFILE_DOC_DIR)));
    }

    @Test
    public void testNotJavaDocsDir() throws Exception {
        assertEquals(false, Utilities.isJavaDocsDir(new File(BASE_PATH)));
    }

    @Test
    public void testGetHomePage() throws Exception {
        String homePage = Utilities.getHomePage(TEST_PROFILE_DOC_DIR);
        assertEquals(true, homePage.equals("overview-summary.html"));
    }

    @Test
    public void testDeleteDirectoryAndContents() throws Exception {
        File toDeleteDir = new File(TEST_DELETE_DIR_PATH);
        generateNestedDirectory(toDeleteDir, 0, 20);
        Utilities.deleteDirectoryAndContents(toDeleteDir);
        assertEquals(false, toDeleteDir.exists());
    }

    private void generateNestedDirectory(File rootDirectory, int current, int max) throws IOException {
        if(current == max)
            return;
        rootDirectory.mkdirs();
        String dummyFilePath = rootDirectory.getAbsolutePath() + File.separator + "dummyFile.txt";
        new File(dummyFilePath).createNewFile();
        File nextLevelDir = new File(rootDirectory.getAbsolutePath() + File.separator + "testDeletion" + current);
        generateNestedDirectory(nextLevelDir, current+1, max);
    }
}
