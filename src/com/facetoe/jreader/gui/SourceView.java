package com.facetoe.jreader.gui;

import com.facetoe.jreader.java.JavaSourceFile;
import com.facetoe.jreader.java.JavaSourceFileParser;
import japa.parser.ParseException;
import org.jdesktop.swingx.JXCollapsiblePane;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 1/11/13
 * Time: 11:28 AM
 */

public class SourceView extends JPanel {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.setLayout(new BorderLayout());
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                JPanel sourcePanel = new SourceView(null);
                frame.add(sourcePanel);
                frame.pack();
                frame.setVisible(true);
            }
        });

    }

    SourceTreeView tree;
    JScrollPane scrollPane;
    JPanel treePanel;
    JXCollapsiblePane collapsiblePane;
    JPanel searchPanel;
    AutoCompleteTextField searchField;
    JButton btnSearch;


    public SourceView(JScrollPane editorPane) {
        JavaSourceFile file = null;
        try {
            file = JavaSourceFileParser.parse(new FileInputStream("/home/facetoe/tmp/src-jdk/javax/swing/JTable.java"));
        } catch ( ParseException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        tree = new SourceTreeView(file);
        /* Save a click by showing the contents of the class on load. */
        tree.expandRow(0);
        tree.addTreeSelectionListener(new SourceTreeSelectionListener(tree));
        scrollPane = new JScrollPane(tree);
        scrollPane.setPreferredSize(new Dimension(300, 200));

        collapsiblePane = new JXCollapsiblePane();

        searchPanel = new JPanel(new BorderLayout());
        searchField = new AutoCompleteTextField();
        searchField.addWordsToTrie(file.getAllDeclarations());
        btnSearch = new JButton("Filter");

        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSearch();
            }
        });
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSearch();
            }
        });

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);

        treePanel = new JPanel(new BorderLayout());
        treePanel.add(scrollPane, BorderLayout.CENTER);
        treePanel.add(searchPanel, BorderLayout.NORTH);


        collapsiblePane.setLayout(new BorderLayout());
        collapsiblePane.setDirection(JXCollapsiblePane.Direction.RIGHT);
        collapsiblePane.add("Center", treePanel);
        collapsiblePane.setCollapsed(true);


        // the Controls panel with a textfield to filter the tree
        JPanel dummyEditorPane = new JPanel(new BorderLayout());
        dummyEditorPane.setPreferredSize(new Dimension(600, 400));
        dummyEditorPane.add(new JTextArea("This is random filler"), BorderLayout.CENTER);
        dummyEditorPane.setBorder(BasicBorders.getTextFieldBorder());

        setLayout(new BorderLayout());
        // Put the tree on the left
        add("West", collapsiblePane);

        // And the pane on the right.
        add("Center", dummyEditorPane);

        // Show/hide the "Controls"
        JButton toggle = new JButton(collapsiblePane.getActionMap().get("toggle"));

        add("South", toggle);
    }

    private void handleSearch() {
        String text = searchField.getText();
        if(text.isEmpty()) {
            return;
        }

        DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
        for(Enumeration e = root.breadthFirstEnumeration() ; e.hasMoreElements() ;) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
            if(node.toString().equals(text)) {
                TreePath treePath = new TreePath(node.getPath());
                tree.setSelectionPath(treePath);
                tree.scrollPathToVisible(treePath);
                return;
            }
        }
        System.out.println("No!");
    }
}

class SourceTreeSelectionListener implements TreeSelectionListener {
    private final SourceTreeView sourceTreeView;

    public SourceTreeSelectionListener(SourceTreeView sourceTreeView) {
        this.sourceTreeView = sourceTreeView;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = ( DefaultMutableTreeNode )
                sourceTreeView.getLastSelectedPathComponent();
        if ( node == null )
            return;

        Object nodeInfo = node.getUserObject();
        System.out.println(nodeInfo);
    }
}



