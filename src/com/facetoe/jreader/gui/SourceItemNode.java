package com.facetoe.jreader.gui;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 2/11/13
 * Time: 12:30 PM
 */

import com.facetoe.jreader.java.JavaObject;

import javax.swing.tree.DefaultMutableTreeNode;

/** Class to hold item data.*/
//TODO Refactor this to hold the actual object so you can add hover over tooltips.
public class SourceItemNode extends DefaultMutableTreeNode {
    private JavaObject javaObject;
    private String title;
    private int type;

    public SourceItemNode(String title, JavaObject object) {
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

    @Override
    public String toString() {
        return javaObject == null ? title : javaObject.getDeclaration();
    }
}