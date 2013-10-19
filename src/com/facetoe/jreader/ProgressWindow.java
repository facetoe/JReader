package com.facetoe.jreader;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 16/10/13
 * Time: 4:01 PM
 * To change this template use File | Settings | File Templates.
 */

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;

/**
 * A window to display processing progress.
 *
 * @param <T> The object returned by the processing.
 */
public abstract class ProgressWindow<T> extends JFrame {
    JPanel panel = new JPanel();

    /* This will be updated by the listener */
    JLabel lblProgress = new JLabel();

    /* This doesn't do anything yet */
    JButton btnCancel = new JButton("Cancel");
    JLabel lblTitle = new JLabel("Loading...");

    /* Display progress */
    JProgressBar progressBar = new JProgressBar();

    public ProgressWindow() {

        //TODO figure out what to do with the button.
//        btnCancel.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//            }
//        });

        progressBar.setIndeterminate(false);

        panel.setLayout(new BorderLayout(10, 10));
        panel.add(lblTitle, BorderLayout.WEST);
        panel.add(lblProgress, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.NORTH);
        panel.add(btnCancel, BorderLayout.EAST);
        panel.setBorder(new CompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        setTitle("JReader");
        setResizable(false);

        getContentPane().add(panel, BorderLayout.NORTH);
        setPreferredSize(new Dimension(550, 72));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Implement this method to do the processing.
     *
     * @return the result of processing.
     */
    abstract public T execute() throws Exception;
}