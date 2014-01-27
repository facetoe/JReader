package com.facetoe.jreader.ui;

/**
 * JReader
 * Created by facetoe on 27/01/14.
 */

/**
 * Update status with a message and progress bar progress.
 */
interface StatusUpdateListener {
    void updateStatus(String message);
    void updateProgressBar(int progress);
}