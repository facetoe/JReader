package com.facetoe.jreader.ui;

import com.facetoe.jreader.parsers.AbstractJavaObject;
import org.jdesktop.swingx.JXCollapsiblePane;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

/**
 * JReader
 * Created by  facetoe on 22/01/14.
 */
class SlideOutSourceTree extends JXCollapsiblePane {
    private SourceTree tree;
    private JScrollPane treeScrollPane;
    private JPanel searchPanel;
    private AutoCompleteTextField searchField;
    private JButton btnSearch;
    private final JSourcePanel sourcePanel;

    public SlideOutSourceTree(JSourcePanel sourcePanel) {
        this.sourcePanel = sourcePanel;
        initTreeView();
    }

    private void initTreeView() {
        createTree();
        createTreeSlideoutPane();
        configureTreeToggleAction();
        constructTreePane();
    }

    private void createTree() {
        tree = new SourceTree(sourcePanel.getJavaSourceFile());
        /* Save a click by showing the contents of the class on load. */
        tree.expandRow(0);
        tree.addTreeSelectionListener(new SourceTreeSelectionListener(tree, sourcePanel));
        treeScrollPane = new JScrollPane(tree);
        treeScrollPane.setPreferredSize(new Dimension(300, 200));
    }

    private void configureTreeToggleAction() {
        String keyStrokeAndKey = "control T";
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(keyStroke, keyStrokeAndKey);
        getActionMap().put(keyStrokeAndKey, new ToggleSourceTreeAction(this, tree));
        //TODO Fix this!
//        if(sourcePanel.getTopPanel() != null)
//            sourcePanel.getTopPanel().setSourceButton(TopPanel.TREE_BUTTON, new ToggleSourceTreeAction(this, tree));
    }

    private void createTreeSlideoutPane() {
        searchPanel = new JPanel(new BorderLayout());
        searchField = new AutoCompleteTextField();
        searchField.addWordsToTrie(sourcePanel.getJavaSourceFile().getAllDeclarations());
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

    private void constructTreePane() {
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(treeScrollPane, BorderLayout.CENTER);
        treePanel.add(searchPanel, BorderLayout.NORTH);
        setLayout(new BorderLayout());
        setDirection(JXCollapsiblePane.Direction.RIGHT);
        add("Center", treePanel);
        setCollapsed(true);
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
                scrollToNode(node);
                sourcePanel.highlightDeclaration((AbstractJavaObject) node.getUserObject());
                return;
            }
        }
    }

    private void scrollToNode(DefaultMutableTreeNode node) {
        TreePath treePath = new TreePath(node.getPath());
        tree.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);
    }
}