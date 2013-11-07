package com.facetoe.jreader.gui;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 2/11/13
 * Time: 11:07 AM
 */

import com.facetoe.jreader.java.*;
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
import java.util.HashMap;

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

        SourceItemNode rootNode = parseSourceFile(sourceFile);
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

    private SourceItemNode parseSourceFile(JavaSourceFile source) {
        SourceItemNode node = new SourceItemNode("Root", null);
        source.extractAllObjectData();
        for ( JavaObject object : sourceFile.getFileContents() ) {
            if(object instanceof JavaClassOrInterface) {
                node.add(parseClass(( JavaClassOrInterface ) object));

            } else if(object instanceof JavaEnum) {
                JavaEnum javaEnum = (JavaEnum)object;
                node.add(parseEnum(javaEnum));

            } else if(object instanceof JavaAnnotation) {
                JavaAnnotation annotation = (JavaAnnotation)object;
                node.add( new SourceItemNode(annotation.getDeclaration(), annotation) );

            }  else {
                log.warn("Unknown object in parseSourceFile: " + object.getClass());
            }
        }
        return node;
    }

    private SourceItemNode parseEnum(JavaEnum javaEnum) {
        return objectsToNode("Enums", JavaObject.ENUM, javaEnum.getConstants());
    }

    /**
     * Encapsulates all the data from a classs in a SourceItemNode. If the class contains nested classes or interfaces,
     * this method will add them as well.
     *
     * @param aClass The JavaClassOrInterface to parse.
     * @return SourceItemNode containing the parsed data.
     */
    private SourceItemNode parseClass(JavaClassOrInterface aClass) {
        String nodeTitle = aClass.getDeclaration();
        SourceItemNode classNode;

        if ( aClass.getType() == JavaClassOrInterface.CLASS ) {
            classNode = new SourceItemNode(nodeTitle, aClass);
        } else {
            classNode = new SourceItemNode(nodeTitle, aClass);
        }

        /* Sort and add items to the tree. */
        ArrayList<String> items;
        if ( aClass.hasConstructors() ) {
            classNode.add(objectsToNode("Constructors", JavaObject.CONSTRUCTOR, aClass.getConstructors()));
        }
        if ( aClass.hasMethods() ) {
            classNode.add(objectsToNode("Methods", JavaObject.METHOD, aClass.getMethods()));
        }
//        if ( aClass.hasEnums() ) {
//            System.out.println("YEs");
//            for ( JavaEnum javaEnum : aClass.getEnums().values() ) {
//                SourceItemNode enumNode = new SourceItemNode(javaEnum.getDeclaration(), ITEM_TYPE.Enums);
//                enumNode.add(objectsToNode(javaEnum.getConstants(), ITEM_TYPE.Enums));
//                classNode.add(enumNode);
//                System.out.println(javaEnum.getConstants().size());
//            }
//
//
//            //classNode.add(objectsToNode(items, ITEM_TYPE.Enums));
//
//        }
        if ( aClass.hasFields() ) {
            classNode.add(objectsToNode("Fields", JavaObject.FIELD, aClass.getFields()));
        }

        /* If we have nested classes or interfaces, add them as well. */
        if ( aClass.hasNestedClasses() ) {
            SourceItemNode nestedClassNode = new SourceItemNode("Nested Classes", JavaObject.CLASS);
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
     * Adds all the constructors to a SourceItemNode.
     *
     * @param objects The objects to add.
     * @return SourceItemNode containing the items.
     */
    private SourceItemNode objectsToNode(String nodeTitle, int type,  HashMap<String, ?> objects ) {
        SourceItemNode rootNode = new SourceItemNode(nodeTitle, type);
        ArrayList<String> items = new ArrayList<String>(objects.keySet());
        Collections.sort(items);
        for ( String item : items ) {
            rootNode.add(new SourceItemNode(item, (JavaObject)objects.get(item)));
        }
        return rootNode;
    }


    /**
     * Class to render icons in the tree view. This can be improved quite a bit by
     * adding modfiers to JavaObject during source file parsing. Then you can have different
     * icons for private, final, protected etc.
     */
    private class SourceViewIconRenderer extends DefaultTreeCellRenderer {

        /**
         * Determine how this cell should be rendered.
         */
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {

            Component component = super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);

            URL iconUrl = null;
            SourceItemNode node = ( SourceItemNode ) value;

            if ( node == null ) {
                log.warn("Type was null for: " + node);
                return component;
            }

            int type = node.getType();

            switch ( type ) {
                case JavaObject.CLASS:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/class.png");
                    break;
                case JavaObject.INTERFACE:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/interface.png");
                    break;
                case JavaObject.CONSTRUCTOR:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/constructor.gif");
                    break;
                case JavaObject.METHOD:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/method.gif");
                    break;
                case JavaObject.FIELD:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/field.gif");
                    break;
                case JavaObject.ENUM:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/enum.png");
                    break;
                case JavaObject.ANNOTATION:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/annotation.gif");
                    break;

                default:
                    log.warn("Unknown type: " + type);
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
