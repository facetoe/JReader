package com.facetoe.jreader.utilities;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 5/10/13
 * Time: 5:59 PM
 */

/**
 * Responsible for setting up JReader the first time it is loaded. This involves locating the Java documentation,
 * parsing the docs, downloading the Java source, extracting the source, and creating a default profile.
 */
public class JReaderSetup {

    /**
     * @return Whether or not setup has been completed.
     */
    public static boolean isSetup() {
        return hasDataDir()
                && hasConfigFile()
                && hasDefaultProfile()
                && Config.getBool(Config.HAS_JAVALANG_SOURCE)
                && Config.getBool(Config.HAS_PARSED_DOCS)
                && !Config.getString(Config.CURRENT_PROFILE).isEmpty()
                && !Config.getString(Config.DATA_DIR).isEmpty()
                && !Config.getString(Config.PROFILE_DIR).isEmpty();
    }

    /**
     * Creates the data directory and config file in the users home.
     * @throws IOException
     */
    public static void createDirectoriesAndConfig() throws IOException {
        boolean wasSuccess;

        File dataDir = new File(System.getProperty("user.home")
                + File.separator
                + Config.DATA_DIR_NAME
                + File.separator);

        if ( !dataDir.exists() ) {
            wasSuccess = dataDir.mkdirs();
            if ( !wasSuccess ) {
                throw new IOException("Failed to create data directory at: "
                        + dataDir.getAbsolutePath());
            }
        } else {
            System.err.println("Data Directory already exists.");
        }

        File configFile = new File(Config.propertiesFilePath);
        if ( !configFile.exists() ) {
            wasSuccess = configFile.createNewFile();
            if(wasSuccess) {
                Config.setString(Config.DATA_DIR, dataDir.getAbsolutePath() + File.separator);
                Config.setString(Config.CURRENT_PROFILE, "");
                Config.setBool(Config.HAS_DEFAULT_PROFILE, false);
                Config.setBool(Config.HAS_JAVALANG_SOURCE, false);
                Config.setBool(Config.HAS_EXTRACTED_SOURCE, false);
            } else {
                System.err.println("Failed to create config file at:" + configFile.getAbsolutePath());
            }

        } else {
            System.err.println("Config already exists.");
        }

        String profileDirPath = dataDir.getAbsolutePath()
                + File.separator
                + Config.PROFILE_DIR_NAME
                + File.separator;

        File profileDir = new File(profileDirPath);

        if ( !profileDir.exists() ) {
            wasSuccess = profileDir.mkdirs();
            if ( wasSuccess ) {
                Config.setString(Config.PROFILE_DIR, profileDirPath);
            } else {
                throw new IOException("Failed to create profile directory at: " + profileDirPath);
            }
        }
    }


    /**
     * Provides a FileChooser window for the user to select the documentation.
     * @return The documentation directory.
     */
    public static File chooseDocs() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        do {
            int result = chooser.showOpenDialog(null);
            if ( result == JFileChooser.APPROVE_OPTION ) {
                File chosenDir = chooser.getSelectedFile();
                chosenDir = Utilities.findDocDir(chosenDir);
                if ( Utilities.isJavaDocsDir(chosenDir) ) {
                    return chosenDir;
                } else {
                    result = JOptionPane.showConfirmDialog(null,
                            "That doesn't appear to be the right directory, the directory should be called \"docs\"\n" +
                                    " and contain an index.html file.\n" +
                                    "Would you like to try again?",
                            "Setup", JOptionPane.OK_CANCEL_OPTION);

                    if ( result == JOptionPane.CANCEL_OPTION ) {
                        return null;
                    }
                }
            } else {
                return null;
            }
        } while ( true );
    }

    private static boolean hasDataDir() {
        return new File(Config.dataDirectory).exists();
    }

    private static boolean hasConfigFile() {
        return new File(Config.propertiesFilePath).exists();
    }

    /**
     * Checks that a default profile exists.
     * @return boolean indicating the default profiles existance.
     */
    public static boolean hasDefaultProfile() {
        if(!new File(Config.dataDirectory).exists()) {
            return false;
        }
        File defaultProfile = new File(Config.getString(Config.PROFILE_DIR)
                + File.separator + Config.DEFAULT_PROFILE_NAME);
        return defaultProfile.exists();
    }
}
