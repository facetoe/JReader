/*   
*    Copyright (C) 2013  facetoe - facetoe@ymail.com
*
*    This program is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation; either version 2 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License along
*    with this program; if not, write to the Free Software Foundation, Inc.,
*    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package com.facetoe.jreader.ui;

import com.facetoe.jreader.helpers.ProfileManager;
import com.facetoe.jreader.helpers.Utilities;
import com.facetoe.jreader.parsers.AbstractJavaObject;
import com.facetoe.jreader.parsers.JavaSourceFile;
import com.facetoe.jreader.parsers.JavaSourceFileParser;
import japa.parser.ParseException;
import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

/**
 * Displays source code with syntax highlighting and cold folding.
 */
public class JSourcePanel extends JPanel implements AutoCompletable {
    private final Logger log = Logger.getLogger(this.getClass());

    private RSyntaxTextArea codeArea;
    private RTextScrollPane codeScrollPane;
    private JavaSourceFile javaSourceFile;
    private File sourceFile;
    private URL fileURL;
    private ProfileManager profileManager;

    /**
     * Listeners that will be notified of parsing progress and search errors.
     */
    private final ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
    private String sourceFileName;

    private TopPanel topPanel; // TODO figure out how to set the button without passing this in here.

    /**
     * Creates a new instance of JSourcePanel and displays the contents of filePath.
     *
     * @param sourceFile of the file containing the code to be displayed.
     */
    public JSourcePanel(File sourceFile, TopPanel topPanel) {
        this.sourceFile = sourceFile;
        this.topPanel = topPanel;
        sourceFileName = sourceFile.getName();
        initPanel(topPanel);
    }

    public JSourcePanel(URL url) {
        this.fileURL = url;
        System.out.println("Called");
        initPanel(null);
    }

    private void initPanel(TopPanel topPanel) {
        profileManager = ProfileManager.getInstance();
        this.topPanel = topPanel;
        setLayout(new BorderLayout());

        /* Set up our text area. */
        createCodeArea();
    }

    public void createDisplay() {
        loadTheme();
        setCodeAreaText();
        parseSourceFile();
        add("West", new SlideOutSourceTree(this));
        add("Center", codeScrollPane);
        highlightDeclaration(javaSourceFile.getEnclosingObject());
    }

    private void setCodeAreaText() {
        try {
            String code;
            if (sourceFile != null) {
                code = Utilities.readFile(sourceFile.getAbsolutePath(), StandardCharsets.UTF_8);
                codeArea.setText(code);
            } else {
                code = Utilities.readURL(fileURL);
                System.out.println(code);
                codeArea.setText(code);
            }
        } catch (IOException e) {
            Utilities.showErrorDialog(this, e.getMessage(), "Error Loading File");
            log.error(e.getMessage(), e);
        }
    }

    private void loadTheme() {
        Theme theme = Utilities.loadTheme();
        theme.apply(codeArea);
    }

    private void createCodeArea() {
        codeArea = new RSyntaxTextArea();
        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        codeArea.setCodeFoldingEnabled(true);
        codeArea.setAntiAliasingEnabled(true);
        codeArea.setEditable(false);
        RTextScrollPane scrollPane = new RTextScrollPane(codeArea);
        scrollPane.setFoldIndicatorEnabled(true);
        codeScrollPane = scrollPane;
    }

    public void highlightDeclaration(AbstractJavaObject object) {
        highlightDeclaration(object.getBeginLine(), object.getEndLine(), object.getBeginColumn());
    }

    private void highlightDeclaration(int beginLine, int endLine, int beginCol) {
        String selectText = extractDeclaration(beginLine, endLine, beginCol);
        findString(selectText, new SearchContext(selectText));
    }

    /**
     * This method is necessary because long method or function declarations are
     * split across multiple lines in the source but not in the declaration returned
     * by the JavaSourceFileParser.
     */
    private String extractDeclaration(int beginLine, int endLine, int beginCol) {
        int start = 0;
        int end = 0;
        try {
            start = codeArea.getLineStartOffset(beginLine - 1);
            end = codeArea.getLineEndOffset(endLine - 1);
        } catch (BadLocationException ex) {
            // Shouldn't happen...
        }
        String body = codeArea.getText();
        //  If you don't do -1 it chops off the first character.
        start += beginCol - 1;
        String declaration = "";

        for (int i = start; i < end; i++) {
            if (body.charAt(i) == '{' || body.charAt(i) == ';') {
                break;
            }
            declaration += body.charAt(i);
        }
        return declaration;
    }

    /**
     * This method attempts to find a string or regexp in the source file.
     * It first searches from the current position to the end of the file,
     * if that doesn't succeed it searches from the beginning of the file to the
     * end.
     * It fires an ActionEvent if nothing is found or if there is a Regexp error.
     */
    private void findString(String text, SearchContext context) {
        boolean found;
        int caretPos = codeArea.getCaretPosition();
        context.setSearchFor(text);

        try {
            found = SearchEngine.find(codeArea, context);
            if (!found) {
                codeArea.setCaretPosition(0);
                found = SearchEngine.find(codeArea, context);
                if (!found) {
                    // If we didn't find anything, reset the caret position to its origional position
                    codeArea.setCaretPosition(caretPos);
                    fireEvent(new ActionEvent(this, 0, "Nothing found for: " + "\"" + text + "\""));
                }
            }
        } catch (PatternSyntaxException ex) {
            fireEvent(new ActionEvent(this, 0, "Regex Error: " + ex.getMessage()));
        } catch (Exception e) { //Sometimes RSyntaxArea barfs randomly
            log.error(e);
        }
    }

    /**
     * Parse the source file and provide feedback to any listeners.
     */
    private void parseSourceFile() {
        long startTime = System.nanoTime();
        fireEvent(new ActionEvent(this, 0, "Parsing: " + sourceFileName));
        try {
            if (sourceFile != null) {
                javaSourceFile = JavaSourceFileParser.parse(new FileInputStream(sourceFile));
            } else {
                HttpsURLConnection connection = (HttpsURLConnection) fileURL.openConnection();
                javaSourceFile = JavaSourceFileParser.parse(connection.getInputStream());
            }
        } catch (ParseException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        } finally {
            long elapsedTime = System.nanoTime() - startTime;
            fireEvent(new ActionEvent(this, 0, String.format(
                    "Parsed %s in %.2f %s", sourceFileName, (double) elapsedTime / 1000000000, "seconds")));
        }
    }

    @Override
    public ArrayList<String> getAutoCompleteWords() {
        return javaSourceFile.getAllDeclarations();
    }

    /**
     * This method first attempts to highlight the declaration. If nothing is found for the key,
     * it then attmepts to search for it.
     *
     * @param key The declaration that we want to highlight.
     */
    @Override
    public void handleAutoComplete(String key) {
        AbstractJavaObject obj = javaSourceFile.getObject(key);
        if (obj != null) {
            highlightDeclaration(obj);
        } else {
            findString(key, profileManager.getSearchContext());
        }
    }

    /**
     * Add an action listener.
     *
     * @param listener The listener to add.
     */
    void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    /**
     * Notify listeners of progress.
     *
     * @param event The progress information.
     */
    private void fireEvent(final ActionEvent event) {
        for (ActionListener listener : listeners) {
            log.debug("Fireing Event: " + event.getActionCommand());
            listener.actionPerformed(event);
        }
    }

    public JavaSourceFile getJavaSourceFile() {
        return javaSourceFile;
    }

    public TopPanel getTopPanel() {//TODO Refactor this so you don't need to pass the panel in here
        return topPanel;
    }
}
