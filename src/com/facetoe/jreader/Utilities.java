package com.facetoe.jreader;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import javax.swing.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utilities {

//    public static void main(String[] args) {
//        String orig = "file:///C:/Users/windowsBox/Desktop/docs/api/org/omg/PortableServer/SERVANT_RETENTION_POLICY_ID.html";
//          System.out.println("Orig: " + orig);
//          System.out.println("Modi: " + docPathToSourcePath(orig));
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

    /**
     * Extracts the method name from path including the first bracket.
     * Without including the first bracket calling JSourcePane.findString(methodName) will usually
     * locate the string in the comments instead of at the declaration.
     *
     * @param path to extract the method name from
     * @return the method name
     */
    //TODO fix this method so it works for constants as well as method names
    public static String extractMethodNameFromPath(String path) {
        String methodName = null;
        if ( path.contains("#") ) {
            String[] parts = path.split("#");
            methodName = parts[1];
            methodName = methodName.substring(0, methodName.indexOf("(") + 1);
        }
        return methodName;
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

        String[] badNames = {"index.html"};

        for ( int i = 0; i < badNames.length; i++ ) {
            if ( path.contains(badNames[i]) ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Takes a JavaClassData object and writes to file at filePath.
     *
     * @param filePath to write the data to. File must exist.
     * @param data     JavaClassData to write
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

