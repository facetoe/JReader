package com.facetoe.jreader.gui;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 2/11/13
 * Time: 12:30 PM
 */

import javax.swing.tree.DefaultMutableTreeNode;

/** Class to hold item data.*/
//TODO Refactor this to hold the actual object so you can add hover over tooltips.
public class SourceItemNode extends DefaultMutableTreeNode {
    final String title;
    final ITEM_TYPE type;

    public SourceItemNode(String title, ITEM_TYPE type) {
        this.title = title;
        this.type = type;
    }

    public ITEM_TYPE getType() {
        return type;
    }

    @Override
    public String toString() {
        return title;
    }
}