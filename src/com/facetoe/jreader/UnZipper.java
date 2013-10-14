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

    public boolean unzip() {
        try {
            ZipFile zipFile = new ZipFile(srcFile);

            // Set runInThread variable of ZipFile to true.
            // When this variable is set, Zip4j will run any task in a new thread
            // If this variable is not set, Zip4j will run all tasks in the current
            // thread.
            zipFile.setRunInThread(true);
            zipFile.extractAll(destFile);
            ProgressMonitor pm = zipFile.getProgressMonitor();
            long totalWork = pm.getTotalWork();

            while ( pm.getState() == ProgressMonitor.STATE_BUSY ) {
                if ( pm.getCurrentOperation() == ProgressMonitor.OPERATION_EXTRACT ) {
                    fireEvent(ActionEvent.ACTION_PERFORMED, pm.getFileName(), pm.getWorkCompleted());
                }
            }

            if ( pm.getResult() == ProgressMonitor.RESULT_ERROR ) {
                return false;
            }

        } catch ( ZipException e ) {
            e.printStackTrace();
        }
        return true;
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

class UnZipperProgressWindow extends ProgressWindow<Boolean> {
    String srcFile;
    String destFile;

    public UnZipperProgressWindow(String srcFile, String destFile) {
        this.srcFile = srcFile;
        this.destFile = destFile;
        progressBar.setIndeterminate(true);
    }

    @Override
    public Boolean execute() {
        UnZipper unZipper = new UnZipper(srcFile, destFile);
        unZipper.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lblTitle.setText("Extracting: ");
                lblProgress.setText(e.getActionCommand());
                progressBar.setValue(( int ) e.getWhen());
            }
        });

        boolean result = unZipper.unzip();
        setVisible(false);
        dispose();

        return result;
    }
}
