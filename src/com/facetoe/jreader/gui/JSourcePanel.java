package com.facetoe.jreader.gui;

import com.facetoe.jreader.ProfileManager;
import com.facetoe.jreader.java.JavaObject;
import com.facetoe.jreader.java.JavaSourceFile;
import com.facetoe.jreader.java.JavaSourceFileParser;
import com.facetoe.jreader.utilities.Utilities;
import japa.parser.ParseException;
import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;


/**
 * Displays source code with syntax highlighting and cold folding.
 */
public class JSourcePanel extends AbstractPanel {
    private final Logger log = Logger.getLogger(this.getClass());

    /**
     * The text area to display the source code.
     */
    private final RSyntaxTextArea textArea;

    /**
     * The object that contains all the definitions for this source file.
     */
    private JavaSourceFile javaSourceFile;

    /**
     * Where this file is located on the file system.
     */
    private final String sourceFilePath;

    /**
     * The top level object for this source file. It could be a class or interface,
     * and there can be any number of nested classes or interfaces within it.
     */
    private JavaObject enclosingObject;

    /**
     * Profile manager instance for this panel.
     */
    private final ProfileManager profileManager;

    /**
     * Listeners that will be notified of parsing progress and search errors.
     */
    private final ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

    /**
     * This source files name.
     */
    private String fileName;

    /**
     * Whether or not we are already waiting for the status information to be cleared.
     */
    private boolean waitingOnLabelReset = false;

    /**
     * Creates a new instance of JSourcePanel and displays the contents of filePath.
     *
     * @param filePath of the file containing the code to be displayed.
     */
    public JSourcePanel(String filePath, ActionListener listener) {
        fireEvent(new ActionEvent(this, 0, "Intitializing... " + fileName));

        /* We only really need this for the SearchContext. */
        profileManager = ProfileManager.getInstance();

        this.sourceFilePath = filePath;
        fileName = new File(sourceFilePath).getName();


        addActionListener(listener);

        /* Set up our text area. */
        textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setEditable(false);

        //TODO Figure out a way for the user to set themes.
        /* Read in the theme for this text area. */
        fireEvent(new ActionEvent(this, 0, "Loading file: " + fileName));
        try {
            InputStream in = getClass().getResourceAsStream("/com/facetoe/jreader/resources/themes/ideaTheme.xml");
            Theme theme = Theme.load(in);
            theme.apply(textArea);
        } catch ( IOException e ) {
            log.error(e.toString(), e);
        } catch ( Exception ex ) {
            System.err.println("Something happened: " + ex.toString());
        }

        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setFoldIndicatorEnabled(true);
        setLayout(new BorderLayout());
        add(scrollPane);

        try {
            String code = Utilities.readFile(Paths.get(sourceFilePath).toString(), StandardCharsets.UTF_8);
            textArea.setText(code);
        } catch ( IOException e ) {
            log.error(e.getMessage(), e);
        }

        /* Parse the source file and extract all the definitions. */
        parseSourceFile();

        if ( javaSourceFile != null ) {
            enclosingObject = javaSourceFile.getEnclosingClass();
        } else {
            log.error("Source file was null: " + filePath);
        }
    }

    /**
     * Highlights the parent class or interface for this file.
     * This needs to be called from JReader or the panel won't scroll
     * to the highlighted text.
     */
    public void highlightEnclosingObject() {
        highlightDeclaration(enclosingObject.getBeginLine(),
                enclosingObject.getEndLine(),
                enclosingObject.beginColumn);
    }

    /**
     * Parse the source file and provide feedback to any listeners.
     */
    private void parseSourceFile() {
        long startTime = System.nanoTime();
        fireEvent(new ActionEvent(this, 0, "Parsing: " + fileName));
        try {
            javaSourceFile = JavaSourceFileParser.parse(new FileInputStream(sourceFilePath));
        } catch ( ParseException e ) {
            log.error(e.toString(), e);
        } catch ( IOException e ) {
            log.error(e.toString(), e);
        } finally {
            long elapsedTime = System.nanoTime() - startTime;
            fireEvent(new ActionEvent(this, 0, String.format(
                    "Parsed %s in %.2f %s", fileName, ( double ) elapsedTime / 1000000000, "seconds")));
        }
    }

    @Override
    ArrayList<String> getAutoCompleteWords() {
        return javaSourceFile.getAllDeclarations();
    }

    /**
     * This method first attempts to highlight the declaration. If nothing is found for the key,
     * it then attmepts to search for it.
     * @param key The declaration that we want to highlight.
     */
    @Override
    void handleAutoComplete(String key) {
        JavaObject obj = javaSourceFile.getObject(key);

         if ( obj != null ) {
            highlightDeclaration(obj.getBeginLine(), obj.getEndLine(),
                    obj.beginColumn);
        } else {
            findString(key, profileManager.getSearchContext());
        }
    }

    /** This method attempts to find a string or regexp in the source file.
     *  It first searches from the current position to the end of the file,
     *  if that doesn't succeed it searches from the end of the file to the
     *  beginning.
     *
     *  It fires an ActionEvent if nothing is found or if there is a Regexp error.
     * @param text    to search for
     * @param context The search context for this search.
     */
    private void findString(String text, SearchContext context) {
        boolean found = false;
        int caretPos = textArea.getCaretPosition();
        context.setSearchFor(text);
        context.setSearchForward(true);

        try {
            found = SearchEngine.find(textArea, context);
            if ( !found ) {
                textArea.setCaretPosition(0);
                found = SearchEngine.find(textArea, context);
                if ( !found ) {
                /* If we didn't find anything, reset the caret position or it ends up
                 * jumping to the end or beginning of the file.. */
                    textArea.setCaretPosition(caretPos);
                    fireEvent(new ActionEvent(this, 0, "Nothing found for: " + "\"" + text + "\""));
                }
            }
        } catch ( PatternSyntaxException ex ) {
            fireEvent(new ActionEvent(this, 0, "Regex Error: " + ex.getMessage()));
        }

        /* Reset the status label if we were successful. This is in a seperate thread for two reasons,
         * First, when the initial call succeeds in highlightEnclosingObject() the text would otherwise be deleted.
         * Doing it this way lets it hang around for a bit. Second, I think it looks better this way when a search
         * succeeds after an error.*/
         if ( found && !waitingOnLabelReset ) {
            SwingWorker worker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    waitingOnLabelReset = true;
                    Thread.sleep(5000);
                    fireEvent(new ActionEvent(this, 0, ""));
                    waitingOnLabelReset = false;
                    return null;
                }
            };
            worker.execute();

        }
    }

    /**
     * Highlights and scrolls to a method, constructor, field or enum declaration.
     * This method is necessary because really long method or function declarations are
     * formatted in the source like: <code>method(ReallyLongClassTypeThing superDescriptiveNameForYou,
     * AnotherAmazingLongObject incredibleyLongJavaName)</code>
     * <p/>
     * Attempts to search for these will fail because of the unexpected newline and spaces.
     * ,
     *
     * @param beginLine Where this selection starts.
     * @param endLine   The end of the entire block if it's a method or constructor.
     * @param beginCol  The column in which this selection begins.
     */
    void highlightDeclaration(int beginLine, int endLine, int beginCol) {
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
            findString(selectText, new SearchContext(selectText));

        } catch ( BadLocationException ex ) {
            log.error("Bad location in highlightDeclaration:", ex);
        }
    }

    /**
     * Add an action listener.
     * @param listener The listener to add.
     */
    void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    /**
     * Notify listeners of progress.
     * @param event The progress information.
     */
    private void fireEvent(ActionEvent event) {
        for ( ActionListener listener : listeners ) {
            listener.actionPerformed(event);
        }
    }
}