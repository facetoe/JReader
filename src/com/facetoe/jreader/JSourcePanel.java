package com.facetoe.jreader;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 5/10/13
 * Time: 12:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class JSourcePanel extends JPanel {
    RSyntaxTextArea textArea;
    RTextScrollPane scrollPane;
    SearchContext context = new SearchContext();

    public JSourcePanel(String filePath) {
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
            String code = Utilities.readFile(filePath, StandardCharsets.UTF_8);
            textArea.setText(code);
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        /**
         * Set the caret position to the top of the page, otherwise the page scrolls to the bottom and search
         * doesn't work as expected.
         */
        textArea.setCaretPosition(0);
    }

    public void findString(String text) {
        context.setSearchFor(text);
        SearchEngine.find(textArea, context);
    }
}
