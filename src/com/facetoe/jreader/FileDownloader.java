package com.facetoe.jreader;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class FileDownloader {
    private interface RBCWrapperDelegate {
        // The RBCWrapperDelegate receives rbcProgressCallback() messages
        // from the read loop.  It is passed the progress as a percentage
        // if known, or -1.0 to indicate indeterminate progress.
        //
        // This callback hangs the read loop so a smart implementation will
        // spend the least amount of time possible here before returning.
        //
        // One possible implementation is to push the progress message
        // atomically onto a queue managed by a secondary thread then
        // wake that thread up.  The queue manager thread then updates
        // the user interface progress bar.  This lets the read loop
        // continue as fast as possible.
        public void rbcProgressCallback(RBCWrapper rbc, double progress);
    }

    public void download(String localPath, String remoteURL) {
        new Downloader(localPath, remoteURL).download();
    }

    private static final class Downloader extends JFrame implements RBCWrapperDelegate {

        JPanel panel = new JPanel();
        JButton btnCancel = new JButton("Cancel");
        JLabel lblProgress = new JLabel("Locating...");
        JProgressBar progressBar = new JProgressBar();
        String localPath;
        String remoteURL;
        ReadableByteChannel rbc;

        public Downloader(String localPath, String remoteURL) {

            this.localPath = localPath;
            this.remoteURL = remoteURL;

            btnCancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Config.setEntry("hasSrc", "false");
                    setVisible(false);
                    dispose();
                    try {
                        rbc.close();
                    } catch ( IOException e1 ) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            });

            panel.setLayout(new BorderLayout(10, 10));
            panel.add(lblProgress, BorderLayout.CENTER);
            panel.add(progressBar, BorderLayout.NORTH);
            panel.add(btnCancel, BorderLayout.EAST);
            panel.setBorder(new CompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)));


            setTitle("JReader");
            setResizable(false);

            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch ( ClassNotFoundException e ) {
                e.printStackTrace();
            } catch ( InstantiationException e ) {
                e.printStackTrace();
            } catch ( IllegalAccessException e ) {
                e.printStackTrace();
            } catch ( UnsupportedLookAndFeelException e ) {
                e.printStackTrace();
            }

            getContentPane().add(panel, BorderLayout.NORTH);
            setPreferredSize(new Dimension(550, 72));
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            pack();
            setVisible(true);
        }

        public void download() {
            FileOutputStream fos;
            URL url;

            try {
                url = new URL(remoteURL);
                rbc = new RBCWrapper(Channels.newChannel(url.openStream()), contentLength(url), this);
                fos = new FileOutputStream(localPath);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                lblProgress.setText("Complete.");

                try {
                    Thread.sleep(1000);
                } catch ( InterruptedException ex ) {
                    ex.printStackTrace();
                }

            } catch ( MalformedURLException ex ) {
                Config.setEntry("hasSrc", "false");
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), ex.getClass().toString(), JOptionPane.ERROR_MESSAGE);

            } catch ( FileNotFoundException ex ) {
                Config.setEntry("hasSrc", "false");
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), ex.getClass().toString(), JOptionPane.ERROR_MESSAGE);

            } catch ( IOException ex ) {
                Config.setEntry("hasSrc", "false");
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), ex.getClass().toString(), JOptionPane.ERROR_MESSAGE);

            } catch ( Exception ex ) {
                Config.setEntry("hasSrc", "false");
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), ex.getClass().toString(), JOptionPane.ERROR_MESSAGE);

            } finally {
                setVisible(false);
                dispose();
            }
        }

        public void rbcProgressCallback(RBCWrapper rbc, double progress) {
            progressBar.setValue(( int ) progress);
            lblProgress.setText(String.format("Downloaded %.02f%% of %s", progress, Utilities.humanReadableByteCount(rbc.expectedSize, true)));
        }

        private long contentLength(URL url) {
            HttpURLConnection connection;
            long contentLength = -1;

            try {
                HttpURLConnection.setFollowRedirects(false);

                connection = ( HttpURLConnection ) url.openConnection();
                connection.setRequestMethod("HEAD");

                contentLength = connection.getContentLengthLong();


                /**
                 * For some reason the content length isn't being returned correctly.
                 * I'm hardcoding the java source zipfile size so the progress bar works.
                 */
                if(contentLength == -1) {
                    contentLength = 36689314;
                }


            } catch ( ProtocolException ex ) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), ex.getClass().toString(), JOptionPane.ERROR_MESSAGE);
            } catch ( IOException ex ) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), ex.getClass().toString(), JOptionPane.ERROR_MESSAGE);
            } catch ( Exception ex ) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), ex.getClass().toString(), JOptionPane.ERROR_MESSAGE);
            }

            return contentLength;
        }
    }

    private static final class RBCWrapper implements ReadableByteChannel {
        private RBCWrapperDelegate delegate;
        private long expectedSize;
        private ReadableByteChannel rbc;
        private long readSoFar;

        RBCWrapper(ReadableByteChannel rbc, long expectedSize, RBCWrapperDelegate delegate) {
            this.delegate = delegate;
            this.expectedSize = expectedSize;
            this.rbc = rbc;
        }

        public void close() throws IOException {
            rbc.close();
        }

        public long getReadSoFar() {
            return readSoFar;
        }

        public boolean isOpen() {
            return rbc.isOpen();
        }

        public int read(ByteBuffer bb) throws IOException {
            int n;
            double progress;

            if ( (n = rbc.read(bb)) > 0 ) {
                readSoFar += n;
                progress = expectedSize > 0 ? ( double ) readSoFar / ( double ) expectedSize * 100.0 : -1.0;
                delegate.rbcProgressCallback(this, progress);
            }
            return n;
        }
    }
}