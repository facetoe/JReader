package com.facetoe.jreader;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.folding.FoldCollapser;
import org.fife.ui.rsyntaxtextarea.folding.FoldManager;
import org.fife.ui.rsyntaxtextarea.folding.FoldType;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;


/**
 * Displays source code with syntax highlighting and cold folding.
 */
public class JSourcePanel extends JPanel {
    RSyntaxTextArea textArea;
    RTextScrollPane scrollPane;

    /**
     * Creates a new instance of JSourcePanel and displays the contents of filePath.
     *
     * @param filePath of the file containing the code to be displayed.
     */
    public JSourcePanel(String filePath) {
        textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setEditable(false);

        //TODO Figure out a way for the user to set themes.
        try {
            InputStream in = getClass().getResourceAsStream("/com/facetoe/jreader/resources/themes/eclipseTheme.xml");
            Theme theme = Theme.load(in);
            theme.apply(textArea);
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        scrollPane = new RTextScrollPane(textArea);
        scrollPane.setFoldIndicatorEnabled(true);
        setLayout(new BorderLayout());
        add(scrollPane);

        try {
            String code = Utilities.readFile(Paths.get(filePath).toString(), StandardCharsets.UTF_8);
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

    /**
     * Collapses all the comments.
     */
    public void collapseAllComments() {
        FoldManager foldManager = textArea.getFoldManager();
        FoldCollapser foldCollapser = new FoldCollapser(FoldType.COMMENT);
        foldCollapser.collapseFolds(foldManager);
    }

    //TODO Figure out how to set the SearchContext in a half decent way.
    /**
     * @param text to search for
     * @param context The search context for this search.
     * @return whether or not anything was found.
     */
    public boolean findString(String text, SearchContext context) {
        context.setSearchFor(text);
        return SearchEngine.find(textArea, context);
    }
}
