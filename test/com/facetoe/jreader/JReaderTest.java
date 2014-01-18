package com.facetoe.jreader;

import com.facetoe.jreader.helpers.Config;
import com.facetoe.jreader.helpers.ProfileManager;
import com.facetoe.jreader.helpers.Utilities;

/**
 * Created by facetoe on 17/01/14.
 */
public class JReaderTest {
    public static ProfileManager pm = ProfileManager.getInstance();
    public static final String BASE_PATH = "/home/facetoe/IdeaProjects/JReader/";
    public static final String TEST_FILE_DIR = BASE_PATH + "testFiles";
    public static final String TEST_PROFILE_NAME = "test";
    public static final String TEST_PROFILE_DOC_DIR = BASE_PATH + "testFiles/swingx-all-1.6.4-javadoc/";
    public static final String TEST_PROFILE_SOURCE_DIR = BASE_PATH + "testFiles/swingx-all-1.6.4-sources/";
    public static final String TEST_PROFILE_DIR = Utilities.constructPath(Config.getString(Config.PROFILE_DIR), TEST_PROFILE_NAME);
    public static final String TEST_TEXT_FILE_PATH = BASE_PATH + "testFiles/testTextFile.txt";
    public static final String JAVA7_DOC_DIR = "/home/facetoe/ebooks/Java/java7Docs/docs/api/";
    public static final String JAVA6_DOC_DIR = "/home/facetoe/ebooks/Java/java6Docs/docs/api/";
}