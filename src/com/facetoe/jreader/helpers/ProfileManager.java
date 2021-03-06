/*   
*    Copyright (C) 2013  facetoe - facetoe@ymail.com
*
*    This program is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation; either version 2 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License along
*    with this program; if not, write to the Free Software Foundation, Inc.,
*    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package com.facetoe.jreader.helpers;

import com.facetoe.jreader.parsers.JavaDocParser;
import org.apache.log4j.Logger;
import org.fife.ui.rtextarea.SearchContext;

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
public final class ProfileManager implements Serializable {

    private static final Logger log = Logger.getLogger(ProfileManager.class);
    private static final long serialVersionUID = 1L;

    private Profile currentProfile;
    private final HashMap<String, Profile> profiles = new HashMap<String, Profile>();
    private static final ProfileManager instance;
    private static final ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

    // If we can't load a profile then the program won't work at all.
    static {
        try {
            instance = new ProfileManager();
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Private constructor for Singleton class.
     */
    private ProfileManager() throws IOException {
        loadProfiles();
        setCurrentProfile(Config.getString(Config.CURRENT_PROFILE));
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
     * Creates and saves a new Profile.
     *
     * @param name    The name of this profile.
     * @param docRoot Where the Documentation is located.
     * @param srcDir  Where the source code is located.
     */
    public void newProfile(String name, File docRoot, File srcDir) throws IOException {
        File docDir = Util.findDocDir(docRoot);
        Profile profile = new Profile(name, docDir, srcDir);
        profiles.put(profile.name, profile);
        HashMap<String, String> classData = parseJavaDocs(profile.docDir);
        createFilesAndSaveProfile(profile, classData);
        log.debug("Created profile: " + profile.name);
    }

    private void createFilesAndSaveProfile(Profile profile, HashMap<String, String> classData) throws IOException {
        Util.checkAndCreateDirectoryIfNotPresent(
                Util.getFileFromPathElements(
                        Config.getString(Config.PROFILE_DIR),
                        profile.profileDirName));

        File classDataFile = Util.getFileFromPathElements(
                Config.getString(Config.PROFILE_DIR),
                profile.profileDirName,
                Config.CLASS_DATA_FILE_NAME);
        writeProfile(profile);
        writeClassData(classDataFile, classData);
    }

    private HashMap<String, String> parseJavaDocs(String docPath) {
        JavaDocParser parser = new JavaDocParser();
        for (ActionListener listener : listeners) {
            parser.addActionListener(listener);
        }
        return parser.parse(Util.getFileFromPathElements(docPath, Config.ALL_CLASSSES_DOC_FILE));
    }

    /**
     * Loads all the profiles.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void loadProfiles() throws IOException {
        profiles.clear();
        File profileDir = new File(Config.getString(Config.PROFILE_DIR));
        File[] profileDirectories = profileDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        for (File profileDirectory : profileDirectories) {
            loadProfileFiles(profileDirectory);
        }
    }

    private void loadProfileFiles(File profileDir) throws IOException {
        File[] profFiles = profileDir.listFiles();
        if (profFiles == null)
            throw new FileNotFoundException("Failed to load profile at: " + profileDir.getAbsolutePath());

        for (File profFile : profFiles) {
            if (isProfileFile(profFile))
                loadProfile(profFile);
        }
    }

    // There should only be two files in each profile directory,
    // classData.ser and <profileName>.ser
    private boolean isProfileFile(File profFile) {
        return profFile.getName().endsWith(".ser")
                && !profFile.getName().equals(Config.CLASS_DATA_FILE_NAME);
    }

    private void loadProfile(File profFile) throws IOException {
        Profile profile = readProfile(profFile);
        // SearchContext isn't serializable so we need to create it on load.
        profile.searchContext = new SearchContext();
        profiles.put(profile.name, profile);
        log.debug("Loaded profile: " + profile.name);
    }

    /**
     * Saves all the profiles.
     *
     * @throws IOException
     */
    public void saveProfiles() throws IOException {
        for (String s : profiles.keySet()) {
            log.debug("Saving: " + s);
            writeProfile(profiles.get(s));
        }
    }

    /**
     * Writes a profile to disk. If the file doesn't exist this method will try to create it.
     *
     * @param profile The profile to save.
     * @throws IOException
     */
    private void writeProfile(Profile profile) throws IOException {
        File outFile = Util.getFileFromPathElements(
                Config.getString(Config.PROFILE_DIR),
                profile.profileDirName,
                profile.profileFileName);
        Util.checkAndCreateFileIfNotPresent(outFile);
        writeProfile(outFile, profile);
    }

    private void writeProfile(File outFile, Profile profile) throws IOException {
        Util.checkAndCreateFileIfNotPresent(outFile);
        Util.writeObject(outFile, profile);
    }

    private void writeClassData(File classDataFile, HashMap<String, String> classData) throws IOException {
        Util.checkAndCreateFileIfNotPresent(classDataFile);
        Util.writeObject(classDataFile, classData);
    }

    /**
     * Reads a profile and returns a Profile object.
     *
     * @param profileFile Path to the profile.
     * @return Profile object.
     * @throws IOException
     */
    private Profile readProfile(File profileFile) throws IOException {
        return (Profile) Util.readObject(profileFile);
    }

    /**
     * Sets <code>profileName</code> as the current profile.
     *
     * @param profileName The name of the profile to be set.
     */
    public void setCurrentProfile(String profileName) {
        if (profiles.containsKey(profileName)) {
            currentProfile = profiles.get(profileName);
            currentProfile.searchContext = new SearchContext();
            Config.setString(Config.CURRENT_PROFILE, profileName);
            log.debug("Profile set to: " + Config.getString(Config.CURRENT_PROFILE));
        } else {
            log.error("Failed to load profile: " + profileName);
        }
    }

    /**
     * Deletes a profile and the enclosing directory.
     * If there are sub directories they will be deleted also.
     *
     * @param profileName The profile to be deleted.
     */
    public void deleteProfile(String profileName) {
        if (profiles.containsKey(profileName)) {
            Profile profile = profiles.get(profileName);
            File profileDir = Util.getFileFromPathElements(
                    Config.getString(Config.PROFILE_DIR),
                    profile.profileDirName);

            Util.deleteDirectoryAndContents(profileDir);
            profiles.remove(profileName);
            setCurrentProfile(Config.DEFAULT_PROFILE_NAME);
            log.debug("Deleted: " + profile.name);
        } else {
            log.error("Profile doesn't exist: " + profileName);
        }
    }

    public String getCurrentProfileName() {
        return currentProfile.name;
    }

    /**
     * Get all the profile names.
     *
     * @return Profile names.
     */
    public ArrayList<String> getProfileNames() {
        return new ArrayList<String>(profiles.keySet());
    }

    /**
     * Add action listeners. They will be informed of parsing progress.
     *
     * @param listener Listener.
     */
    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    /**
     * The full path to the current profile directory.
     *
     * @return The path.
     */
    public String getPath() {
        return Util.constructPath(
                Config.getString(Config.PROFILE_DIR),
                currentProfile.profileDirName);
    }

    /**
     * Get all the class names for this profiles class data.
     *
     * @return Class names.
     */
    public ArrayList<String> getClassNames() {
        return new ArrayList<String>(getClassData().keySet());
    }

    /**
     * Get the HashMap containing class names and relative paths for
     * this profile.
     *
     * @return Class data.
     */
    public HashMap<String, String> getClassData() {
        return currentProfile.getClassData();
    }

    /**
     * Where this profiles documentation is located.
     *
     * @return Path to documentation.
     */
    public String getDocDir() {
        return currentProfile.docDir;
    }

    /**
     * Where the source code for this profile is located.
     *
     * @return Path to the source code.
     */
    public String getSrcDir() {
        return currentProfile.srcDir;
    }

    /**
     * Get the homepage for this profile.
     *
     * @return The homepage path.
     */
    public String getHome() {
        return Util.constructPath(getDocDir(), currentProfile.home);
    }

    /**
     * Get the search context for this profile.
     */
    public SearchContext getSearchContext() {
        currentProfile.updateSearchContext();
        return currentProfile.searchContext;
    }

    /**
     * @return Whether regexp is enabled in this profiles search context.
     */
    public boolean regexpIsEnabled() {
        return currentProfile.regexpIsEnabled;
    }

    /**
     * @return Whether whole word is enabled in this profiles search context.
     */
    public boolean wholeWordIsEnabled() {
        return currentProfile.wholeWordIsEnabled;
    }

    /**
     * @return Whether match case is enabled in this profiles search context.
     */
    public boolean matchCaseIsEnabled() {
        return currentProfile.matchCaseIsEnabled;
    }

    /**
     * Set regexp enabled.
     *
     * @param value Boolean.
     */
    public void setRegexpEnabled(boolean value) {
        currentProfile.regexpIsEnabled = value;
    }

    /**
     * Set whole word enabled.
     *
     * @param value Boolean.
     */
    public void setWholeWordEnabled(boolean value) {
        currentProfile.wholeWordIsEnabled = value;
    }

    /**
     * Set match case enabled.
     *
     * @param value Boolean.
     */
    public void setMatchCaseEnabled(boolean value) {
        currentProfile.matchCaseIsEnabled = value;
    }

    /**
     * Class to represent a profile.
     */
    private class Profile implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String profileDirName;
        private final String docDir;
        private final String srcDir;
        private final String profileFileName;

        // All the class names and relative paths.
        private HashMap<String, String> classData;

        // The documentation HTML file that is set as home.
        private final String home;

        private final String name;
        private transient SearchContext searchContext;
        private boolean regexpIsEnabled;
        private boolean wholeWordIsEnabled;
        private boolean matchCaseIsEnabled;

        public Profile(String name, File docDir, File srcDir) {
            this.name = name;
            this.docDir = docDir.getAbsolutePath() + File.separator;
            this.srcDir = srcDir.getAbsolutePath() + File.separator;
            this.regexpIsEnabled = false;
            this.wholeWordIsEnabled = false;
            this.matchCaseIsEnabled = false;
            this.home = Util.getHomePage(docDir.getAbsolutePath());

            // Don't want funny characters and spaces in the profile directory or file
            this.profileFileName = name.replaceAll("[^A-Za-z0-9]", "_") + ".ser";
            this.profileDirName = name.replaceAll("[^a-zA-Z0-9.-]", "_");
        }

        /**
         * Loads the class data if it isn't already loaded for this profile.
         *
         * @return The class data.
         */
        HashMap<String, String> getClassData() {
            if (classData == null) {
                log.debug("Loading classData for: " + name);
                File classDataFile = Util.getFileFromPathElements(
                        Config.getString(Config.PROFILE_DIR),
                        profileDirName,
                        Config.CLASS_DATA_FILE_NAME);
                try {
                    //noinspection unchecked
                    classData = (HashMap<String, String>) Util.readObject(classDataFile);
                } catch (IOException e) {
                    log.error(e);
                    Util.showErrorDialog("Failed to load auto-complete data for this profile.\n" +
                            " Try deleting the it and adding it again. Sorry! ", "Error");
                    classData = new HashMap<String, String>(); // Return an empty hashmap so we don't get a NPE
                }
            }
            return classData;
        }

        /**
         * Make sure the search context is up to date.
         */
        private void updateSearchContext() {
            searchContext.setMatchCase(matchCaseIsEnabled);
            searchContext.setRegularExpression(regexpIsEnabled);
            searchContext.setWholeWord(wholeWordIsEnabled);
        }
    }
}

