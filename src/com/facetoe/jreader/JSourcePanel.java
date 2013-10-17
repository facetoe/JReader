package com.facetoe.jreader;

import com.facetoe.jreader.util.Utilities;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.folding.FoldCollapser;
import org.fife.ui.rsyntaxtextarea.folding.FoldManager;
import org.fife.ui.rsyntaxtextarea.folding.FoldType;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
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
            InputStream in = getClass().getResourceAsStream("/com/facetoe/jreader/resources/themes/ideaTheme.xml");
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
     * @param text    to search for
     * @param context The search context for this search.
     * @return whether or not anything was found.
     */
    public boolean findString(String text, SearchContext context) {
        boolean found;
        int caretPos = textArea.getCaretPosition();
        context.setSearchFor(text);
        context.setSearchForward(true);

        found = SearchEngine.find(textArea, context);
        if ( !found ) {
            textArea.setCaretPosition(0);
            found = SearchEngine.find(textArea, context);
            if ( !found ) {
                textArea.setCaretPosition(caretPos);
            }
        }
        return found;
    }

    /**
     * Highlights and scrolls to a method, constructor, field or enum declaration.
     * This method is necessary because really long method or function declarations are
     * formatted in the source like method(ReallyLongClassTypeThing superDescriptiveNameForYou,
     * AnotherAmazingLongObject incredibleyLongJavaName)
     * <p/>
     * Attempts to search for these will fail because of the unexpected newline and spaces.
     * ,
     *
     * @param beginLine Where this selection starts.
     * @param endLine   The end of the entire block if it's a method or constructor.
     * @param beginCol  The column in which this selection begins.
     */
    public void highlightDeclaration(int beginLine, int endLine, int beginCol) {
        try {
            int start = textArea.getLineStartOffset(beginLine - 1);
            int end = textArea.getLineEndOffset(endLine - 1);

            String body = textArea.getText();

            //  If you don't do -1 it chops off the first character.
            start += beginCol - 1;
            String selectText = "";

            // Loop through until we hit a semicolon or opening bracket appending to selectText.
            for ( int i = start; i < end; i++ ) {
                if ( body.charAt(i) == '{' || body.charAt(i) == ';' ) {
                    break;
                }
                selectText += body.charAt(i);
            }

            // Now search for the text and it should succeed.
            SearchContext context = new SearchContext(selectText);
            SearchEngine.find(textArea, context);
        } catch ( BadLocationException ex ) {
            System.out.println(ex);
        }

    }
}
