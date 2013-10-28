package com.facetoe.jreader;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 18/10/13
 * Time: 4:23 PM
 */

public class ProfileManager implements Serializable {
    private static final Logger log = Logger.getLogger(ProfileManager.class);

    private static final long serialVersionUID = 1L;
    private Profile currentProfile;
    private HashMap<String, Profile> profiles = new HashMap<String, Profile>();
    private static ProfileManager instance = new ProfileManager();

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
        } else {
            log.error("No default profile");
        }
    }

    public static ProfileManager getInstance() {
        return instance;
    }

    public void newProfile(String name, String fileName, String docDir, String srcDir) {
        Profile profile = new Profile(name, fileName, docDir, srcDir);
        profiles.put(profile.name, profile);
        currentProfile = profile;

        try {
            writeProfile(currentProfile);
        } catch ( IOException e ) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        File profileDir = new File(getPath());
        File[] files = profileDir.listFiles();
        boolean hasClassData = false;
        for ( File file : files ) {
            if ( file.getName().equalsIgnoreCase(Config.CLASS_DATA_FILE_NAME) ) {
                hasClassData = true;
            }
        }
        if ( !hasClassData ) {
            try {
                JavaDocParser parser = new JavaDocParser();
                HashMap<String, String> classData = parser.parse(getDocDir() +
                        Config.ALL_CLASSSES_DOC_FILE);

                Utilities.writeCLassData(getPath() + Config.CLASS_DATA_FILE_NAME, classData);
            } catch ( Exception e ) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
        Config.setString(Config.CURRENT_PROFILE, currentProfile.name);
        log.debug("Created profile: " + currentProfile.name);
    }

    public void loadProfiles() throws IOException, ClassNotFoundException {
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
                                log.debug("Loaded profile: " + profile.name);
                                profiles.put(profile.name, profile);
                            }
                        }
                    }
                }
            }
        }
    }

    public void saveProfiles() throws IOException {
        for ( String s : profiles.keySet() ) {
            log.debug("Saving: " + s);
            writeProfile(profiles.get(s));
        }
    }

    private void writeProfile(Profile profile) throws IOException {
        String profileDirPath = Config.getString(Config.PROFILE_DIR) + profile.profileDirName;
        File profileDir = new File(profileDirPath);

        if ( !profileDir.exists() ) {
            boolean wasSuccess = profileDir.mkdirs();
            if ( !wasSuccess ) {
                throw new IOException("Failed to create profile directory at: " + profileDirPath);
            }
        }

        File file = new File(profileDirPath + File.separator + profile.fileName);

        if ( !file.exists() ) {
            boolean wasSuccess = file.createNewFile();
            if ( !wasSuccess ) {
                throw new IOException("Failed to create profile file at: " + profileDir);
            }
        }

        FileOutputStream fileOut = new FileOutputStream(file);
        ObjectOutputStream outStream = new ObjectOutputStream(fileOut);
        outStream.writeObject(profile);
        outStream.close();
        fileOut.close();
    }

    public Profile readProfile(String filePath) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(filePath);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Profile profile = ( Profile ) in.readObject();
        in.close();
        fileIn.close();
        return profile;
    }

    public ArrayList<String> getProfileNames() {
        return new ArrayList<String>(profiles.keySet());
    }

    public String getPath() {
        return Config.getString(Config.PROFILE_DIR) + File.separator + currentProfile.profileDirName + File.separator;
    }

    public String getClassDataFilePath() {
        return getPath() + Config.CLASS_DATA_FILE_NAME;
    }

    public void setCurrentProfile(String profileName) {
        if ( profiles.containsKey(profileName) ) {
            currentProfile = profiles.get(profileName);
            Config.setString(Config.CURRENT_PROFILE, profileName);
            log.debug("Profile set to: " + Config.getString(Config.CURRENT_PROFILE));
        } else {
            log.warn("No such profile: " + profileName);
        }
    }

    public void deleteProfile(String profileName) {
        Profile profile = profiles.get(profileName);
        File profileDir = new File(Config.getString(Config.PROFILE_DIR) + File.separator + profile.profileDirName);
        if(profile != null) {
            try {
                Utilities.deleteDirectoryAndContents(profileDir);
                profiles.remove(profileName);
                setCurrentProfile("Default");
                log.debug("Deleted: " + profile.name);
            } catch ( IOException e ) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public String getDocDir() {
        return currentProfile.docDir;
    }

    public String getSrcDir() {
        return currentProfile.srcDir;
    }

    public String getName() {
        return currentProfile.name;
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

    private class Profile implements Serializable {
        private static final long serialVersionUID = 1L;
        private String profileDirName;
        private String docDir;
        private String srcDir;
        private String fileName;
        private String home;
        private String name;

        private boolean regexpIsEnabled;
        private boolean wholeWordIsEnabled;
        private boolean matchCaseIsEnabled;

        public Profile() {

        }

        public Profile(String name, String fileName, String docDir, String srcDir) {
            this.name = name;
            this.fileName = fileName;

            /* Don't want funny characters and spaces in the directory name */
            this.profileDirName = name.replaceAll("[^a-zA-Z0-9.-]", "_");
            this.docDir = docDir;
            this.srcDir = srcDir;
            this.regexpIsEnabled = false;
            this.wholeWordIsEnabled = false;
            this.matchCaseIsEnabled = false;
        }
    }
}

