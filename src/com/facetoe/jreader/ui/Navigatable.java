package com.facetoe.jreader.ui;

/**
 * JReader
 * Created by facetoe on 23/01/14.
 */

/**
 * Interface for navigatable panels, such as JReaderPanel.
 */
interface Navigatable {
    // History next
    void next();

    // History previous
    void back();

    // Go to home page
    void home();
}