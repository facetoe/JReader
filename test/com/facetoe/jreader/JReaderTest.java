package com.facetoe.jreader;

import com.facetoe.jreader.helpers.Config;
import com.facetoe.jreader.helpers.ProfileManager;
import com.facetoe.jreader.helpers.Util;

import java.io.File;

/**
 * Created by facetoe on 17/01/14.
 */
public class JReaderTest {
    public static void main(String[] args) {
        System.out.println();
    }
    public static ProfileManager pm = ProfileManager.getInstance();
    public static final String BASE_PATH = new File("").getAbsolutePath() + File.separator;
    public static final String TEST_FILE_DIR = BASE_PATH + "testFiles";
    public static final String TEST_PROFILE_NAME = "test";
    public static final String TEST_PROFILE_DOC_DIR = BASE_PATH + "testFiles/swingx-all-1.6.4-javadoc" + File.separator;
    public static final String TEST_PROFILE_SOURCE_DIR = BASE_PATH + "testFiles/swingx-all-1.6.4-sources" + File.separator;
    public static final String TEST_PROFILE_DIR = Util.constructPath(Config.getString(Config.PROFILE_DIR), TEST_PROFILE_NAME);
    public static final String TEST_TEXT_FILE_PATH = BASE_PATH + "testFiles/testTextFile.txt";
}