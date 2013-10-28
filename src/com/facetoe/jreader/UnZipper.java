package com.facetoe.jreader;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.progress.ProgressMonitor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class UnZipper {
    String srcFile;
    String destFile;

    ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

    public UnZipper(String sourceFile, String destinationFile) {
        srcFile = sourceFile;
        destFile = destinationFile;
    }

    public void unzip() throws ZipException, Exception {
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
            throw new Exception(pm.getException().getMessage(), pm.getException().getCause());
        }
    }

    public void fireEvent(int eventType, String message, long progress) {
        ActionEvent event = new ActionEvent(this, eventType, message, progress, 0);
        for ( ActionListener listener : listeners ) {
            listener.actionPerformed(event);
        }
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }
}
