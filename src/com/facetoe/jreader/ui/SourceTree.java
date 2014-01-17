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

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 2/11/13
 * Time: 11:07 AM
 */

import com.facetoe.jreader.parsers.*;
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
class SourceTree extends JTree {

    private static final Logger log = Logger.getLogger(SourceTree.class);

    /**
     * Constructor to build the tree view.
     *
     * @param sourceFile The file to display in the tree view.
     */
    public SourceTree(JavaSourceFile sourceFile) {

        createTree(sourceFile);

        /* We don't want to see the root node. */
        setRootVisible(false);
        getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        setCellRenderer(new SourceViewIconRenderer());
    }

    private void createTree(JavaSourceFile sourceFile) {
        SourceItemNode rootNode = parseSource(sourceFile);
        DefaultTreeModel model = new DefaultTreeModel(rootNode);
        setModel(model);
    }

    private SourceItemNode parseSource(JavaSourceFile sourceFile) {
        SourceItemNode node = new SourceItemNode("Root", null);
        for (AbstractJavaObject object : sourceFile.getFileContents()) {
            if (object instanceof JavaClassOrInterface) {
                node.add(parseClass((JavaClassOrInterface) object));

            } else if (object instanceof JavaEnum) {
                JavaEnum javaEnum = (JavaEnum) object;
                node.add(parseEnum(javaEnum));

            } else if (object instanceof JavaAnnotation) {
                JavaAnnotation annotation = (JavaAnnotation) object;
                node.add(parseAnnotation(annotation));

            } else {
                log.warn("Unknown object in parseSource(): " + object.getClass());
            }
        }
        return node;
    }

    private SourceItemNode parseEnum(JavaEnum javaEnum) {
        return objectsToNode(javaEnum.getDeclaration(), AbstractJavaObject.ENUM, javaEnum.getConstants());
    }

    private SourceItemNode parseAnnotation(JavaAnnotation annotation) {
        return new SourceItemNode(annotation.getDeclaration(), annotation);
    }

    private SourceItemNode parseClass(JavaClassOrInterface aClass) {
        String nodeTitle = aClass.getDeclaration();
        SourceItemNode classNode;

        if (aClass.getType() == JavaClassOrInterface.CLASS) {
            classNode = new SourceItemNode(nodeTitle, aClass);
        } else {
            classNode = new SourceItemNode(nodeTitle, aClass);
        }

        if (aClass.hasConstructors()) {
            classNode.add(extractConstructors(aClass));
        }
        if (aClass.hasMethods()) {
            classNode.add(extractMethods(aClass));
        }
        if (aClass.hasFields()) {
            classNode.add(extractFields(aClass));
        }
        if (aClass.hasEnums()) {
            classNode.add(extractEnums(aClass));
        }

        if (aClass.hasNestedClasses()) {
            // Sort them now so they appear sorted in the tree.
            ArrayList<SourceItemNode> nestedClasses = sortNestedClasses(aClass);

            SourceItemNode nestedClassNode = new SourceItemNode("Nested Classes", AbstractJavaObject.CLASS);
            for (SourceItemNode node : nestedClasses) {
                nestedClassNode.add(node);
                classNode.add(nestedClassNode);
            }
        }
        return classNode;
    }

    private SourceItemNode extractConstructors(JavaClassOrInterface aClass) {
        return objectsToNode("Constructors", AbstractJavaObject.CONSTRUCTOR, aClass.getConstructors());
    }

    private SourceItemNode extractMethods(JavaClassOrInterface aClass) {
        return objectsToNode("Methods", AbstractJavaObject.METHOD, aClass.getMethods());
    }

    private SourceItemNode extractFields(JavaClassOrInterface aClass) {
        return objectsToNode("Fields", AbstractJavaObject.FIELD, aClass.getFields());
    }

    private SourceItemNode extractEnums(JavaClassOrInterface aClass) {
        SourceItemNode enumsNode = new SourceItemNode("Enums", AbstractJavaObject.ENUM);
        for (String key : aClass.getEnums().keySet()) {
            JavaEnum anEnum = aClass.getEnums().get(key);
            enumsNode.add(objectsToNode(anEnum.getDeclaration(), AbstractJavaObject.ENUM, anEnum.getConstants()));
        }
        return enumsNode;
    }

    private ArrayList<SourceItemNode> sortNestedClasses(JavaClassOrInterface aClass) {
        ArrayList<SourceItemNode> nestedClasses = new ArrayList<SourceItemNode>();
        for (JavaClassOrInterface nestedClass : aClass.getNestedClassesAsArrayList()) {
            nestedClasses.add(parseClass(nestedClass));
        }

        Collections.sort(nestedClasses, new Comparator<SourceItemNode>() {
            @Override
            public int compare(SourceItemNode firstObj, SourceItemNode secondObj) {
                return firstObj.toString().compareTo(secondObj.toString());
            }
        });
        return nestedClasses;
    }

    /**
     * Adds all the objects to a SourceItemNode.
     *
     * @param objects The objects to add.
     * @return SourceItemNode containing the objects.
     */
    private SourceItemNode objectsToNode(String nodeTitle, int type, HashMap<String, ? extends AbstractJavaObject> objects) {
        SourceItemNode rootNode = new SourceItemNode(nodeTitle, type);
        ArrayList<String> items = new ArrayList<String>(objects.keySet());
        Collections.sort(items);
        for (String item : items) {
            rootNode.add(new SourceItemNode(item, objects.get(item)));
        }
        return rootNode;
    }


    /**
     * Class to render icons in the tree view.
     */
    private class SourceViewIconRenderer extends DefaultTreeCellRenderer {

        // These were generated using a Ruby script, they may not be 100% correct.
        private static final int DEFAULT = 0;
        private static final int ABSTRACT = 1024;
        private static final int ABSTRACT_STATIC_CLASS = 1032;
        private static final int FINAL = 16;
        private static final int FINAL_SYNCHRONIZED = 48;
        private static final int FINAL_TRANSIENT = 144;
        private static final int NATIVE = 256;
        private static final int NATIVE_SYNCHRONIZED = 288;
        private static final int PRIVATE = 2;
        private static final int PRIVATE_ABSTRACT_CLASS = 1026;
        private static final int PRIVATE_ABSTRACT_STATIC_CLASS = 1034;
        private static final int PRIVATE_FINAL = 18;
        private static final int PRIVATE_FINAL_NATIVE = 274;
        private static final int PRIVATE_FINAL_SYNCHRONIZED = 50;
        private static final int PRIVATE_FINAL_TRANSIENT = 146;
        private static final int PRIVATE_NATIVE = 258;
        private static final int PRIVATE_NATIVE_SYNCHRONIZED = 290;
        private static final int PRIVATE_STATIC = 10;
        private static final int PRIVATE_STATIC_FINAL = 26;
        private static final int PRIVATE_STATIC_FINAL_SYNCHRONIZED = 58;
        private static final int PRIVATE_STATIC_NATIVE = 266;
        private static final int PRIVATE_STATIC_NATIVE_SYNCHRONIZED = 298;
        private static final int PRIVATE_STATIC_SYNCHRONIZED = 42;
        private static final int PRIVATE_STATIC_TRANSIENT = 138;
        private static final int PRIVATE_STATIC_VOLATILE = 74;
        private static final int PRIVATE_SYNCHRONIZED = 34;
        private static final int PRIVATE_TRANSIENT = 130;
        private static final int PRIVATE_TRANSIENT_VOLATILE = 194;
        private static final int PRIVATE_VOLATILE = 66;
        private static final int PROTECTED = 4;
        private static final int PROTECTED_ABSTRACT = 1028;
        private static final int PROTECTED_ABSTRACT_STATIC_CLASS = 1036;
        private static final int PROTECTED_FINAL = 20;
        private static final int PROTECTED_FINAL_SYNCHRONIZED = 52;
        private static final int PROTECTED_NATIVE = 260;
        private static final int PROTECTED_NATIVE_SYNCHRONIZED = 292;
        private static final int PROTECTED_STATIC = 12;
        private static final int PROTECTED_STATIC_FINAL = 28;
        private static final int PROTECTED_STATIC_NATIVE = 268;
        private static final int PROTECTED_STATIC_SYNCHRONIZED = 44;
        private static final int PROTECTED_SYNCHRONIZED = 36;
        private static final int PROTECTED_TRANSIENT = 132;
        private static final int PROTECTED_VOLATILE = 68;
        private static final int PUBLIC = 1;
        private static final int PUBLIC_STRICTFP = 2049;
        private static final int PUBLIC_ABSTRACT = 1025;
        private static final int PUBLIC_ABSTRACT_STATIC_CLASS = 1033;
        private static final int PUBLIC_FINAL = 17;
        private static final int PUBLIC_FINAL_NATIVE = 273;
        private static final int PUBLIC_FINAL_SYNCHRONIZED = 49;
        private static final int PUBLIC_NATIVE = 257;
        private static final int PUBLIC_NATIVE_SYNCHRONIZED = 289;
        private static final int PUBLIC_STATIC_STRICTFP = 2057;
        private static final int PUBLIC_STATIC = 9;
        private static final int PUBLIC_STATIC_FINAL = 25;
        private static final int PUBLIC_STATIC_FINAL_SYNCHRONIZED = 57;
        private static final int PUBLIC_STATIC_NATIVE = 265;
        private static final int PUBLIC_STATIC_SYNCHRONIZED = 41;
        private static final int PUBLIC_STATIC_VOLATILE = 73;
        private static final int PUBLIC_SYNCHRONIZED = 33;
        private static final int PUBLIC_TRANSIENT = 129;
        private static final int PUBLIC_VOLATILE = 65;
        private static final int STATIC = 8;
        private static final int STATIC_FINAL = 24;
        private static final int STATIC_FINAL_NATIVE = 280;
        private static final int STATIC_NATIVE = 264;
        private static final int STATIC_NATIVE_SYNCHRONIZED = 296;
        private static final int STATIC_SYNCHRONIZED = 40;
        private static final int STATIC_TRANSIENT = 136;
        private static final int STATIC_VOLATILE = 72;
        private static final int SYNCHRONIZED = 32;
        private static final int TRANSIENT = 128;
        private static final int TRANSIENT_VOLATILE = 192;
        private static final int VOLATILE = 64;


        /**
         * Determine how this cell should be rendered. The icon chosen depends on the nodes type.
         */
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            SourceItemNode node = (SourceItemNode) value;
            setIcon(node);
            return this;
        }

        private void setIcon(SourceItemNode node) {
            ImageIcon icon;
            AbstractJavaObject object = node.getJavaObject();
            if (object == null) {
                icon = getTypeIcon(node.getType());
                if (icon != null) {
                    setIcon(icon);
                }
            } else {
                icon = getModifierIcon(node.getModifiers(), node.getType());
                if (icon != null) {
                    setIcon(icon);
                }
            }
        }

        private ImageIcon getTypeIcon(int type) {
            URL iconUrl;
            switch (type) {
                case AbstractJavaObject.CLASS:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/class.png");
                    break;
                case AbstractJavaObject.INTERFACE:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/interface.png");
                    break;
                case AbstractJavaObject.CONSTRUCTOR:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/constructor.gif");
                    break;
                case AbstractJavaObject.METHOD:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/method.gif");
                    break;
                case AbstractJavaObject.FIELD:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/field.gif");
                    break;
                case AbstractJavaObject.ENUM:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/enum.png");
                    break;
                case AbstractJavaObject.ANNOTATION:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/annotation.gif");
                    break;

                default:
                    log.warn("Unknown type: " + type);
                    return null;
            }
            return new ImageIcon(iconUrl);
        }

        private ImageIcon getModifierIcon(int modifiers, int type) {
            URL iconUrl;
            if (type == AbstractJavaObject.METHOD || type == AbstractJavaObject.CONSTRUCTOR) {
                iconUrl = getMethodIconUrl(modifiers);
            } else if (type == AbstractJavaObject.FIELD) {
                iconUrl = getFieldIconUrl(modifiers);
            } else if (type == AbstractJavaObject.CLASS) {
                iconUrl = getClassIconUrl(modifiers);
            } else {
                iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/field_public.png");
            }
            return new ImageIcon(iconUrl);
        }

        private URL getClassIconUrl(int modifiers) {
            URL iconUrl = null;
            int result;
            result = simplifyModifier(modifiers);
            switch (result) {
                case PUBLIC:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/innerclass_default.png");
                    break;

                case PRIVATE:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/innerclass_private.png");
                    break;

                case PROTECTED:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/innerclass_protected.png");
                    break;

                case DEFAULT:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/innerclass_default.png");
                    break;
            }
            return iconUrl;
        }

        private URL getFieldIconUrl(int modifiers) {
            URL iconUrl = null;
            int result;
            result = simplifyModifier(modifiers);
            switch (result) {
                case PUBLIC:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/field_public.png");
                    break;

                case PRIVATE:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/field_private.png");
                    break;

                case PROTECTED:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/field_protected.png");
                    break;

                case DEFAULT:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/field_default.png");
                    break;
            }
            return iconUrl;
        }

        private URL getMethodIconUrl(int modifiers) {
            int result;
            URL iconUrl = null;
            result = simplifyModifier(modifiers);
            switch (result) {
                case PUBLIC:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/method_public.png");
                    break;

                case PRIVATE:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/method_private.png");
                    break;

                case PROTECTED:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/method_protected.png");
                    break;

                case DEFAULT:
                    iconUrl = SourceTree.class.getResource("/com/facetoe/jreader/resources/icons/method_default.png");
                    break;
            }
            return iconUrl;
        }

        private int simplifyModifier(int modifiers) {
            switch (modifiers) {
                case PUBLIC:
                case PUBLIC_STRICTFP:
                case PUBLIC_ABSTRACT:
                case PUBLIC_ABSTRACT_STATIC_CLASS:
                case PUBLIC_FINAL:
                case PUBLIC_FINAL_NATIVE:
                case PUBLIC_FINAL_SYNCHRONIZED:
                case PUBLIC_NATIVE:
                case PUBLIC_NATIVE_SYNCHRONIZED:
                case PUBLIC_STATIC_STRICTFP:
                case PUBLIC_STATIC:
                case PUBLIC_STATIC_FINAL:
                case PUBLIC_STATIC_FINAL_SYNCHRONIZED:
                case PUBLIC_STATIC_NATIVE:
                case PUBLIC_STATIC_SYNCHRONIZED:
                case PUBLIC_STATIC_VOLATILE:
                case PUBLIC_SYNCHRONIZED:
                case PUBLIC_TRANSIENT:
                case PUBLIC_VOLATILE:
                    return PUBLIC;

                case PROTECTED:
                case PROTECTED_ABSTRACT:
                case PROTECTED_ABSTRACT_STATIC_CLASS:
                case PROTECTED_FINAL:
                case PROTECTED_FINAL_SYNCHRONIZED:
                case PROTECTED_NATIVE:
                case PROTECTED_NATIVE_SYNCHRONIZED:
                case PROTECTED_STATIC:
                case PROTECTED_STATIC_FINAL:
                case PROTECTED_STATIC_NATIVE:
                case PROTECTED_STATIC_SYNCHRONIZED:
                case PROTECTED_SYNCHRONIZED:
                case PROTECTED_TRANSIENT:
                case PROTECTED_VOLATILE:
                    return PROTECTED;

                case PRIVATE:
                case PRIVATE_ABSTRACT_CLASS:
                case PRIVATE_ABSTRACT_STATIC_CLASS:
                case PRIVATE_FINAL:
                case PRIVATE_FINAL_NATIVE:
                case PRIVATE_FINAL_SYNCHRONIZED:
                case PRIVATE_FINAL_TRANSIENT:
                case PRIVATE_NATIVE:
                case PRIVATE_NATIVE_SYNCHRONIZED:
                case PRIVATE_STATIC:
                case PRIVATE_STATIC_FINAL:
                case PRIVATE_STATIC_FINAL_SYNCHRONIZED:
                case PRIVATE_STATIC_NATIVE:
                case PRIVATE_STATIC_NATIVE_SYNCHRONIZED:
                case PRIVATE_STATIC_SYNCHRONIZED:
                case PRIVATE_STATIC_TRANSIENT:
                case PRIVATE_STATIC_VOLATILE:
                case PRIVATE_SYNCHRONIZED:
                case PRIVATE_TRANSIENT:
                case PRIVATE_TRANSIENT_VOLATILE:
                case PRIVATE_VOLATILE:
                    return PRIVATE;

                default:
                    return DEFAULT;
            }
        }

    }
}
