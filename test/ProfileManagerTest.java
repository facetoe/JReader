
import com.facetoe.jreader.Config;
import com.facetoe.jreader.ProfileManager;
import com.facetoe.jreader.Utilities;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Created by facetoe on 16/01/14.
 */
public class ProfileManagerTest {
    ProfileManager pm;
    final String testProfileName = "test";
    final String testProfileDocDir = "/home/facetoe/IdeaProjects/JReader/testFiles/swingx-all-1.6.4-javadoc";
    final String testProfileSourceDir = "/home/facetoe/IdeaProjects/JReader/testFiles/swingx-all-1.6.4-sources";
    final String testProfileDir = Utilities.constructPath(Config.getString(Config.PROFILE_DIR), testProfileName);
    final String defaultProfileName = Config.DEFAULT_PROFILE_NAME;
    final File profileDir = Utilities.getFile(testProfileDir);
    final File profileFile = Utilities.getFile(testProfileDir, testProfileName + ".ser");
    final File classDataFile = Utilities.getFile(testProfileDir, Config.CLASS_DATA_FILE_NAME);

    @Before
    public void setUp() throws Exception {
        pm = ProfileManager.getInstance();
    }

    @Test
    public void testGetInstanceNotNull() throws Exception {
        assertNotNull(pm);
    }

    @Test
    public void testGetInstance() throws Exception {
        ProfileManager anotherInstance = ProfileManager.getInstance();
        assertEquals(pm, anotherInstance);
    }

    @Test
    public void testNewProfile() throws Exception {
        pm.newProfile(testProfileName, testProfileDocDir, testProfileSourceDir);
    }

    @Test
    public void testProfileFileCreated() throws Exception {
        assertEquals(true, profileFile.exists());
    }

    @Test
    public void testProfileDirCreated() throws Exception {
        assertEquals(true, profileDir.exists());
    }

    @Test
    public void testClassDataFileCreated() throws Exception {
        assertEquals(true, classDataFile.exists());
    }

    @Test
    public void testSetCurrentProfile() throws Exception {
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
    public void testLoadProfiles() throws Exception {
        pm.loadProfiles();
        ArrayList<String> profiles = pm.getProfileNames();
        for (String profileName : profiles) {
            pm.setCurrentProfile(profileName);
            assertEquals(profileName, pm.getCurrentProfileName());
        }
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
    public void testProfileDirDeleted() throws Exception {
        assertEquals(false, profileDir.exists());
    }

    @Test
    public void testProfileFileDeleted() throws Exception {
        assertEquals(false, profileFile.exists());
    }

    @Test
    public void testClassDataFileDeleted() throws Exception {
        assertEquals(false, classDataFile.exists());
    }
}
