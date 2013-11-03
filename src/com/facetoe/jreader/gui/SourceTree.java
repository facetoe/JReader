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
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/** Type to specify what sort of icons to use. */
enum ITEM_TYPE {
    Constructors,
    Methods,
    Fields,
    Enums,
    Class,
    Interface
}

/**
 * Provides a tree view of a source file's contents.
 */
public class SourceTree extends JTree {

    private static final Logger log = Logger.getLogger(SourceTree.class);
    private JavaSourceFile sourceFile;

    /**
     * Constructor to build the tree view.
     *
     * @param sourceFile The file to display in the tree view.
     */
    public SourceTree(JavaSourceFile sourceFile) {
        this.sourceFile = sourceFile;

        SourceItemNode rootNode = new SourceItemNode("Root", null);

        /* Build the tree. */
        for ( JavaClassOrInterface classOrInterface : sourceFile.getFileContents() ) {
            SourceItemNode node = parseClass(classOrInterface);
            rootNode.add(node);
        }
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
     * Encapsulates all the data from a classs in a SourceItemNode. If the class contains nested classes or interfaces,
     * this method will add them as well.
     *
     * @param aClass The JavaClassOrInterface to parse.
     * @return SourceItemNode containing the parsed data.
     */
    private SourceItemNode parseClass(JavaClassOrInterface aClass) {
        String nodeTitle;
        SourceItemNode classNode;

        if ( aClass.getType() == JavaClassOrInterface.CLASS ) {
            nodeTitle = aClass.getDeclaration();
            classNode = new SourceItemNode(nodeTitle, ITEM_TYPE.Class);
        } else {
            nodeTitle = aClass.getDeclaration();
            classNode = new SourceItemNode(nodeTitle, ITEM_TYPE.Interface);
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
            SourceItemNode nestedClassNode = new SourceItemNode("Nested Classes", ITEM_TYPE.Class);
            ArrayList<SourceItemNode> nestedClasses = new ArrayList<SourceItemNode>();

            for ( JavaClassOrInterface nestedClass : aClass.getNestedClassesAsArrayList() ) {
                nestedClasses.add(parseClass(nestedClass));
            }

            /* Sort them alphabetically before we add them so
             * they are sorted in the SourceTree. */
            Collections.sort(nestedClasses, new Comparator<SourceItemNode>() {
                @Override
                public int compare(SourceItemNode firstObj, SourceItemNode secondObj) {
                    return firstObj.toString().compareTo(secondObj.toString());
                }
            });

            for ( SourceItemNode node : nestedClasses ) {
                nestedClassNode.add(node);
                classNode.add(nestedClassNode);
            }
        }
        return classNode;
    }

    /**
     * Adds all the items to a SourceItemNode. Items can be methods, fields, constructors or enums.
     *
     * @param items The items to add.
     * @param type  The name of the items, such as: "Methods"
     * @return SourceItemNode containing the items.
     */
    private SourceItemNode itemsToNode(ArrayList<String> items, ITEM_TYPE type) {
        SourceItemNode rootNode = new SourceItemNode(type.name(), type);
        ArrayList<SourceItemNode> itemNodes = new ArrayList<SourceItemNode>();
        for ( String item : items ) {
            itemNodes.add(new SourceItemNode(item, type));
        }

        Collections.sort(itemNodes, new Comparator<SourceItemNode>() {
            @Override
            public int compare(SourceItemNode firstObj, SourceItemNode secondObj) {
                return firstObj.toString().compareTo(secondObj.toString());
            }
        });
        for ( SourceItemNode itemNode : itemNodes ) {
            rootNode.add(itemNode);
        }
        return rootNode;
    }




    /**
     * Class to render icons in the tree view. This can be improved quite a bit by
     * adding modfiers to JavaObject during source file parsing. Then you can have different
     * icons for private, final, protected etc.
     */
    private class SourceViewIconRenderer extends DefaultTreeCellRenderer {

        /** Determine how this cell should be rendered.*/
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {

            Component component = super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);

            URL iconUrl = null;
            SourceItemNode node = ( SourceItemNode ) value;

            if(node == null) {
                log.warn("Type was null for: " + node);
                return component;
            }

            ITEM_TYPE type = node.getType();

            switch ( type ) {
                case Class:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/class.png");
                    break;
                case Interface:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/interface.png");
                    break;
                case Constructors:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/constructor.gif");
                    break;
                case Methods:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/method.gif");
                    break;
                case Fields:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/field.gif");
                    break;
                case Enums:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/enum.png");
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
