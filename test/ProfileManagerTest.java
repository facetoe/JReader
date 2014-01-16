
import com.facetoe.jreader.helpers.Config;
import com.facetoe.jreader.helpers.ProfileManager;
import com.facetoe.jreader.helpers.Utilities;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Created by facetoe on 16/01/14.
 */
public class ProfileManagerTest {
    static ProfileManager pm;
    static final String testProfileName = "test";
    static final String testProfileDocDir = "/home/facetoe/IdeaProjects/JReader/testFiles/swingx-all-1.6.4-javadoc";
    static final String testProfileSourceDir = "/home/facetoe/IdeaProjects/JReader/testFiles/swingx-all-1.6.4-sources";
    static final String testProfileDir = Utilities.constructPath(Config.getString(Config.PROFILE_DIR), testProfileName);
    static final String defaultProfileName = Config.DEFAULT_PROFILE_NAME;

    @BeforeClass
    public static void setUpClass() {
        System.out.println("Setting up class");
        pm = ProfileManager.getInstance();
    }

    @Test
    public void testNewProfile() throws Exception {
        pm.newProfile(testProfileName, testProfileDocDir, testProfileSourceDir);
        pm.saveProfiles();
    }

    @Test
    public void testSetCurrentProfile() throws Exception {
        pm.setCurrentProfile(testProfileName);
        assertEquals(testProfileName, pm.getCurrentProfileName());
    }

    @Test
    public void testProfileFileCreated() throws Exception {
        assertEquals(true, (Utilities.getFile(testProfileDir, testProfileName + ".ser")).exists());
    }

    @Test
    public void testProfileDirCreated() throws Exception {
        assertEquals(true, (Utilities.getFile(testProfileDir)).exists());
    }

    @Test
    public void testClassDataFileCreated() throws Exception {
        assertEquals(true, (Utilities.getFile(testProfileDir, Config.CLASS_DATA_FILE_NAME)).exists());
    }

    @Test
    public void testLoadProfiles() throws Exception {
        pm.loadProfiles();
        ArrayList<String> profiles = pm.getProfileNames();
        for (String profileName : profiles) {
            pm.setCurrentProfile(profileName);
            assertEquals(profileName, pm.getCurrentProfileName());
        }
        pm.setCurrentProfile(testProfileName);
    }

    @Test
    public void testCurrentProfileName() throws Exception {
        pm.setCurrentProfile(testProfileName);
        assertEquals(testProfileName, pm.getCurrentProfileName());
    }

    @Test
    public void testCurrentProfileDocDir() throws Exception {
        pm.setCurrentProfile(testProfileName);
        assertEquals(testProfileDocDir, pm.getDocDir());
    }

    @Test
    public void testCurrentProfileSrcDir() throws Exception {
        pm.setCurrentProfile(testProfileName);
        assertEquals(testProfileSourceDir, pm.getSrcDir());
    }

    @Test
    public void testSaveProfiles() throws Exception {
        pm.saveProfiles();
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
    public void testGetPath() throws Exception {
        pm.setCurrentProfile(testProfileName);
        assertEquals(testProfileDir, pm.getPath());
    }

    @Test
    public void testDeleteProfile() throws Exception {
        pm.deleteProfile(testProfileName);
    }

    @Test
    public void testProfileFileDeleted() throws Exception {
        assertEquals(false, (Utilities.getFile(testProfileDir, testProfileName + ".ser")).exists());
    }

    @Test
    public void testClassDataFileDeleted() throws Exception {
        assertEquals(false, (Utilities.getFile(testProfileDir, Config.CLASS_DATA_FILE_NAME)).exists());
    }

    @Test
    public void testProfileDirDeleted() throws Exception {
        assertEquals(false, (Utilities.getFile(testProfileDir)).exists());
    }
}
