package com.facetoe.jreader;

import com.facetoe.jreader.java.JavaClassOrInterface;
import com.facetoe.jreader.java.JavaSourceFile;
import com.facetoe.jreader.java.JavaSourceFileParser;
import japa.parser.ParseException;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 1/11/13
 * Time: 11:28 AM
 */

class SourceTreeSelectionListener implements TreeSelectionListener {
    SourceTreeView sourceTreeView;
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

/**
 * Provides a tree view of the files contents.
 */
public class SourceTreeView extends JTree {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Test");
        JScrollPane scrollPane;
        frame.setLayout(new BorderLayout());
        frame.setPreferredSize(new Dimension(400, 400));

        JavaSourceFile file = null;
        JavaSourceFileParser parser = new JavaSourceFileParser();
        try {
            file = parser.parse(new FileInputStream("/home/facetoe/tmp/src-jdk/java/awt/Component.java"));
        } catch ( ParseException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        SourceTreeView tree = new SourceTreeView(file);
        tree.addTreeSelectionListener(new SourceTreeSelectionListener(tree));

        scrollPane = new JScrollPane(tree);
        frame.add(scrollPane);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Constructor to build the tree.
     * @param sourceFile The file to display in the tree view.
     */
    public SourceTreeView(JavaSourceFile sourceFile) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Root");

        /* Build the tree. */
        DefaultMutableTreeNode enclosingClassNode = parseClass(sourceFile.getEnclosingClass());
        rootNode.add(enclosingClassNode);
        DefaultTreeModel model = new DefaultTreeModel(rootNode);
        setModel(model);

        /* We don't want to see the root node. */
        setRootVisible(false);

        /* Only allow one selection at time. */
        getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    /**
     * Encapsulates all the data from a classs in a DefaultMutableTreeNode. If the class contains nested classes or interfaces,
     * this method will add them as well.
     * @param aClass The JavaClassOrInterface to parse.
     * @return DefaultMutableTreeNode containing the parsed data.
     */
    private static DefaultMutableTreeNode parseClass(JavaClassOrInterface aClass) {
        String nodeTitle = (aClass.getType() == JavaClassOrInterface.CLASS ? "Class " : "Interface ") + aClass.getDeclaration();
        DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(nodeTitle);

        if ( aClass.hasConstructors() ) {
            classNode.add(itemsToNode(
                    new ArrayList<String>(aClass.getConstructors().keySet()),
                    "Constructors"));
        }

        if ( aClass.hasMethods() ) {
            classNode.add(itemsToNode(
                    new ArrayList<String>(aClass.getMethods().keySet()),
                    "Methods"));
        }

        if ( aClass.hasEnums() ) {
            classNode.add(itemsToNode(
                    new ArrayList<String>(aClass.getEnums().keySet()),
                    "Enums"
            ));
        }

        if ( aClass.hasFields() ) {
            classNode.add(itemsToNode(
                    new ArrayList<String>(aClass.getFields().keySet()),
                    "Fields"
            ));
        }

        /* If we have nested classes or interfaces, add them as well. */
        if ( aClass.hasNestedClasses() ) {
            DefaultMutableTreeNode nestedNode = new DefaultMutableTreeNode("Nested Classes");
            for ( JavaClassOrInterface nestedClass : aClass.getNestedClassesAsArrayList() ) {
                nestedNode.add(parseClass(nestedClass));
                classNode.add(nestedNode);
            }
        }
        return classNode;
    }

    /**
     * Adds all the items to a DefaultMutableTreeNode. Items can be methods, fields, constructors etc.
     * @param items the items to add.
     * @param itemsType The name of the items, such as: "Methods"
     * @return DefaultMutableTreeNode containing the items.
     */
    private static DefaultMutableTreeNode itemsToNode(ArrayList<String> items, String itemsType) {
        DefaultMutableTreeNode itemsNode = new DefaultMutableTreeNode(itemsType);
        for ( String item : items ) {
            itemsNode.add(new DefaultMutableTreeNode(item));
        }
        return itemsNode;
    }
}
