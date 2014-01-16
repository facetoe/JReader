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
public class ProfileManager implements Serializable {

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
     * @param name   The name of this profile.
     * @param docDir Where the Documentation is located.
     * @param srcDir Where the source code is located.
     */
    public void newProfile(String name, String docDir, String srcDir) throws IOException {
        Profile profile = new Profile(name, docDir, srcDir);
        profiles.put(profile.getName(), profile);
        HashMap<String, String> classData = parseJavaDocs(profile.getDocDir());
        createFilesAndSaveProfile(profile, classData);
        log.debug("Created profile: " + profile.getName());
    }

    private void createFilesAndSaveProfile(Profile profile, HashMap<String, String> classData) throws IOException {
        Utilities.checkAndCreateDirectoryIfNotPresent(
                Utilities.getFile(
                        Config.getString(Config.PROFILE_DIR),
                        profile.getProfileDirName()));

        File classDataFile = Utilities.getFile(
                Config.getString(Config.PROFILE_DIR),
                profile.getProfileDirName(),
                Config.CLASS_DATA_FILE_NAME);
        writeProfile(profile);
        writeClassData(classDataFile, classData);
    }

    private HashMap<String, String> parseJavaDocs(String docPath) {
        JavaDocParser parser = new JavaDocParser();
        for (ActionListener listener : listeners) {
            parser.addActionListener(listener);
        }
        return parser.parse(Utilities.getFile(docPath, Config.ALL_CLASSSES_DOC_FILE));
    }

    /**
     * Loads all the profiles.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void loadProfiles() throws IOException {
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
                && !profFile.getName().equalsIgnoreCase(Config.CLASS_DATA_FILE_NAME);
    }

    private void loadProfile(File profFile) throws IOException {
        Profile profile = readProfile(profFile);
        // SearchContext isn't serializable so we need to create it on load.
        profile.setSearchContext(new SearchContext());
        profiles.put(profile.getName(), profile);
        log.debug("Loaded profile: " + profile.getName());
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
        File profileFile = Utilities.getFile(
                Config.getString(Config.PROFILE_DIR),
                profile.getProfileDirName(),
                profile.getProfileFileName());
        Utilities.checkAndCreateFileIfNotPresent(profileFile);
        writeProfile(profileFile, profile);
    }

    private void writeClassData(File classDataFile, HashMap<String, String> classData) throws IOException {
        Utilities.checkAndCreateFileIfNotPresent(classDataFile);
        Utilities.writeObject(classDataFile, classData);
    }

    private void writeProfile(File outFile, Profile profile) throws IOException {
        Utilities.checkAndCreateFileIfNotPresent(outFile);
        Utilities.writeObject(outFile, profile);
    }

    /**
     * Reads a profile and returns a Profile object.
     *
     * @param profileFile Path to the profile.
     * @return Profile object.
     * @throws IOException
     */
    private Profile readProfile(File profileFile) throws IOException {
        return (Profile) Utilities.readObject(profileFile);
    }

    /**
     * Sets <code>profileName</code> as the current profile.
     *
     * @param profileName The name of the profile to be set.
     */
    public void setCurrentProfile(String profileName) {
        if (profiles.containsKey(profileName)) {
            currentProfile = profiles.get(profileName);
            currentProfile.setSearchContext(new SearchContext());
            Config.setString(Config.CURRENT_PROFILE, profileName);
            log.debug("Profile set to: " + Config.getString(Config.CURRENT_PROFILE));
        } else if(!profileName.equals(Config.DEFAULT_PROFILE_NAME)) {
            setCurrentProfile(Config.DEFAULT_PROFILE_NAME);
            log.error("No such profile: " + profileName);
        } else {
            log.error("Failed to load default profile");
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
        if (profile != null) {
            File profileDir = Utilities.getFile(
                    Config.getString(Config.PROFILE_DIR),
                    profile.getProfileDirName());

            Utilities.deleteDirectoryAndContents(profileDir);
            profiles.remove(profileName);
            setCurrentProfile(Config.DEFAULT_PROFILE_NAME);
            log.debug("Deleted: " + profile.getName());
        } else {
            log.error("Profile doesn't exist: " + profileName);
        }
    }

    public String getCurrentProfileName() {
        return currentProfile.getName();
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
        return Utilities.constructPath(
                Config.getString(Config.PROFILE_DIR),
                currentProfile.getProfileDirName());
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
        return currentProfile.getDocDir();
    }

    /**
     * Where the source code for this profile is located.
     *
     * @return Path to the source code.
     */
    public String getSrcDir() {
        return currentProfile.getSrcDir();
    }

    /**
     * Get the homepage for this profile.
     *
     * @return The homepage path.
     */
    public String getHome() {
        return getDocDir() + File.separator + currentProfile.getHome();
    }

    /**
     * Get the search context for this profile.
     */
    public SearchContext getSearchContext() {
        currentProfile.updateSearchContext();
        return currentProfile.getSearchContext();
    }

    /**
     * @return Whether regexp is enabled in this profiles search context.
     */
    public boolean regexpIsEnabled() {
        return currentProfile.regexpIsEnabled();
    }

    /**
     * @return Whether whole word is enabled in this profiles search context.
     */
    public boolean wholeWordIsEnabled() {
        return currentProfile.wholeWordIsEnabled();
    }

    /**
     * @return Whether match case is enabled in this profiles search context.
     */
    public boolean matchCaseIsEnabled() {
        return currentProfile.matchCaseIsEnabled();
    }

    /**
     * Set regexp enabled.
     *
     * @param value Boolean.
     */
    public void setRegexpEnabled(boolean value) {
        currentProfile.setRegexpIsEnabled(value);
    }

    /**
     * Set whole word enabled.
     *
     * @param value Boolean.
     */
    public void setWholeWordEnabled(boolean value) {
        currentProfile.setWholeWordIsEnabled(value);
    }

    /**
     * Set match case enabled.
     *
     * @param value Boolean.
     */
    public void setMatchCaseEnabled(boolean value) {
        currentProfile.setMatchCaseIsEnabled(value);
    }

}

