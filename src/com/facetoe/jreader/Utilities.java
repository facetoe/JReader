package com.facetoe.jreader;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import javax.swing.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 5/10/13
 * Time: 2:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class Utilities {
    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

    public static String urlToFileName(String url) {
        String[] parts = url.split("\\/");
        return parts[parts.length - 1];
    }

    //TODO Don't hardcode source path
    public static String docPathToSourcePath(String docPath) {
        String sourcePath = docPath.replaceAll("file\\:\\/\\/", "");
        sourcePath = sourcePath.replaceAll("(\\/)\\1+", "$1");
        sourcePath = sourcePath.replaceAll("index\\.html\\?", "");
        sourcePath = sourcePath.replaceAll(Config.getEntry("apiDir"), Config.getEntry("srcDir"));
        sourcePath = sourcePath.replaceAll("\\.html", ".java");
        System.out.println("SourcePath: " + sourcePath);
        return sourcePath;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if ( bytes < unit ) return bytes + " B";
        int exp = ( int ) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static void unzip(String sourcePath, String destPath){
        try {
            ZipFile zipFile = new ZipFile(sourcePath);
            zipFile.extractAll(destPath);
        } catch (ZipException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), e.getClass().toString(), JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void writeCLassData(String fileName, JavaClassData data) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(fileName);
        ObjectOutputStream outStream = new ObjectOutputStream(fileOut);
        outStream.writeObject(data);
        outStream.close();
        fileOut.close();
    }

    public static JavaClassData readClassData(File file) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(file);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        JavaClassData classData = ( JavaClassData ) in.readObject();
        in.close();
        fileIn.close();

        return classData;
    }
}
