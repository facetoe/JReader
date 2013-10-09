package com.facetoe.jreader;

import net.lingala.zip4j.exception.ZipException;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 5/10/13
 * Time: 5:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class JReaderSetup {

//    public static void main(String[] args) {
//            setup();
//    }

    public static void setup() {
        String userHome = System.getProperty("user.home");
        String dataFolderPath = userHome + File.separator + ".jreader" + File.separator;
        String srcDirPath = dataFolderPath + File.separator + "src-jdk.zip";

        File dataDir = new File(dataFolderPath);

        if ( !dataDir.exists() ) {
            if ( !dataDir.mkdirs() ) {
                JOptionPane.showMessageDialog(null, "Failed to create data directory", "Fatal Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }

        try {
            createConfig(dataFolderPath);
            createClassDataFile(dataFolderPath);

            if ( !Config.getEntry("hasDocs").equals("true") ) {
                chooseDocs(null);
            }

            if ( !Config.getEntry("dataIsParsed").equals("true") ) {
                parseDocumentation();
            }

            if ( !Config.getEntry("hasSrc").equals("true") ) {
                getJavaSource(srcDirPath);
            }

            if ( !Config.getEntry("srcIsExtracted").equals("true") && Config.getEntry("hasSrc").equals("true") ) {
                extractSource(srcDirPath);
            }

        } catch ( IOException e ) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        if ( !isSetup() ) {
            JOptionPane.showMessageDialog(null, "Setup was not successful. Please try again.", "Setup Failed", JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        } else {
            JOptionPane.showMessageDialog(null, "Setup complete.", "Setup Successful", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void createConfig(String dataDir) throws IOException {
        String fileName = dataDir + "config.properties";
        File config = new File(fileName);

        if ( !config.exists() ) {
            if ( config.createNewFile() ) {
                Config.setEntry("configFile", fileName);
                Config.setEntry("hasDocs", "false");
                Config.setEntry("dataIsParsed", "false");
                Config.setEntry("hasSrc", "false");
                Config.setEntry("srcIsExtracted", "false");
                Config.setEntry("srcDir", dataDir + "src-jdk" + File.separator);
            } else {
                throw new IOException("Unable to create config file at: " + dataDir);
            }
        }
    }

    public static void createClassDataFile(String dataDir) throws IOException {
        String fileName = dataDir + File.separator + "classData.ser";
        File classData = new File(fileName);
        if ( !classData.exists() ) {
            if ( !classData.createNewFile() ) {
                throw new IOException("Unable to create class data file at: " + fileName);
            } else {
                Config.setEntry("classDataFile", classData.getAbsolutePath());
            }
        }
    }

    public static void chooseDocs(JFrame frame) {

        int result = JOptionPane.showConfirmDialog(null,
                "Before you can run JReader we need to do some setup.\n" +
                        "First, you need to locate the Java 7 documentation on your computer.\n" +
                        "\nIf you don't have it yet, you can download it here:\n" +
                        "http://www.oracle.com/technetwork/java/javase/downloads/index.html",
                "", JOptionPane.OK_CANCEL_OPTION);

        if ( result == JOptionPane.CANCEL_OPTION ) {
            JOptionPane.showMessageDialog(null, "Goodbye");
            System.exit(0);
        }

        do {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            result = fileChooser.showOpenDialog(frame);

            if ( result == JFileChooser.APPROVE_OPTION ) {
                File docDir = fileChooser.getSelectedFile();
                if ( isJava7DocsDir(docDir) ) {
                    Config.setEntry("docDir", docDir.getAbsolutePath() + File.separator);
                    Config.setEntry("apiDir", docDir.getAbsolutePath() + File.separator + "api" + File.separator);
                    Config.setEntry("hasDocs", "true");
                    break;
                } else {
                    result = JOptionPane.showConfirmDialog(null,
                            "That doesn't appear to be the right directory, the directory should be called \"docs\"\nWould you like to try again?",
                            "",
                            JOptionPane.YES_NO_OPTION);
                    if ( result == JOptionPane.NO_OPTION ) {
                        JOptionPane.showMessageDialog(null, "Goodbye");
                        System.exit(0);
                    }
                }

            } else {
                result = JOptionPane.showConfirmDialog(null, "You cannot use the program without the documentation. Would you like to try again?", "", JOptionPane.YES_NO_OPTION);
                if ( result == JOptionPane.NO_OPTION ) {
                    JOptionPane.showMessageDialog(null, "Goodbye");
                    System.exit(0);
                }
            }

        } while ( true );
    }

    public static void parseDocumentation() {
        int result = JOptionPane.showConfirmDialog(null, "JReader now needs to parse the Java docs.\nThis will only happen once.", "", JOptionPane.OK_CANCEL_OPTION);
        if ( result == JOptionPane.OK_OPTION ) {
            try {
                ParserWindow parserWindow = new ParserWindow();
                JavaClassData data = parserWindow.parser.get();
                Utilities.writeCLassData(Config.getEntry("classDataFile"), data);
                Config.setEntry("dataIsParsed", "true");

            } catch ( IOException e ) {
                e.printStackTrace();

            } catch ( InterruptedException ex ) {
                ex.printStackTrace();

            } catch ( ExecutionException ex ) {
                ex.printStackTrace();
                Config.setEntry("dataIsParsed", "false");

            } catch ( CancellationException ex ) {
                JOptionPane.showMessageDialog(null, "Goodbye");
                System.exit(0);
            }


        } else {
            JOptionPane.showMessageDialog(null, "The program cannot function until the docs have been parsed.");
            System.exit(0);
        }
    }

    public static void getJavaSource(String srcDirPath) {
        int result = JOptionPane.showConfirmDialog(null, "JReader needs to download the Java source code.", "", JOptionPane.OK_CANCEL_OPTION);
        if ( result == JOptionPane.OK_OPTION ) {
            try {
                downloadJavaSource(srcDirPath, "http://sourceforge.net/projects/jdk7src/files/latest/download");
                Config.setEntry("hasSrc", "true");

            } catch ( IOException e ) {
                Config.setEntry("hasSrc", "false");
                JOptionPane.showMessageDialog(null, e.getMessage(), e.getClass().toString(), JOptionPane.ERROR_MESSAGE);

            } catch ( CancellationException e ) {
                Config.setEntry("hasSrc", "false");
                JOptionPane.showMessageDialog(null, e.getMessage(), e.getClass().toString(), JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "JReader cannot function without the source code. Please try again later");
            System.exit(0);
        }
    }

    public static void extractSource(String srcDirPath) {
        int result = JOptionPane.showConfirmDialog(null, "JReader will now extract the source code", "", JOptionPane.OK_CANCEL_OPTION);
        if ( result == JOptionPane.OK_OPTION ) {
            try {
                Utilities.unzip(srcDirPath, srcDirPath.replaceAll("\\.zip", ""));
                Config.setEntry("srcIsExtracted", "true");
            } catch ( ZipException e ) {
                Config.setEntry("srcIsExtracted", "false");
                JOptionPane.showMessageDialog(null, e.getMessage(), e.getClass().toString(), JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "JReader cannot function without the source code. Please try again later.");
            System.exit(0);
        }
    }


    public static void downloadJavaSource(String localDestination, String remoteURL) throws IOException, CancellationException {
        FileDownloader downloader = new FileDownloader();
        downloader.download(localDestination, remoteURL);
    }

    public static boolean isJava7DocsDir(File docDir) {
        File[] dirList = docDir.listFiles();

        for ( File file : dirList ) {
            if ( file.getName().equalsIgnoreCase("index.html") ) {
                try {
                    Scanner scanner = new Scanner(file);
                    while ( scanner.hasNextLine() ) {
                        if ( scanner.nextLine().contains("Java Platform Standard Edition 7 Documentation") ) {
                            return true;
                        }
                    }
                } catch ( FileNotFoundException e ) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static boolean isSetup() {
        String dataDirPath = System.getProperty("user.home") +
                File.separator +
                ".jreader" +
                File.separator +
                "config.properties";

        File dataDir = new File(dataDirPath);
        if ( dataDir.exists() ) {
            if ( Config.getEntry("hasDocs").equals("true")
                    && Config.getEntry("dataIsParsed").equals("true")
                    && Config.getEntry("hasSrc").equals("true")
                    && Config.getEntry("srcIsExtracted").equals("true") ) {
                return true;
            }
        }
        return false;
    }
}