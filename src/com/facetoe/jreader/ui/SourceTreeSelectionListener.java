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

import com.facetoe.jreader.parsers.AbstractJavaObject;
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
class SourceTreeSelectionListener implements TreeSelectionListener {
    private final SourceTree sourceTreeView;
    private final JSourcePanel sourcePanel;
    private static final Logger log = Logger.getLogger(SourceTreeSelectionListener.class);

    public SourceTreeSelectionListener(SourceTree sourceTreeView, JSourcePanel sourcePanel) {
        this.sourceTreeView = sourceTreeView;
        this.sourcePanel = sourcePanel;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        SourceItemNode node = ( SourceItemNode )
                sourceTreeView.getLastSelectedPathComponent();
        if ( node == null ) {
            log.warn("Null node in SourceTreeSelectionListener.");
            return;
        }
        scrollToNode(node);
        highlightDeclaration(node);
    }

    private void highlightDeclaration(SourceItemNode node) {
        AbstractJavaObject object = sourcePanel.getJavaSourceFile().getObject(node.getTitle());
        if ( object != null ) {
            sourcePanel.highlightDeclaration(object);
        }
    }

    private void scrollToNode(SourceItemNode node) {
        TreePath treePath = new TreePath(node.getPath());
        sourceTreeView.scrollPathToVisible(treePath);
    }
}