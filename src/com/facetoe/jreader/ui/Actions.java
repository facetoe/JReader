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

import com.facetoe.jreader.helpers.Util;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 26/10/13
 * Time: 3:42 PM
 */


/**
 * Action to expand and collapse the SourceTree.
 */
class ToggleSourceTreeAction extends AbstractAction {

    private final SlideOutSourceTree treePane;

    public ToggleSourceTreeAction(SlideOutSourceTree treePane) {
        super("Toggle Tree");
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
        this.treePane = treePane;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (treePane.isCollapsed()) {
            treePane.setCollapsed(false);
        } else {
            treePane.setCollapsed(true);
        }
        treePane.selectFirstEntry();
    }
}

/**
 * Action to view the source code in a JSourcePanel.
 */
class NewSourceTabAction extends AbstractAction {
    private final JReader jReader;

    public NewSourceTabAction(JReader jReader) {
        super("View Source");
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        this.jReader = jReader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (jReader.getCurrentTab() instanceof JReaderPanel) {
            JReaderPanel readerTab = (JReaderPanel) jReader.getCurrentTab();
            String systemPath = Util.docPathToSourcePath(readerTab.getCurrentPath());
            if (Util.isGoodSourcePath(systemPath)) {
                jReader.createAndShowNewSourceTab();
            }
        }
    }
}

/**
 * Action to close a tab.
 */
class CloseTabAction extends AbstractAction {
    private final JTabbedPane tabbedPane;
    private final JReader jReader;

    public CloseTabAction(JTabbedPane tabbedPane, JReader jReader) {
        this.tabbedPane = tabbedPane;
        this.jReader = jReader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int index = tabbedPane.getSelectedIndex();
        ButtonTabComponent component = (ButtonTabComponent) tabbedPane.getTabComponentAt(index);

        if (component != null) {
            component.removeTab();
            jReader.resetSearchBar();
        }
    }
}

class HistoryBackAction extends AbstractAction {
    private final JReader jReader;

    public HistoryBackAction(JReader jReader) {
        this.jReader = jReader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ((JReaderPanel) jReader.getCurrentTab()).back();
    }
}

class HistoryNextAction extends AbstractAction {
    private final JReader jReader;

    public HistoryNextAction(JReader jReader) {
        this.jReader = jReader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ((JReaderPanel) jReader.getCurrentTab()).next();
    }
}

class HomeAction extends AbstractAction {
    private final JReader jReader;

    public HomeAction(JReader jReader) {
        this.jReader = jReader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ((JReaderPanel) jReader.getCurrentTab()).home();
    }
}

/**
 * Action to create a new JReaderPanel tab.
 */
class NewReaderTabAction extends AbstractAction {
    private final JReader jreader;

    public NewReaderTabAction(JReader jreader) {
        super("New Reader Tab");
        this.jreader = jreader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        jreader.createAndShowNewReaderTab();
    }
}

/**
 * Action to quit the application.
 */
class QuitAction extends AbstractAction {
    private final JReader jReader;

    public QuitAction(JReader jReader) {
        super("Quit");
        this.jReader = jReader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        jReader.saveAndQuit();
    }
}