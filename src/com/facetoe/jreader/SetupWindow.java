package com.facetoe.jreader;

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
import java.util.HashMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 20/10/13
 * Time: 10:34 AM
 */
public class SetupWindow extends JDialog {
    private final Logger log = Logger.getLogger(this.getClass());

    private JPanel pnlParent;

    private JPanel pnlInfo;
    private JPanel pnlDocs;
    private JButton btnOK;
    private JButton btnCancel;
    private JProgressBar progressBar;
    private JLabel icnLocate;
    private JLabel icnDownload;
    private JLabel icnParse;
    private JLabel lblStatus;
    private JLabel lblInfo;
    private JLabel icnExtract;


    SetupWorker worker = new SetupWorker();


    public SetupWindow() {
        btnOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    btnOK.setEnabled(false);
                    worker.execute();
                } catch ( Exception e1 ) {
                    log.error(e1.getMessage(), e1);
                }
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                worker.wasCanceled = true;
                worker.cancel(true);
            }
        });

        setTitle("JReader Setup");
        setModalityType(ModalityType.APPLICATION_MODAL);
        setContentPane(pnlParent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setVisible(true);
    }

    private void setTickIcon(JLabel label) {
        label.setIcon(Utilities.readIcon(
                getClass().getResourceAsStream("/com/facetoe/jreader/resources/icons/system_tick_alt_03.png")));
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
        pnlParent = new JPanel();
        pnlParent.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
        pnlParent.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20), null));
        pnlInfo = new JPanel();
        pnlInfo.setLayout(new FormLayout("fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:grow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:grow,top:4dlu:noGrow,center:d:grow"));
        pnlParent.add(pnlInfo, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Locate Documentation");
        CellConstraints cc = new CellConstraints();
        pnlInfo.add(label1, cc.xy(1, 3));
        final JLabel label2 = new JLabel();
        label2.setText("Download Java Source");
        pnlInfo.add(label2, cc.xy(1, 5));
        final JLabel label3 = new JLabel();
        label3.setText("Parse Documentation");
        pnlInfo.add(label3, cc.xy(1, 9));
        icnLocate = new JLabel();
        icnLocate.setText("");
        pnlInfo.add(icnLocate, cc.xy(3, 3));
        icnDownload = new JLabel();
        icnDownload.setText("");
        pnlInfo.add(icnDownload, cc.xy(3, 5));
        icnParse = new JLabel();
        icnParse.setText("");
        pnlInfo.add(icnParse, cc.xy(3, 9));
        final JLabel label4 = new JLabel();
        label4.setText("Extract Java Source");
        pnlInfo.add(label4, cc.xy(1, 7));
        icnExtract = new JLabel();
        icnExtract.setText("");
        pnlInfo.add(icnExtract, cc.xy(3, 7));
        pnlDocs = new JPanel();
        pnlDocs.setLayout(new FormLayout("fill:d:grow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        pnlParent.add(pnlDocs, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(20, 20), null, 0, false));
        progressBar = new JProgressBar();
        progressBar.setVisible(true);
        pnlDocs.add(progressBar, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        lblStatus = new JLabel();
        lblStatus.setText("Label");
        pnlDocs.add(lblStatus, cc.xy(1, 3));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FormLayout("fill:d:grow", "center:d:grow"));
        pnlParent.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Before you can use JReader we need to complete the following installation steps:");
        panel1.add(label5, cc.xy(1, 1));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:384px:noGrow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        pnlParent.add(panel2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        lblInfo = new JLabel();
        lblInfo.setText("Please locate the Java Documentation on your system.");
        panel2.add(lblInfo, cc.xyw(1, 1, 3));
        btnOK = new JButton();
        btnOK.setText("OK");
        panel2.add(btnOK, cc.xy(1, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
        btnCancel = new JButton();
        btnCancel.setText("Cancel");
        panel2.add(btnCancel, cc.xy(3, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return pnlParent;
    }

    class SetupWorker extends SwingWorker<Boolean, String> {

        boolean wasCanceled = false;

        @Override
        protected Boolean doInBackground() throws Exception {

            JReaderSetup.createDirectoriesAndConfig();

            File docDir = JReaderSetup.chooseDocs();
            if ( docDir == null ) {
                wasCanceled = true;
                cancel(true);
                return false;
            } else {
                setTickIcon(icnLocate);
            }

            if ( !Config.getBool(Config.HAS_JAVALANG_SOURCE) )
                downloadSource();

            if ( !Config.getBool(Config.HAS_EXTRACTED_SOURCE) )
                extractSource();

            if ( !JReaderSetup.hasDefaultProfile() ) {
                setUpDefaultProfile(docDir);
            }

            if ( !Config.getBool(Config.HAS_PARSED_DOCS) )
                parseDocs(docDir);

            return true;
        }

        private void downloadSource() throws IOException {
            progressBar.setValue(0);
            lblInfo.setText("Downloading Source");
            lblStatus.setText("Connecting...");

            FileDownloader downloader = new FileDownloader(Config.dataDirectory + Config.JAVA_LANG_ZIP,
                    "http://sourceforge.net/projects/jdk7src/files/latest/download");
            downloader.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    progressBar.setValue(( int ) e.getWhen());
                    lblStatus.setText(e.getActionCommand());
                }
            });

            downloader.download();

            Config.setBool(Config.HAS_JAVALANG_SOURCE, true);
            setTickIcon(icnDownload);
            lblStatus.setText("Complete");
            log.debug("Downloaded source");
        }

        private void extractSource() throws Exception {

            lblInfo.setText("Extracting Source");
            lblStatus.setText("Initializing...");
            progressBar.setValue(0);
            progressBar.setIndeterminate(true);

            String zipPath = Config.dataDirectory + Config.JAVA_LANG_ZIP;
            UnZipper unZipper = new UnZipper(zipPath, zipPath.replace(".zip", ""));
            unZipper.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    progressBar.setValue(( int ) e.getWhen());
                    lblStatus.setText(e.getActionCommand());
                }
            });
            unZipper.unzip();

            Config.setBool(Config.HAS_EXTRACTED_SOURCE, true);
            lblStatus.setText("Complete");
            setTickIcon(icnExtract);
            log.debug("Extracted source");
        }

        private void setUpDefaultProfile(File docDir) throws IOException {
            String srcPath = Config.getString(Config.DATA_DIR)
                    + Config.JAVA_LANG_ZIP.replace(".zip", "")
                    + File.separator;

            ProfileManager profileManager = ProfileManager.getInstance();
            String javaDocsPath = docDir.getAbsolutePath() + File.separator;
            profileManager.newProfile(Config.DEFAULT_PROFILE_NAME, javaDocsPath, srcPath);

            profileManager.saveProfiles();
            Config.setBool(Config.HAS_DEFAULT_PROFILE, true);
            Config.setString(Config.CURRENT_PROFILE, Config.DEFAULT_PROFILE_NAME);
            log.debug("Created default profile");
        }

        private void parseDocs(File docDir) throws Exception {
            progressBar.setValue(0);
            progressBar.setIndeterminate(false);
            lblInfo.setText("Parsing Documentation");
            lblStatus.setText("Loading...");

            String path = docDir.getAbsolutePath()
                    + File.separator
                    + "allclasses-noframe.html";

            JavaDocParser parser = new JavaDocParser();
            parser.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    progressBar.setValue(0);
                    lblStatus.setText(e.getActionCommand());
                }
            });

            HashMap<String, String> classes = parser.parse(path);
            Utilities.writeCLassData(ProfileManager.getInstance().getPath()
                    + File.separator
                    + Config.CLASS_DATA_FILE_NAME, classes);

            Config.setBool(Config.HAS_PARSED_DOCS, true);
            lblStatus.setText("Complete");
            setTickIcon(icnParse);
            log.debug("Parsed docs");
        }

        @Override
        protected void done() {
            try {
                get();
                log.debug("Setup complete.");
            } catch ( InterruptedException e ) {
                wasCanceled = true;
                log.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(null, e.getMessage());

            } catch ( ExecutionException e ) {
                log.error(e.getMessage(), e);

            } catch ( CancellationException e ) {
                wasCanceled = true;
                log.error(e.getMessage(), e);

            } finally {
                setVisible(false);
                dispose();

                if ( wasCanceled ) {
                    Config.setBool(Config.HAS_JAVALANG_SOURCE, false);
                    JOptionPane.showMessageDialog(null, "Goobye.");
                    System.exit(0);
                }
            }
        }
    }
}
