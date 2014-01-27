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
import com.facetoe.jreader.helpers.Util;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private SlideOutSourceTree sourceTree;

    /**
     * Listeners that will be notified of parsing progress and search errors.
     */
    private final ArrayList<StatusUpdateListener> listeners = new ArrayList<StatusUpdateListener>();
    private String sourceFileName;
    private JReader reader;

    /**
     * Creates a new instance of JSourcePanel and displays the contents of filePath.
     *
     * @param sourceFile of the file containing the code to be displayed.
     */
    public JSourcePanel(File sourceFile, JReader reader) {
        this.sourceFile = sourceFile;
        sourceFileName = sourceFile.getName();
        this.reader = reader;
        init();
    }

    /**
     * Creates a new instance of JSourcePanel and displays the file pointed to by url.
     *
     * @param url
     */
    public JSourcePanel(URL url, JReader reader) {
        this.fileURL = url;
        this.reader = reader;
        init();
    }

    private void init() {
        profileManager = ProfileManager.getInstance();
        setLayout(new BorderLayout());
        createCodeArea();
    }

    private void createCodeArea() {
        codeArea = new RSyntaxTextArea();
        //TODO codeArea.getPopupMenu().add(new JMenuItem("I am YOU an ME"));
        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        codeArea.setCodeFoldingEnabled(true);
        codeArea.setAntiAliasingEnabled(true);
        codeArea.setEditable(false);
        codeArea.getPopupMenu().add(createPopupItem());

        RTextScrollPane scrollPane = new RTextScrollPane(codeArea);
        scrollPane.setFoldIndicatorEnabled(true);
        codeScrollPane = scrollPane;
    }

    private JMenuItem createPopupItem() {
        JMenuItem item = new JMenuItem("Search Github for selection");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedText = codeArea.getSelectedText();
                reader.createAndShowNewGithubSearchPanel(selectedText);
            }
        });
        return item;
    }

    public void createDisplay() {
        loadTheme();
        setCodeAreaText();
        parseSourceFile();
        sourceTree = new SlideOutSourceTree(this);
        add("West", sourceTree);
        add("Center", codeScrollPane);
        highlightDeclaration(javaSourceFile.getEnclosingObject());
    }

    private void loadTheme() {
        Theme theme = Util.loadTheme();
        theme.apply(codeArea);
    }

    private void setCodeAreaText() {
        try {
            String code;
            if (sourceFile != null) {
                code = Util.readFile(sourceFile.getAbsolutePath(), StandardCharsets.UTF_8);
                codeArea.setText(code);
            } else {
                code = Util.readURL(fileURL);
                codeArea.setText(code);
            }
        } catch (IOException e) {
            Util.showErrorDialog(this, e.getMessage(), "Error Loading File");
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Parse the source file and provide feedback to any listeners.
     */
    private void parseSourceFile() {
        long startTime = System.nanoTime();
        updateStatus("Parsing: " + sourceFileName);
        try {
            if (sourceFile != null) {
                javaSourceFile = parseSourceFile(sourceFile);
            } else {
                javaSourceFile = parseSourceFile(fileURL);
            }
            updateStatus(buildParseTimeMessage(System.nanoTime() - startTime));
        } catch (ParseException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }
    }

    private JavaSourceFile parseSourceFile(URL url) throws IOException, ParseException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        InputStream in = null;
        try {
            in = connection.getInputStream();
            return JavaSourceFileParser.parse(in);
        } catch (IOException e) {
            log.error(e);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return null;
    }

    private JavaSourceFile parseSourceFile(File file) throws ParseException, IOException {
        return JavaSourceFileParser.parse(new FileInputStream(file));
    }

    private String buildParseTimeMessage(double elapsedTime) {
        return String.format(
                "Parsed %s in %.2f %s",
                (sourceFileName != null ?
                        sourceFile :
                        Util.extractFileName(fileURL.getPath())),
                Util.percent(elapsedTime, 100000000000L),
                "seconds");
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

    public void scrollToEnclosingObject() {
        highlightDeclaration(javaSourceFile.getEnclosingObject());
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
            ex.printStackTrace();
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

    public void find(String text) {
        findString(text, profileManager.getSearchContext());
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
                    updateStatus("Nothing found for: " + "\"" + text + "\"");
                }
            }
        } catch (PatternSyntaxException ex) {
            updateStatus("Regex Error: " + ex.getMessage());
        } catch (Exception e) { //Sometimes RSyntaxArea barfs randomly
            log.error(e);
        }
    }

    /**
     * Add an action listener.
     *
     * @param statusUpdateListener The listener to add.
     */
    void addStatusUpdateListener(StatusUpdateListener statusUpdateListener) {
        listeners.add(statusUpdateListener);
    }

    /**
     * Update panel with message.
     *
     * @param message
     */
    private void updateStatus(String message) {
        for (StatusUpdateListener statusUpdateListener : listeners) {
            statusUpdateListener.updateStatus(message);
        }
    }

    public JavaSourceFile getJavaSourceFile() {
        return javaSourceFile;
    }

    public SlideOutSourceTree getTreePane() {
        return sourceTree;
    }
}

