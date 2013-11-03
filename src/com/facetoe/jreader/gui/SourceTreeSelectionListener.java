package com.facetoe.jreader.gui;

import com.facetoe.jreader.java.JavaObject;
import org.apache.log4j.Logger;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 3/11/13
 * Time: 10:04 AM
 */
public class SourceTreeSelectionListener implements TreeSelectionListener {
    private final SourceTree sourceTreeView;
    private final JSourcePanel sourcePanel;

    public SourceTreeSelectionListener(SourceTree sourceTreeView, JSourcePanel sourcePanel) {
        this.sourceTreeView = sourceTreeView;
        this.sourcePanel = sourcePanel;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        SourceItemNode node = (SourceItemNode)
                sourceTreeView.getLastSelectedPathComponent();
        if ( node == null ) {
            Logger.getLogger(SourceTreeSelectionListener.class).warn("Null node in SourceTreeSelectionListener.");
            return;
        }
        /* Scroll to show the newly selected node. */
        TreePath treePath = new TreePath(node.getPath());
        sourceTreeView.scrollPathToVisible(treePath);
        JavaObject object = sourcePanel.getJavaSourceFile().getObject(node.title);
        if(object != null) {
            sourcePanel.highlightDeclaration(object.getBeginLine(), object.getEndLine(), object.getBeginColumn());
        }
    }
}