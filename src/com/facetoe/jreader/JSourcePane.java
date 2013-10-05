package com.facetoe.jreader;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 5/10/13
 * Time: 12:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class JSourcePane extends JPanel {
    RSyntaxTextArea textArea;
    RTextScrollPane scrollPane;

    public JSourcePane(String filePath) {
        textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setEditable(false);

        scrollPane = new RTextScrollPane(textArea);
        scrollPane.setFoldIndicatorEnabled(true);

        setLayout(new BorderLayout());
        add(scrollPane);

        try {
            String code = readFile(filePath, StandardCharsets.UTF_8);
            textArea.setText(code);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }
}
