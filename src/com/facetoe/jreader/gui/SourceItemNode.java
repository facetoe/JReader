package com.facetoe.jreader.gui;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 2/11/13
 * Time: 12:30 PM
 */

import com.facetoe.jreader.java.AbstractJavaObject;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Class to hold item data.
 */
public class SourceItemNode extends DefaultMutableTreeNode {
    private AbstractJavaObject javaObject;
    private String title;
    private int type;

    public SourceItemNode(String title, AbstractJavaObject object) {
        this.javaObject = object;
        this.title = title;
    }

    public SourceItemNode(String title, int type) {
        this.title = title;
        this.type = type;
    }

    public int getType() {
        return javaObject == null ? type : javaObject.getType();
    }

    public int getModifiers() {
        return javaObject == null ? -1 : javaObject.getModifiers();
    }

    public String getTitle() {
        return title;
    }

    public AbstractJavaObject getJavaObject() {
        return javaObject;
    }

    @Override
    public String toString() {
        return javaObject == null ? title : javaObject.getDeclaration();
    }
}