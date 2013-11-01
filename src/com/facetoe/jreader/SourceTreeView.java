package com.facetoe.jreader;

import com.facetoe.jreader.java.JavaClassOrInterface;
import com.facetoe.jreader.java.JavaSourceFile;
import com.facetoe.jreader.java.JavaSourceFileParser;
import japa.parser.ParseException;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
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
 * Provides a tree view of a source files contents.
 */
public class SourceTreeView extends JTree {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Test");
        JScrollPane scrollPane;
        frame.setLayout(new BorderLayout());
        frame.setPreferredSize(new Dimension(400, 400));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

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

    private static final Logger log = Logger.getLogger(SourceTreeView.class);

    private enum ITEM_TYPE {
        Constructors, Methods, Fields, Enums, Class, Interface
    }

    /**
     * Constructor to build the tree.
     *
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
        setCellRenderer(new SourceViewIconRenderer());
    }

    /**
     * Encapsulates all the data from a classs in a DefaultMutableTreeNode. If the class contains nested classes or interfaces,
     * this method will add them as well.
     *
     * @param aClass The JavaClassOrInterface to parse.
     * @return DefaultMutableTreeNode containing the parsed data.
     */
    private DefaultMutableTreeNode parseClass(JavaClassOrInterface aClass) {
        String nodeTitle;
        DefaultMutableTreeNode classNode;

        if ( aClass.getType() == JavaClassOrInterface.CLASS ) {
            nodeTitle = "Class " + aClass.getDeclaration();
            classNode = new DefaultMutableTreeNode(new SourceItemNode(nodeTitle, ITEM_TYPE.Class));
        } else {
            nodeTitle = "Interface " + aClass.getDeclaration();
            classNode = new DefaultMutableTreeNode(new SourceItemNode(nodeTitle, ITEM_TYPE.Interface));
        }

        if ( aClass.hasConstructors() ) {
            classNode.add(itemsToNode(
                    new ArrayList<String>(aClass.getConstructors().keySet()),
                    ITEM_TYPE.Constructors));
        }

        if ( aClass.hasMethods() ) {
            classNode.add(itemsToNode(
                    new ArrayList<String>(aClass.getMethods().keySet()),
                    ITEM_TYPE.Methods));
        }

        if ( aClass.hasEnums() ) {
            classNode.add(itemsToNode(
                    new ArrayList<String>(aClass.getEnums().keySet()),
                    ITEM_TYPE.Enums
            ));
        }

        if ( aClass.hasFields() ) {
            classNode.add(itemsToNode(
                    new ArrayList<String>(aClass.getFields().keySet()),
                    ITEM_TYPE.Fields
            ));
        }

        /* If we have nested classes or interfaces, add them as well. */
        if ( aClass.hasNestedClasses() ) {
            DefaultMutableTreeNode nestedNode = new DefaultMutableTreeNode(new SourceItemNode("Nested Classes", ITEM_TYPE.Class));
            for ( JavaClassOrInterface nestedClass : aClass.getNestedClassesAsArrayList() ) {
                nestedNode.add(parseClass(nestedClass));
                classNode.add(nestedNode);
            }
        }
        return classNode;
    }

    /**
     * Adds all the items to a DefaultMutableTreeNode. Items can be methods, fields, constructors etc.
     *
     * @param items the items to add.
     * @param type  The name of the items, such as: "Methods"
     * @return DefaultMutableTreeNode containing the items.
     */
    private DefaultMutableTreeNode itemsToNode(ArrayList<String> items, ITEM_TYPE type) {
        DefaultMutableTreeNode itemsNode = new DefaultMutableTreeNode(new SourceItemNode(type.name(), type));
        for ( String item : items ) {
            itemsNode.add(new DefaultMutableTreeNode(new SourceItemNode(item, type)));
        }
        return itemsNode;
    }

    private class SourceItemNode {
        String title;
        ITEM_TYPE type;

        public SourceItemNode(String title, ITEM_TYPE type) {
            this.title = title;
            this.type = type;
        }

        private ITEM_TYPE getType() {
            return type;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private class SourceViewIconRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            Component component = super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);


            try {
            URL iconUrl = null;
            DefaultMutableTreeNode node = ( DefaultMutableTreeNode ) value;
            SourceItemNode sourceNode = ( SourceItemNode ) node.getUserObject();
            ITEM_TYPE type = sourceNode.getType();


            switch ( type ) {
                case Class:
                    iconUrl = SourceTreeView.class.getResource("/com/facetoe/jreader/resources/icons/class.png");
                    break;
                case Interface:
                    iconUrl = SourceTreeView.class.getResource("/com/facetoe/jreader/resources/icons/interface.png");
                    break;
                case Constructors:
                    iconUrl = SourceTreeView.class.getResource("/com/facetoe/jreader/resources/icons/constructor.gif");
                    break;
                case Methods:
                    iconUrl = SourceTreeView.class.getResource("/com/facetoe/jreader/resources/icons/method.gif");
                    break;
                case Fields:
                    iconUrl = SourceTreeView.class.getResource("/com/facetoe/jreader/resources/icons/field.gif");
                    break;
                case Enums:
                    iconUrl = SourceTreeView.class.getResource("/com/facetoe/jreader/resources/icons/enum.png");
                    break;
                default:
                    log.warn("Unknown type: " + type.name());
                    break;
            }

            if ( iconUrl != null ) {
                setIcon(new ImageIcon(iconUrl));
                //setToolTipText("This book is in the Tutorial series.");
            } else {
                log.warn("Icon was null");
                //setToolTipText(null); //no tool tip
            }
            return component;
            }catch ( Exception ex ) {
                System.out.println(value);
                return component;
            }
        }
    }
}
