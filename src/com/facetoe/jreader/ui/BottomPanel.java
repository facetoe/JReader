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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 28/10/13
 * Time: 12:53 PM
 */

/**
 * The bottom panel for the JReader window.
 */


class BottomPanel extends JPanel {

    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel lblStatus = new JLabel();

    BottomPanel() {
        initBottomPanel();
    }

    private void initBottomPanel() {
        setLayout(new BorderLayout(5, 0));
        setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));

        add(lblStatus, BorderLayout.CENTER);

        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);
        add(progressBar, BorderLayout.EAST);
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getLblStatus() {
        return lblStatus;
    }
}
