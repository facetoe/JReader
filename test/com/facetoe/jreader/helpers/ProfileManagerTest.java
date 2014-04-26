package com.facetoe.jreader.helpers;

import com.facetoe.jreader.JReaderTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by facetoe on 16/01/14.
 */

public class ProfileManagerTest extends JReaderTest {

    @BeforeClass
    public static void setUpClass() {
        System.out.println("Setting up class");
        pm = ProfileManager.getInstance();
    }

    @AfterClass
    public static void tearDownClass() {
        for (String profileName : pm.getProfileNames()) {
            if (!profileName.equals(Config.DEFAULT_PROFILE_NAME))
                pm.deleteProfile(profileName);
        }
    }

    @Test
    public void testNewProfile() throws Exception {
        pm.newProfile(TEST_PROFILE_NAME, new File(TEST_PROFILE_DOC_DIR), new File(TEST_PROFILE_SOURCE_DIR));
        assertEquals(true, (Util.getFileFromPathElements(TEST_PROFILE_DIR, TEST_PROFILE_NAME + ".ser")).exists());
        assertEquals(true, (Util.getFileFromPathElements(TEST_PROFILE_DIR)).exists());
        assertEquals(true, (Util.getFileFromPathElements(TEST_PROFILE_DIR, Config.CLASS_DATA_FILE_NAME)).exists());
    }

    @Test
    public void testSetCurrentProfile() throws Exception {
        pm.setCurrentProfile(Config.DEFAULT_PROFILE_NAME);
        assertEquals(Config.DEFAULT_PROFILE_NAME, pm.getCurrentProfileName());
    }

    @Test
    public void testLoadProfiles() throws Exception {
        pm.loadProfiles();
        ArrayList<String> profiles = pm.getProfileNames();
        for (String profileName : profiles) {
            pm.setCurrentProfile(profileName);
            assertEquals(profileName, pm.getCurrentProfileName());
        }
    }

    @Test
    public void testCurrentProfile() throws Exception {
        pm.newProfile(TEST_PROFILE_NAME, new File(TEST_PROFILE_DOC_DIR), new File(TEST_PROFILE_SOURCE_DIR));
        pm.setCurrentProfile(TEST_PROFILE_NAME);
        assertEquals(TEST_PROFILE_NAME, pm.getCurrentProfileName());
        assertEquals(TEST_PROFILE_DIR, pm.getPath());
    }

    @Test
    public void testSaveProfiles() throws Exception {
        for (int i = 0; i < 10; i++) {
            pm.newProfile(TEST_PROFILE_NAME, new File(TEST_PROFILE_DOC_DIR), new File(TEST_PROFILE_SOURCE_DIR));
        }
        pm.saveProfiles();
        ArrayList<String> profiles = pm.getProfileNames();
        File[] profileDirs = getProfileDirs();

        Collections.sort(profiles);
        Arrays.sort(profileDirs);
        assertEquals(true, (profileDirs.length == profiles.size()));
        int len = profiles.size();
        for (int i = 0; i < len; i++) {
            assertEquals(true, (profiles.get(i).equals(profileDirs[i].getName())));
            assertEquals(2, profileDirs[i].listFiles().length); // There are two files generated for each profile.
        }
    }

    private File[] getProfileDirs() {
        File profileDir = new File(Config.getString(Config.PROFILE_DIR));
        return profileDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
    }

    @Test
    public void testGetProfileNames() throws Exception {
        assertNotNull(pm.getProfileNames());
    }

    @Test
    public void testGetClassNames() throws Exception {
        assertNotNull(pm.getClassNames());
    }

    @Test
    public void testGetClassData() throws Exception {
        assertNotNull(pm.getClassData());
    }

    @Test
    public void testDeleteProfile() throws Exception {
        pm.deleteProfile(TEST_PROFILE_NAME);
        assertEquals(false, (Util.getFileFromPathElements(TEST_PROFILE_DIR, TEST_PROFILE_NAME + ".ser")).exists());
        assertEquals(false, (Util.getFileFromPathElements(TEST_PROFILE_DIR, Config.CLASS_DATA_FILE_NAME)).exists());
        assertEquals(false, (Util.getFileFromPathElements(TEST_PROFILE_DIR)).exists());
    }
}
