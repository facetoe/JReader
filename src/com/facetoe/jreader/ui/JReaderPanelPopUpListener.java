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

import com.facetoe.jreader.helpers.Utilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 29/10/13
 * Time: 5:30 PM
 */

/**
 * Displays a popup menu when the user right clicks a JReaderPanel.
 */
class JReaderPanelPopUpListener implements MouseListener {

    private final JReader reader;
    private final JReaderPanel readerPanel;

    public JReaderPanelPopUpListener(JReader reader) {
        this.reader = reader;
        this.readerPanel = (JReaderPanel) reader.getCurrentTab();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger())
            doPop(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger())
            doPop(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger())
            doPop(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private void doPop(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem newReaderTab = buildNewReaderItem();
        menu.add(newReaderTab);

        JMenuItem viewSourceTab = buildViewSourceItem();
        menu.add(viewSourceTab);

        JMenuItem newGitItem = buildNewGithubItem();
        menu.add(newGitItem);

        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private JMenuItem buildViewSourceItem() {
        JMenuItem viewSourceItem = new JMenuItem();
        String systemPath = Utilities.docPathToSourcePath(readerPanel.getCurrentPath());
        if (Utilities.isGoodSourcePath(systemPath)) {
            String className = Utilities.getClassNameFromPath(readerPanel.getCurrentPath());
            viewSourceItem.setAction(new NewSourceTabAction(reader, "Show source for " + className));
        }
        return viewSourceItem;
    }

    private JMenuItem buildNewGithubItem() {
        JMenuItem newGitItem = new JMenuItem();
        String systemPath = Utilities.docPathToSourcePath(readerPanel.getCurrentPath());

        // If it's a good source path then its a Java class so we can search for it.
        if (Utilities.isGoodSourcePath(systemPath)) {
            final String className = Utilities.getClassNameFromPath(readerPanel.getCurrentPath());
            newGitItem.setText("Search Github for " + className);
            newGitItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    reader.createAndShowNewGithubSearchPanel(className);
                }
            });
        }
        return newGitItem;
    }

    private JMenuItem buildNewReaderItem() {
        JMenuItem newReaderTab = new JMenuItem("New Tab");
        newReaderTab.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reader.createAndShowNewReaderTab();
            }
        });
        return newReaderTab;
    }
}