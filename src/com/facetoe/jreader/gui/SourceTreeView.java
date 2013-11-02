package com.facetoe.jreader.gui;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 2/11/13
 * Time: 11:07 AM
 */

import com.facetoe.jreader.java.JavaClassOrInterface;
import com.facetoe.jreader.java.JavaSourceFile;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Provides a tree view of a source file's contents.
 */
public class SourceTreeView extends JTree {
    private static final Logger log = Logger.getLogger(SourceTreeView.class);

    /** Type to specify what sort of icons to use. */
    private enum ITEM_TYPE {
        Constructors, Methods, Fields, Enums, Class, Interface
    }
    /**
     * Constructor to build the tree view.
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

        //TODO Get the modifers in JavaSourceFileParser and display them as tooltips.
        /* We need to register the tree to enable tooltips. */
        //ToolTipManager.sharedInstance().registerComponent(this);
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
            nodeTitle = aClass.getDeclaration();
            classNode = new DefaultMutableTreeNode(new SourceItem(nodeTitle, ITEM_TYPE.Class));
        } else {
            nodeTitle = aClass.getDeclaration();
            classNode = new DefaultMutableTreeNode(new SourceItem(nodeTitle, ITEM_TYPE.Interface));
        }

        /* Sort and add items to the tree. */
        ArrayList<String> items;
        if ( aClass.hasConstructors() ) {
            items = new ArrayList<String>(aClass.getConstructors().keySet());
            classNode.add(itemsToNode(items, ITEM_TYPE.Constructors));
        }
        if ( aClass.hasMethods() ) {
            items = new ArrayList<String>(aClass.getMethods().keySet());
            classNode.add(itemsToNode(items, ITEM_TYPE.Methods));
        }
        if ( aClass.hasEnums() ) {
            items = new ArrayList<String>(aClass.getEnums().keySet());
            classNode.add(itemsToNode(items, ITEM_TYPE.Enums));
        }
        if ( aClass.hasFields() ) {
            items = new ArrayList<String>(aClass.getFields().keySet());
            classNode.add(itemsToNode(items, ITEM_TYPE.Fields));
        }

        /* If we have nested classes or interfaces, add them as well. */
        if ( aClass.hasNestedClasses() ) {
            DefaultMutableTreeNode nestedClassNode = new DefaultMutableTreeNode(new SourceItem("Nested Classes", ITEM_TYPE.Class));
            ArrayList<DefaultMutableTreeNode> nestedClasses = new ArrayList<DefaultMutableTreeNode>();

            for ( JavaClassOrInterface nestedClass : aClass.getNestedClassesAsArrayList() ) {
                nestedClasses.add(parseClass(nestedClass));
            }

            /* Sort them alphabetically before we add them to the tree */
            Collections.sort(nestedClasses, new Comparator<DefaultMutableTreeNode>() {
                @Override
                public int compare(DefaultMutableTreeNode firstObj, DefaultMutableTreeNode secondObj) {
                    return firstObj.toString().compareTo(secondObj.toString());
                }
            });

            for ( DefaultMutableTreeNode node : nestedClasses ) {
                nestedClassNode.add(node);
                classNode.add(nestedClassNode);
            }
        }
        return classNode;
    }

    /**
     * Adds all the items to a DefaultMutableTreeNode. Items can be methods, fields, constructors or enums.
     *
     * @param items The items to add.
     * @param type  The name of the items, such as: "Methods"
     * @return DefaultMutableTreeNode containing the items.
     */
    private DefaultMutableTreeNode itemsToNode(ArrayList<String> items, ITEM_TYPE type) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new SourceItem(type.name(), type));
        ArrayList<DefaultMutableTreeNode> itemNodes = new ArrayList<DefaultMutableTreeNode>();
        for ( String item : items ) {
            itemNodes.add(new DefaultMutableTreeNode(new SourceItem(item, type)));
        }

        Collections.sort(itemNodes, new Comparator<DefaultMutableTreeNode>() {
            @Override
            public int compare(DefaultMutableTreeNode firstObj, DefaultMutableTreeNode secondObj) {
                return firstObj.toString().compareTo(secondObj.toString());
            }
        });
        for ( DefaultMutableTreeNode itemNode : itemNodes ) {
            rootNode.add(itemNode);
        }
        return rootNode;
    }


    /** Class to hold item data.*/
    //TODO Refactor this to hold the actual object so you can add hover over tooltips.
    private class SourceItem {
        final String title;
        final ITEM_TYPE type;

        public SourceItem(String title, ITEM_TYPE type) {
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

    /**
     * Class to render icons in the tree view. This can be improved quite a bit by
     * adding modfiers to JavaObject during source file parsing. Then you can have different
     * icons for private, final, protected etc.
     */
    private class SourceViewIconRenderer extends DefaultTreeCellRenderer {

        /** Deterimine how this cell should be rendered.*/
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {

            Component component = super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);

            URL iconUrl = null;
            DefaultMutableTreeNode node = ( DefaultMutableTreeNode ) value;
            SourceItem sourceNode = ( SourceItem ) node.getUserObject();
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
                //setToolTipText("");

            } else {
                log.warn("Icon was null");
                //setToolTipText(null); //no tool tip
            }
            return component;
        }
    }
}
