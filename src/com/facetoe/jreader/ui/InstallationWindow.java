package com.facetoe.jreader.ui;

import com.facetoe.jreader.helpers.*;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * Created by facetoe on 18/01/14.
 */


/**
 * Handles the installation of JReader. Extracts the Java7 source and docs to users home directory and
 * creates  default profile. This is a blocking dialog.
 */
public class InstallationWindow implements ZipProgressListener {
    private final Logger log = Logger.getLogger(this.getClass());
    private final JDialog dialog;
    private JPanel panel;
    private JProgressBar progressBar;
    private JButton btnInstall;
    private JButton btnCancel;
    private JPanel pnlStatus;
    private JLabel lblStatus;
    private static final String SOURCE_REFERENCE = "/com/facetoe/jreader/resources/zipfiles/src-jdk.zip";
    private static final String DOCS_REFERENCE = "/com/facetoe/jreader/resources/zipfiles/jdk-7u51-apidocs.zip";
    private SwingWorker installWorker;
    private UnZipper unZipper;

    public static void main(String[] args) {
        new InstallationWindow();
    }

    public InstallationWindow() {
        dialog = new JDialog();
        dialog.setTitle("Jreader Installation");
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(panel);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.pack();
        initUI();
        dialog.setVisible(true);
    }

    private void initUI() {
        unZipper = new UnZipper();
        unZipper.addZipProgressListener(this);
        btnInstall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                install();
            }
        });
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
    }

    private void cancel() {
        if (unZipper != null)
            unZipper.cancel();
        if (installWorker != null)
            installWorker.cancel(true);

        dialog.dispose();
        System.exit(0);
    }

    private void install() {
        progressBar.setIndeterminate(true);
        try {
            installWorker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    lblStatus.setText("Initializing...");
                    JReaderSetup.createDirectoriesAndConfig();
                    if (isCancelled())
                        return null;
                    unzipSource();

                    if (isCancelled())
                        return null;
                    unzipDocs();

                    if (isCancelled())
                        return null;
                    lblStatus.setText("Creating default profile..");
                    createDefaultProfile();
                    lblStatus.setText("Complete.");

                    dialog.dispose();
                    return null;
                }
            };
            installWorker.execute();

        } catch (Exception e) {
            log.error(e);
            Util.showErrorDialog(dialog, e.getMessage(), "Error Installing JReader");
            dialog.dispose();
        }
    }

    private void createDefaultProfile() throws IOException {
        ProfileManager profileManager = ProfileManager.getInstance();
        File docDir = Util.getFileFromPathElements(
                Config.getString(Config.DATA_DIR),
                Config.JAVA_DOCS_ZIP_FILE_NAME,
                "docs");
        File srcDir = Util.getFileFromPathElements(
                Config.getString(Config.DATA_DIR),
                Config.JAVA_LANG_ZIP_FILE_NAME);

        profileManager.newProfile(Config.DEFAULT_PROFILE_NAME, docDir, srcDir);
        profileManager.setCurrentProfile(Config.DEFAULT_PROFILE_NAME);
        Config.setBool(Config.HAS_DEFAULT_PROFILE, true);
    }

    private void unzipSource() throws Exception {
        File zipFile = Util.getResourceAsFile(getClass(), SOURCE_REFERENCE);
        String destinationPath = Util.constructPath(Config.getString(Config.DATA_DIR), Config.JAVA_LANG_ZIP_FILE_NAME);
        unZipper.unzip(zipFile, destinationPath);
    }

    private void unzipDocs() throws Exception {
        File zipFile = Util.getResourceAsFile(getClass(), DOCS_REFERENCE);
        String destinationPath = Util.constructPath(Config.getString(Config.DATA_DIR), Config.JAVA_DOCS_ZIP_FILE_NAME);
        unZipper.unzip(zipFile, destinationPath);
    }


    @Override
    public void updateProgress(String message) {
        lblStatus.setText("Extracting: " + message);
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
        panel = new JPanel();
        panel.setLayout(new GridLayoutManager(4, 1, new Insets(10, 10, 10, 10), -1, -1));
        panel.setPreferredSize(new Dimension(500, 105));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        pnlStatus = new JPanel();
        pnlStatus.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:416px:noGrow", "center:d:grow"));
        panel.add(pnlStatus, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Status: ");
        CellConstraints cc = new CellConstraints();
        pnlStatus.add(label1, cc.xy(1, 1));
        lblStatus = new JLabel();
        lblStatus.setText("Idle");
        pnlStatus.add(lblStatus, cc.xy(3, 1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FormLayout("fill:d:grow", "center:d:grow"));
        panel.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        progressBar = new JProgressBar();
        panel1.add(progressBar, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:grow"));
        panel.add(panel2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnInstall = new JButton();
        btnInstall.setText("Install");
        panel2.add(btnInstall, cc.xy(1, 1));
        btnCancel = new JButton();
        btnCancel.setText("Cancel");
        panel2.add(btnCancel, cc.xy(3, 1));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new FormLayout("fill:d:grow", "center:d:grow"));
        panel.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setFont(new Font(label2.getFont().getName(), Font.BOLD, 16));
        label2.setText("JReader Installation");
        panel3.add(label2, cc.xy(1, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }
}
