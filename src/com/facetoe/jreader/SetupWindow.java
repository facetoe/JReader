package com.facetoe.jreader;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 20/10/13
 * Time: 10:34 AM
 */
public class SetupWindow {
    private JPanel pnlParent;
    private JButton btnLocateDocs;
    private JProgressBar progressDownload;
    private JProgressBar progressParse;
    private JButton btnOk;
    private JButton btnCancel;
    private JPanel pnlInfo;
    private JLabel lblInfo;
    private JLabel pnlTitle;
    private JPanel pnlDocs;
    private JLabel lblLocateDocs;
    private JLabel lblSuccessDocs;
    private JPanel pnlDownloadSrc;
    private JLabel lblPathDocs;
    private JLabel lblDownloadDocs;
    private JLabel lblProgressDownload;
    private JLabel lblSucessDownload;
    private JPanel pnlParse;
    private JLabel lblParse;
    private JLabel lblProgressParse;
    private JLabel lblSuccessParse;

    public static void main(String[] args) {
        JFrame frame = new JFrame("SetupWindow");
        frame.setContentPane(new SetupWindow().pnlParent);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
