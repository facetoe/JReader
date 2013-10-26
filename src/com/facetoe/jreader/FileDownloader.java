package com.facetoe.jreader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;


interface RBCWrapperDelegate {
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

class FileDownloader implements RBCWrapperDelegate {
    String localPath;
    String remoteURL;
    ReadableByteChannel rbc;
    HttpURLConnection connection;
    ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

//    public static void main(String[] args) {
//        if ( new FileDownloaderProgressWindow("/home/facetoe/tmp/test/test.img", "https://upload.wikimedia.org/wikipedia/commons/1/1e/Piwo_gratis.jpg").execute() ) {
//            System.out.println("Success");
//        } else {
//            System.out.println("Canceled");
//        }
//    }

    public FileDownloader(String localPath, String remoteURL) {

        this.localPath = localPath;
        this.remoteURL = remoteURL;
    }

    public void download() throws IOException, CancellationException {
        FileOutputStream fos;
        URL url;

        url = new URL(remoteURL);
        rbc = new RBCWrapper(Channels.newChannel(url.openStream()), contentLength(url), this);
        fos = new FileOutputStream(localPath);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    }

    public void cancel() {
        /**
         * If cancel() is called immediately after download() is called the rbc and connection won't be initialized.
         * Wait until the connection begins and then cancel it.
         */
        while ( true ) {
            if ( rbc == null || connection == null ) {
                try {
                    Thread.sleep(1000);
                } catch ( InterruptedException ex ) {
                    ex.printStackTrace();
                }
            } else if ( rbc.isOpen() ) {
                break;
            }
        }

        try {
            connection.disconnect();
            rbc.close();

        } catch ( IOException e1 ) {
            e1.printStackTrace();
        }
    }

    public void rbcProgressCallback(RBCWrapper rbc, double progress) {
        fireEvent(ActionEvent.ACTION_PERFORMED, Utilities.humanReadableByteCount(rbc.getReadSoFar(), true), ( long ) progress);
    }

    private long contentLength(URL url) {
        long contentLength = -1;

        try {

            HttpURLConnection.setFollowRedirects(true);

            connection = ( HttpURLConnection ) url.openConnection();
            connection.setConnectTimeout(15 * 1000);
            connection.setRequestMethod("HEAD");

            contentLength = connection.getContentLengthLong();

            /**
             * For some reason the content length isn't being returned correctly.
             * I'm hardcoding the java source zipfile size so the progress bar works.
             */
            if ( contentLength == -1 ) {
                contentLength = 36689314;
            }


        } catch ( ProtocolException ex ) {
            ex.printStackTrace();
        } catch ( IOException ex ) {
            ex.printStackTrace();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        return contentLength;
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    public void fireEvent(int eventType, String message, long progress) {
        ActionEvent event = new ActionEvent(this, eventType, message, progress, 0);
        for ( ActionListener listener : listeners ) {
            listener.actionPerformed(event);
        }
    }
}

class RBCWrapper implements ReadableByteChannel {
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

class FileDownloaderProgressWindow extends ProgressWindow<Boolean> {

    String localDest;
    String remoteSrc;
    private boolean wasCanceled = false;

    public FileDownloaderProgressWindow(String localDestination, String remoteSource) {
        localDest = localDestination;
        remoteSrc = remoteSource;
    }

    @Override
    public Boolean execute() {
        final FileDownloader downloader = new FileDownloader(localDest, remoteSrc);

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downloader.cancel();
                wasCanceled = true;
            }
        });

        downloader.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( e.getID() == ActionEvent.ACTION_PERFORMED ) {
                    progressBar.setValue(( int ) e.getWhen());
                    lblTitle.setText("Downloaded: ");
                    lblProgress.setText(e.getActionCommand());
                }
            }
        });

        try {
            downloader.download();
        } catch ( IOException e ) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return Boolean.FALSE;
        }

        setVisible(false);
        dispose();
        return (!wasCanceled);
    }
}
