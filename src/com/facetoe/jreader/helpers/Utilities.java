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

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
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
     * This should work on both Windows and Unix systems...
     */
    public static String docPathToSourcePath(String docPath) {
        docPath = browserPathToSystemPath(docPath);
        ProfileManager profileManager = ProfileManager.getInstance();

        Path fullPath = Paths.get(docPath);
        Path docSection = Paths.get(profileManager.getDocDir());

        /* Here we extract the section of the path we are after,
        which is everything after the documentation directory. */
        String subPath = fullPath.subpath(docSection.getNameCount(),
                fullPath.getNameCount())
                .toString();

        /* And add the source directory to the beginning to get the complete path. */
        String path = constructPath(profileManager.getSrcDir(), subPath);

        /* If there are more than 2 periods it's probably a nested class like: /dir/dir/SomeClass.SomeNestedClass.html.
         * Extract the class name. */
        String fileName = Paths.get(path).getFileName().toString();
        if (fileName.split("\\.").length > 2) {
            String objectName = fileName.substring(0, fileName.indexOf("."));
            path = path.substring(0, path.lastIndexOf(File.separator) + 1) + objectName + ".java";
        }

        return path.replace(".html", ".java");
    }

    /**
     * Converts a path from a URL to a system path. Trys to get rid of any crud that gets tacked on
     * such as ?overview-summary.html etc.
     *
     * @param path
     * @return the converted path.
     */
    public static String browserPathToSystemPath(String path) {
        assert path != null;
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

        /* Some paths look like: docs/api/java/awt/dnd/DropTarget.html#DropTarget()
         * We are only interested in the information before the '#', so remove the rest */
        if (path.contains("#")) {
            path = path.substring(0, path.indexOf("#"));
        }

        /* If the user is browsing with frames then this will be present.
         * Remove it   */
        if (path.contains("?overview-summary.html")) {
            path = path.replace("?overview-summary.html", "");

        } else if (path.contains("index.html?")) {
            path = path.replace("index.html?", "");
        }
        return path;
    }


    /**
     * Extracts the title from a local HTML file or the filename for remote pages.
     *
     * @param path
     * @return the title or an empty string if something failed.
     */
    public static String extractTitle(String path) {
        if (isWebAddress(path)) {
            return extractFileName(path);
        }
        path = browserPathToSystemPath(path);
        String title = "";
        try {
            title = extractHtmlTitle(path);
        } catch (FileNotFoundException e) {
            log.error(e);
        }
        return title;
    }

    private static String extractHtmlTitle(String path) throws FileNotFoundException {
        String html;
        FileInputStream inStream;
        Scanner scanner;
        inStream = new FileInputStream(path);
        scanner = new Scanner(inStream);

        while (scanner.hasNextLine()) {
            html = scanner.nextLine();
            /* This is sufficient for the Java 7 docs as the title is all in one line */
            if (html.toLowerCase().contains("<title>") && html.toLowerCase().contains("<title/>")) {
                return extractTitleFromHtml(html);
            } else {
                /* The Java 6 documentation and older generated javadocs spread the title over multiple lines */
                while (scanner.hasNextLine()) {
                    html += scanner.nextLine();
                    if (html.toLowerCase().contains("</title>")) {
                        return extractTitleFromHtml(html);
                    }
                }
            }
        }
        return "";
    }

    private static String extractTitleFromHtml(String html) {
        return Jsoup.parse(html, "UTF-8").select("title").text();
    }

    public static String extractFileName(String path) {
        String title = new File(path).getName();
        int nameEnd;

        if (title.contains("#")) {
            nameEnd = title.indexOf("#");
        } else {
            nameEnd = title.length();
        }
        return title.substring(0, nameEnd);
    }

    public static boolean isGoodSourcePath(String path) {

        if (path == null || isWebAddress(path) || !path.endsWith(".java")) {
            return false;
        }

        /**
         * Some paths look like: src-jdk/javax/imageio/ImageReader.java#readAll(int, javax.imageio.ImageReadParam)
         * Extract the actual path to avoid an error.
         */
        if (path.contains("#")) {
            /* Get the actual path */
            String[] parts = path.split("#");
            path = parts[0];
        }

        return new File(path).isFile();
    }

    public static void writeObject(File outFile, Object data) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(outFile);
        ObjectOutputStream outStream = new ObjectOutputStream(fileOut);
        try {
            outStream.writeObject(data);
        } finally {
            outStream.close();
            fileOut.close();
        }
    }

    /**
     * Reads a HashMap of class names and urls from classDataFile and returns it.
     *
     * @param classDataFile The file to write to
     * @return HashMap The data to write.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object readObject(File classDataFile) throws IOException {
        FileInputStream fileIn = new FileInputStream(classDataFile);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Object object = null;
        try {
            object = in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace(); //This shouldn't happen...
        } finally {
            in.close();
            fileIn.close();
        }
        return object;
    }

    public static void checkAndCreateFileIfNotPresent(File file) throws IOException {
        if (!file.exists()) {
            boolean wasSuccess = file.createNewFile();
            if (!wasSuccess) {
                throw new IOException("Failed to create file at: " + file.getAbsolutePath());
            }
        }
    }

    public static void checkAndCreateDirectoryIfNotPresent(File directory) throws IOException {
        if (!directory.exists()) {
            boolean wasSuccess = directory.mkdirs();
            if (!wasSuccess) {
                throw new IOException("Failed to create or directory at: " + directory.getAbsolutePath());
            }
        }
    }

    public static File getFile(String... pathElements) {
        return new File(constructPath(pathElements));
    }

    public static String constructPath(String... pathElement) {
        String outPath = "";
        for (String element : pathElement) {
            if (!element.startsWith(File.separator))
                outPath += File.separator + element;
            else
                outPath += element;
        }
        return outPath;
    }

    public static boolean isJavaDocsDir(File docDir) {
        docDir = findDocDir(docDir);
        File indexFile = new File(docDir.getAbsoluteFile() + File.separator + "index.html");
        File classFile = new File(docDir.getAbsoluteFile() + File.separator + "allclasses-noframe.html");
        if (!indexFile.exists() || !classFile.exists()) {
            return false;
        }

        Scanner scanner;
        String line;
        try {
            scanner = new Scanner(indexFile);
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if ((line.contains("Java") && line.contains("Documentation"))
                        || line.contains("Generated Documentation")
                        || line.contains("Generated by javadoc")) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public static String getHomePage(String docDirPath) {
        File docDir = new File(docDirPath);
        File[] filesArr = docDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String fileName = file.getName();
                return fileName.equals("overview-summary.html")
                        || fileName.equals("allclasses-noframe.html")
                        || fileName.equals("index.html");
            }

        });
        return findHomePage(filesArr);
    }

    private static String findHomePage(File[] filesArr) {
        HashMap<String, File> files = new HashMap<String, File>();
        String homeFileName = "";
        if (filesArr == null)
            return "";

        for (File file : filesArr) {
            files.put(file.getName(), file);
        }

        if (files.containsKey("overview-summary.html")) {
            homeFileName = "overview-summary.html";
        } else if (files.containsKey("allclasses-noframe.html")) {
            homeFileName = "allclasses-noframe.html";
        } else if (files.containsKey("index.html")) {
            homeFileName = "index.html";
        }
        return homeFileName;
    }

    private static boolean isWebAddress(String path) {
        return path.startsWith("http:/")
                || path.startsWith("www.")
                || path.startsWith("https:/");
    }

    /**
     * This method looks for the api directory in the Java documentation docs directory.
     * If it doesn't find anything then this docmentation is probably a library, so
     * it just returns the directory it was passed.
     */
    public static File findDocDir(File rootDir) {
        File[] files = rootDir.listFiles();
        if (files == null)
            return rootDir;

        for (File file : files) {
            if (file.getName().equals("api")) {
                return file;
            }
        }
        return rootDir;
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteDirectoryAndContents(File file) {
        if (file.isDirectory()) {
            if (file.list().length == 0) {
                file.delete();
            } else {
                // Recursive delete everything in here
                for (String temp : file.list()) {
                    File fileDelete = new File(file, temp);
                    deleteDirectoryAndContents(fileDelete);
                }
                // Delete enclosing file
                if (file.list().length == 0) {
                    file.delete();
                }
            }
        } else {
            // It's a file, just delete it.
            file.delete();
        }
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
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static File getResourceAsFile(Class sourceClass, String reference) throws URISyntaxException {
        return new File(sourceClass.getResource(reference).toURI());
    }

    public static void showErrorDialog(String message, String title) {
        showErrorDialog(null, message, title);
    }

    public static void showErrorDialog(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
}

