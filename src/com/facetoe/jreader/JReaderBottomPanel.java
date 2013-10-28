package com.facetoe.jreader;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 28/10/13
 * Time: 12:53 PM
 */
public class JReaderBottomPanel extends JPanel {

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
}
