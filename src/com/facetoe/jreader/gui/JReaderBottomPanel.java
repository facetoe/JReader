package com.facetoe.jreader.gui;

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
public class JReaderBottomPanel extends JPanel implements ActionListener {

    JReader jReader;
    JProgressBar progressBar = new JProgressBar();
    JLabel lblStatus = new JLabel();

    JReaderBottomPanel(JReader jReader) {
        this.jReader = jReader;
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

    @Override
    public void actionPerformed(ActionEvent e) {
         lblStatus.setText(e.getActionCommand());
    }
}
