package com.facetoe.jreader;

import org.apache.log4j.Logger;
import org.fife.ui.rtextarea.SearchContext;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 18/10/13
 * Time: 4:23 PM
 */

/**
 * This class is responsible for creating, deleting and querying profiles.
 */
public class ProfileManager implements Serializable {
    private static final Logger log = Logger.getLogger(ProfileManager.class);
    private static final long serialVersionUID = 1L;

    /* The current profile. */
    private Profile currentProfile;

    /* All the profiles. */
    private final HashMap<String, Profile> profiles = new HashMap<String, Profile>();

    /* Singlton instance of ProfileManager. */
    private static final ProfileManager instance = new ProfileManager();

    private static final ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

    /**
     * Private constructor for Singleton class.
     */
    private ProfileManager() {
        try {
            loadProfiles();
        } catch ( IOException e ) {
            log.error(e.getMessage(), e);
        } catch ( ClassNotFoundException e ) {
            log.error(e.getMessage(), e);
        }

        String currentProfileName = Config.getString(Config.CURRENT_PROFILE);
        if ( !currentProfileName.isEmpty() ) {
            currentProfile = profiles.get(currentProfileName);

            /* If it doesn't exist, try and load the default profile.*/
            if(currentProfile == null) {
                if(JReaderSetup.hasDefaultProfile()) {
                    setCurrentProfile(Config.DEFAULT_PROFILE_NAME);
                } else {
                    log.error("No profile or default profile.");
                    Config.setBool(Config.HAS_DEFAULT_PROFILE, false);
                }
            }
        } else {
            log.error("Current profile not set.");
        }
    }

    /**
     * Get an instance of ProfileManager.
     *
     * @return ProfileManager instance.
     */
    public static ProfileManager getInstance() {
        return instance;
    }

    /**
     * Create a new Profile.
     *
     * @param name   The name of this profile.
     * @param docDir Where the Documentation is located.
     * @param srcDir Where the source code is located.
     */
    public void newProfile(String name, String docDir, String srcDir) {
        Profile profile = new Profile(name, docDir, srcDir);
        profiles.put(profile.name, profile);
        currentProfile = profile;

        try {
            /* Save the profile now just in case. */
            writeProfile(currentProfile);

            /* Parse the class data and save it. */
            JavaDocParser parser = new JavaDocParser();

            for ( ActionListener listener : listeners ) {
                parser.addActionListener(listener);
            }

            HashMap<String, String> classData = parser.parse(getDocDir() +
                    Config.ALL_CLASSSES_DOC_FILE);
            Utilities.writeCLassData(getPath() + Config.CLASS_DATA_FILE_NAME, classData);

            /* Set it to the default so we can load it straight up on next launch. */
            Config.setString(Config.CURRENT_PROFILE, currentProfile.name);
            log.debug("Created profile: " + currentProfile.name);

        } catch ( IOException e ) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        } catch ( Exception e ) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    /**
     * Loads all the profiles.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    void loadProfiles() throws IOException, ClassNotFoundException {
        File profileDir = new File(Config.getString(Config.PROFILE_DIR));
        File[] profDirs = profileDir.listFiles();
        if ( profDirs != null ) {
            for ( File profDir : profDirs ) {
                if ( profDir.isDirectory() ) {
                    File[] profFiles = profDir.listFiles();
                    if ( profFiles != null ) {
                        for ( File profFile : profFiles ) {
                            if ( profFile.getName().endsWith(".ser")
                                    && !profFile.getName()
                                    .equalsIgnoreCase(Config.CLASS_DATA_FILE_NAME) ) {

                                Profile profile = readProfile(profFile.getAbsolutePath());

                                /* SearchContext isnt't serializable so we need to create it on load. */
                                profile.searchContext = new SearchContext();

                                log.debug("Loaded profile: " + profile.name);
                                profiles.put(profile.name, profile);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Saves all the profiles.
     *
     * @throws IOException
     */
    public void saveProfiles() throws IOException {
        for ( String s : profiles.keySet() ) {
            log.debug("Saving: " + s);
            writeProfile(profiles.get(s));
        }
    }

    /**
     * Writes a profile to disk. If the direcory doesn't exist this method will try to create it.
     *
     * @param profile The profile to save.
     * @throws IOException
     */
    private void writeProfile(Profile profile) throws IOException {
        String profileDirPath = Config.getString(Config.PROFILE_DIR) + profile.profileDirName;
        File profileDir = new File(profileDirPath);

        if ( !profileDir.exists() ) {
            boolean wasSuccess = profileDir.mkdirs();
            if ( !wasSuccess ) {
                throw new IOException("Failed to create profile directory at: " + profileDirPath);
            }
        }

        File classDataFile = new File(profileDirPath + File.separator + profile.profileFileName);

        if ( !classDataFile.exists() ) {
            boolean wasSuccess = classDataFile.createNewFile();
            if ( !wasSuccess ) {
                throw new IOException("Failed to create profile file at: " + profileDir);
            }
        }

        FileOutputStream fileOut = new FileOutputStream(classDataFile);
        ObjectOutputStream outStream = new ObjectOutputStream(fileOut);
        outStream.writeObject(profile);
        outStream.close();
        fileOut.close();
    }

    /**
     * Reads a profile and returns a Profile object.
     *
     * @param filePath Path to the profile.
     * @return Profile object.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    Profile readProfile(String filePath) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(filePath);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Profile profile = ( Profile ) in.readObject();
        in.close();
        fileIn.close();
        return profile;
    }

    /**
     * Sets <code>profileName</code> as the current profile.
     *
     * @param profileName The name of the profile to be set.
     */
    public void setCurrentProfile(String profileName) {
        if ( profiles.containsKey(profileName) ) {
            currentProfile = profiles.get(profileName);
            currentProfile.searchContext = new SearchContext();
            Config.setString(Config.CURRENT_PROFILE, profileName);
            log.debug("Profile set to: " + Config.getString(Config.CURRENT_PROFILE));
        } else {
            log.error("No such profile: " + profileName);
        }
    }

    /**
     * Deletes a profile and the enclosing directory.
     * If there are sub directories they will be deleted also.
     *
     * @param profileName The profile to be deleted.
     */
    public void deleteProfile(String profileName) {
        Profile profile = profiles.get(profileName);
        File profileDir = new File(Config.getString(Config.PROFILE_DIR) + File.separator + profile.profileDirName);
        Utilities.deleteDirectoryAndContents(profileDir);
        profiles.remove(profileName);
        setCurrentProfile(Config.DEFAULT_PROFILE_NAME);
        log.debug("Deleted: " + profile.name);
    }

    public ArrayList<String> getProfileNames() {
        return new ArrayList<String>(profiles.keySet());
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    public String getPath() {
        return Config.getString(Config.PROFILE_DIR)
                + File.separator
                + currentProfile.profileDirName
                + File.separator;
    }

    public ArrayList<String> getClassNames() {
        return new ArrayList<String>(getClassData().keySet());
    }

    public HashMap<String, String> getClassData() {
        return currentProfile.getClassData();
    }

    public String getDocDir() {
        return currentProfile.docDir;
    }

    public String getSrcDir() {
        return currentProfile.srcDir;
    }

    public String getHome() {
        return getDocDir() + File.separator + currentProfile.home;
    }

    public SearchContext getSearchContext() {
        currentProfile.updateSearchContext();
        return currentProfile.searchContext;
    }

    public boolean regexpIsEnabled() {
        return currentProfile.regexpIsEnabled;
    }

    public boolean wholeWordIsEnabled() {
        return currentProfile.wholeWordIsEnabled;
    }

    public boolean matchCaseIsEnabled() {
        return currentProfile.matchCaseIsEnabled;
    }

    public void setRegexpEnabled(boolean value) {
        currentProfile.regexpIsEnabled = value;
    }

    public void setWholeWordEnabled(boolean value) {
        currentProfile.wholeWordIsEnabled = value;
    }

    public void setMatchCaseEnabled(boolean value) {
        currentProfile.matchCaseIsEnabled = value;
    }

    /**
     * Class to represent a profile.
     */
    private class Profile implements Serializable {
        private static final long serialVersionUID = 1L;

        /* The name of the directory holding this profile */
        private final String profileDirName;

        /* Where the Java documentation is located for this profile.  */
        private final String docDir;

        /* Where the Java source code is located for this profile. */
        private final String srcDir;

        /* The name of the class data file. */
        private final String profileFileName;

        /* All the class names and relative paths. */
        private HashMap<String, String> classData;

        /* The documentation HTML file that is set as home. */
        private String home;

        /* This profiles name. */
        private final String name;

        /* The search context for this profile. */
        private transient SearchContext searchContext;

        private boolean regexpIsEnabled;
        private boolean wholeWordIsEnabled;
        private boolean matchCaseIsEnabled;

        public Profile(String name, String docDir, String srcDir) {
            this.name = name;

            /* Don't want funny characters and spaces in the data file name */
            this.profileFileName = name.replaceAll("[^A-Za-z0-9]", "_") + ".ser";

            /* Don't want funny characters and spaces in the directory name */
            this.profileDirName = name.replaceAll("[^a-zA-Z0-9.-]", "_");
            this.docDir = docDir;
            this.srcDir = srcDir;
            this.regexpIsEnabled = false;
            this.wholeWordIsEnabled = false;
            this.matchCaseIsEnabled = false;
            home = Utilities.getHomePage(docDir);
        }

        HashMap<String, String> getClassData() {
            if(classData == null) {
                log.debug("Loading classData for: " + name);

                File classDataFile = new File(Config.getString(Config.PROFILE_DIR)
                        + File.separator + profileDirName + File.separator + Config.CLASS_DATA_FILE_NAME);
                try {
                    classData = Utilities.readClassData(classDataFile);
                } catch ( IOException e ) {
                    log.error(e.toString(), e);
                    handleLoadError(e);
                } catch ( ClassNotFoundException e ) {
                    log.error(e.toString(), e);
                    handleLoadError(e);
                }
            }
            return classData;
        }

        private void updateSearchContext() {
            searchContext.setMatchCase(matchCaseIsEnabled);
            searchContext.setRegularExpression(regexpIsEnabled);
            searchContext.setWholeWord(wholeWordIsEnabled);
        }

        /* Return an empty hashmap so the program doesn't crash. */
        private void handleLoadError(Exception e) {
            classData = new HashMap<String, String>();
            JOptionPane.showMessageDialog(null, "Failed to load class data: " + e.toString());
        }
    }
}

