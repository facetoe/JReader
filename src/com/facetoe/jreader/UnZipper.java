package com.facetoe.jreader;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.progress.ProgressMonitor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Unzips a file and provides feedback on progress.
 */
public class UnZipper {

    /**
     * The zip file to extract.
     */
    private final String srcFile;

    /**
     * Where to extract it to.
     */
    private final String destFile;

    /**
     * Listeners to notfy of progress.
     */
    private final ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

    /**
     * Constructor.
     * @param sourceFile The file to be unzipped.
     * @param destinationFile The location to unzip it to.
     */
    public UnZipper(String sourceFile, String destinationFile) {
        srcFile = sourceFile;
        destFile = destinationFile;
    }

    /**
     * Unizp the file.
     * @throws Exception
     */
    public void unzip() throws Exception {
        ZipFile zipFile = new ZipFile(srcFile);

        // Set runInThread variable of ZipFile to true.
        // When this variable is set, Zip4j will run any task in a new thread
        // If this variable is not set, Zip4j will run all tasks in the current
        // thread.
        zipFile.setRunInThread(true);
        zipFile.extractAll(destFile);
        ProgressMonitor pm = zipFile.getProgressMonitor();

        while ( pm.getState() == ProgressMonitor.STATE_BUSY ) {
            if ( pm.getCurrentOperation() == ProgressMonitor.OPERATION_EXTRACT ) {
                fireEvent(ActionEvent.ACTION_PERFORMED, pm.getFileName(), pm.getWorkCompleted());
            }
        }

        if ( pm.getResult() == ProgressMonitor.RESULT_ERROR ) {
            throw new Exception(pm.getException());
        }
    }

    /**
     * Notify the listers of progress.
     * @param eventType The type of event.
     * @param message The message.
     * @param progress How far we have progressed.
     */
    void fireEvent(int eventType, String message, long progress) {
        for ( ActionListener listener : listeners ) {
            listener.actionPerformed(new ActionEvent(this, eventType, message, progress, 0));
        }
    }

    /**
     * Add an action listener.
     * @param listener The action listener to add.
     */
    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }
}
