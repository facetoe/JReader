package com.facetoe.jreader;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;

public class Utilities {
    private static final Logger log = Logger.getLogger(Utilities.class);


    public static void main(String[] args) {



        try {
            String path = "/home/facetoe/tmp/docs/api/java/awt/dnd/DropTarget.html";
            FileInputStream inStream = new FileInputStream(path);
            String line = null;
            Scanner scanner = new Scanner(inStream);
            while(scanner.hasNextLine()) {
                line = scanner.nextLine();
                if(line.contains("<title>")) {
                    System.out.println(Jsoup.parse(line, "UTF-8").select("title").text());
                    break;
                }
            }
            System.out.println(line);
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        }

    }

    /**
     * Reads a file and returns a String.
     *
     * @param path     to the file
     * @param encoding of the file
     * @return the file as a string
     * @throws IOException
     */
    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

    /**
     * Takes a path like "file:///user/home/docPath/api/ClassName.html"
     * and returns "/user/home/srcPath/api/ClassName.java" so it can be loaded in a JSourcePanel.
     *
     * This should work on both Windows and Unix systems...
     * @param docPath
     * @return the converted path
     */
    public static String docPathToSourcePath(String docPath) {
        docPath = browserPathToSystemPath(docPath);
        ProfileManager profileManager = ProfileManager.getInstance();

        Path fullPath = Paths.get(docPath);
        Path docSection = Paths.get(profileManager.getDocDir());

        /* Here we extract the section of the path we are after,
        which is everything after the documentation directory. */
        String subPath =  fullPath.subpath(docSection.getNameCount(), fullPath.getNameCount()).toString();

        /* And add the source directory to the beginning to get the complete path. */
        String path =  profileManager.getSrcDir() + subPath;

        /* If there are more than 2 periods it's probably a nested class like: /dir/dir/SomeClass.SomeNestedClass.html.
         * Extract the just the class name. */
         String fileName = Paths.get(path).getFileName().toString();
         if ( fileName.split("\\.").length >= 2 ) {
            String objectName = fileName.substring(0, fileName.indexOf("."));
            path = path.substring(0, path.lastIndexOf("/") + 1) + objectName + ".java";
             System.out.println(path);
         }

        return path.replace(".html", ".java");
    }

    /**
     * Converts a path from a URL to a system path. Trys to get rid of any crud that gets tacked on
     * such as ?overview-summary.html etc.
     * @param path
     * @return the converted path.
     */
    public static String browserPathToSystemPath(String path) {
        path = path.replace("file://", "");

        try {
            /* On Windows the above replace operation will leave the path as "/c:/path/path. */
            /* We create a path object, convert it to a file object and get the path as "C:/path/path.
             * There must be a better way to do this... */
            path = URLDecoder.decode(path, "utf-8");
            path = new File(path).getPath();
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }

        /* Some paths are like: docs/api/java/awt/dnd/DropTarget.html#DropTarget()
         * We are only interested in the information before the '#', so remove the rest */
        if ( path.contains("#") ) {
            path = path.substring(0, path.indexOf("#"));
        }

        /* If the user is browsing with frames then this will be present.
         * Remove it   */
        if (path.contains("?overview-summary.html")) {
            path = path.replace("?overview-summary.html", "");
        }
        return path;
    }

    /**
     * Extracts the title from a local HTML file or the filename for remote pages.
     * @param path
     * @return the title
     */
    public static String extractTitle(String path) {
        if(path.startsWith("http:/")
                || path.startsWith("www.")
                || path.startsWith("https:/") ) {
            return extractFileName(path);
        }

        try {
            path = browserPathToSystemPath(path);
            FileInputStream inStream = new FileInputStream(path);
            String html;
            Scanner scanner = new Scanner(inStream);

            while(scanner.hasNextLine()) {
                html = scanner.nextLine();

                /* This is sufficient for the Java 7 docs as the title is all in one line */
                if(html.toLowerCase().contains("<title>") && html.toLowerCase().contains("<title/>")) {
                    return Jsoup.parse(html, "UTF-8").select("title").text();
                } else {

                    /* The Java 6 documentation and generated javadocs spread the title over multiple lines */
                    while(scanner.hasNextLine())
                    {
                        html += scanner.nextLine();
                        if(html.toLowerCase().contains("</title>")) {
                            return Jsoup.parse(html, "UTF-8").select("title").text();
                        }
                    }
                }
            }
        } catch ( FileNotFoundException e ) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    public static String extractFileName(String path) {
        int nameBegin = path.lastIndexOf("/") + 1;
        int nameEnd;

        if ( path.contains("#") ) {
            nameEnd = path.indexOf("#");
        } else {
            nameEnd = path.length();
        }

        return path.substring(nameBegin, nameEnd);
    }

    /**
     * Takes bytes and returns a human readable string like "35KB"
     *
     * @param bytes to convert.
     * @param si
     * @return The human readable string
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if ( bytes < unit ) return bytes + " B";
        int exp = ( int ) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static boolean isGoodSourcePath(String path) {

        if ( path == null ) {
            return false;
        } else if ( path.startsWith("http:/")
                || path.startsWith("https:/")
                || path.startsWith("www.") ) {
            return false;
        }

        /**
         * Some paths look like: src-jdk/javax/imageio/ImageReader.java#readAll(int, javax.imageio.ImageReadParam)
         * Extract the actual path to avoid an error.
         */
        if ( path.contains("#") ) {
            /* Get the actual path */
            String[] parts = path.split("#");
            path = parts[0];
        }

        File file = new File(path);

        if ( !file.exists() || file.isDirectory() ) {
            return false;
        }
        return true;
    }

    /**
     * Takes a HashMap of class names and relative urls and writes to file at filePath.
     *
     * @param filePath to write the data to.
     * @param data     HashMap to write
     * @throws IOException
     */
    public static void writeCLassData(String filePath, HashMap<String, String> data) throws IOException {
        File file = new File(filePath);

        if ( !file.exists() ) {
            boolean wasSuccess = file.createNewFile();
            if ( !wasSuccess ) {
                throw new IOException("Failed to save class data at: " + filePath);
            }
        }
        FileOutputStream fileOut = new FileOutputStream(filePath);
        ObjectOutputStream outStream = new ObjectOutputStream(fileOut);
        outStream.writeObject(data);
        outStream.close();
        fileOut.close();
    }

    /**
     * Reads a HashMap of class names and urls from classDataFile and returns it.
     *
     * @param classDataFile The file to write to
     * @return HashMap The data to write.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static HashMap<String, String> readClassData(File classDataFile) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(classDataFile); //TODO change this to string to be more consistent with the write method
        ObjectInputStream in = new ObjectInputStream(fileIn);
        HashMap<String, String> classData = ( HashMap<String, String> ) in.readObject();
        in.close();
        fileIn.close();

        return classData;
    }

    public static boolean isJavaDocsDir(File docDir) {
        docDir = findDocDir(docDir);
        File overviewFile = new File(docDir.getAbsoluteFile() + File.separator + "overview-summary.html");
        File indexFile = new File(docDir.getAbsoluteFile() + File.separator + "index.html");
        File classFile = new File(docDir.getAbsoluteFile() + File.separator + "allclasses-noframe.html");
        if ( !indexFile.exists()
               || !classFile.exists()
               || !overviewFile.exists()) {
            return false;
        }

        Scanner scanner;
        String line;
        try {
            scanner = new Scanner(indexFile);
            while ( scanner.hasNextLine() ) {
                line = scanner.nextLine();
                if ( (line.contains("Java") && line.contains("Documentation"))
                        || line.contains("Generated Documentation")
                        || line.contains("Generated by javadoc") ) {
                    return true;
                }
            }
        } catch ( FileNotFoundException e ) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public static File findDocDir(File rootDir) {
        File[] files = rootDir.listFiles();
        if(files != null) {
            for ( File file : files ) {
                if(file.getName().equals("api")) {
                    return file;
                }
            }
        }
        return rootDir;
    }


    public static void deleteDirectoryAndContents(File file)
            throws IOException {
        if ( file.isDirectory() ) {
            if ( file.list().length == 0 ) {
                file.delete();

            } else {
                String files[] = file.list();

                // Recursive delete everything in here
                for ( String temp : files ) {
                    File fileDelete = new File(file, temp);
                    deleteDirectoryAndContents(fileDelete);
                }

                // Delete enclosing file
                if ( file.list().length == 0 ) {
                    file.delete();
                }
            }
        } else {
            // It's a file, just delete it.
            file.delete();
        }
    }

    public static ImageIcon readAnimatedGif(URL imgURL, Component component) {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Image img = null;
        try {
            MediaTracker m = new MediaTracker(component);
            img = tk.getImage(imgURL);
            m.addImage(img, 0);
            m.waitForAll();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new ImageIcon(img);
    }

    public static ImageIcon readIcon(InputStream inStream, int width, int height) {
        ImageIcon imageIcon = readIcon(inStream);
        Image image = imageIcon.getImage();
        Image resizedImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    public static ImageIcon readIcon(InputStream inStream) {
        try {
            BufferedImage icon = ImageIO.read(inStream);
            return new ImageIcon(icon);
        } catch ( IOException e ) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}

