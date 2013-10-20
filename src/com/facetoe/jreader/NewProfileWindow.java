package com.facetoe.jreader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 18/10/13
 * Time: 11:55 AM
 */
public class NewProfileWindow {
    private static JFrame frame = new JFrame("New Profile");

    private JPanel parentPanel;
    private JPanel pnlName;
    private JTextField txtName;
    private JPanel pnlDocs;
    private JTextField txtDocs;
    private JButton btnDocs;
    private JPanel pnlSrc;
    private JButton btnSrc;
    private JButton btnOK;
    private JButton btnCancel;
    private JTextField txtSrc;

    private File docDir;
    private File srcDir;
    private String profileName;

    public NewProfileWindow() {
        btnDocs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File chosenDir = showFileDialog();
                if(chosenDir != null) {
                    if(!Utilities.isJavaDocsDir(chosenDir)) {
                        JOptionPane.showMessageDialog(parentPanel,
                                "Invalid directory. Please choose the top level directory that contains the index.html file.",
                                "Invalid Directory",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        txtDocs.setText(chosenDir.getAbsolutePath());
                        docDir = chosenDir;
                    }
                }
            }
        });

        btnSrc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File chosenDir = showFileDialog();
                if(chosenDir != null) {
                    srcDir = chosenDir;
                    txtSrc.setText(srcDir.getAbsolutePath());
                }
            }
        });

        btnOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProfileManager manager = ProfileManager.getInstance();
                String name = txtName.getText();
                String fileName = name.replaceAll("[^A-Za-z0-9]", "_") + ".ser";
                manager.newProfile(name, fileName,
                        txtDocs.getText() + File.separator,
                        txtSrc.getText() + File.separator);
                try {
                    manager.saveProfiles();
                } catch ( IOException e1 ) {
                    JOptionPane.showMessageDialog(frame, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }

                frame.setVisible(false);
                frame.dispose();
            }
        });

        btnCancel.addActionListener(new
            ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.setVisible(false);
                    frame.dispose();
                }
            });

        frame.setContentPane(parentPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private File showFileDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = chooser.showOpenDialog(parentPanel);
        if(result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

//    public static void main(String[] args) {
//        new NewProfileWindow();
//    }
}
