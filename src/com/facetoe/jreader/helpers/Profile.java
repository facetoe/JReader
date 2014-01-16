package com.facetoe.jreader.helpers;

/**
 * Created by facetoe on 16/01/14.
 */

import org.apache.log4j.Logger;
import org.fife.ui.rtextarea.SearchContext;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Class to represent a profile.
 */
public class Profile implements Serializable {
    private static final Logger log = Logger.getLogger(Profile.class);

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

    public Profile(String name, String docDir, String srcDir) {
        this.name = name;
        this.docDir = docDir;
        this.srcDir = srcDir;
        this.regexpIsEnabled = false;
        this.wholeWordIsEnabled = false;
        this.matchCaseIsEnabled = false;
        this.home = Utilities.getHomePage(docDir);

        // Don't want funny characters and spaces in the profile directory or file
        this.profileFileName = name.replaceAll("[^A-Za-z0-9]", "_") + ".ser";
        this.profileDirName = name.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    /**
     * Loads the class data if it isn't already loaded for this profile.
     *
     * @return The class data.
     */
    public HashMap<String, String> getClassData() {
        if (classData == null) {
            log.debug("Loading classData for: " + name);
            File classDataFile = new File(Config.getString(Config.PROFILE_DIR)
                    + File.separator + profileDirName + File.separator + Config.CLASS_DATA_FILE_NAME);
            try {
                classData = (HashMap<String, String>) Utilities.readObject(classDataFile);
            } catch (IOException e) {
                log.error(e);
                Utilities.showError("Failed to load auto-complete data for this profile.\n" +
                        " Try deleting the it and adding it again. Sorry! ", "Error");
                classData = new HashMap<String, String>(); // Return an empty hashmap so we don't get a NPE
            }
        }
        return classData;
    }

    /**
     * Make sure the search context is up to date.
     */
    public void updateSearchContext() {
        searchContext.setMatchCase(matchCaseIsEnabled);
        searchContext.setRegularExpression(regexpIsEnabled);
        searchContext.setWholeWord(wholeWordIsEnabled);
    }

    public String getProfileDirName() {
        return profileDirName;
    }

    public String getDocDir() {
        return docDir;
    }

    public String getSrcDir() {
        return srcDir;
    }

    public String getProfileFileName() {
        return profileFileName;
    }

    public String getHome() {
        return home;
    }

    public String getName() {
        return name;
    }

    public SearchContext getSearchContext() {
        return searchContext;
    }

    public void setSearchContext(SearchContext searchContext) {
        this.searchContext = searchContext;
    }

    public boolean regexpIsEnabled() {
        return regexpIsEnabled;
    }

    public void setRegexpIsEnabled(boolean regexpIsEnabled) {
        this.regexpIsEnabled = regexpIsEnabled;
    }

    public boolean wholeWordIsEnabled() {
        return wholeWordIsEnabled;
    }

    public void setWholeWordIsEnabled(boolean wholeWordIsEnabled) {
        this.wholeWordIsEnabled = wholeWordIsEnabled;
    }

    public boolean matchCaseIsEnabled() {
        return matchCaseIsEnabled;
    }

    public void setMatchCaseIsEnabled(boolean matchCaseIsEnabled) {
        this.matchCaseIsEnabled = matchCaseIsEnabled;
    }
}