package com.facetoe.jreader;

import org.apache.log4j.Logger;

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
    private final Logger log = Logger.getLogger(this.getClass());

    private final String localPath;
    private final String remoteURL;
    private final ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

    public FileDownloader(String localPath, String remoteURL) {

        this.localPath = localPath;
        this.remoteURL = remoteURL;
    }

    public void download() throws IOException, CancellationException {
        fireEvent(ActionEvent.ACTION_FIRST, "Connecting...", 0);
        FileOutputStream fos;
        URL url;

        url = new URL(remoteURL);
        ReadableByteChannel rbc = new RBCWrapper(Channels.newChannel(url.openStream()), contentLength(url), this);
        fos = new FileOutputStream(localPath);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    }

    public void rbcProgressCallback(RBCWrapper rbc, double progress) {
        String message = String.format("Downloaded %s of %s",
                Utilities.humanReadableByteCount(rbc.getReadSoFar(), true),
                Utilities.humanReadableByteCount(rbc.getExpectedSize(), true));

        fireEvent(ActionEvent.ACTION_PERFORMED, message, ( long ) progress);
    }

    private long contentLength(URL url) {
        long contentLength = -1;

        try {

            HttpURLConnection.setFollowRedirects(true);

            HttpURLConnection connection = ( HttpURLConnection ) url.openConnection();
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
            log.error(ex.getMessage(), ex);
        } catch ( IOException ex ) {
            log.error(ex.getMessage(), ex);
        } catch ( Exception ex ) {
            log.error(ex.getMessage(), ex);
        }

        return contentLength;
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    void fireEvent(int eventType, String message, long progress) {
        ActionEvent event = new ActionEvent(this, eventType, message, progress, 0);
        for ( ActionListener listener : listeners ) {
            listener.actionPerformed(event);
        }
    }
}

class RBCWrapper implements ReadableByteChannel {
    private final RBCWrapperDelegate delegate;
    private final long expectedSize;
    private final ReadableByteChannel rbc;
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

    public long getExpectedSize() {
        return expectedSize;
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
