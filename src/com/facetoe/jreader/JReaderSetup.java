package com.facetoe.jreader;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 5/10/13
 * Time: 5:59 PM
 */
public class JReaderSetup {

//    public static void main(String[] args) {
//        setUp();
//    }

    public static void setUp() {
        if ( !hasDataDir() || !hasConfigFile() ) {
            try {
                Config.createConfig();
            } catch ( IOException ex ) {
                JOptionPane.showMessageDialog(null,
                        ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }


        int result = JOptionPane.showConfirmDialog(null,
                "Before you can use JReader we need to setup a default profile.\n" +
                        "Setting up the default profile will allow you to browse the Java\n" +
                        "documentation and source code.",
                "Setup",
                JOptionPane.OK_CANCEL_OPTION);

        File profileDir = new File(Config.getString(Config.DATA_DIR) + Config.PROFILE_DIR_NAME);
        if ( !profileDir.exists() ) {
            boolean wasSuccess = profileDir.mkdirs();
            if ( !wasSuccess ) {
                JOptionPane.showMessageDialog(null, "Failed to create profile directory at: "
                        + profileDir.getAbsolutePath(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }

        if ( result == JOptionPane.OK_OPTION ) {
            if ( !Config.getBool(Config.HAS_JAVALANG_SOURCE) ) {
                downloadJavaSource();
            }

            if ( !Config.getBool(Config.HAS_EXTRACTED_SOURCE) && Config.getBool(Config.HAS_JAVALANG_SOURCE) ) {
                extractJavaSource();
            }

            if ( Config.getBool(Config.HAS_JAVALANG_SOURCE)
                    && Config.getBool(Config.HAS_EXTRACTED_SOURCE)
                    && !Config.getBool(Config.HAS_DEFAULT_PROFILE) ) {
                setUpDefaultProfile();
            }

            if(Config.getBool(Config.HAS_JAVALANG_SOURCE)
                    && Config.getBool(Config.HAS_EXTRACTED_SOURCE)
                    && Config.getBool(Config.HAS_DEFAULT_PROFILE)
                    && !Config.getBool(Config.HAS_PARSED_DOCS)) {

                try {
                    HashMap<String, String> classData = parseDocs();
                    String filePath = ProfileManager.getInstance().getPath();
                    Utilities.writeCLassData(filePath + Config.CLASS_DATA_FILE_NAME, classData);
                    Config.setBool(Config.HAS_PARSED_DOCS, true);
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            }

        } else {
            JOptionPane.showMessageDialog(null, "Goodbye");
        }

        if ( isSetup() ) {
            JOptionPane.showMessageDialog(null, "Setup Complete");
        } else {
            JOptionPane.showMessageDialog(null, "Setup failed", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    private static void downloadJavaSource() {
        JOptionPane.showMessageDialog(null,
                "JReader will now download the Java source code.");

        FileDownloaderProgressWindow progressWindow = new FileDownloaderProgressWindow(
                Config.dataDirectory + Config.JAVA_LANG_ZIP,
                "http://sourceforge.net/projects/jdk7src/files/latest/download");

        if ( progressWindow.execute() ) {
            Config.setBool(Config.HAS_JAVALANG_SOURCE, true);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Failed to download docs. Please try again later.");
            Config.setBool(Config.HAS_JAVALANG_SOURCE, false);
        }
    }

    private static void extractJavaSource() {
        JOptionPane.showMessageDialog(null,
                "JReader will now extract the Java source code.");

        String zipPath = Config.dataDirectory + Config.JAVA_LANG_ZIP;
        UnZipperProgressWindow unzipWindow = new UnZipperProgressWindow(zipPath, zipPath.replace(".zip", ""));

        try {
            unzipWindow.execute();
            Config.setBool(Config.HAS_EXTRACTED_SOURCE, true);
        } catch ( Exception ex ) {
            JOptionPane.showMessageDialog(null,
                    "Error: " + ex.getMessage(),
                    ex.getCause().toString(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static File chooseDocs() {
        JOptionPane.showMessageDialog(null,
                "Please select the directory containing the Java documentation on your system.\n" +
                        "If you don't have it yet, you can download it here:\n" +
                        "http://www.oracle.com/technetwork/java/javase/downloads/index.html");
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        do {
            int result = chooser.showOpenDialog(null);
            if ( result == JFileChooser.APPROVE_OPTION ) {
                File chosenDir = chooser.getSelectedFile();
                if ( Utilities.isJavaDocsDir(chosenDir) ) {
                    return chosenDir;
                } else {
                    result = JOptionPane.showConfirmDialog(null,
                            "That doesn't appear to be the right directory, the directory should be called \"docs\".\n" +
                                    "Would you like to try again?",
                            "Setup", JOptionPane.OK_CANCEL_OPTION);

                    if ( result == JOptionPane.CANCEL_OPTION ) {
                        return null;
                    }
                }
            }
        } while ( true );
    }

    private static void setUpDefaultProfile() {
        File docDir = chooseDocs();
        if ( docDir == null ) {
            JOptionPane.showMessageDialog(null,
                    "JReader cannot function without completing the default profile.\n" +
                            "Please try again later.");
        } else {
            String srcPath = Config.getString(Config.DATA_DIR)
                    + Config.JAVA_LANG_ZIP.replace(".zip", "")
                    + File.separator;

            ProfileManager profileManager = ProfileManager.getInstance();
            String javaDocsPath = docDir.getAbsolutePath() + File.separator + "api" + File.separator;
            profileManager.newProfile("Default", "default.ser", javaDocsPath, srcPath);

            try {
                profileManager.saveProfiles();
                Config.setBool(Config.HAS_DEFAULT_PROFILE, true);
                Config.setString(Config.CURRENT_PROFILE, "Default");

            } catch ( IOException e ) {
                Config.setBool(Config.HAS_DEFAULT_PROFILE, false);
                e.printStackTrace();
            }
        }
    }

    private static HashMap<String, String> parseDocs() throws Exception {
        int result = JOptionPane.showConfirmDialog(null, "JReader now needs to parse the Java docs.\nThis will only happen once.", "", JOptionPane.OK_CANCEL_OPTION);
        if ( result == JOptionPane.OK_OPTION ) {
            ParserProgressWindow progressWindow = new ParserProgressWindow();
            HashMap<String, String> data = progressWindow.execute();
            return data;
        } else {
            JOptionPane.showMessageDialog(null, "The profile cannot function without parsing the documentation.\n" +
                    "Please try again later");
            return null;
        }
    }

    private static boolean hasDataDir() {
        return new File(Config.dataDirectory).exists();
    }

    private static boolean hasConfigFile() {
        return new File(Config.propertiesFilePath).exists();
    }

    private static boolean hasDefaultProfile() {
        return Config.getBool(Config.HAS_DEFAULT_PROFILE);
    }

    public static boolean isSetup() {
        return hasDataDir()
                && hasConfigFile()
                && hasDefaultProfile()
                && hasDataDir()
                && Config.getBool(Config.HAS_DEFAULT_PROFILE)
                && Config.getBool(Config.HAS_JAVALANG_SOURCE)
                && Config.getBool(Config.HAS_PARSED_DOCS)
                && !Config.getString(Config.CURRENT_PROFILE).isEmpty()
                && !Config.getString(Config.DATA_DIR).isEmpty()
                && !Config.getString(Config.PROFILE_DIR).isEmpty();
    }
}
