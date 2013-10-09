package com.facetoe.jreader;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import javax.swing.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class Utilities {

//    public static void main(String[] args) {
//        String orig = "file:///C:/Users/windowsBox/Desktop/docs/api/org/omg/PortableServer/SERVANT_RETENTION_POLICY_ID.html";
//          System.out.println("Orig: " + orig);
//          System.out.println("Modi: " + docPathToSourcePath(orig));
//    }

    /**
     * Reads a file and returns a String.
     *
     * @param path to the file
     * @param encoding of the file
     * @return the file as a string
     * @throws IOException
     */
    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

    /**
     * Takes a url and returns the file name it points to.
     *
     * @param url to extract file name from
     * @return the file name
     */
    public static String urlToFileName(String url) {
        String fileName;

        /*Split on Windows or Unix separators. If you don't wrap in Pattern.quote you get an exception from windows separators.  */
        String separator = Pattern.quote(System.getProperty("file.separator"));

        /* Sometimes the urls contain "path/filename.html#methodname" so extract the file name substring */
        if ( url.contains("#") ) {
            fileName = url.substring(url.lastIndexOf(separator) + 1, url.indexOf("#"));
        } else {
            /* Otherwise just grab the filename at the end */
            String[] parts = url.split(separator);
            fileName = parts[parts.length - 1];
        }
        return fileName;
    }

    /**
     * Extracts the method name from path including the first bracket.
     * Without including the first bracket calling JSourcePane.findString(methodName) will usually
     * locate the string in the comments instead of at the declaration.
     *
     * @param path to extract the method name from
     * @return the method name
     */
    public static String extractMethodNameFromPath(String path) {
        String[] parts = path.split("#");
        String methodName = parts[1];
        methodName = methodName.substring(0, methodName.indexOf("(") + 1);
        return methodName;
    }

    /**
     * Changes the path from the Java documentation to the relevant file of the Java source code.
     * Eg,
     * Takes a path like: file:///home/facetoe/tmp/docs//api/javax/imageio/ImageWriter.html
     * and returns: /home/facetoe/.jreader/src-jdk/javax/imageio/ImageWriter.java
     *
     * @param docPath to be converted.
     * @return the converted path.
     */
    public static String docPathToSourcePath(String docPath) {
        String path = docPath.substring(docPath.lastIndexOf("api")+3, docPath.length());
        String srcPath = Paths.get(Config.getEntry("srcDir") + path).toString();
        return srcPath.replaceAll("\\.html", ".java");
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
     * @param destPath location to extract to.
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

    /**
     * Takes a JavaClassData object and writes to file at filePath.
     *
     * @param filePath to write the data to. File must exist.
     * @param data JavaClassData to write
     * @throws IOException
     */
    public static void writeCLassData(String filePath, JavaClassData data) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(filePath);
        ObjectOutputStream outStream = new ObjectOutputStream(fileOut);
        outStream.writeObject(data);
        outStream.close();
        fileOut.close();
    }

    /**
     * Reads a JavaClassData object from classDataFile and returns it.
     *
     * @param classDataFile The file to write to
     * @return JavaClassData The data to write.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static JavaClassData readClassData(File classDataFile) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(classDataFile);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        JavaClassData classData = ( JavaClassData ) in.readObject();
        in.close();
        fileIn.close();

        return classData;
    }
}
