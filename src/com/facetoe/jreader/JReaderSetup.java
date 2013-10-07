package com.facetoe.jreader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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

    public static void main(String[] args) {
        downloadJavaSource();
        Utilities.unzip("/home/facetoe/tmp/test/javaSource.zip", "/home/facetoe/tmp/test/finish.zip");
    }

    public static void setup() {
        String userHome = System.getProperty("user.home");
        String dataFolderPath = userHome + File.separator + ".jreader" + File.separator;
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

        } catch ( IOException e ) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
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

    private static String getSourceDownloadLink() {
        try {
            Document doc = Jsoup.connect("http://sourceforge.net/projects/jdk7src/files/latest/download").followRedirects(false).get();
            return doc.select("a").get(0).attr("href");
        } catch ( IOException e ) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }


    public static void downloadJavaSource() {
        FileDownloader downloader = new FileDownloader();
        downloader.download("/home/facetoe/tmp/test/javaSource.zip", getSourceDownloadLink());
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
            if ( Config.getEntry("hasDocs").equals("true") && Config.getEntry("dataIsParsed").equals("true") ) {
                return true;
            }
        }
        return false;
    }
}
