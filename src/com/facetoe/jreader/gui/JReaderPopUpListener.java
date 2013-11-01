package com.facetoe.jreader.gui;

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
class JReaderPopUpListener implements MouseListener {

    JReader reader;

    public JReaderPopUpListener(JReader reader) {
        this.reader = reader;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if ( e.isPopupTrigger() )
            doPop(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if ( e.isPopupTrigger() )
            doPop(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if ( e.isPopupTrigger() )
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
        JMenuItem newTab = new JMenuItem("New Tab");
        newTab.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reader.newJReaderTab("JReader", true);
            }
        });

        menu.add(newTab);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }
}