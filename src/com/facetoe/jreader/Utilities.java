package com.facetoe.jreader;

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
