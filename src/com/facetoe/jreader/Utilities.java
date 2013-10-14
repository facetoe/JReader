package com.facetoe.jreader;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class Utilities {

//    public static void main(String[] args) {
//        try {
//            long startTime = System.currentTimeMillis();
//
//            Document doc = Jsoup.parse(new File("/home/facetoe/tmp/docs/api/java/awt/Container.html"), "UTF-8");
//            Elements title = doc.getElementsByClass("title");
//            for ( Element element : title ) {
//                System.out.println(title.text());
//            }
//            long estimatedTime = System.currentTimeMillis() - startTime;
//            System.out.println("Took: " + estimatedTime / 1000000000);
//
//        } catch ( FileNotFoundException e ) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch ( IOException ex ) {
//            ex.printStackTrace();
//        }
//    }

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

    public static String docPathToSourcePath(String docPath) {
        docPath = browserPathToSystemPath(docPath);

        /* Chop off the section of docPath that points to the Java docs */
        String path = docPath.substring(docPath.lastIndexOf("api") + 3, docPath.length());

        /* If there are more than 2 periods it's probably a nested class like: /dir/dir/SomeClass.SomeNestedClass.html */
        if ( path.split("\\.").length > 2 ) {
            String objectName = path.substring(path.lastIndexOf("/") + 1, path.indexOf("."));
            String[] parts = path.split("\\.");
            path = path.substring(0, path.lastIndexOf("/") + 1) + objectName + ".java";
        }

        String srcPath = (Config.getEntry("srcDir") + path).replace(".html", ".java");
        return srcPath;
    }

    public static String browserPathToSystemPath(String path) {
        path = path.replace("file://", "");
        if ( path.contains("#") ) {
            path = path.substring(0, path.indexOf("#"));
        }
        return path;
    }

    public static String extractTitle(String path) {
        try {
            path = browserPathToSystemPath(path);

            Document doc = Jsoup.parse(new File(path), "UTF-8");
            Elements title = doc.getElementsByTag("h2");

            if ( title.size() >= 1 ) {
                return title.get(0).text().toString();
            }

        } catch ( FileNotFoundException e ) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch ( IOException ex ) {
            ex.printStackTrace();
        }
        return "";
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

    /**
     * Takes zip file at sourcePath and extracts to destPath.
     *
     * @param sourcePath of zip file.
     * @param destPath   location to extract to.
     * @throws ZipException
     */
    public static void unzip(String sourcePath, String destPath) throws ZipException {
        try {
            ZipFile zipFile = new ZipFile(sourcePath);
            zipFile.extractAll(destPath);
        } catch ( ZipException e ) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), e.getClass().toString(), JOptionPane.ERROR_MESSAGE);
        }
        Config.setEntry("hasSrc", "true");
    }

    public static boolean isGoodSourcePath(String path) {

        if ( path == null ) {
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
     * Takes a HashMap of JavaObjects and writes to file at filePath.
     *
     * @param filePath to write the data to. File must exist.
     * @param data     HashMap to write
     * @throws IOException
     */
    public static void writeCLassData(String filePath, HashMap<String, JavaObject> data) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(filePath);
        ObjectOutputStream outStream = new ObjectOutputStream(fileOut);
        outStream.writeObject(data);
        outStream.close();
        fileOut.close();
    }

    /**
     * Reads a HashMap of JavaObjects from classDataFile and returns it.
     *
     * @param classDataFile The file to write to
     * @return HashMap The data to write.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static HashMap<String, JavaObject> readClassData(File classDataFile) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(classDataFile);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        HashMap<String, JavaObject> classData = ( HashMap<String, JavaObject> ) in.readObject();
        in.close();
        fileIn.close();

        return classData;
    }
}

