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
package com.facetoe.jreader;

import japa.parser.ParseException;
import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.jdesktop.swingx.JXCollapsiblePane;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.PatternSyntaxException;


class ToggleSourceTreeAction extends AbstractAction {

    JXCollapsiblePane treePane;
    SourceTree tree;

    public ToggleSourceTreeAction(JXCollapsiblePane treePane, SourceTree tree) {
        super("Toggle Tree");
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
        this.treePane = treePane;
        this.tree = tree;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (treePane.isCollapsed()) {
            treePane.setCollapsed(false);
        } else {
            treePane.setCollapsed(true);
        }
        tree.setSelectionRow(0);
        tree.requestFocus();
    }
}

/**
 * Displays source code with syntax highlighting and cold folding.
 */
public class JSourcePanel extends AbstractPanel {
    private final Logger log = Logger.getLogger(this.getClass());
    private final static String DEFAULT_THEME = "/com/facetoe/jreader/resources/themes/ideaTheme.xml";

    private RSyntaxTextArea codeArea;
    private RTextScrollPane codeScrollPane;
    private JavaSourceFile javaSourceFile;
    private final String sourceFilePath;
    private AbstractJavaObject enclosingObject;
    private final ProfileManager profileManager;

    /**
     * Listeners that will be notified of parsing progress and search errors. //TODO Figure out why this isn't working.
     */
    private final ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();


    private String sourceFileName;
    private SourceTree tree;
    private JScrollPane treeScrollPane;
    private JPanel treePanel;
    private JXCollapsiblePane collapsiblePane;
    private JPanel searchPanel;
    private AutoCompleteTextField searchField;
    private JButton btnSearch;

    private JReaderTopPanel topPanel;

    /**
     * Creates a new instance of JSourcePanel and displays the contents of filePath.
     *
     * @param filePath of the file containing the code to be displayed.
     */
    public JSourcePanel(final String filePath, JReaderTopPanel topPanel) {
        profileManager = ProfileManager.getInstance();
        this.sourceFilePath = filePath;
        this.topPanel = topPanel;
        sourceFileName = new File(sourceFilePath).getName();
        setLayout(new BorderLayout());

        /* Set up our text area. */
        codeScrollPane = createCodeArea();
        add(codeScrollPane);
    }

    public void createDisplay() {
        loadTheme();
        fireEvent(new ActionEvent(this, 0, "Loading: " + sourceFileName));
        setCodeAreaText();
        parseSourceFile();
        initTreeView();
        highlightEnclosingClassOrInterface();
    }

    private void setCodeAreaText() {
        try {
            fireEvent(new ActionEvent(this, 0, "Loading: " + sourceFileName));
            String code = Utilities.readFile(Paths.get(sourceFilePath).toString(), StandardCharsets.UTF_8);
            codeArea.setText(code);
        } catch (IOException e) {
            Utilities.showError(this, e.getMessage(), "Error Loading File");
            log.error(e.getMessage(), e);
        }
    }

    private void loadTheme() {
        try {
            InputStream in = getClass().getResourceAsStream(DEFAULT_THEME);
            Theme theme = Theme.load(in);
            theme.apply(codeArea);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private RTextScrollPane createCodeArea() {
        codeArea = new RSyntaxTextArea();
        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        codeArea.setCodeFoldingEnabled(true);
        codeArea.setAntiAliasingEnabled(true);
        codeArea.setEditable(false);
        RTextScrollPane scrollPane = new RTextScrollPane(codeArea);
        scrollPane.setFoldIndicatorEnabled(true);
        return scrollPane;
    }

    private void initTreeView() {
        createTree();
        createTreelideoutPane();
        configureTreeToggleAction();
        constructTreePane();
    }

    private void constructTreePane() {
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);
        treePanel = new JPanel(new BorderLayout());
        treePanel.add(treeScrollPane, BorderLayout.CENTER);
        treePanel.add(searchPanel, BorderLayout.NORTH);
        collapsiblePane.setLayout(new BorderLayout());
        collapsiblePane.setDirection(JXCollapsiblePane.Direction.RIGHT);
        collapsiblePane.add("Center", treePanel);
        collapsiblePane.setCollapsed(true);

        setLayout(new BorderLayout());
        add("West", collapsiblePane);
        add("Center", codeScrollPane);
    }

    private void configureTreeToggleAction() {
        String keyStrokeAndKey = "control T";
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(keyStroke, keyStrokeAndKey);
        getActionMap().put(keyStrokeAndKey, new ToggleSourceTreeAction(collapsiblePane, tree));
        topPanel.setSourceButton(JReaderTopPanel.TREE_BUTTON, new ToggleSourceTreeAction(collapsiblePane, tree));
    }

    private void createTreelideoutPane() {
        collapsiblePane = new JXCollapsiblePane();
        searchPanel = new JPanel(new BorderLayout());
        searchField = new AutoCompleteTextField();
        searchField.addWordsToTrie(javaSourceFile.getAllDeclarations());
        btnSearch = new JButton("Search");
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleTreeSearch();
            }
        });
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleTreeSearch();
            }
        });
    }

    private void createTree() {
        tree = new SourceTree(javaSourceFile);
        /* Save a click by showing the contents of the class on load. */
        tree.expandRow(0);
        tree.addTreeSelectionListener(new SourceTreeSelectionListener(tree, this));
        treeScrollPane = new JScrollPane(tree);
        treeScrollPane.setPreferredSize(new Dimension(300, 200));
    }

    private void highlightEnclosingClassOrInterface() {
        highlightDeclaration(enclosingObject.getBeginLine(),
                enclosingObject.getEndLine(),
                enclosingObject.getBeginColumn());
    }

    /**
     * Parse the source file and provide feedback to any listeners.
     */
    private void parseSourceFile() {
        long startTime = System.nanoTime();
        fireEvent(new ActionEvent(this, 0, "Parsing: " + sourceFileName));
        try {
            javaSourceFile = JavaSourceFileParser.parse(new FileInputStream(sourceFilePath));
            enclosingObject = javaSourceFile.getEnclosingObject();
        } catch (ParseException e) {
            log.error(e.toString(), e);
        } catch (IOException e) {
            log.error(e.toString(), e);
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
            highlightDeclaration(obj.getBeginLine(), obj.getEndLine(),
                    obj.getBeginColumn());
        } else {
            findString(key, profileManager.getSearchContext());
        }
    }

    private void handleTreeSearch() {
        String text = searchField.getText();
        if (text.isEmpty()) {
            return;
        }

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        for (Enumeration e = root.depthFirstEnumeration(); e.hasMoreElements(); ) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            if (node.toString().equals(text)) {
                TreePath treePath = new TreePath(node.getPath());
                tree.setSelectionPath(treePath);
                tree.scrollPathToVisible(treePath);
                findString(text, profileManager.getSearchContext()); // Locate in the source also
                return;
            }
        }
    }

    /**
     * This method attempts to find a string or regexp in the source file.
     * It first searches from the current position to the end of the file,
     * if that doesn't succeed it searches from the beginning of the file to the
     * end.
     * It fires an ActionEvent if nothing is found or if there is a Regexp error.
     */
    private void findString(String text, SearchContext context) {
        boolean found = false;
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
        }
    }

    /**
     * Highlights and scrolls to a method, constructor, field or enum declaration.
     * This method is necessary because long method or function declarations are
     * split across multiple lines in the source, but not in the declaration returned
     * by the JavaSourceFileParser.
     *
     * @param beginLine Where this selection starts.
     * @param endLine   The end of the entire block if it's a method or constructor.
     * @param beginCol  The column in which this selection begins.
     */
    void highlightDeclaration(int beginLine, int endLine, int beginCol) {
        try {
            int start = codeArea.getLineStartOffset(beginLine - 1);
            int end = codeArea.getLineEndOffset(endLine - 1);

            String body = codeArea.getText();

            //  If you don't do -1 it chops off the first character.
            start += beginCol - 1;
            String selectText = "";

            for (int i = start; i < end; i++) {
                if (body.charAt(i) == '{' || body.charAt(i) == ';') {
                    break;
                }
                selectText += body.charAt(i);
            }
            findString(selectText, new SearchContext(selectText));

        } catch (BadLocationException ex) {
            log.error("Bad location in highlightDeclaration:", ex);
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
}
