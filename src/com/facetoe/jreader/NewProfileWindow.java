package com.facetoe.jreader;

import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 18/10/13
 * Time: 11:55 AM
 */

/**
 * Provides a window where users can enter the details of a new profile.
 */
public class NewProfileWindow extends JDialog {
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
    private JProgressBar progressBar;
    private JLabel lblStatus;

    /**
     * Constructor for NewProfileWindow. No need to call any methods, this does everything.
     */
    public NewProfileWindow() {
        btnDocs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File chosenDir = showFileDialog();
                if (chosenDir != null) {
                    chosenDir = Utilities.findDocDir(chosenDir);

                    /* Make sure we have a documentation directory or big problem later. */
                    if (!Utilities.isJavaDocsDir(chosenDir)) {
                        JOptionPane.showMessageDialog(parentPanel,
                                "Invalid directory. Please choose the top level directory that contains the index.html file.",
                                "Invalid Directory",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        txtDocs.setText(chosenDir.getAbsolutePath());
                    }
                }
            }
        });

        //TODO Write a method that confirms this is actually the source directory for the chosen docs.
        btnSrc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File chosenDir = showFileDialog();
                if (chosenDir != null) {
                    txtSrc.setText(chosenDir.getAbsolutePath());
                }
            }
        });

        btnOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!hasAllFields()) {
                    JOptionPane.showMessageDialog(null, "You need to fill in all the fields");
                    return;
                }

                /* Run this in a SwingWorker or the UI hangs. */
                SwingWorker worker = new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {

                        /* If we don't hide these then when the progress bar and label
                         * become visible the buttons get pushed off the bottom of the frame.
                         * There is probably a better way to do it...*/
                        pnlName.setVisible(false);
                        pnlDocs.setVisible(false);
                        pnlSrc.setVisible(false);

                        progressBar.setVisible(true);
                        progressBar.setStringPainted(true);
                        lblStatus.setVisible(true);

                        ProfileManager manager = ProfileManager.getInstance();
                        String name = txtName.getText();

                        /** Add a listener to <code>ProfileManager</code>  that will end up in <code>JavaDocParser</code>
                         * so we can monitor progress. */
                        manager.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                progressBar.setValue((int) e.getWhen());
                                progressBar.setString(e.getWhen() + "%");
                                lblStatus.setText("Parsing: " + e.getActionCommand());
                            }
                        });

                        /* Create the profile. It will be saved in the newProfile method. */
                        manager.newProfile(name,
                                txtDocs.getText() + File.separator,
                                txtSrc.getText() + File.separator);
                        manager.setCurrentProfile(name);

                        setVisible(false);
                        dispose();
                        return null;
                    }
                };
                worker.execute();

            }
        });

        btnCancel.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setVisible(false);
                        dispose();
                    }
                });

        setContentPane(parentPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private File showFileDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = chooser.showOpenDialog(parentPanel);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    private boolean hasAllFields() {
        return !txtDocs.getText().isEmpty()
                && !txtSrc.getText().isEmpty()
                && !txtName.getText().isEmpty();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        parentPanel = new JPanel();
        parentPanel.setLayout(new FormLayout("fill:d:grow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:grow,top:4dlu:noGrow,center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        parentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        pnlName = new JPanel();
        pnlName.setLayout(new FormLayout("fill:104px:noGrow,left:17dlu:noGrow,fill:d:grow", "center:d:noGrow,top:4dlu:noGrow,center:d:grow"));
        CellConstraints cc = new CellConstraints();
        parentPanel.add(pnlName, cc.xy(1, 5));
        pnlName.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        txtName = new JTextField();
        txtName.setPreferredSize(new Dimension(350, 20));
        txtName.setToolTipText("Name for this profile");
        pnlName.add(txtName, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.FILL));
        final JLabel label1 = new JLabel();
        label1.setText("Profile Name:");
        pnlName.add(label1, cc.xy(1, 1));
        final Spacer spacer1 = new Spacer();
        pnlName.add(spacer1, cc.xy(3, 3, CellConstraints.DEFAULT, CellConstraints.FILL));
        pnlDocs = new JPanel();
        pnlDocs.setLayout(new FormLayout("fill:101px:noGrow,left:19dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:noGrow"));
        parentPanel.add(pnlDocs, cc.xy(1, 7));
        pnlDocs.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        final JLabel label2 = new JLabel();
        label2.setPreferredSize(new Dimension(300, 14));
        label2.setText("Javadoc Directory:");
        pnlDocs.add(label2, cc.xyw(1, 1, 2));
        txtDocs = new JTextField();
        txtDocs.setText("");
        txtDocs.setToolTipText("Path to the Javadocs");
        pnlDocs.add(txtDocs, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        btnDocs = new JButton();
        btnDocs.setText("...");
        btnDocs.setToolTipText("Choose directory");
        pnlDocs.add(btnDocs, cc.xy(5, 1));
        pnlSrc = new JPanel();
        pnlSrc.setLayout(new FormLayout("fill:101px:noGrow,left:19dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:noGrow"));
        parentPanel.add(pnlSrc, cc.xy(1, 9));
        pnlSrc.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        final JLabel label3 = new JLabel();
        label3.setPreferredSize(new Dimension(300, 14));
        label3.setText("Source Directory:");
        pnlSrc.add(label3, cc.xyw(1, 1, 2));
        txtSrc = new JTextField();
        txtSrc.setToolTipText("Path to the source code");
        pnlSrc.add(txtSrc, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        btnSrc = new JButton();
        btnSrc.setText("...");
        btnSrc.setToolTipText("Choose directory");
        pnlSrc.add(btnSrc, cc.xy(5, 1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:noGrow"));
        parentPanel.add(panel1, cc.xy(1, 11));
        btnOK = new JButton();
        btnOK.setText("OK");
        panel1.add(btnOK, cc.xy(1, 1));
        btnCancel = new JButton();
        btnCancel.setText("Cancel");
        panel1.add(btnCancel, cc.xy(3, 1));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FormLayout("fill:d:grow", "center:d:grow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        parentPanel.add(panel2, cc.xy(1, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));
        final JLabel label4 = new JLabel();
        label4.setFont(new Font(label4.getFont().getName(), Font.BOLD, label4.getFont().getSize()));
        label4.setText("Select a name, source and documentation directory for this profile.");
        panel2.add(label4, cc.xy(1, 3));
        progressBar = new JProgressBar();
        progressBar.setVerifyInputWhenFocusTarget(true);
        progressBar.setVisible(false);
        panel2.add(progressBar, cc.xy(1, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        lblStatus = new JLabel();
        lblStatus.setText("Preparing to parse...");
        lblStatus.setVisible(false);
        panel2.add(lblStatus, cc.xy(1, 7));
        final Spacer spacer2 = new Spacer();
        parentPanel.add(spacer2, cc.xy(1, 3, CellConstraints.DEFAULT, CellConstraints.FILL));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return parentPanel;
    }

    //    public static void main(String[] args) {
//        new NewProfileWindow();
//    }
}
